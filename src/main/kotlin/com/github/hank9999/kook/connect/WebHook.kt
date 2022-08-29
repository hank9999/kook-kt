package com.github.hank9999.kook.connect

import com.github.hank9999.kook.Config
import com.github.hank9999.kook.connect.Utils.Companion.decompressZlib
import com.github.hank9999.kook.handler.Handler
import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.json.JSON.Extension.Int
import com.github.hank9999.kook.json.JSON.Extension.Long
import com.github.hank9999.kook.json.JSON.Extension.String
import com.github.hank9999.kook.json.JSON.Extension.get
import com.github.hank9999.kook.types.types.MessageTypes
import com.github.hank9999.kook.types.types.MessageTypes.*
import io.javalin.Javalin
import io.javalin.http.Context
import kotlinx.serialization.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WebHook(config: Config, handler: Handler) {
    private var config: Config
    private var handler: Handler
    private val logger: Logger = LoggerFactory.getLogger(WebHook::class.java)

    init {
        this.config = config
        this.handler = handler
    }

    fun initialize(javalin: Javalin? = null): Javalin {
        val app = javalin ?: Javalin.create { it.showJavalinBanner = false }.start(config.host, config.port)
        app.post(config.path) { ctx -> messageHandler(ctx) }
        return app
    }

    private fun messageHandler(ctx: Context) {
        val body = decompressZlib(ctx.bodyAsBytes())
        val element = json.parseToJsonElement(body)
        if (element["s"].Int != 0) {
            logger.debug("[WebHook] Unknown signaling, ignored")
            return
        }
        val dObject = element["d"]
        if (dObject["verify_token"].String != config.verify_token) {
            logger.warn("[WebHook] Wrong Verify Token, message may be fake, ignored")
            return
        }
        ctx.status(200)
        try {
            val data = if (dObject["type"].String == "SYS_MSG") {
                buildJsonObject {
                    put("type", 255)
                    put("channel_type", dObject["channelType"].String)
                    put("target_id", dObject["toUserId"].String)
                    put("author_id", dObject["fromUserId"].String)
                    put("content", dObject["content"].String)
                    put("msg_id", dObject["msgId"].String)
                    put("msg_timestamp", dObject["msgTimestamp"].Long)
                    put("nonce", dObject["nonce"].String)
                    put("from_type", dObject["from_type"].Int)
                    putJsonObject("extra") {
                        put("type", "broadcast")
                    }
                }
            } else {
                 dObject
            }
            when (MessageTypes.fromInt(data["type"].Int)) {
                KMD, TEXT, CARD, VIDEO, IMG, AUDIO, FILE -> handler.addMessageQueue(dObject)
                SYS -> eventHandler(ctx, dObject)
                ALL -> {}
            }
        } catch (e: Exception) {
            // 如果遇到什么奇怪的bug 打印全文
            logger.error(body)
            logger.error("${e.message}\n${e.stackTraceToString()}")
        }
    }

    private fun eventHandler(ctx: Context, element: JsonElement) {
        when (element["channel_type"].String) {
            "WEBHOOK_CHALLENGE" -> {
                val challenge = element["challenge"].String
                val resp = buildJsonObject { put("challenge", challenge) }.toString()
                ctx.contentType("application/json").result(resp)
                logger.debug("[WebHook] Received WEBHOOK_CHALLENGE request, challenge: $challenge, Responded")
            }
            else -> handler.addEventQueue(element)
        }
    }


}