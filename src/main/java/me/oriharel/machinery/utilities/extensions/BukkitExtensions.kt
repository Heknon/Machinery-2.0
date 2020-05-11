package me.oriharel.machinery.utilities.extensions

import org.bukkit.block.Block

fun Block.subtract(x: Int, y: Int, z: Int): Block {
    return this.location.clone().subtract(x.toDouble(), y.toDouble(), z.toDouble()).block
}