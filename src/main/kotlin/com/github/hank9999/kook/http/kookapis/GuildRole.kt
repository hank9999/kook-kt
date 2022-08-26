package com.github.hank9999.kook.http.kookapis

import com.github.hank9999.kook.http.Api
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.json.JSON
import com.github.hank9999.kook.types.Role
import kotlinx.serialization.json.decodeFromJsonElement

class GuildRole(private val httpApi: HttpApi) {
    suspend fun list(guildId: String): List<Role> {
        val data = httpApi.request(Api.GuildRole.List(guildId))
        return JSON.json.decodeFromJsonElement(data)
    }
}
