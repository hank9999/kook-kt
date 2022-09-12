package com.github.hank9999.kook.http.kookapis

import com.github.hank9999.kook.http.Api
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.json.JSON
import kotlinx.serialization.json.decodeFromJsonElement

class Channel(private val httpApi: HttpApi) {
    suspend fun userList(channelId: String): List<com.github.hank9999.kook.types.User> {
        val data = httpApi.request(Api.Channel.UserList(channelId))
        return JSON.json.decodeFromJsonElement(data)
    }
}