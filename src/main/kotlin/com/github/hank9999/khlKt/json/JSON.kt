package com.github.hank9999.khlKt.json

import kotlinx.serialization.json.*

class JSON {

    object Extension {
        operator fun JsonElement.get(i: String): JsonElement {
            return jsonObject[i]!!
        }

        val JsonElement.String: String
            get() { return this.jsonPrimitive.content }

        val JsonElement.Int: Int
            get() { return this.jsonPrimitive.int }

        val JsonElement.Bool: Boolean
            get() { return this.jsonPrimitive.boolean }

        val JsonElement.Long: Long
            get() { return this.jsonPrimitive.long }

        val JsonElement.Double: Double
            get() { return this.jsonPrimitive.double }

        val JsonElement.Float: Float
            get() { return this.jsonPrimitive.float }
    }

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "#class"

        }

    }
}