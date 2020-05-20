package me.oriharel.machinery.structure.schematic

import com.google.gson.GsonBuilder
import me.oriharel.machinery.structure.schematic.utilities.*
import me.oriharel.machinery.utilities.extensions.*
import me.oriharel.machinery.utilities.schedulers.IterativeScheduler
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools
import net.minecraft.server.v1_15_R1.NBTTagCompound
import net.minecraft.server.v1_15_R1.NBTTagList
import org.bukkit.*
import org.bukkit.block.*
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.Fence
import org.bukkit.block.data.type.WallSign
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.io.FileInputStream
import java.nio.file.Path
import java.util.*

typealias BuildTask = IterativeScheduler<Location, Unit>

/**
 * This class provides the ability to build schematics.
 * Lots of features are provided. One of the main features is the fact that a server can restart and upon loading the server
 * the schematic will keep on building.
 * This behavior is allowed by the fact that a schematic's runtime data is either stored
 * in the first block with state it has built or if no such block has been encountered, it is stored in a folder named
 * schematicsInProgress in the appropriate world folder. Schematics which have encountered a "stateful" block will place most
 * of their data in the block however there will still be a file named "schematicsInProgress.dat" which will store the locations
 * to the stateful blocks as Longs. This file will be located in the appropriate world folder.
 */
abstract class Schematic(private val plugin: JavaPlugin, val schematic: Path) {
    private lateinit var blocks: MutableMap<Int, BlockData>
    private lateinit var blockData: ByteArray
    private lateinit var dimensions: SchematicDimensions
    private var chests: Map<Vector, MutableList<IndexedInventoryItem>>? = null
    private val signsInSchematic: MutableMap<Vector, List<String>> = HashMap()
    private val states: MutableSet<SchematicState> = mutableSetOf()

    protected val gson = GsonBuilder()
            .registerTypeHierarchyAdapter(ByteArray::class.java, ByteArrayToBase64TypeAdapter())
            .registerTypeHierarchyAdapter(SchematicState::class.java, SchematicStateTypeAdapter())
            .create()


    abstract fun blockPlaced(loc: Location, state: SchematicState, ctx: BuildTask)
    abstract fun buildingFinished(ctx: BuildTask, state: SchematicState)

    fun buildSchematic(
            buildLocation: Location,
            builder: OfflinePlayer? = null,
            placeBlockEvery: Long = 20,
            vararg options: SchematicOption
    ): BuildResult {
        return buildSchematic(SchematicState(
                buildLocation = buildLocation,
                options = setOf(*options),
                scheduler = null,
                currentIndex = 0,
                builder = builder?.uniqueId,
                finishedBuilding = false,
                placeBlockEvery = placeBlockEvery
        ))
    }

