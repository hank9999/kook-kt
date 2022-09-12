package com.github.hank9999.kook.http.kookapis

import com.github.hank9999.kook.http.Api
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.http.types.apiResponse.guild.View
import com.github.hank9999.kook.json.JSON
import kotlinx.serialization.json.decodeFromJsonElement

class Guild(private val httpApi: HttpApi) {
    suspend fun view(guildId: String): View {
        val data = httpApi.request(Api.Guild.View(guildId))
        return JSON.json.decodeFromJsonElement(data)
    }
}