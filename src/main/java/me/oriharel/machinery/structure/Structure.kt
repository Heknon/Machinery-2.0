package me.oriharel.machinery.structure


import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Used to abstract the logic of building a schematic.
 */
class Structure(private val schematic: Schematic, val name: String) {

    data class PrintResult(val placementLocations: List<Location?>, var openGUIBlockLocation: Location?)

}