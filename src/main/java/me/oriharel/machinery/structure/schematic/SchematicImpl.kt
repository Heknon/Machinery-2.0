package me.oriharel.machinery.structure.schematic

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

class SchematicImpl(plugin: JavaPlugin, schematic: Path) : Schematic(plugin, schematic), BlockableSchematic {
    override val interactionBlocker: InteractionBlocker = buildInteractionBlocker(plugin)

    override fun blockPlaced(loc: Location, state: SchematicState, ctx: BuildTask) {
        println("A block has been placed at $loc")
        println(gson.toJson(state, SchematicState::class.java))
    }

    override fun buildingFinished(ctx: BuildTask, state: SchematicState) {
        println("Building has finished!")
    }

    override fun schematicLocationBreakAttempt(location: Location, player: Player) {
        println("Attempted to break a block on a building schematic at: $location by $player")
    }

    override fun schematicLocationPlaceAttempt(location: Location, player: Player) {
        println("Attempted to place a block on a building schematic at $location by $player")
    }
}