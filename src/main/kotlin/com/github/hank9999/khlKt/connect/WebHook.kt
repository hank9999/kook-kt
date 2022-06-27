package com.github.hank9999.khlKt.connect

import com.github.hank9999.khlKt.Config
import com.github.hank9999.khlKt.connect.Utils.Companion.decompressZlib
import com.github.hank9999.khlKt.handler.KhlHandler
import com.github.hank9999.khlKt.json.JSON.Companion.json
import com.github.hank9999.khlKt.json.JSON.Extension.Int
import com.github.hank9999.khlKt.json.JSON.Extension.Long
import com.github.hank9999.khlKt.json.JSON.Extension.String
import com.github.hank9999.khlKt.json.JSON.Extension.get
import com.github.hank9999.khlKt.types.types.MessageTypes
import com.github.hank9999.khlKt.types.types.MessageTypes.*
import io.javalin.Javalin
import io.javalin.http.Context
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WebHook(config: Config, khlHandler: KhlHandler) {
    private var config: Config
    private var khlHandler: KhlHandler
    private val logger: Logger = LoggerFactory.getLogger(WebHook::class.java)

    init {
        this.config = config
        this.khlHandler = khlHandler
    }

    fun initialize() {
        // Disable Javalin and Jetty Logger
        io.javalin.core.util.JavalinLogger.enabled = false
        org.eclipse.jetty.util.log.Log.setLog(io.javalin.jetty.JettyUtil.NoopLogger())
        org.eclipse.jetty.util.log.Log.getProperties().setProperty("org.eclipse.jetty.LEVEL", "OFF")
        org.eclipse.jetty.util.log.Log.getProperties().setProperty("org.eclipse.jetty.util.log.announce", "false")
        org.eclipse.jetty.util.log.Log.getRootLogger().isDebugEnabled = false
        val app = Javalin.create().start(config.host, config.port)
        app.post(config.path) { ctx -> khlHandler(ctx) }
    }

    private fun khlHandler(ctx: Context) {
        val body = decompressZlib(ctx.bodyAsBytes())
        val element = json.parseToJsonElement(body)
        if (element["s"].Int != 0) {
            logger.warn("[Khl] Unknown signaling, ignored")
            return
        }
        val dObject = element["d"].jsonPrimitive
        if (dObject["verify_token"].String != config.verify_token) {
            logger.warn("[Khl] Wrong Verify Token, message may be fake, ignored")
            return
        }
        ctx.status(200)
        try {
            when (MessageTypes.fromInt(dObject["type"].int)) {
                KMD, TEXT, CARD, VIDEO, IMG, AUDIO, FILE -> khlHandler.addMessageQueue(dObject)
                SYS -> khlEventHandler(ctx, dObject)
                ALL -> {}
            }
        } catch (e: Exception) {
            // 如果遇到什么奇怪的bug 打印全文
            logger.error(body)
            logger.error("${e.javaClass.name} ${e.message}")
        }
    }

    private fun khlEventHandler(ctx: Context, element: JsonElement) {
        when (element["channel_type"].String) {
            "WEBHOOK_CHALLENGE" -> {
                val challenge = element["challenge"].String
                val resp = buildJsonObject { put("challenge", challenge) }.toString()
                ctx.contentType("application/json").result(resp)
                logger.info("[Khl] Received WEBHOOK_CHALLENGE request, challenge: $challenge, Responded")
            }
            else -> khlHandler.addEventQueue(element)
        }
    }


}