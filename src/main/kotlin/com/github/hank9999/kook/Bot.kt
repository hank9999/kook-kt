package com.github.hank9999.kook

import com.github.hank9999.kook.connect.WebHook
import com.github.hank9999.kook.connect.WebSocket
import com.github.hank9999.kook.handler.Handler
import com.github.hank9999.kook.handler.types.FilterTypes
import com.github.hank9999.kook.types.Event
import com.github.hank9999.kook.types.Message
import com.github.hank9999.kook.types.types.EventTypes
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Bot(config: Config) {
    private val logger: Logger = LoggerFactory.getLogger(Bot::class.java)
    private val handler: Handler

    companion object {
        lateinit var config: Config
    }

    init {
        Companion.config = config
        handler = Handler(config)
        if (config.host.isNotEmpty()) {
            WebHook(config, handler).initialize()
        } else {
            WebSocket(config, handler).connect()
        }
        logger.info("Initialization complete")
    }

    fun <T : Any> registerClass(t: T) {
        handler.registerClassHandler(t)
    }

    fun registerMessageFunc(type: MessageTypes = MessageTypes.ALL, func: (msg: Message, cs: CoroutineScope) -> Unit) {
        handler.registerMessageFuncHandler(type, func)
    }

    fun registerEventFunc(type: EventTypes = EventTypes.ALL, func: (event: Event, cs: CoroutineScope) -> Unit) {
        handler.registerEventFuncHandler(type, func)
    }

    fun registerFilterFunc(type: FilterTypes, startWith: String = "", keyword: String = "", regex: String = "", ignoreCase: Boolean = true, func: (msg: Message, cs: CoroutineScope) -> Unit) {
        handler.registerFilterFuncHandler(type, startWith, keyword, regex, ignoreCase, func)
    }

    fun registerCommandFunc(name: String, prefixes: Array<String> = emptyArray(), aliases: Array<String> = emptyArray(), ignoreCase: Boolean = true, func: (msg: Message, cs: CoroutineScope) -> Unit) {
        handler.registerCommandFuncHandler(name, prefixes, aliases, ignoreCase, func)
    }

    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnMessage(val type: MessageTypes = MessageTypes.ALL)

    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnEvent(val type: EventTypes = EventTypes.ALL)

    /**
     * @param type: FilterTypes, filter types
     * @param startWith: Filter START_WITH string
     * @param keyword: Filter KEYWORD string
     * @param regex: Filter REGEX string
     * @param ignoreCase: whether to ignore case, only apply for Filter START_WITH and KEYWORD
     * This annotation only catch MessageTypes TEXT and KMD
     */
    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnFilter(val type: FilterTypes, val startWith: String = "", val keyword: String = "", val regex: String = "", val ignoreCase: Boolean = true)

    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnCommand(val name: String, val prefixes: Array<String> = [], val aliases: Array<String> = [], val ignoreCase: Boolean = true)
}