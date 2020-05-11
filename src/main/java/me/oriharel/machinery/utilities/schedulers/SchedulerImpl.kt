package me.oriharel.machinery.utilities.schedulers

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class SchedulerImpl<R>(plugin: JavaPlugin, repeatAmount: Long, period: Long, delay: Long) : Scheduler<BukkitRunnable, R>(plugin, repeatAmount, period, delay) {
    override fun taskExecutor(task: BukkitRunnable.() -> R, ctx: BukkitRunnable): R {
        return task(ctx)
    }
}