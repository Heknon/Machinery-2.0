package me.oriharel.machinery.utilities.extensions

import me.oriharel.machinery.utilities.schedulers.IterativeScheduler
import me.oriharel.machinery.utilities.schedulers.Scheduler
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

inline fun <T> Iterable<T>.contextualForEach(action: T.() -> Unit) {
    for (i in this) {
        action(i)
    }
}

fun <T, R> Iterable<T>.scheduledIteration(plugin: JavaPlugin, period: Long, delay: Long = 0, task: T.(Int, Scheduler<T, Int, R>) -> R): Scheduler<T, Int, R> {
    val iterativeScheduler: IterativeScheduler<T, R> = IterativeScheduler(this, plugin, period, delay)
    return iterativeScheduler.setTask { t, ctx ->
        task(this, t, ctx)
    }.run()
}