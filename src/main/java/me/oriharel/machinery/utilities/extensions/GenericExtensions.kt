package me.oriharel.machinery.utilities.extensions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.oriharel.machinery.utilities.schedulers.IterativeScheduler
import me.oriharel.machinery.utilities.schedulers.Scheduler
import org.apache.commons.lang.SerializationException
import org.apache.commons.lang.SerializationUtils
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*
import java.util.function.Consumer

fun <T, A> T.execute(async: Boolean, plugin: JavaPlugin? = null, executor: () -> A): T {
    if (async) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin!!, Runnable {
            executor()
        })
    } else {
        executor()
    }
    return this
}

inline fun <T> Iterable<T>.forEachContextual(action: T.() -> Unit) {
    for (i in this) {
        action(i)
    }
}

fun <T, R> Iterable<T>.scheduledIteration(plugin: JavaPlugin, period: Long, delay: Long = 0, task: T.(Int, Scheduler<T, Int, R>) -> R): IterativeScheduler<T, R> {
    val iterativeScheduler: IterativeScheduler<T, R> = IterativeScheduler(this, plugin, period, delay)
    return iterativeScheduler.setTask { t, ctx ->
        task(this, t, ctx)
    }.run() as IterativeScheduler<T, R>
}

/**
 * index is inclusive
 */
fun <T> Collection<T>.removeUpTo(index: Int): Collection<T> {
    val list: MutableCollection<T> = mutableListOf()
    forEachIndexed { i, it ->
        if (i < index) return@forEachIndexed
        list.add(it)
    }
    return list
}

fun Serializable.serialize(): ByteArray {
    val outStream = ByteArrayOutputStream(10000)
    val out = ObjectOutputStream(outStream)
    out.writeObject(this)
    return outStream.toByteArray()
}

fun Function<*>.serialize(): ByteArray? {
    return try {
        (this as Serializable).serialize()
    } catch (e: SerializationException) {
        print("Unable to serialize $this!")
        e.printStackTrace()
        null
    }
}

inline fun <reified T> ByteArray.deserialize(): T? {
    val deserialized = SerializationUtils.deserialize(this)
    if (deserialized !is T) return null
    return deserialized
}

fun ByteArray.deserializeAndExecute(vararg arguments: Any?): Function<*>? {
    val deserialized = this.deserialize<Function<*>>()!!
    val method = deserialized::class.java.declaredMethods[0]
    method.isAccessible = true
    method.invoke(deserialized, *arguments)
    return deserialized
}

fun UUID.toJsonElement(): JsonObject {
    return JsonObject().apply {
        add("least", JsonPrimitive(this@toJsonElement.leastSignificantBits))
        add("most", JsonPrimitive(this@toJsonElement.mostSignificantBits))
    }
}

fun JsonElement.toUUID(): UUID {
    val obj = this.asJsonObject
    return UUID(obj.get("most").asLong, obj.get("least").asLong)
}