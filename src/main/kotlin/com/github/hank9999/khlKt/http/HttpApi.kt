package com.github.hank9999.khlKt.http

import com.github.hank9999.khlKt.Bot
import com.github.hank9999.khlKt.http.exceptions.HttpException
import com.github.hank9999.khlKt.http.types.apiResponse.GuildView
import com.github.hank9999.khlKt.http.types.apiResponse.MessageCreate
import com.github.hank9999.khlKt.json.JSON.Companion.json
import com.github.hank9999.khlKt.json.JSON.Extension.Int
import com.github.hank9999.khlKt.json.JSON.Extension.String
import com.github.hank9999.khlKt.json.JSON.Extension.get
import com.github.hank9999.khlKt.types.types.MessageTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.FormBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class HttpApi {
    companion object {
        private const val api = "https://www.kaiheila.cn/api/v3"
        private val authHeader = mapOf("Authorization" to "Bot ${Bot.config.token}")
        private val rateLimit = RateLimit()
        private val logger: Logger = LoggerFactory.getLogger(HttpApi::class.java)
    }

    object Message {
        suspend fun create(content: String, target_id: String, type: MessageTypes = MessageTypes.TEXT, quote: String = "", temp_target_id: String = ""): MessageCreate = withContext(Dispatchers.IO) {
            val bucket = "message/create"
            val route = "message/create"
            val sleepTime = rateLimit.getSleepTime(bucket)
            logger.debug("[HttpApi] $bucket request, sleep $sleepTime ms")
            delay(sleepTime)
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
            val resp = Http.apost("$api/$route", authHeader, formData.build())
            val respJson = json.parseToJsonElement(resp.body)
            if (respJson["code"].Int != 0) {
                throw HttpException("HttpApi ERROR ${respJson["code"].Int} $route ${respJson["message"].String}")
            }
            if (resp.headers.containsKey("x-rate-limit-limit")) {
                rateLimit.updateRateLimitInfo(
                    bucket,
                    resp.headers["x-rate-limit-limit"]!![0].toInt(),
                    resp.headers["x-rate-limit-remaining"]!![0].toInt(),
                    resp.headers["x-rate-limit-reset"]!![0].toInt()
                )
            }
            logger.debug("[HttpApi] $bucket response, $respJson, ${resp.headers}")
            json.decodeFromJsonElement(respJson["data"])
        }
    }

    object User {
        suspend fun me(): com.github.hank9999.khlKt.types.User = withContext(Dispatchers.IO) {
            val bucket = "user/me"
            val route = "user/me"
            val sleepTime = rateLimit.getSleepTime(bucket)
            logger.debug("[HttpApi] $bucket request, sleep $sleepTime ms")
            logger.debug("[HttpApi] $bucket request, sleep $sleepTime ms")
            delay(sleepTime)
            val resp = Http.aget("$api/$route", authHeader)
            val respJson = json.parseToJsonElement(resp.body)
            if (respJson["code"].Int != 0) {
                throw HttpException("HttpApi ERROR ${respJson["code"].Int} $route ${respJson["message"].String}")
            }
            if (resp.headers.containsKey("x-rate-limit-limit")) {
                rateLimit.updateRateLimitInfo(
                    bucket,
                    resp.headers["x-rate-limit-limit"]!![0].toInt(),
                    resp.headers["x-rate-limit-remaining"]!![0].toInt(),
                    resp.headers["x-rate-limit-reset"]!![0].toInt()
                )
            }
            logger.debug("[HttpApi] $bucket response, $respJson, ${resp.headers}")
            json.decodeFromJsonElement(respJson["data"])
        }
    }

    object Guild {
        suspend fun view(guildId: String): GuildView = withContext(Dispatchers.IO) {
            val bucket = "guild/view"
            val route = "guild/view"
            val sleepTime = rateLimit.getSleepTime(bucket)
            logger.debug("[HttpApi] $bucket request, sleep $sleepTime ms")
            delay(sleepTime)
            val resp = Http.aget("$api/$route", authHeader, mapOf("guild_id" to guildId))
            val respJson = json.parseToJsonElement(resp.body)
            if (respJson["code"].Int != 0) {
                throw HttpException("HttpApi ERROR ${respJson["code"].Int} $route ${respJson["message"].String}")
            }
            if (resp.headers.containsKey("x-rate-limit-limit")) {
                rateLimit.updateRateLimitInfo(
                    bucket,
                    resp.headers["x-rate-limit-limit"]!![0].toInt(),
                    resp.headers["x-rate-limit-remaining"]!![0].toInt(),
                    resp.headers["x-rate-limit-reset"]!![0].toInt()
                )
            }
            logger.debug("[HttpApi] $bucket response, $respJson, ${resp.headers}")
            json.decodeFromJsonElement(respJson["data"])
        }
    }

    object Gateway {
        suspend fun index(): String = withContext(Dispatchers.IO) {
            val bucket = "gateway/index"
            val route = "gateway/index"
            val sleepTime = rateLimit.getSleepTime(bucket)
            logger.debug("[HttpApi] $bucket request, sleep $sleepTime ms")
            delay(sleepTime)
            val resp = Http.aget("$api/$route", authHeader)
            val respJson = json.parseToJsonElement(resp.body)
            if (respJson["code"].Int != 0) {
                throw HttpException("HttpApi ERROR ${respJson["code"].Int} $route ${respJson["message"].String}")
            }
            if (resp.headers.containsKey("x-rate-limit-limit")) {
                rateLimit.updateRateLimitInfo(
                    bucket,
                    resp.headers["x-rate-limit-limit"]!![0].toInt(),
                    resp.headers["x-rate-limit-remaining"]!![0].toInt(),
                    resp.headers["x-rate-limit-reset"]!![0].toInt()
                )
            }
            logger.debug("[HttpApi] $bucket response, $respJson, ${resp.headers}")
            respJson["data"]["url"].String
        }
    }
}