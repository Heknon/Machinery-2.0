package me.oriharel.machinery.structure.schematic.utilities

import com.google.gson.*
import me.oriharel.machinery.utilities.extensions.*
import org.bukkit.Location
import java.lang.reflect.Type

class LocationTypeAdapter : JsonSerializer<Location>, JsonDeserializer<Location> {
    override fun serialize(loc: Location, type: Type, ctx: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.add("xyz", JsonPrimitive(loc.compress()))
        obj.add("world", loc.world?.uid?.toJsonElement())

        return obj
    }

    override fun deserialize(elem: JsonElement, type: Type, ctx: JsonDeserializationContext): Location {
        val obj = elem.asJsonObject

        return obj.get("xyz").asLong.decompress(obj.get("world").toUUID().toWorld())
    }
}