    fun buildSchematic(
            schematicState: SchematicState
    ): BuildResult {
        states.add(schematicState)
        val width = dimensions.width.toInt()
        val height = dimensions.height.toInt()
        val length = dimensions.length.toInt()
        val builder: OfflinePlayer? = schematicState.builder?.toOfflinePlayer()
        val placementLocations: MutableSet<Location> = mutableSetOf()
        val placementLocationsPlacedLast: MutableSet<Location> = mutableSetOf()
        val locationsWithNBTData: MutableMap<Int, Any?> = mutableMapOf()
        val builderFacing: BlockFace = builder?.player?.getDirection() ?: BlockFace.NORTH
        val indices: MutableList<Int> = mutableListOf()
        val indicesPlacedLast: MutableList<Int> = mutableListOf()

        for (widthCurr in 0 until width)
            for (heightCurr in 0 until height)
                for (lengthCurr in 0 until length) {
                    val blockIndex = dimensions.getBlockIndex(widthCurr, heightCurr, lengthCurr)
                    val blockData = dimensions.getBlockData(widthCurr, heightCurr, lengthCurr)
                    val location: Location = schematicState.buildLocation.getSchematicBlockLocation(builderFacing, widthCurr, heightCurr, lengthCurr)!!
                    val point = Vector(widthCurr, heightCurr, lengthCurr)


                    val blockMaterial = blockData.material
                    if (blockMaterial != Material.AIR)
                        if (!BLOCKS_PLACED_LAST.contains(blockMaterial)) {
                            indices.add(blockIndex)
                            placementLocations.add(location)
                        } else {
                            indicesPlacedLast.add(blockIndex)
                            placementLocationsPlacedLast.add(location)
                        }



                    if (signsInSchematic.containsKey(point)) locationsWithNBTData[blockIndex] = signsInSchematic[point]
                    if (chests?.containsKey(point) == true) locationsWithNBTData[blockIndex] = chests?.get(point)
                }

        indices.addAll(indicesPlacedLast)
        indicesPlacedLast.clear()

        placementLocations.addAll(placementLocationsPlacedLast)
        placementLocationsPlacedLast.clear()

        if (schematicState.options.contains(SchematicOption.PREVENT_BREAK_WHILE_BUILD) && this is BlockableSchematic)
            interactionBlocker.addUninteractableLocations(*placementLocations.toTypedArray())

        if (!validateBuildLocation(builder, placementLocations, *schematicState.options.toTypedArray())) return BuildResult(placementLocations, false, schematicState)

        val blocksToUpdateAfterPaste: MutableList<Block> = mutableListOf()

        placementLocations.forEachIndexed { i, location ->
            val block = location.block
            val data = dimensions.getBlockData(i)

            if (MaterialType.FENCE.materials.contains(data.material))
                blocksToUpdateAfterPaste.add(block)
        }

        schematicState.finishedBuilding = true
        if (schematicState.scheduler == null) {
            schematicState.scheduler = placementLocations.removeUpTo(if (schematicState.currentIndex == 0) 0 else schematicState.currentIndex + 1)
                    .scheduledIteration<Location, Unit>(plugin, schematicState.placeBlockEvery) { _, _ ->
                        blockPlacementRoutine(
                                builderFacing,
                                block,
                                dimensions.getBlockData(indices[schematicState.currentIndex]),
                                locationsWithNBTData,
                                schematicState.currentIndex
                        ).also {
                            if (schematicState.statefulBlockEncounterLocation != null && it.state !is TileState) return@also
                            schematicState.statefulBlockEncounterLocation = it.location
                        }
                        schematicState.currentIndex++
                    }.setOnSingleTaskComplete { _, ctx ->

                        if (schematicState.options.contains(SchematicOption.PLAY_DEFAULT_SOUND)) {
                            block.location.world?.playEffect(block.location, Effect.STEP_SOUND, block.type)
                        }

                        if (schematicState.options.contains(SchematicOption.SUMMON_DEFAULT_PARTICLES)) {
                            block.location.world?.spawnParticle(Particle.CLOUD, block.location, 6)
                        }

                        blockPlaced(this, schematicState, ctx as BuildTask)

                    }.setOnRepeatComplete {
                        buildFinalizationRoutine(blocksToUpdateAfterPaste)
                        schematicBuildingFinishedCleanupRoutine()
                        buildingFinished(this as BuildTask, schematicState)
                        states.remove(schematicState)
                    } as BuildTask
        } else {
            schematicState.scheduler!!.run()
        }

        return BuildResult(placementLocations, true, schematicState)
    }

    fun loadSchematic(): Schematic {
        val fis = FileInputStream(schematic.toFile())
        val nbt: NBTTagCompound = NBTCompressedStreamTools.a(fis)

        dimensions = SchematicDimensions(nbt.getShort("Width"), nbt.getShort("Height"), nbt.getShort("Length"))
        blockData = nbt.getByteArray("BlockData")

        val palette = nbt.getCompound("Palette")
        val tiles = nbt["BlockEntities"] as NBTTagList?
        var currentTile = 0

        if (tiles != null) {
            repeat(tiles.size) {
                val compound = tiles.getCompound(currentTile)
                if (compound.isEmpty) return@repeat

                val id = compound.getString("Id").replace("minecraft:", "").toUpperCase()
                if (EnumUtils.isValidEnum(NBTMaterial::class.java, id) && NBTMaterial.valueOf(id) == NBTMaterial.SIGN) {
                    val lines: MutableList<String> = ArrayList()
                    val pos = compound.getIntArray("Pos")

                    lines.add(compound.getSignLineFromNBT("Text1") ?: "")
                    lines.add(compound.getSignLineFromNBT("Text2") ?: "")
                    lines.add(compound.getSignLineFromNBT("Text3") ?: "")
                    lines.add(compound.getSignLineFromNBT("Text4") ?: "")

                    if (lines.isNotEmpty()) signsInSchematic[Vector(pos[0], pos[1], pos[2])] = lines

                    tiles.removeAt(currentTile)
                }
                currentTile++
            }
            try {
                chests = tiles.getItemsFromNBT()
            } catch (e: RuntimeException) {
                //it wasn't a chest
            }
        }

        blocks = mutableMapOf()
        palette.keys.forEach {
            val id = palette.getInt(it)
            val blockData = Bukkit.createBlockData(it)
            blocks[id] = blockData
        }

        fis.close()
        return this
    }

