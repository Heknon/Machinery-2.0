package me.oriharel.machinery.utilities.schedulers

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.function.Consumer

/**
 * @param T The context type a task has. For example the IterativeScheduler has as it's context the type of the iterable
 * @param P a parameter that could be passed down to a task
 * @param R the return type of a task. These are accumulated to then be sent in the callback as a list of all the returns
 */
abstract class Scheduler<T, P, R>(private val plugin: JavaPlugin, private var repeatAmount: Long, internal var period: Long, private var delay: Long) {
    private var taskConsumer: Consumer<BukkitTask>? = null
    var task: BukkitTask? = null
    private var onSingleTaskComplete: (T.(R, Scheduler<T, P, R>) -> Unit)? = null
    private var onRepeatComplete: (Scheduler<T, P, R>.(List<R>) -> Unit)? = null
    private var mounted = false

    private var returnStore: MutableList<R> = mutableListOf()
    private var repeatedAmount: Long = 0

    fun setTask(task: T.(P, Scheduler<T, P, R>) -> R): Scheduler<T, P, R> {
        taskConsumer = Consumer {
            if (!mounted) {
                this.task = it
                mounted = true
            }
            if (repeatedAmount >= repeatAmount) {
                onRepeatComplete?.invoke(this, returnStore)
                it.cancel()
                return@Consumer
            }
            val returnedValue = taskExecutor(task, it)
            returnStore.add(returnedValue)
            singleTaskCompletedExecutor(onSingleTaskComplete, returnedValue, it)
            repeatedAmount++

        }
        return this
    }

    abstract fun taskExecutor(task: T.(P, Scheduler<T, P, R>) -> R, ctx: BukkitTask): R
    abstract fun singleTaskCompletedExecutor(singleTaskComplete: (T.(R, Scheduler<T, P, R>) -> Unit)?, returnedValue: R, ctx: BukkitTask)

    fun setOnSingleTaskComplete(singleTaskComplete: T.(R, Scheduler<T, P, R>) -> Unit): Scheduler<T, P, R> {
        this.onSingleTaskComplete = singleTaskComplete
        return this
    }

    fun setOnRepeatComplete(repeatCompleted: Scheduler<T, P, R>.(List<R>) -> Unit): Scheduler<T, P, R> {
        onRepeatComplete = repeatCompleted
        return this
    }

    fun update(period: Long = this.period, repeatAmount: Long = this.repeatAmount, delay: Long = this.delay) {
        this.period = period
        this.repeatAmount = repeatAmount
        this.delay = delay
        remount()
    }

    /**
     * If you want to change the delay/period of execution or even the task you can do so but you must call remount.
     */
    fun remount(): Scheduler<T, P, R> {
        task?.cancel()
        task = null
        mounted = false
        run()
        return this
    }

    fun run(): Scheduler<T, P, R> {
        print("before: $this")
        Bukkit.getScheduler().runTaskTimer(plugin, taskConsumer
                ?: throw RuntimeException("You must set a task!"), delay, period)
        print("after: $this")
        return this
    }

    override fun toString(): String {
        return "Scheduler(plugin=$plugin, repeatAmount=$repeatAmount, period=$period, delay=$delay, taskConsumer=$taskConsumer, task=$task, onSingleTaskComplete=$onSingleTaskComplete, onRepeatComplete=$onRepeatComplete, mounted=$mounted, returnStore=$returnStore, repeatedAmount=$repeatedAmount)"
    }


}
