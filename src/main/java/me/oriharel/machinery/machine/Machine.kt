package me.oriharel.machinery.machine

import me.oriharel.machinery.Buildable
import me.oriharel.machinery.OwnableStructure
import me.oriharel.machinery.structure.Schematic
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.*

class Machine(
        override val structure: Pair<String, Schematic>,
        override val owner: UUID,
        override val allowedAccessors: Set<UUID>
) : OwnableStructure, InventoryHolder, Buildable {

    override fun getInventory(): Inventory {
        return Bukkit.createInventory(this, 2)
    }

    override fun build(location: Location) {

    }
}