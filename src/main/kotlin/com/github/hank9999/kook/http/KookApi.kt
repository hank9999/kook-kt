package com.github.hank9999.kook.http

import com.github.hank9999.kook.http.kookapis.*

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

    fun ChannelUser(): ChannelUser {
        return ChannelUser(httpApi)
    }
}