package com.github.hank9999.kook.http

import com.github.hank9999.kook.http.exceptions.HttpException
import com.github.hank9999.kook.http.types.HttpApiResponse
import com.github.hank9999.kook.http.types.Method
import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.json.JSON.Extension.Int
import com.github.hank9999.kook.json.JSON.Extension.String
import com.github.hank9999.kook.json.JSON.Extension.get
import com.github.hank9999.kook.utils.NamedThreadFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class HttpApi(token: String) {

    private val api = "https://www.kookapp.cn/api/v3"
    private val authHeader = mapOf("Authorization" to "Bot $token")
    private val rateLimit = RateLimit()
    private val logger: Logger = LoggerFactory.getLogger(HttpApi::class.java)
    private val threadNumber = Runtime.getRuntime().availableProcessors()*2 + 1
    private val threadPool = ThreadPoolExecutor(threadNumber, threadNumber,0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue(10240), NamedThreadFactory("HttpApi"))
    private val coroutineContext = threadPool.asCoroutineDispatcher()
    private val coroutineScope = CoroutineScope(coroutineContext)
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

    suspend fun execRequest(data: Api, page: Int? = null): HttpApiResponse = withContext(coroutineContext) {
        val sleepTime = rateLimit.getSleepTime(data.bucket)
        logger.debug("[HttpApi] ${data.route} request, sleep $sleepTime ms")
        delay(sleepTime)
        val params = mutableMapOf<String, String>()
        if (page != null) {
            params["page"] = page.toString()
        }
        val apiBaseUrl = if (data.baseApiUrl != null) data.baseApiUrl else api
        val resp = if (data.method == Method.GET) {
            data.params.entries.forEach {
                params[it.key] = it.value
            }
            Http.aget("${apiBaseUrl}/${data.route}", authHeader, params)
        } else {
            Http.apost("$apiBaseUrl/${data.route}", authHeader, data.postData, params)
        }
        val respJson = json.parseToJsonElement(resp.body)
        if (respJson["code"].Int != 0) {
            throw HttpException("[HttpApi] Error ${respJson["code"].Int} ${data.route} ${respJson["message"].String}")
        }
        updateRateLimitInfo(data.bucket, resp.headers)
        logger.debug("[HttpApi] ${data.route} response, $respJson, ${resp.headers}")
        HttpApiResponse(respJson["data"], resp.headers)
    }

    suspend fun request(data: Api): JsonElement = withContext(coroutineContext) {
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

    fun requestAsFlow(data: Api): Flow<JsonElement> = flow {
        val json1 = execRequest(data).json
        if (data.pageable) {
            val pageTotal = json1["meta"]["page_total"].Int
            for (page in 1..pageTotal) {
                if (page == 1) {
                    emit(json1["items"])
                } else {
                    emit(execRequest(data, page).json["items"])
                }
            }
        } else {
            emit(json1)
        }
    }

    suspend fun requestAsIterator(data: Api): Iterator<JsonElement> {
        val json1 = execRequest(data).json
        var currentPage = 0
        val pageTotal = if (data.pageable) {
            json1["meta"]["page_total"].Int
        } else {
            1
        }
        class Itr : Iterator<JsonElement> {
            override fun hasNext(): Boolean {
                return currentPage < pageTotal
            }

            override fun next(): JsonElement {
                currentPage += 1
                return if (currentPage == 1){
                    json1["items"]
                } else {
                    var jsonData: JsonElement? = null
                    val job = coroutineScope.launch {
                        jsonData = execRequest(data, currentPage).json["items"]
                    }
                    runBlocking {
                        job.join()
                    }
                    jsonData!!
                }
            }
        }
        return Itr()
    }
}