package com.github.hank9999.kook.http.kookapis

import com.github.hank9999.kook.http.Api
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.http.types.apiResponse.MessageCreate
import com.github.hank9999.kook.json.JSON
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.json.decodeFromJsonElement

class Message(private val httpApi: HttpApi) {
    suspend fun create(targetId: String, content: Any, type: MessageTypes? = null, quote: String? = null, nonce: String? = null, tempTargetId: String? = null): MessageCreate {
        val data = httpApi.request(Api.Message.Create(targetId, content, type, quote, nonce, tempTargetId))
        return JSON.json.decodeFromJsonElement(data)
    }
    suspend fun delete(msgId: String) {
        httpApi.request(Api.Message.Delete(msgId))
    }
    suspend fun update(msgId: String, content: Any, quote: String? = null, tempTargetId: String? = null) {
        httpApi.request(Api.Message.Update(msgId, content, quote, tempTargetId))
    }
}