package me.oriharel.machinery.utilities.extensions

import net.minecraft.server.v1_15_R1.NBTBase
import net.minecraft.server.v1_15_R1.NBTTagCompound
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.modifyMeta(modifier: (ItemMeta) -> Unit): ItemStack {
    val meta = itemMeta!!
    modifier(meta)
    meta.lore?.map { ChatColor.translateAlternateColorCodes('&', it) }
    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.displayName))
    itemMeta = meta
    return this
}

fun ItemStack.modifyNBT(modifier: (MutableMap<String, NBTBase>) -> Unit): ItemStack {
    modifier(getNBT())
    return this
}

fun ItemStack.metaReference(): ItemMeta? {
    val meta: ItemMeta? = getPrivateFieldValue("meta")
    if (meta == null) itemMeta = Bukkit.getItemFactory().getItemMeta(type)
    return getPrivateFieldValue("meta")
}

fun ItemStack.getNBT(): MutableMap<String, NBTBase> {
    val metaReference = metaReference()
    return metaReference.getPrivateFieldValue("unhandledTags")
            ?: metaReference.setPrivateFieldValue("unhandledTags", mutableMapOf<String, NBTBase>())
                    .let { metaReference?.getPrivateFieldValue<ItemMeta, MutableMap<String, NBTBase>>("unhandledTags")!! }
}

fun ItemStack.getNBTClone(): NBTTagCompound {
    return CraftItemStack.asNMSCopy(this).orCreateTag
}
