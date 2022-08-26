package com.github.hank9999.kook.http.kookapis

import com.github.hank9999.kook.http.Api
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.json.JSON
import kotlinx.serialization.json.decodeFromJsonElement

class User(private val httpApi: HttpApi) {
    suspend fun me(): com.github.hank9999.kook.types.User {
        val data = httpApi.request(Api.User.Me())
        return JSON.json.decodeFromJsonElement(data)
    }

    suspend fun offline() {
        httpApi.request(Api.User.Offline())
    }
}