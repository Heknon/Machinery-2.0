package me.oriharel.machinery.utilities.schedulers

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class IterativeScheduler<T, R>(iterated: Iterable<T>, plugin: JavaPlugin, period: Long, delay: Long) : Scheduler<T, Int, R>(plugin, iterated.count().toLong(), period, delay) {
    private val iterator: Iterator<IndexedValue<T>> = iterated.iterator().withIndex()
    var currentItem: IndexedValue<T>? = null

    override fun taskExecutor(task: T.(Int, Scheduler<T, Int, R>) -> R, ctx: BukkitTask): R {
        val indexed = iterator.next()
        currentItem = indexed
        return task(indexed.value, indexed.index, this)
    }

    override fun singleTaskCompletedExecutor(singleTaskComplete: (T.(R, Scheduler<T, Int, R>) -> Unit)?, returnedValue: R, ctx: BukkitTask) {
        singleTaskComplete?.invoke(currentItem?.value!!, returnedValue, this)
    }
}