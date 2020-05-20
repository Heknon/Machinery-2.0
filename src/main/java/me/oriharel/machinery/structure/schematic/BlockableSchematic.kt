package me.oriharel.machinery.structure.schematic

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

interface BlockableSchematic {
    val interactionBlocker: InteractionBlocker

    fun buildInteractionBlocker(plugin: JavaPlugin): InteractionBlocker {
        return InteractionBlocker(plugin, { loc, player ->
            schematicLocationBreakAttempt(loc, player)
        }, { loc, player ->
            schematicLocationPlaceAttempt(loc, player)
        })
    }

    /**
     * if SchematicOption PREVENT_BREAK_WHILE_BUILD is set, once a player attempts to break a block this function will be fired
     */
    fun schematicLocationBreakAttempt(location: Location, player: Player)

    /**
     * if SchematicOption PREVENT_BREAK_WHILE_BUILD is set, once a player attempts to place a block this function will be fired
     */
    fun schematicLocationPlaceAttempt(location: Location, player: Player)
}