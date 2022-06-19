import types.KhlMessage
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import json.JSON.Companion.json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KhlMessageHandler {
    private val logger: Logger = LoggerFactory.getLogger(KhlMessageHandler::class.java)

    fun messageHandler(element: JsonElement) {
        val data = json.decodeFromJsonElement<KhlMessage>(element)
        // TODO()
    }

    fun systemHandler(element: JsonElement) {
        // TODO()
    }
}