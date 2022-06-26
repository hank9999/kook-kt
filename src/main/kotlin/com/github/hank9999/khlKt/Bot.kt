package com.github.hank9999.khlKt

import com.github.hank9999.khlKt.handler.KhlHandler
import com.github.hank9999.khlKt.handler.types.FilterTypes
import com.github.hank9999.khlKt.http.HttpApi
import com.github.hank9999.khlKt.types.KhlEvent
import com.github.hank9999.khlKt.types.KhlMessage
import com.github.hank9999.khlKt.types.types.EventTypes
import com.github.hank9999.khlKt.types.types.MessageTypes
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Bot(config: Config) {
    private val logger: Logger = LoggerFactory.getLogger(Bot::class.java)
    var config: Config
    var khlHandler: KhlHandler
    var httpApi: HttpApi

    init {
        this.config = config
        httpApi = HttpApi(config)
        khlHandler = KhlHandler(config)
        if (config.host.isNotEmpty()) WebHook(config, khlHandler).initialize()
//        else WebSocket(config, khlHandler).connect()
        logger.info("Initialization complete")
    }

    fun <T : Any> registerClass(t: T) {
        khlHandler.registerClassHandler(t)
    }

    fun registerMessageFunc(type: MessageTypes = MessageTypes.ALL, func: (msg: KhlMessage, cs: CoroutineScope) -> Unit) {
        khlHandler.registerMessageFuncHandler(type, func)
    }

    fun registerEventFunc(type: EventTypes = EventTypes.ALL, func: (event: KhlEvent, cs: CoroutineScope) -> Unit) {
        khlHandler.registerEventFuncHandler(type, func)
    }

    fun registerFilterFunc(type: FilterTypes, startWith: String = "", keyword: String = "", regex: String = "", ignoreCase: Boolean = true, func: (msg: KhlMessage, cs: CoroutineScope) -> Unit) {
        khlHandler.registerFilterFuncHandler(type, startWith, keyword, regex, ignoreCase, func)
    }

    fun registerCommandFunc(name: String, prefixes: Array<String> = emptyArray(), aliases: Array<String> = emptyArray(), ignoreCase: Boolean = true, func: (msg: KhlMessage, cs: CoroutineScope) -> Unit) {
        khlHandler.registerCommandFuncHandler(name, prefixes, aliases, ignoreCase, func)
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