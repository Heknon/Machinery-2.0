package me.oriharel.machinery.utilities.schedulers

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.io.Serializable

class IterativeScheduler<T, R>(private val iterated: Iterable<T>, plugin: JavaPlugin, period: Long, delay: Long) : Scheduler<T, Int, R>(plugin, iterated.count().toLong(), period, delay), Serializable {
    private val iterator: Iterator<IndexedValue<T>> = iterated.iterator().withIndex()
    var currentItem: IndexedValue<T>? = null

    override fun taskExecutor(task: T.(Int, Scheduler<T, Int, R>) -> R, ctx: BukkitTask): Pair<Boolean, R?> {
        if (!iterator.hasNext()) {
            return Pair(false, null)
        }
        var indexed = iterator.next()
        while (indexed.index < currentItem?.index ?: -1 && iterator.hasNext()) {
            indexed = iterator.next()
        }
        currentItem = indexed
        return Pair(iterator.hasNext(), task(indexed.value, indexed.index, this))
    }

    override fun singleTaskCompletedExecutor(singleTaskComplete: (T.(R, Scheduler<T, Int, R>) -> Unit)?, returnedValue: R, ctx: BukkitTask) {
        singleTaskComplete?.invoke(currentItem?.value!!, returnedValue, this)
    }
}