    private fun blockPlacementRoutine(builderFacing: BlockFace, block: Block, blockDataToBePlaced: BlockData, locationsWithNBTData: Map<Int, Any?>, index: Int): Block {
        block.type = blockDataToBePlaced.material
        block.blockData = blockDataToBePlaced

        when (blockDataToBePlaced.material) {
            in MaterialType.SIGN.materials -> {
                handleSignBlockInfoUpdate<org.bukkit.block.data.type.Sign>(block, blockDataToBePlaced, locationsWithNBTData, index)
            }
            in MaterialType.WALL_SIGN.materials -> {
                handleSignBlockInfoUpdate<WallSign>(block, blockDataToBePlaced, locationsWithNBTData, index)
            }
            in MaterialType.CHEST.materials -> {
                val chestData = blockDataToBePlaced as org.bukkit.block.data.type.Chest
                block.blockData = chestData

                if (locationsWithNBTData.containsKey(index)) {

                    val items: MutableList<*> = locationsWithNBTData[index] as MutableList<*>
                    val chest = block.state as Chest

                    for (indexItem in items) {
                        if (indexItem !is IndexedInventoryItem) continue
                        chest.blockInventory.setItem(indexItem.index, indexItem.item)
                    }
                }
            }
            else -> Unit
        }

        if (block.state.blockData is MultipleFacing) {
            val multiFace = block.state.blockData as MultipleFacing
            val setFaces: MutableSet<BlockFace> = mutableSetOf()

            multiFace.faces.forEach {
                if (!setFaces.contains(it))
                    multiFace.setFace(it, false)
                val faceToSet = getRealBlockFace(builderFacing, it)
                multiFace.setFace(faceToSet, true)
                setFaces.add(faceToSet)
            }

            block.blockData = multiFace

        } else if (block.blockData is Directional) {
            val blockDirectional = block.state.blockData as Directional
            val blockFacing = blockDirectional.facing

            blockDirectional.facing = getRealBlockFace(builderFacing, blockFacing)

            block.blockData = blockDirectional
        }
        block.state.update(true, false)
        return block
    }

    private fun buildFinalizationRoutine(blocksToUpdate: List<Block>) {
        blocksToUpdate.forEach {
            val state: BlockState = it.state

            if (!MaterialType.FENCE.materials.contains(it.type)) {
                it.state.update(true, false)
                return@forEach
            }

            val fence: Fence = state.blockData as Fence
            fence.allowedFaces.forEach { face ->
                val relative: Block = it.getRelative(face)
                val relativeTypeAsString = relative.type.toString()

                if (relative.type == Material.AIR || relativeTypeAsString.contains("SLAB") || relativeTypeAsString.contains("STAIRS")) {
                    fence.setFace(face, false)
                } else if (!relativeTypeAsString.contains("SLAB")
                        && !relativeTypeAsString.contains("STAIRS")
                        && !MaterialType.ANVIL.materials.contains(relative.type)
                        && relative.type.isSolid
                        && relative.type.isBlock
                        && !fence.hasFace(face)) {
                    fence.setFace(face, true)
                }
            }

            state.blockData = fence
            state.update(true, false)
        }
    }


    private fun schematicBuildingFinishedCleanupRoutine() {
        // TODO: Logic to clear all files that might've been created by the schematic due to a server restart

    }

    fun onPluginDisable() {
        states.forEach {
            val serializedState: String = gson.toJson(it, SchematicState::class.java)
            print(serializedState)
        }
    }

    /**
     * Used to keep track of the state of a schematic of this type being built
     * later on serialized if the server is restarted to keep the schematic building.
     */
    data class SchematicState(

            val buildLocation: Location,

            val options: Set<SchematicOption>,

            var scheduler: BuildTask? = null,
            var currentIndex: Int = 0,

            var builder: UUID? = null,
            var finishedBuilding: Boolean = false,
            var placeBlockEvery: Long = 20,

            var statefulBlockEncounterLocation: Location? = null,

            val uuid: UUID = UUID.randomUUID()
    )

