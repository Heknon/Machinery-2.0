package me.oriharel.machinery

import com.google.gson.GsonBuilder
import me.oriharel.machinery.structure.schematic.Schematic
import me.oriharel.machinery.structure.schematic.SchematicImpl
import me.oriharel.machinery.structure.schematic.SchematicOption
import me.oriharel.machinery.structure.schematic.utilities.ByteArrayToBase64TypeAdapter
import me.oriharel.machinery.structure.schematic.utilities.SchematicStateTypeAdapter
import me.oriharel.machinery.utilities.listen
import me.oriharel.machinery.utilities.schedulers.IterativeScheduler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Files

class Machinery : JavaPlugin() {

    val schematicsBuildingRegistry: MutableList<Schematic> = mutableListOf()

    override fun onLoad() {
        INSTANCE = this
    }

    override fun onEnable() {
        if (!Files.exists(dataFolder.toPath())) {
            Files.createFile(dataFolder.toPath())
        }

        val gson = GsonBuilder()
                .registerTypeHierarchyAdapter(ByteArray::class.java, ByteArrayToBase64TypeAdapter())
                .registerTypeHierarchyAdapter(Schematic.SchematicState::class.java, SchematicStateTypeAdapter())
                .create()
        val schem = SchematicImpl(this@Machinery, dataFolder.toPath().resolve("miner.schem")).loadSchematic()
        schematicsBuildingRegistry.add(schem)

        listen<BlockPlaceEvent>(
                this
        ) {
            schem.buildSchematic(
                    block.location,
                    player,
                    20,
                    SchematicOption.PREVENT_BREAK_WHILE_BUILD
            )
        }
    }

    override fun onDisable() {
        schematicsBuildingRegistry.forEach {
            it.onPluginDisable()
        }
    }

    companion object {
        lateinit var INSTANCE: Machinery
    }

}