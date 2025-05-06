package com.github.hank9999.kook.common.utils

import kotlinx.serialization.json.Json

class JSON {
    companion object {
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
}