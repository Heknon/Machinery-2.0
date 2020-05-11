package me.oriharel.machinery

import me.oriharel.machinery.structure.Schematic
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

        listen<BlockPlaceEvent>(
                this
        ) {
            Schematic(dataFolder.toPath().resolve("miner.schem")).loadSchematic().buildSchematic(block.location, player)
        }
    }

    override fun onDisable() {

    }

    private fun test() {

    }

    companion object {
        lateinit var INSTANCE: Machinery
    }

}