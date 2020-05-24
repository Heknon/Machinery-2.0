package me.oriharel.machinery.structure


import me.oriharel.machinery.structure.schematic.BuildTask
import me.oriharel.machinery.structure.schematic.Schematic
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

/**
 * Used to abstract the logic of building a schematic.
 */
class Structure(plugin: JavaPlugin, schematic: Path) : Schematic(plugin, schematic) {
    override fun blockPlaced(loc: Location, state: SchematicState, ctx: BuildTask) {
        TODO("Not yet implemented")
    }

    override fun buildingFinished(ctx: BuildTask, state: SchematicState) {
        TODO("Not yet implemented")
    }

}