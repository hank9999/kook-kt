package com.github.hank9999.kook.http

import com.github.hank9999.kook.http.kookapis.*

class KookApi(private val httpApi: HttpApi) {
    val message = Message(httpApi)
    val guildRole = GuildRole(httpApi)
    val user = User(httpApi)
    val guild = Guild(httpApi)
    val gateway = Gateway(httpApi)
    val channelUser = ChannelUser(httpApi)
    val channel = Channel(httpApi)
    val directMessage = DirectMessage(httpApi)

    fun Message(): Message {
        return message
    }

    fun GuildRole(): GuildRole {
        return guildRole
    }

    fun User(): User {
        return user
    }

    fun Guild(): Guild {
        return guild
    }

    fun Gateway(): Gateway {
        return gateway
    }

    fun ChannelUser(): ChannelUser {
        return channelUser
    }

    fun Channel(): Channel {
        return channel
    }

    fun DirectMessage(): DirectMessage {
        return directMessage
    }
}
