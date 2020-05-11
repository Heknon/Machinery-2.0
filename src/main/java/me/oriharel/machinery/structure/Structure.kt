package me.oriharel.machinery.structure


import me.oriharel.machinery.structure.schematic.Schematic
import org.bukkit.Location

/**
 * Used to abstract the logic of building a schematic.
 */
class Structure(private val schematic: Schematic, val name: String) {

    data class PrintResult(val placementLocations: List<Location?>, var openGUIBlockLocation: Location?)

}