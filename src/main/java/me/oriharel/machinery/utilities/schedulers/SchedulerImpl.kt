package me.oriharel.machinery.utilities.schedulers

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class SchedulerImpl<R>(plugin: JavaPlugin, repeatAmount: Long, period: Long, delay: Long) : Scheduler<BukkitTask, Unit, R>(plugin, repeatAmount, period, delay) {
    override fun taskExecutor(task: BukkitTask.(Unit, Scheduler<BukkitTask, Unit, R>) -> R, ctx: BukkitTask): Pair<Boolean, R?> {
        return Pair(true, task(ctx, Unit, this))
    }

    override fun singleTaskCompletedExecutor(singleTaskComplete: (BukkitTask.(R, Scheduler<BukkitTask, Unit, R>) -> Unit)?, returnedValue: R, ctx: BukkitTask) {
        singleTaskComplete?.invoke(ctx, returnedValue, this)
    }
}