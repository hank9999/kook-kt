package com.github.hank9999.kook.http.kookapis

import com.github.hank9999.kook.http.Api
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.json.JSON.Extension.String
import com.github.hank9999.kook.json.JSON.Extension.get

class Gateway(private val httpApi: HttpApi) {
    suspend fun index(): String {
        val data = httpApi.request(Api.Gateway.Index())
        return data["url"].String
    }
}