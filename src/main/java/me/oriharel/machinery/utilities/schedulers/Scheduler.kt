package me.oriharel.machinery.utilities.schedulers

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

abstract class Scheduler<T, R>(private val plugin: JavaPlugin, private var repeatAmount: Long, private var period: Long, private var delay: Long) {
    private var runnable: BukkitRunnable? = null
    private var onSingleTaskComplete: (BukkitRunnable.() -> Unit)? = null
    private var onRepeatComplete: (BukkitRunnable.(List<R>) -> Unit)? = null
    private var mounted = false

    fun setTask(task: T.() -> R): Scheduler<T, R> {
        runnable = object : BukkitRunnable() {
            var returnStore: MutableList<R> = mutableListOf()
            var repeatedAmount: Long = 0
            override fun run() {
                if (!mounted) {
                    returnStore = mutableListOf()
                    repeatedAmount = 0
                    mounted = true
                }
                if (repeatedAmount >= repeatAmount) {
                    onRepeatComplete?.invoke(runnable!!, returnStore)
                    cancel()
                    return
                }
                returnStore.add(taskExecutor(task, this))
                onSingleTaskComplete?.invoke(runnable!!)
                repeatedAmount++
            }
        }
        return this
    }

    abstract fun taskExecutor(task: T.() -> R, ctx: BukkitRunnable): R

    fun setOnSingleTaskComplete(singleTaskComplete: BukkitRunnable.() -> Unit): Scheduler<T, R> {
        this.onSingleTaskComplete = singleTaskComplete
        return this
    }

    fun setOnRepeatComplete(repeatCompleted: BukkitRunnable.(List<R>) -> Unit): Scheduler<T, R> {
        onRepeatComplete = repeatCompleted
        return this
    }

    /**
     * If you want to change the delay/period of execution or even the task you can do so but you must call remount.
     */
    fun remount(): Scheduler<T, R> {
        runnable?.cancel()
        mounted = false
        run()
        return this
    }

    fun run(): Scheduler<T, R> {
        runnable?.runTaskTimer(plugin, delay, period) ?: throw RuntimeException("You must set a task!")
        return this
    }
}
