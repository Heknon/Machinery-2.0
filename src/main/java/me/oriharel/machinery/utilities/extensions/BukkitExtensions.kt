package me.oriharel.machinery.utilities.extensions

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.block.Block
import java.util.*

fun Block.subtract(x: Int, y: Int, z: Int): Block {
    return this.location.clone().subtract(x.toDouble(), y.toDouble(), z.toDouble()).block
}

fun UUID.toOfflinePlayer(): OfflinePlayer {
    return Bukkit.getOfflinePlayer(this)
}

fun Location.compress(): Long {
    val x = this.blockX
    val y = this.blockY
    val z = this.blockZ
    return x.toLong() and 0x7FFFFFF or (z.toLong() and 0x7FFFFFF shl 27) or (y.toLong() shl 54)
}

private fun Long.decompress(): Location {
    val packed = this
    val x = (packed shl 37 shr 37).toInt()
    val y = (packed ushr 54).toInt()
    val z = (packed shl 10 shr 37).toInt()
    return Location(null, x.toDouble(), y.toDouble(), z.toDouble())
}

fun Long.decompress(world: World?): Location {
    val loc = this.decompress()
    loc.world = world
    return loc
}

fun UUID.toWorld(): World? {
    return Bukkit.getWorld(this)
}