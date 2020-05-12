package me.oriharel.machinery.structure.schematic

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import me.oriharel.machinery.utilities.extensions.*
import org.bukkit.Location
import java.lang.reflect.Type
import java.util.*

class SchematicStateTypeAdapter : JsonSerializer<Schematic.SchematicState>, JsonDeserializer<Schematic.SchematicState> {
    override fun serialize(state: Schematic.SchematicState, type: Type, ctx: JsonSerializationContext): JsonElement {
        val obj = JsonObject()

        obj.add("buildLocation", JsonPrimitive(state.buildLocation.compress()))
        obj.add("buildLocationWorld", state.buildLocation.world?.uid?.toJsonElement())
        obj.add("options", ctx.serialize(state.options, object : TypeToken<Set<SchematicOption>>() {}.type))
        obj.add("scheduler", ctx.serialize(state.scheduler, object : TypeToken<BuildTask>() {}.type))
        obj.add("currentIndex", JsonPrimitive(state.currentIndex))
        obj.add("builder", state.builder?.toJsonElement())
        obj.add("finishedBuilding", JsonPrimitive(state.finishedBuilding))
        obj.add("placeBlockEvery", JsonPrimitive(state.placeBlockEvery))
        obj.add("blockPlacedCallback", ctx.serialize(state.blockPlacedCallback?.serialize(), ByteArray::class.java))
        obj.add("buildingFinishedCallback", ctx.serialize(state.buildingFinishedCallback?.serialize(), ByteArray::class.java))
        obj.add("statefulBlockEncounterLocation", JsonPrimitive(state.statefulBlockEncounterLocation?.compress()))
        obj.add("statefulBlockEncounterLocationWorld", state.statefulBlockEncounterLocation?.world?.uid?.toJsonElement())
        obj.add("uuid", state.uuid.toJsonElement())

        return obj
    }

    override fun deserialize(element: JsonElement, type: Type, ctx: JsonDeserializationContext): Schematic.SchematicState {
        val obj = element.asJsonObject

        val buildLocation: Location = obj.get("buildLocation").asLong.decompress(obj.get("buildLocationWorld").toUUID().toWorld())
        val options: Set<SchematicOption> = ctx.deserialize(obj.get("options"), object : TypeToken<Set<SchematicOption>>() {}.type)
        val scheduler: BuildTask? = ctx.deserialize(obj.get("scheduler"), object : TypeToken<BuildTask>() {}.type)
        val currentIndex: Int = obj.get("currentIndex").asInt
        val builder: UUID? = obj.get("builder").toUUID()
        val finishedBuilding: Boolean = obj.get("finishedBuilding").asBoolean
        val placeBlockEvery: Long = obj.get("placeBlockEvery").asLong
        val blockPlacedCallback: (Location.(Unit, BuildTask) -> Unit)? = ctx.deserialize(obj.get("blockPlacedCallback"), ByteArray::class.java)
        val buildingFinishedCallback: ((BuildTask) -> Unit)? = ctx.deserialize(obj.get("buildingFinishedCallback"), ByteArray::class.java)
        val statefulBlockEncounterLocation: Location? = obj.get("statefulBlockEncounterLocation").asLong.decompress(obj.get("statefulBlockEncounterLocationWorld").toUUID().toWorld())
        val uuid = obj.get("uuid").toUUID()

        return Schematic.SchematicState(
                buildLocation = buildLocation,
                options = options,
                scheduler = scheduler,
                currentIndex = currentIndex,
                builder = builder,
                finishedBuilding = finishedBuilding,
                placeBlockEvery = placeBlockEvery,
                blockPlacedCallback = blockPlacedCallback,
                buildingFinishedCallback = buildingFinishedCallback,
                statefulBlockEncounterLocation = statefulBlockEncounterLocation,
                uuid = uuid
        )
    }
}