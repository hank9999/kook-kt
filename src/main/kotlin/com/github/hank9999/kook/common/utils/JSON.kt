package com.github.hank9999.kook.common.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

object JSON {
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

    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun defaultJson(): Json {
        return json
    }
}