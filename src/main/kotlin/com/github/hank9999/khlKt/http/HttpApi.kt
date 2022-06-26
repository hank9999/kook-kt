package com.github.hank9999.khlKt.http

import com.github.hank9999.khlKt.Bot
import com.github.hank9999.khlKt.Config
import com.github.hank9999.khlKt.http.types.apiResponse.GuildView
import com.github.hank9999.khlKt.http.types.apiResponse.MessageCreate
import com.github.hank9999.khlKt.types.types.MessageTypes
import com.github.hank9999.khlKt.http.exceptions.HttpException
import com.github.hank9999.khlKt.json.JSON.Companion.json
import com.github.hank9999.khlKt.json.JSON.Companion.t
import com.github.hank9999.khlKt.json.JSON.Operator.get
import com.github.hank9999.khlKt.json.JSON.Operator.invoke
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.FormBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class HttpApi(config: Config) {
    companion object {
        private const val api = "https://www.kaiheila.cn/api/v3"
        private var authHeader: Map<String, String> = emptyMap()
        private val rateLimit = RateLimit()
        private val logger: Logger = LoggerFactory.getLogger(HttpApi::class.java)
    }

    init {
        authHeader = mapOf("Authorization" to "Bot ${config.token}")
    }

    object Message {
        fun create(content: String, target_id: String, type: MessageTypes = MessageTypes.TEXT, quote: String = "", temp_target_id: String = ""): MessageCreate {
            val bucket = "message/create"
            val route = "message/create"
            val sleepTime = rateLimit.getSleepTime(bucket)
            Thread.sleep(sleepTime)
            val formData = FormBody.Builder()
                .add("content", content)
                .add("target_id", target_id)
            if (type != MessageTypes.TEXT) {
                formData.add("type", type.type.toString())
            }
            if (quote.isNotEmpty()) {
                formData.add("quote", quote)
            }
            if (temp_target_id.isNotEmpty()) {
                formData.add("temp_target_id", temp_target_id)
            }
            val resp = Http.post("$api/$route", authHeader, formData.build())
            val respJson = json.parseToJsonElement(resp.body)
            if (respJson["code"](t.int) != 0) {
                throw HttpException("HttpApi ERROR ${respJson["code"](t.int)} $route ${respJson["message"](t.string)}")
            }
            if (resp.headers.containsKey("x-rate-limit-limit")) {
                rateLimit.updateRateLimitInfo(
                    bucket,
                    resp.headers["x-rate-limit-limit"]!![0].toInt(),
                    resp.headers["x-rate-limit-remaining"]!![0].toInt(),
                    resp.headers["x-rate-limit-reset"]!![0].toInt()
                )
            }
            return json.decodeFromJsonElement(respJson["data"])
        }
    }

    object User {
        fun me(): com.github.hank9999.khlKt.types.User {
            val bucket = "user/me"
            val route = "user/me"
            val sleepTime = rateLimit.getSleepTime(bucket)
            Thread.sleep(sleepTime)
            val resp = Http.get("$api/$route", authHeader)
            val respJson = json.parseToJsonElement(resp.body)
            if (respJson["code"](t.int) != 0) {
                throw HttpException("HttpApi ERROR ${respJson["code"](t.int)} $route ${respJson["message"](t.string)}")
            }
            if (resp.headers.containsKey("x-rate-limit-limit")) {
                rateLimit.updateRateLimitInfo(
                    bucket,
                    resp.headers["x-rate-limit-limit"]!![0].toInt(),
                    resp.headers["x-rate-limit-remaining"]!![0].toInt(),
                    resp.headers["x-rate-limit-reset"]!![0].toInt()
                )
            }
            return json.decodeFromJsonElement(respJson["data"])
        }
    }

    object Guild {
        fun view(guildId: String): GuildView {
            val bucket = "guild/view"
            val route = "guild/view"
            val sleepTime = rateLimit.getSleepTime(bucket)
            Thread.sleep(sleepTime)
            val resp = Http.get("$api/$route", authHeader, mapOf("guild_id" to guildId))
            val respJson = json.parseToJsonElement(resp.body)
            if (respJson["code"](t.int) != 0) {
                throw HttpException("HttpApi ERROR ${respJson["code"](t.int)} $route ${respJson["message"](t.string)}")
            }
            if (resp.headers.containsKey("x-rate-limit-limit")) {
                rateLimit.updateRateLimitInfo(
                    bucket,
                    resp.headers["x-rate-limit-limit"]!![0].toInt(),
                    resp.headers["x-rate-limit-remaining"]!![0].toInt(),
                    resp.headers["x-rate-limit-reset"]!![0].toInt()
                )
            }
            return json.decodeFromJsonElement(respJson["data"])
        }
    }
}