package me.oriharel.machinery.structure.schematic

import org.bukkit.Material
import org.bukkit.Tag

enum class MaterialType(val materials: Set<Material>) {
    MULTI_DIRECTIONAL(setOf(
            Material.STONE_BRICK_WALL,
            Material.BRICK_WALL,
            Material.ANDESITE_WALL,
            Material.COBBLESTONE_WALL,
            Material.DIORITE_WALL,
            Material.END_STONE_BRICK_WALL,
            Material.GRANITE_WALL,
            Material.MOSSY_COBBLESTONE_WALL,
            Material.MOSSY_STONE_BRICK_WALL,
            Material.NETHER_BRICK_WALL,
            Material.PRISMARINE_WALL,
            Material.RED_NETHER_BRICK_WALL,
            Material.RED_SANDSTONE_WALL,
            Material.SANDSTONE_WALL
    )),
    SIGN(setOf(
            Material.SPRUCE_SIGN,
            Material.DARK_OAK_SIGN,
            Material.ACACIA_SIGN,
            Material.BIRCH_SIGN,
            Material.JUNGLE_SIGN,
            Material.OAK_SIGN
    )),
    ANVIL(setOf(
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL
    )),
    FENCE(Tag.FENCES.values),
    WALL_SIGN(setOf(
            Material.SPRUCE_WALL_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.OAK_WALL_SIGN
    )),
    CHEST(setOf(
            Material.CHEST,
            Material.TRAPPED_CHEST
    ))
}