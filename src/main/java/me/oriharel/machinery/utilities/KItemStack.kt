package me.oriharel.machinery.utilities

import me.oriharel.machinery.utilities.extensions.modifyMeta
import me.oriharel.machinery.utilities.extensions.modifyNBT
import net.minecraft.server.v1_15_R1.NBTBase
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class KItemStack @JvmOverloads constructor(
        material: Material,
        amount: Int = 1,
        displayName: String? = null,
        lore: List<String>? = null,
        metadataModifier: ((ItemMeta) -> Unit)? = null,
        nbtModifier: ((MutableMap<String, NBTBase>) -> Unit)? = null
) : ItemStack(
        material,
        amount
) {

    init {
        modifyMeta {
            if (displayName != null) it.setDisplayName(displayName)
            if (lore != null) it.lore = lore
        }.modifyMeta(metadataModifier ?: {}).modifyNBT(nbtModifier ?: {})
    }


}