package com.github.hank9999.khlKt.connect

import com.github.hank9999.khlKt.Config
import com.github.hank9999.khlKt.connect.Type.Status
import com.github.hank9999.khlKt.handler.KhlHandler
import com.github.hank9999.khlKt.http.HttpApi
import com.github.hank9999.khlKt.json.JSON.Companion.json
import com.github.hank9999.khlKt.json.JSON.Companion.t
import com.github.hank9999.khlKt.json.JSON.Operator.get
import com.github.hank9999.khlKt.json.JSON.Operator.invoke
import com.github.hank9999.khlKt.types.types.MessageTypes
import com.github.hank9999.khlKt.types.types.MessageTypes.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class WebSocket(config: Config, khlHandler: KhlHandler) {
    private var config: Config
    private var khlHandler: KhlHandler
    private var mWebSocket: okhttp3.WebSocket? = null
    private val coroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    companion object {
        private val messageQueue: MutableList<String> = mutableListOf()
        val logger: Logger = LoggerFactory.getLogger(WebSocket::class.java)
        var sn = 0
        var status = Status.INIT
        fun addQueue(text: String) {
            messageQueue.add(text)
        }
    }

    init {
        this.config = config
        this.khlHandler = khlHandler
    }

    fun connect() {
        coroutineScope.launch {
            launch { handler() }
            launch {
                while (true) {
                    if (status == Status.CONNECTED) {
                        mWebSocket?.send("{\"s\":2,\"sn\":$sn}")
                        logger.debug("[WebSocket] Ping")
                        delay(30 * 1000)
                    } else {
                        delay(100)
                    }
                }
            }
            while (true) {
                val gateway = HttpApi.Gateway.index()
                logger.debug("[WebSocket] Get gateway: $gateway")
                val mClient = OkHttpClient.Builder().pingInterval(30, TimeUnit.SECONDS).build()
                val request = Request.Builder().url(gateway).build()
                mWebSocket = mClient.newWebSocket(request, WsListener())
                status = Status.CONNECTING
                launch resume@ {
                    var lastWebSocket = mWebSocket.hashCode()
                    while (true) {
                        while (true) {
                            if (status == Status.CLOSED) break
                            delay(100)
                            if (lastWebSocket != mWebSocket.hashCode()) return@resume
                        }
                        mWebSocket = mClient.newWebSocket(request, WsListener())
                        status = Status.CONNECTING
                        delay(100)
                        mWebSocket!!.send("{\"s\":4,\"sn\":$sn}")
                        lastWebSocket = mWebSocket.hashCode()
                    }
                }
                while (true) {
                    if (status == Status.RESTARTING) break
                    delay(100)
                }
            }
        }
        while (true) {
            if (status == Status.CONNECTED) return
        }
    }

    suspend fun handler() {
        while (true) {
            if (messageQueue.size != 0) {
                val data = json.parseToJsonElement(messageQueue.pop(0))
                when (data["s"](t.int)) {
                    0 -> {
                        logger.debug("[WebSocket] Received Event: $data")
                        sn = data["sn"](t.int)
                        when (MessageTypes.fromInt(data["d"]["type"](t.int))) {
                            KMD, TEXT, CARD, VIDEO, IMG, AUDIO, FILE -> khlHandler.addMessageQueue(data["d"])
                            SYS -> khlHandler.addEventQueue(data["d"])
                            ALL -> {}
                        }
                    }
                    1 -> {
                        logger.debug("[WebSocket] Received Hello: $data")
                        val code = data["d"]["code"](t.int)
                        status = if (code == 0) Status.CONNECTED else Status.RESTARTING
                    }
                    3 -> {
                        logger.debug("[WebSocket] Received Pong")
                    }
                    5 -> {
                        status = Status.RESTARTING
                        sn = 0
                        messageQueue.clear()
                    }
                    6 -> {
                        logger.debug("[WebSocket] Received RESUME ACK: $data")
                        status = Status.CONNECTED
                    }
                    else -> {}
                }
            }
            delay(100)
        }
    }
}

private fun <E> MutableList<E>.pop(i: Int): E {
    if (size >= i + 1) {
        val data = get(i)
        removeAt(i)
        return data
    } else {
        throw IndexOutOfBoundsException("Index: $i, Size: $size")
    }
}
