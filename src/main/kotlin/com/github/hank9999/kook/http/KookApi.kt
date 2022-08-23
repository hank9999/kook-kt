package com.github.hank9999.kook.http

import com.github.hank9999.kook.http.types.apiResponse.GuildView
import com.github.hank9999.kook.http.types.apiResponse.MessageCreate
import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.json.JSON.Extension.String
import com.github.hank9999.kook.json.JSON.Extension.get
import com.github.hank9999.kook.types.Role
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.json.decodeFromJsonElement

class KookApi(private val httpApi: HttpApi) {
    fun Message(): Message {
        return Message(httpApi)
    }

    fun GuildRole(): GuildRole {
        return GuildRole(httpApi)
    }

    fun User(): User {
        return User(httpApi)
    }

    fun Guild(): Guild {
        return Guild(httpApi)
    }

    fun Gateway(): Gateway {
        return Gateway(httpApi)
    }
}

class Message(private val httpApi: HttpApi) {
    suspend fun create(targetId: String, content: Any, type: MessageTypes? = null, quote: String? = null, nonce: String? = null, tempTargetId: String? = null): MessageCreate {
        val data = httpApi.request(Api.Message.Create(targetId, content, type, quote, nonce, tempTargetId))
        return json.decodeFromJsonElement(data)
    }
    suspend fun delete(msgId: String) {
        httpApi.request(Api.Message.Delete(msgId))
    }
    suspend fun update(msgId: String, content: Any, quote: String? = null, tempTargetId: String? = null) {
        httpApi.request(Api.Message.Update(msgId, content, quote, tempTargetId))
    }
}

class GuildRole(private val httpApi: HttpApi) {
    suspend fun list(guildId: String): List<Role> {
        val data = httpApi.request(Api.GuildRole.List(guildId))
        return json.decodeFromJsonElement(data)
    }
}

class User(private val httpApi: HttpApi) {
    suspend fun me(): com.github.hank9999.kook.types.User {
        val data = httpApi.request(Api.User.Me())
        return json.decodeFromJsonElement(data)
    }

    suspend fun offline() {
        httpApi.request(Api.User.Offline())
    }
}

class Guild(private val httpApi: HttpApi) {
    suspend fun view(guildId: String): GuildView {
        val data = httpApi.request(Api.Guild.View(guildId))
        return json.decodeFromJsonElement(data)
    }
}

class Gateway(private val httpApi: HttpApi) {
    suspend fun index(): String {
        val data = httpApi.request(Api.Gateway.Index())
        return data["url"].String
    }
}