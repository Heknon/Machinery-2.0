package me.oriharel.machinery.structure.schematic.utilities

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import me.oriharel.machinery.structure.schematic.Schematic
import me.oriharel.machinery.structure.schematic.SchematicOption
import me.oriharel.machinery.utilities.extensions.*
import org.bukkit.Location
import org.bukkit.block.BlockFace
import java.lang.reflect.Type
import java.util.*

class SchematicStateTypeAdapter : JsonSerializer<Schematic.SchematicState>, JsonDeserializer<Schematic.SchematicState> {
    override fun serialize(state: Schematic.SchematicState, type: Type, ctx: JsonSerializationContext): JsonElement {
        val obj = JsonObject()

        obj.add("buildLocation", JsonPrimitive(state.buildLocation.compress()))
        obj.add("buildLocationWorld", state.buildLocation.world?.uid?.toJsonElement())
        obj.add("options", ctx.serialize(state.options, object : TypeToken<Set<SchematicOption>>() {}.type))
        obj.add("currentIndex", JsonPrimitive(state.currentIndex))
        obj.add("builder", state.builder?.toJsonElement())
        obj.add("finishedBuilding", JsonPrimitive(state.finishedBuilding))
        obj.add("placeBlockEvery", JsonPrimitive(state.placeBlockEvery))
        obj.add("uuid", state.uuid.toJsonElement())
        obj.add("originalFacing", ctx.serialize(state.originalFacing, BlockFace::class.java))
        obj.add("placementLocations", ctx.serialize(state.placementLocations, object : TypeToken<MutableSet<Location>>() {}.type))

        return obj
    }

    override fun deserialize(element: JsonElement, type: Type, ctx: JsonDeserializationContext): Schematic.SchematicState {
        val obj = element.asJsonObject

        val buildLocation: Location = obj.get("buildLocation").asLong.decompress(obj.get("buildLocationWorld").toUUID().toWorld())
        val options: Set<SchematicOption> = ctx.deserialize(obj.get("options"), object : TypeToken<Set<SchematicOption>>() {}.type)
        val currentIndex: Int = obj.get("currentIndex").asInt
        val builder: UUID? = obj.get("builder").toUUID()
        val finishedBuilding: Boolean = obj.get("finishedBuilding").asBoolean
        val placeBlockEvery: Long = obj.get("placeBlockEvery").asLong
        val uuid = obj.get("uuid").toUUID()
        val placementLocations: MutableSet<Location> = ctx.deserialize(obj.get("placementLocations"), object : TypeToken<MutableSet<Location>>() {}.type)
        val originalFacing: BlockFace = ctx.deserialize(obj.get("originalFacing"), BlockFace::class.java)

        return Schematic.SchematicState(
                buildLocation = buildLocation,
                options = options,
                currentIndex = currentIndex,
                builder = builder,
                finishedBuilding = finishedBuilding,
                placeBlockEvery = placeBlockEvery,
                uuid = uuid,
                originalFacing = originalFacing,
                placementLocations = placementLocations
        )
    }
}