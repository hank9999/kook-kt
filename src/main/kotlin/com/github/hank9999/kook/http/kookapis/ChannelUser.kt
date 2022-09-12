package com.github.hank9999.kook.http.kookapis

import com.github.hank9999.kook.http.Api
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.types.Channel
import kotlinx.serialization.json.decodeFromJsonElement

class ChannelUser(private val httpApi: HttpApi) {
    suspend fun getJoinedChannel(guildId: String, userId: String): List<Channel> {
        val data = httpApi.request(Api.ChannelUser.GetJoinedChannel(guildId, userId))
        return json.decodeFromJsonElement(data)
    }
}