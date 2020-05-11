package me.oriharel.machinery.utilities.extensions

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

fun <T, A> T.execute(async: Boolean, plugin: JavaPlugin? = null, callback: ((A) -> Unit)? = null, executor: () -> A): T {
    if (async) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin!!, Runnable {
            callback?.invoke(executor())
        })
    } else {
        callback?.invoke(executor())
    }
    return this
}