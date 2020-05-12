package me.oriharel.machinery

import me.oriharel.machinery.structure.schematic.Schematic
import me.oriharel.machinery.utilities.extensions.scheduledIteration
import me.oriharel.machinery.utilities.listen
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

        listOf<Int>(1, 2, 3, 4, 5, 6, 7, 8).scheduledIteration<Int, Unit>(this, 20) { index, ctx ->
            print("NUMBER: $this, INDEX: $index")
        }

        listen<BlockPlaceEvent>(
                this
        ) {
            Schematic(this@Machinery, dataFolder.toPath().resolve("miner.schem")).loadSchematic().buildSchematicEvaluatedTime(
                    block.location,
                    player
            ) { index, loc, ctx ->
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