    private fun Location.getSchematicBlockLocation(
            face: BlockFace,
            widthCurr: Int,
            heightCurr: Int,
            lengthCurr: Int
    ): Location? = when (face) {
        BlockFace.NORTH -> Location(
                world,
                (-widthCurr + x) + (dimensions.width / 2),
                heightCurr + y,
                lengthCurr + z + (dimensions.length / 2)
        )
        BlockFace.EAST -> Location(
                world,
                (-lengthCurr + x) - (dimensions.length / 2),
                heightCurr + y,
                (-widthCurr - 1) + (dimensions.width + z) - (dimensions.width / 2)
        )
        BlockFace.SOUTH -> Location(
                world,
                widthCurr + x - (dimensions.width / 2),
                heightCurr + y,
                -lengthCurr + z - (dimensions.length / 2)
        )
        BlockFace.WEST -> Location(
                world,
                lengthCurr + x + (dimensions.length / 2),
                heightCurr + y,
                (widthCurr + 1) - (dimensions.width - z) + (dimensions.width / 2)
        )
        else -> null
    }


    /**
     * @return true if can build schematic, otherwise, false
     */
    private fun validateBuildLocation(builder: OfflinePlayer? = null, locationsToValidate: MutableSet<Location>, vararg options: SchematicOption): Boolean {
        if (options.contains(SchematicOption.OVERWRITE_BLOCKS) && !options.contains(SchematicOption.BUILD_PREVIEW)) {
            return true
        }

        val validatedLocations: MutableSet<Location> = mutableSetOf()
        val showPreview = options.contains(SchematicOption.BUILD_PREVIEW)
        val limeGlassData = Material.LIME_STAINED_GLASS.createBlockData()
        val airData = Material.AIR.createBlockData()

        locationsToValidate.forEachContextual {
            if (block.isPassable) {
                if (showPreview) {
                    builder?.player?.sendBlockChange(this, limeGlassData)
                }
                validatedLocations.add(this)
            } else {
                validatedLocations.forEach {
                    builder?.player?.sendBlockChange(it, airData)
                }
                return false
            }
        }

        return true
    }

    private fun Player.getDirection(): BlockFace {
        var yaw = location.yaw

        if (yaw < 0) yaw += 360f

        return if (yaw >= 315 || yaw < 45) BlockFace.SOUTH
        else if (yaw < 135) BlockFace.WEST
        else if (yaw < 225) BlockFace.NORTH
        else if (yaw < 315) BlockFace.EAST
        else BlockFace.NORTH
    }

    private inline fun <reified T : BlockData> handleSignBlockInfoUpdate(block: Block, blockDataToBePlaced: BlockData, locationsWithNBTData: Map<Int, Any?>, index: Int) {
        val signData = blockDataToBePlaced as T
        block.blockData = signData
        if (locationsWithNBTData.containsKey(index)) {
            val lines = locationsWithNBTData[index] as List<*>
            val sign = block.state as Sign

            lines.forEachIndexed { i, line ->
                sign.setLine(i, line as String)
            }

            sign.update()
        }
    }

    private fun getRealBlockFace(referenceFace: BlockFace, face: BlockFace): BlockFace {
        return when (referenceFace) {
            BlockFace.NORTH ->
                when (face) {
                    BlockFace.EAST, BlockFace.WEST -> face.oppositeFace
                    else -> face
                }
            BlockFace.SOUTH ->
                when (face) {
                    BlockFace.NORTH, BlockFace.SOUTH -> face.oppositeFace
                    else -> face
                }
            BlockFace.EAST ->
                when (face) {
                    BlockFace.NORTH, BlockFace.SOUTH -> DirectBlockFace.valueOf(face.toString()).next().blockFace
                    BlockFace.EAST, BlockFace.WEST -> DirectBlockFace.valueOf(face.toString()).next(3).blockFace
                    else -> face
                }
            BlockFace.WEST ->
                when (face) {
                    BlockFace.NORTH, BlockFace.SOUTH -> DirectBlockFace.valueOf(face.toString()).next(3).blockFace
                    BlockFace.EAST, BlockFace.WEST -> DirectBlockFace.valueOf(face.toString()).next().blockFace
                    else -> face
                }
            else -> face
        }
    }

    inner class SchematicDimensions(val width: Short, val height: Short, val length: Short) {

        internal fun getBlockIndex(x: Int, y: Int, z: Int): Int {
            return (y * width * length) + (z * width) + x
        }

        internal fun getBlockData(x: Int, y: Int, z: Int): BlockData {
            return getBlockData(getBlockIndex(x, y, z))
        }

        internal fun getBlockData(index: Int): BlockData {
            return blocks[blockData[index].toInt()]!!
        }

    }

