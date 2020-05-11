package me.oriharel.machinery.structure

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minecraft.server.v1_15_R1.NBTTagCompound
import net.minecraft.server.v1_15_R1.NBTTagList
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

/**
 * @param position - position of text to read from
 * @return text at the specified position on the sign
 */
fun NBTTagCompound.getSignLineFromNBT(position: String): String? {
    val obj = Gson().fromJson(getString(position), JsonObject::class.java)

    return if (obj["extra"] != null)
        obj["extra"].asJsonArray[0].asJsonObject["text"].asString
    else null
}

/**
 * @return a map, with the key as the vector, and the value as a second map with the key as the slot and the value as the item
 */
fun NBTTagList.getItemsFromNBT(): Map<Vector, MutableList<IndexedInventoryItem>> {
    val allItems: MutableMap<Vector, MutableList<IndexedInventoryItem>> = HashMap()

    forEach {
        if (it !is NBTTagCompound) return@forEach
        if (it.getString("Id") == "minecraft:chest") {
            val items = it["Items"] as NBTTagList
            val pos = it.getIntArray("Pos")
            val loc = Vector(pos[0], pos[1], pos[2])
            if (allItems[loc] == null) allItems[loc] = mutableListOf()

            for (itemInChest in items) {
                if (itemInChest !is NBTTagCompound) continue

                val mat = Material.valueOf(itemInChest.getString("id").replace("minecraft:", "").toUpperCase())
                val item = ItemStack(mat, itemInChest.getInt("Count"))

                allItems[Vector(pos[0], pos[1], pos[2])]?.add(IndexedInventoryItem(itemInChest.getInt("Slot"), item))
            }
        }
    }

    return allItems
}

class IndexedInventoryItem(val index: Int, val item: ItemStack)