package com.github.hank9999.kook.http.kookapis

import com.github.hank9999.kook.http.Api
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.http.types.apiResponse.MessageCreate
import com.github.hank9999.kook.json.JSON
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.json.decodeFromJsonElement

class DirectMessage(private val httpApi: HttpApi) {
    suspend fun create(targetId: String, content: Any, type: MessageTypes? = null, quote: String? = null, nonce: String? = null): MessageCreate {
        val data = httpApi.request(Api.DirectMessage.Create(targetId, type?.type, content.toString(), quote, nonce))
        return JSON.json.decodeFromJsonElement(data)
    }
    suspend fun delete(msgId: String) {
        httpApi.request(Api.DirectMessage.Delete(msgId))
    }
    suspend fun update(msgId: String, content: Any, quote: String? = null) {
        httpApi.request(Api.DirectMessage.Update(msgId, content.toString(), quote))
    }
}
