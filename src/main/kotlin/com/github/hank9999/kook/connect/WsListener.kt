package com.github.hank9999.kook.connect

import com.github.hank9999.kook.connect.Utils.Companion.decompressZlib
import okhttp3.Response
import okhttp3.WebSocketListener
import okio.ByteString
import org.slf4j.Logger

class WsListener(val logger: Logger, val setWsFailure: () -> Unit, val addQueue: (text: String) -> Unit) : WebSocketListener() {
    override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        logger.debug("[WebSocket] Closed $code, $reason")
    }

    override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        logger.debug("[WebSocket] Closing $code, $reason")
    }

    override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        logger.error("[WebSocket] Failure ${t.cause}, ${t.message}, ${response?.message}")
        setWsFailure()
    }

    override fun onMessage(webSocket: okhttp3.WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        val text = decompressZlib(bytes.toByteArray())
        addQueue(text)
        logger.debug("[WebSocket] Received ws compressed message: $text")
    }

    override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
        super.onMessage(webSocket, text)
        addQueue(text)
        logger.debug("[WebSocket] Received ws message: $text")
    }

    override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        logger.debug("[WebSocket] Connected")
    }

}