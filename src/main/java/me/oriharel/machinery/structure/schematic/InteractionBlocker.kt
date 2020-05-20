package me.oriharel.machinery.structure.schematic

import me.oriharel.machinery.utilities.listen
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.plugin.java.JavaPlugin

class InteractionBlocker(
        private val plugin: JavaPlugin,
        private val onBreak: (loc: Location, player: Player) -> Unit,
        private val onPlace: (loc: Location, player: Player) -> Unit
) {
    private val uninteractableLocations: MutableSet<Location> = mutableSetOf()

    init {
        listen<BlockBreakEvent>(plugin) {
            if (uninteractableLocations.contains(block.location)) {
                isCancelled = true
                onBreak(block.location, player)
            }
        }

        listen<BlockPlaceEvent>(plugin) {
            if (uninteractableLocations.contains(block.location)) {
                isCancelled = true
                onPlace(block.location, player)
            }
        }
    }

    fun addUninteractableLocations(vararg loc: Location) {
        uninteractableLocations.addAll(loc)
    }

    fun addUninteractableLocation(loc: Location) {
        uninteractableLocations.add(loc)
    }


}