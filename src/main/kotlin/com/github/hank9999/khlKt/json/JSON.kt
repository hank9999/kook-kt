package com.github.hank9999.khlKt.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class JSON {

    object Extension {
        operator fun JsonElement.get(i: String): JsonElement {
            return jsonObject[i]!!
        }

        val JsonElement.string: String
            get() { return jsonObject.jsonPrimitive.string }

        val JsonElement.int: Int
            get() { return jsonObject.jsonPrimitive.int }

        val JsonElement.bool: Boolean
            get() { return jsonObject.jsonPrimitive.bool }

        val JsonElement.long: Long
            get() { return jsonObject.jsonPrimitive.long }

        val JsonElement.double: Double
            get() { return jsonObject.jsonPrimitive.double }

        val JsonElement.float: Float
            get() { return jsonObject.jsonPrimitive.float }
    }

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "#class"

        }

    }
}