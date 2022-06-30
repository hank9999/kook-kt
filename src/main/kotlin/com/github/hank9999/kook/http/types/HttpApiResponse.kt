package com.github.hank9999.kook.http.types

import kotlinx.serialization.json.JsonElement

data class HttpApiResponse(
    val json: JsonElement,
    val headers: Map<String, List<String>>
)
