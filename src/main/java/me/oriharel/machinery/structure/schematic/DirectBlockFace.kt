package me.oriharel.machinery.structure.schematic

import org.bukkit.block.BlockFace

enum class DirectBlockFace(val blockFace: BlockFace) {
    NORTH(BlockFace.NORTH), EAST(BlockFace.EAST), SOUTH(BlockFace.SOUTH), WEST(BlockFace.WEST);
}