    data class BuildResult(val blockLocations: Set<Location>, val success: Boolean, val state: SchematicState)

    companion object {
        private val BLOCKS_PLACED_LAST: Set<Material> = setOf(
                Material.LAVA,
                Material.WATER,
                Material.GRASS,
                Material.ARMOR_STAND,
                Material.TALL_GRASS,
                Material.BLACK_BANNER,
                Material.BLACK_WALL_BANNER,
                Material.BLUE_BANNER,
                Material.BLUE_WALL_BANNER,
                Material.BROWN_BANNER,
                Material.BROWN_WALL_BANNER,
                Material.CYAN_BANNER,
                Material.CYAN_WALL_BANNER,
                Material.GRAY_BANNER,
                Material.GRAY_WALL_BANNER,
                Material.GREEN_BANNER,
                Material.GREEN_WALL_BANNER,
                Material.LIGHT_BLUE_BANNER,
                Material.LIGHT_BLUE_WALL_BANNER,
                Material.LIGHT_GRAY_BANNER,
                Material.LIGHT_GRAY_WALL_BANNER,
                Material.LIME_BANNER,
                Material.LIME_WALL_BANNER,
                Material.MAGENTA_BANNER,
                Material.MAGENTA_WALL_BANNER,
                Material.ORANGE_BANNER,
                Material.ORANGE_WALL_BANNER,
                Material.PINK_BANNER,
                Material.PINK_WALL_BANNER,
                Material.PURPLE_BANNER,
                Material.PURPLE_WALL_BANNER,
                Material.RED_BANNER,
                Material.RED_WALL_BANNER,
                Material.WHITE_BANNER,
                Material.WHITE_WALL_BANNER,
                Material.YELLOW_BANNER,
                Material.YELLOW_WALL_BANNER,

                Material.GRASS,
                Material.TALL_GRASS,
                Material.SEAGRASS,
                Material.TALL_SEAGRASS,
                Material.FLOWER_POT,
                Material.SUNFLOWER,
                Material.CHORUS_FLOWER,
                Material.OXEYE_DAISY,
                Material.DEAD_BUSH,
                Material.FERN,
                Material.DANDELION,
                Material.POPPY,
                Material.BLUE_ORCHID,
                Material.ALLIUM,
                Material.AZURE_BLUET,
                Material.RED_TULIP,
                Material.ORANGE_TULIP,
                Material.WHITE_TULIP,
                Material.PINK_TULIP,
                Material.BROWN_MUSHROOM,
                Material.RED_MUSHROOM,
                Material.END_ROD,
                Material.ROSE_BUSH,
                Material.PEONY,
                Material.LARGE_FERN,
                Material.REDSTONE,
                Material.REPEATER,
                Material.COMPARATOR,
                Material.LEVER,
                Material.SEA_PICKLE,
                Material.SUGAR_CANE,
                Material.FIRE,
                Material.WHEAT,
                Material.WHEAT_SEEDS,
                Material.CARROTS,
                Material.BEETROOT,
                Material.BEETROOT_SEEDS,
                Material.MELON,
                Material.MELON_STEM,
                Material.MELON_SEEDS,
                Material.POTATOES,
                Material.PUMPKIN,
                Material.PUMPKIN_STEM,
                Material.PUMPKIN_SEEDS,
                Material.TORCH,
                Material.RAIL,
                Material.ACTIVATOR_RAIL,
                Material.DETECTOR_RAIL,
                Material.POWERED_RAIL,

                Material.ACACIA_FENCE,
                Material.ACACIA_FENCE_GATE,
                Material.BIRCH_FENCE,
                Material.BIRCH_FENCE_GATE,
                Material.DARK_OAK_FENCE,
                Material.DARK_OAK_FENCE_GATE,
                Material.JUNGLE_FENCE,
                Material.JUNGLE_FENCE_GATE,
                Material.NETHER_BRICK_FENCE,
                Material.OAK_FENCE,
                Material.OAK_FENCE_GATE,
                Material.SPRUCE_FENCE,
                Material.SPRUCE_FENCE_GATE,

                Material.OAK_DOOR,
                Material.ACACIA_DOOR,
                Material.BIRCH_DOOR,
                Material.DARK_OAK_DOOR,
                Material.JUNGLE_DOOR,
                Material.SPRUCE_DOOR,
                Material.IRON_DOOR,

                Material.IRON_BARS,

                *MaterialType.MULTI_DIRECTIONAL.materials.toTypedArray()
        )
    }
}
