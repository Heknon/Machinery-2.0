package me.oriharel.machinery.structure.schematic

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import me.oriharel.machinery.Machinery
import me.oriharel.machinery.utilities.extensions.deserialize
import me.oriharel.machinery.utilities.extensions.getPrivateFieldValue
import me.oriharel.machinery.utilities.extensions.serialize
import me.oriharel.machinery.utilities.extensions.setPrivateFieldValue
import me.oriharel.machinery.utilities.schedulers.IterativeScheduler
import org.bukkit.Location
import org.bukkit.scheduler.BukkitTask
import java.lang.reflect.Type
import java.util.function.Consumer

class BuildTaskTypeAdapter : JsonSerializer<BuildTask>, JsonDeserializer<BuildTask> {
    override fun serialize(buildTask: BuildTask, type: Type, ctx: JsonSerializationContext): JsonElement {
        val obj = JsonObject()



        val taskConsumer: Consumer<BukkitTask>? = buildTask.getPrivateFieldValue("taskConsumer")
        val onSingleTaskComplete: (Location.(Unit, BuildTask) -> Unit)? = buildTask.getPrivateFieldValue("onSingleTaskComplete")
        val onRepeatComplete: (BuildTask.(List<Unit>) -> Unit)? = buildTask.getPrivateFieldValue("onRepeatComplete")
        println("------------------------------------------ AAAAAA")
        println(onRepeatComplete?.serialize()?.toList())
        println("------------------------------------------")
        val mounted: Boolean? = buildTask.getPrivateFieldValue("mounted")
        val repeatAmount: Long? = buildTask.getPrivateFieldValue("repeatAmount")
        val delay: Long? = buildTask.getPrivateFieldValue("delay")
        val repeatedAmount: Long? = buildTask.getPrivateFieldValue("repeatedAmount")

        val returnStore: MutableList<Unit>? = buildTask.getPrivateFieldValue("returnStore")
        val period: Long = buildTask.period
        val iterated: Iterable<Location>? = buildTask.getPrivateFieldValue("iterated")
        val currentItem: IndexedValue<Location>? = buildTask.getPrivateFieldValue("currentItem")
        obj.add("period", JsonPrimitive(period))
        obj.add("returnStore", ctx.serialize(returnStore, object : TypeToken<MutableList<Unit>>() {}.type))
        obj.add("repeatedAmount", JsonPrimitive(repeatedAmount))
        obj.add("delay", JsonPrimitive(delay))
        obj.add("repeatAmount", JsonPrimitive(repeatAmount))
        obj.add("mounted", JsonPrimitive(mounted))
        obj.add("onRepeatComplete", ctx.serialize(onRepeatComplete?.serialize(), ByteArray::class.java))
        obj.add("onSingleTaskComplete", ctx.serialize(onSingleTaskComplete?.serialize(), ByteArray::class.java))
        obj.add("taskConsumer", ctx.serialize(taskConsumer?.serialize(), ByteArray::class.java))
        obj.add("iterated", ctx.serialize(iterated, object : TypeToken<Collection<Location>>() {}.type))
        obj.add("currentItem", ctx.serialize(currentItem, object : TypeToken<IndexedValue<Location>>() {}.type))

        return obj
    }

    override fun deserialize(element: JsonElement, type: Type, ctx: JsonDeserializationContext): BuildTask? {
        val obj = element.asJsonObject

        val taskConsumer: Consumer<BukkitTask>? = ctx.deserialize<ByteArray>(obj.get("taskConsumer"), ByteArray::class.java).deserialize()
        val onSingleTaskComplete: (Location.(Unit, BuildTask) -> Unit)? = ctx.deserialize<ByteArray>(obj.get("onSingleTaskComplete"), ByteArray::class.java).deserialize()
        val onRepeatComplete: (BuildTask.(List<Unit>) -> Unit)? = ctx.deserialize<ByteArray>(obj.get("onRepeatComplete"), ByteArray::class.java).deserialize()
        val mounted: Boolean? = obj.get("mounted").asBoolean
        val repeatAmount: Long? = obj.get("repeatAmount").asLong
        val delay: Long? = obj.get("delay").asLong
        val repeatedAmount: Long? = obj.get("repeatedAmount").asLong

        val returnStore: MutableList<Unit>? = ctx.deserialize(obj.get("returnStore"), object : TypeToken<MutableList<Unit>>() {}.type)
        val period: Long = obj.get("period").asLong
        val iterated: Iterable<Location> = ctx.deserialize(obj.get("iterated"), object : TypeToken<Collection<Location>>() {}.type)
        val currentItem: IndexedValue<Location> = ctx.deserialize(obj.get("currentItem"), object : TypeToken<IndexedValue<Location>>() {}.type)

        val scheduler = IterativeScheduler<Location, Unit>(iterated, Machinery.INSTANCE, period, delay!!)

        scheduler.setPrivateFieldValue("taskConsumer", taskConsumer)
        scheduler.setPrivateFieldValue("onSingleTaskComplete", onSingleTaskComplete)
        scheduler.setPrivateFieldValue("onRepeatComplete", onRepeatComplete)
        scheduler.setPrivateFieldValue("mounted", mounted)
        scheduler.setPrivateFieldValue("repeatAmount", repeatAmount)
        scheduler.setPrivateFieldValue("repeatedAmount", repeatedAmount)
        scheduler.setPrivateFieldValue("returnStore", returnStore)
        scheduler.setPrivateFieldValue("currentItem", currentItem)

        return scheduler
    }
}