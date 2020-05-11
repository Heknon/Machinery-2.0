package me.oriharel.machinery.utilities.schedulers

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class IterativeScheduler<T, R>(iterated: Iterable<T>, plugin: JavaPlugin, period: Long, delay: Long) : Scheduler<T, R>(plugin, iterated.count().toLong(), period, delay) {
    val iterator: Iterator<T> = iterated.iterator()

    override fun taskExecutor(task: T.() -> R, ctx: BukkitRunnable): R {
        return task(iterator.next())
    }
}