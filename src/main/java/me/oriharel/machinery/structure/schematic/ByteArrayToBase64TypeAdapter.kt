package me.oriharel.machinery.structure.schematic

import com.google.gson.*
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64
import java.lang.reflect.Type

class ByteArrayToBase64TypeAdapter : JsonSerializer<ByteArray>, JsonDeserializer<ByteArray> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ByteArray {
        return Base64.decodeBase64(json.asString)
    }

    override fun serialize(src: ByteArray, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(Base64.encodeBase64String(src))
    }
}