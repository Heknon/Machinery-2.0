package me.oriharel.machinery

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.oriharel.machinery.structure.schematic.*
import me.oriharel.machinery.utilities.listen
import me.oriharel.machinery.utilities.schedulers.IterativeScheduler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Files

class Machinery : JavaPlugin() {

    override fun onLoad() {
        INSTANCE = this
    }

    override fun onEnable() {
        if (!Files.exists(dataFolder.toPath())) {
            Files.createFile(dataFolder.toPath())
        }
        val gson = GsonBuilder()
                .registerTypeHierarchyAdapter(IterativeScheduler::class.java, BuildTaskTypeAdapter())
                .registerTypeHierarchyAdapter(ByteArray::class.java, ByteArrayToBase64TypeAdapter())
                .registerTypeHierarchyAdapter(Schematic.SchematicState::class.java, SchematicStateTypeAdapter())
                .create()

        listen<BlockPlaceEvent>(
                this
        ) {
            Schematic(this@Machinery, dataFolder.toPath().resolve("miner.schem")).loadSchematic().buildSchematicEvaluatedTime(
                    block.location,
                    player
            ) { index, loc, ctx ->
                print(gson.toJson(ctx, object : TypeToken<BuildTask>() {}.type))
                1L
            }
        }
    }

    override fun onDisable() {

    }

    companion object {
        lateinit var INSTANCE: Machinery
    }

}