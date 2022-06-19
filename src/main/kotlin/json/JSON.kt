package json

import json.types.T
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

class JSON {

    object Operator {
        operator fun JsonElement.get(i: String): JsonElement {
            return jsonObject[i]!!
        }

        operator fun JsonElement.invoke(type: KClass<Int>): Int {
            return jsonPrimitive.int
        }

        operator fun JsonElement.invoke(type: KClass<String>): String {
            return jsonPrimitive.content
        }

        operator fun JsonElement.invoke(type: KClass<Long>): Long {
            return jsonPrimitive.long
        }

        operator fun JsonElement.invoke(type: KClass<Boolean>): Boolean {
            return jsonPrimitive.boolean
        }

    }

    companion object {
        val t = T()
        val json = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "#class"

        }

    }
}