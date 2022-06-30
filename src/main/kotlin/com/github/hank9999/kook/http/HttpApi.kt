package com.github.hank9999.kook.http

import com.github.hank9999.kook.Bot
import com.github.hank9999.kook.http.exceptions.HttpException
import com.github.hank9999.kook.http.types.HttpApiResponse
import com.github.hank9999.kook.http.types.Method
import com.github.hank9999.kook.http.types.apiResponse.GuildView
import com.github.hank9999.kook.http.types.apiResponse.MessageCreate
import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.json.JSON.Extension.Int
import com.github.hank9999.kook.json.JSON.Extension.String
import com.github.hank9999.kook.json.JSON.Extension.get
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import okhttp3.FormBody
import okhttp3.Headers
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class HttpApi {
    companion object {
        private const val api = "https://www.kookapp.cn/api/v3"
        private val authHeader = mapOf("Authorization" to "Bot ${Bot.config.token}")
        private val rateLimit = RateLimit()
        private val logger: Logger = LoggerFactory.getLogger(HttpApi::class.java)

        fun updateRateLimitInfo(bucket: String, headers: Map<String, List<String>>) {
            if (headers.containsKey("x-rate-limit-limit")) {
                rateLimit.updateRateLimitInfo(
                    bucket,
                    headers["x-rate-limit-limit"]!![0].toInt(),
                    headers["x-rate-limit-remaining"]!![0].toInt(),
                    headers["x-rate-limit-reset"]!![0].toInt()
                )
            }
        }

        suspend fun execRequest(data: Api, page: Int? = null): HttpApiResponse = withContext(Dispatchers.IO) {
            val sleepTime = rateLimit.getSleepTime(data.bucket)
            logger.debug("[HttpApi] ${data.route} request, sleep $sleepTime ms")
            delay(sleepTime)
            val params = mutableMapOf<String, String>()
            if (page != null) {
                params["page"] = page.toString()
            }
            val resp = if (data.method == Method.GET) {
                data.params.entries.forEach {
                    params[it.key] = it.value
                }
                Http.aget("$api/${data.route}", authHeader, params)
            } else {
                Http.apost("$api/${data.route}", authHeader, data.formData, params)
            }
            val respJson = json.parseToJsonElement(resp.body)
            if (respJson["code"].Int != 0) {
                throw HttpException("[HttpApi] Error ${respJson["code"].Int} ${data.route} ${respJson["message"].String}")
            }
            updateRateLimitInfo(data.bucket, resp.headers)
            logger.debug("[HttpApi] ${data.route} response, $respJson, ${resp.headers}")
            HttpApiResponse(respJson["data"], resp.headers)
        }

        suspend fun request(data: Api): JsonElement = withContext(Dispatchers.IO) {
            if (!data.pageable) {
                execRequest(data).json
            } else {
                val json1 = execRequest(data).json
                val pageTotal = json1["meta"]["page_total"].Int
                if (pageTotal == 1) {
                    json1["items"]
                } else {
                    val listData = mutableListOf<JsonElement>()
                    listData.add(json1["items"])
                    for (page in 2..pageTotal) {
                        logger.debug("[HttpApi] request ${data.route} page $page")
                        listData.add(execRequest(data, page).json["items"])
                    }
                    buildJsonArray {
                        listData.forEach { list ->
                            list.jsonArray.forEach { element ->
                                add(element)
                            }
                        }
                    }
                }
            }
        }
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
        suspend fun me(): com.github.hank9999.kook.types.User = withContext(Dispatchers.IO) {
            val bucket = "user/me"
            val route = "user/me"
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
            json.decodeFromJsonElement(respJson["data"])
        }

        suspend fun offline() = withContext(Dispatchers.IO) {
            val bucket = "user/offline"
            val route = "user/offline"
            val sleepTime = rateLimit.getSleepTime(bucket)
            logger.debug("[HttpApi] $bucket request, sleep $sleepTime ms")
            delay(sleepTime)
            val resp = Http.apost("$api/$route", authHeader, FormBody.Builder().build())
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