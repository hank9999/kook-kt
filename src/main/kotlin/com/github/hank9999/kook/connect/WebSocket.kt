package com.github.hank9999.kook.connect

import com.github.hank9999.kook.connect.Type.Status
import com.github.hank9999.kook.handler.Handler
import com.github.hank9999.kook.http.KookApi
import com.github.hank9999.kook.http.exceptions.HttpException
import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.json.JSON.Extension.Int
import com.github.hank9999.kook.json.JSON.Extension.Long
import com.github.hank9999.kook.json.JSON.Extension.String
import com.github.hank9999.kook.json.JSON.Extension.get
import com.github.hank9999.kook.types.types.MessageTypes
import com.github.hank9999.kook.types.types.MessageTypes.*
import com.github.hank9999.kook.utils.NamedThreadFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class WebSocket(val handler: Handler, val kookApi: KookApi) {
    private var mWebSocket: okhttp3.WebSocket? = null
    private val coroutineScope = CoroutineScope(Executors.newSingleThreadExecutor(NamedThreadFactory("KookWebSocket")).asCoroutineDispatcher())
    private val coroutineScopeHandler = CoroutineScope(Executors.newSingleThreadExecutor(NamedThreadFactory("KookWebSocketHandler")).asCoroutineDispatcher())
    private val messageQueue: MutableList<String> = mutableListOf()
    val logger: Logger = LoggerFactory.getLogger(WebSocket::class.java)
    var sn = 0
    var status = Status.INIT
    var lastPong = 0L
    var sessionId = ""
    var isResuming = false
    var isWsFailure = false
    private fun addQueue(text: String) {
        messageQueue.add(text)
    }

    private fun setWsFailure() {
        isWsFailure = true
    }

    private suspend fun getGateway(): String {
        var gateway = ""
        while (true) {
            try {
                gateway = kookApi.Gateway().index()
            } catch (e: HttpException) {
                logger.error(e.message)
            } catch (_: Exception) {}
            if (gateway.isNotEmpty()) {
                break
            } else {
                logger.error("Get gateway error, sleep 10s")
                delay(10 * 1000)
            }
        }
        return gateway
    }

    private suspend fun offline() {
        while (true) {
            var success = false
            try {
                kookApi.User().offline()
                success = true
            } catch (e: HttpException) {
                logger.error(e.message)
            } catch (_: Exception) {}
            if (success) {
                break
            } else {
                logger.error("offline error, sleep 10s")
                delay(10 * 1000)
            }
        }
    }

    fun connect() {
        coroutineScopeHandler.launch { handler() }
        coroutineScope.launch {
            launch {
                var sendTime: Long
                while (true) {
                    if (status == Status.CONNECTED) {
                        sendTime = System.currentTimeMillis()
                        mWebSocket?.send("{\"s\":2,\"sn\":$sn}")
                        logger.debug("Ping sn: $sn")
                        delay(6 * 1000)
                        if (lastPong != 0L && (lastPong < sendTime) && status != Status.RECONNECT) {
                            logger.error("No pong response after ping more than 6s")
                            status = Status.CLOSED
                        } else {
                            delay(24 * 1000)
                        }
                    } else {
                        delay(100)
                    }
                }
            }
            launch {
                while (true) {
                    if (isWsFailure) {
                        isWsFailure = false
                        status = Status.RECONNECT
                    }
                    delay(500)
                }
            }
            val mClient = OkHttpClient.Builder().pingInterval(30, TimeUnit.SECONDS).build()
            val wsListener = WsListener(logger, { setWsFailure() }, { addQueue(it) })
            while (true) {
                var gateway = getGateway()
                logger.debug("Get gateway: $gateway")
                var request = Request.Builder().url(gateway).build()
                mWebSocket = mClient.newWebSocket(request, wsListener)
                status = Status.CONNECTING
                launch resume@ {
                    var lastWebSocket = mWebSocket.hashCode()
                    while (true) {
                        if (lastWebSocket != mWebSocket.hashCode()) break
                        if (status == Status.RECONNECT) break
                        if (status == Status.CLOSED && !isResuming) {
                            logger.error("Connection closed, resuming")
                            mWebSocket?.close(1002, "restart")
                            mWebSocket = null
                            gateway = "${getGateway()}&resume=1&sn=$sn&session_id=$sessionId"
                            logger.debug("Get gateway: $gateway")
                            request = Request.Builder().url(gateway).build()
                            mWebSocket = mClient.newWebSocket(request, wsListener)
                            lastWebSocket = mWebSocket.hashCode()
                            status = Status.CONNECTING
                            isResuming = true
                            while (true) {
                                if (status == Status.CONNECTED) {
                                    isResuming = false
                                    logger.info("Resume finished")
                                    break
                                }
                                delay(100)
                            }
                        }
                        delay(100)
                    }
                }
                while (true) {
                    if (status == Status.RECONNECT) {
                        logger.info("Reconnecting")
                        mWebSocket?.close(1002, "reconnect")
                        mWebSocket = null
                        offline()
                        sn = 0
                        sessionId = ""
                        messageQueue.clear()
                        lastPong = 0L
                        isResuming = false
                        status = Status.INIT
                        break
                    }
                    delay(100)
                }
            }
        }
    }

    suspend fun handler() {
        while (true) {
            if (messageQueue.size != 0) {
                val message = messageQueue.removeAt(0)
                val data = json.parseToJsonElement(message)
                when (data["s"].Int) {
                    0 -> {
                        logger.debug("Received Event: $data")
                        if (data["sn"].Int > sn) {
                            sn = data["sn"].Int
                            try {
                                val dData = if (data["d"]["type"].String == "SYS_MSG") {
                                    buildJsonObject {
                                        put("type", 255)
                                        put("channel_type", data["d"]["channelType"].String)
                                        put("target_id", data["d"]["toUserId"].String)
                                        put("author_id", data["d"]["fromUserId"].String)
                                        put("content", data["d"]["content"].String)
                                        put("msg_id", data["d"]["msgId"].String)
                                        put("msg_timestamp", data["d"]["msgTimestamp"].Long)
                                        put("nonce", data["d"]["nonce"].String)
                                        put("from_type", data["d"]["from_type"].Int)
                                        putJsonObject("extra") {
                                            put("type", "broadcast")
                                        }
                                    }
                                } else {
                                    data["d"]
                                }
                                when (MessageTypes.fromInt(dData["type"].Int)) {
                                    KMD, TEXT, CARD, VIDEO, IMG, AUDIO, FILE, ITEM -> handler.addMessageQueue(data["d"])
                                    SYS -> handler.addEventQueue(data["d"])
                                    ALL -> {}
                                    NONE -> logger.error(message)
                                }
                            } catch (e: Exception) {
                                // 如果遇到什么奇怪的bug 打印全文
                                logger.error(message)
                                logger.error("${e.message}\n${e.stackTraceToString()}")
                            }
                        }
                    }

                    1 -> {
                        logger.debug("Received Hello: $data")
                        val code = data["d"]["code"].Int
                        if (code == 0 && !isResuming) {
                            status = Status.CONNECTED
                            logger.info("Connected")
                        } else if (code != 0) {
                            status = Status.RECONNECT
                        }
                        if (status == Status.CONNECTED) sessionId = data["d"]["session_id"].String
                    }

                    3 -> {
                        logger.debug("Received Pong")
                        lastPong = System.currentTimeMillis()
                    }

                    5 -> {
                        logger.debug("Received RECONNECT")
                        status = Status.RECONNECT
                    }

                    6 -> {
                        logger.debug("Received RESUME ACK: $data")
                        status = Status.CONNECTED
                    }

                    else -> {}
                }
            } else {
                delay(1)
            }
        }
    }
}