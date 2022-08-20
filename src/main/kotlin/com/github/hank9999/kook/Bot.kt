package com.github.hank9999.kook

import com.github.hank9999.kook.connect.WebHook
import com.github.hank9999.kook.connect.WebSocket
import com.github.hank9999.kook.handler.Handler
import com.github.hank9999.kook.handler.types.FilterTypes
import com.github.hank9999.kook.http.HttpApi
import com.github.hank9999.kook.http.KookApi
import com.github.hank9999.kook.types.Event
import com.github.hank9999.kook.types.Message
import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import com.github.hank9999.kook.types.types.EventTypes
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Bot(config: Config, connect: Boolean = true) {
    private val logger: Logger = LoggerFactory.getLogger(Bot::class.java)
    private val handler = Handler(config)
    val httpApi = HttpApi(config.token)
    val kookApi = KookApi(httpApi)

    companion object {
        lateinit var kookApi: KookApi
    }

    init {
        Companion.kookApi = kookApi
        if (connect) {
            if (config.host.isNotEmpty()) {
                WebHook(config, handler).initialize()
            } else {
                WebSocket(handler, kookApi).connect()
            }
        }
        logger.info("Initialization complete")
    }

    fun <T : Any> registerClass(t: T) {
        handler.registerClassHandler(t)
    }

    fun registerMessageFunc(type: MessageTypes = MessageTypes.ALL, channelPrivacyTypes: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP, func: (msg: Message, cs: CoroutineScope) -> Unit) {
        handler.registerMessageFuncHandler(type, channelPrivacyTypes, func)
    }

    fun registerEventFunc(type: EventTypes = EventTypes.ALL, func: (event: Event, cs: CoroutineScope) -> Unit) {
        handler.registerEventFuncHandler(type, func)
    }

    fun registerFilterFunc(type: FilterTypes, channelPrivacyTypes: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP, startWith: String = "", keyword: String = "", regex: String = "", ignoreCase: Boolean = true, func: (msg: Message, cs: CoroutineScope) -> Unit) {
        handler.registerFilterFuncHandler(type, channelPrivacyTypes, startWith, keyword, regex, ignoreCase, func)
    }

    fun registerCommandFunc(name: String, prefixes: Array<String> = emptyArray(), aliases: Array<String> = emptyArray(), ignoreCase: Boolean = true, channelPrivacyTypes: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP, func: (msg: Message, cs: CoroutineScope) -> Unit) {
        handler.registerCommandFuncHandler(name, prefixes, aliases, ignoreCase, channelPrivacyTypes, func)
    }

    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnMessage(val type: MessageTypes = MessageTypes.ALL, val channelPrivacyTypes: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP)

    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnEvent(val type: EventTypes = EventTypes.ALL)

    /**
     * @param type: FilterTypes, filter types
     * @param startWith: Filter START_WITH string
     * @param keyword: Filter KEYWORD string
     * @param regex: Filter REGEX string
     * @param ignoreCase: whether to ignore case, only apply for Filter START_WITH and KEYWORD
     * @param channelPrivacyTypes: specify the listening message source including group and person
     * This annotation only catch MessageTypes TEXT and KMD
     */
    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnFilter(val type: FilterTypes, val startWith: String = "", val keyword: String = "", val regex: String = "", val ignoreCase: Boolean = true, val channelPrivacyTypes: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP)

    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnCommand(val name: String, val prefixes: Array<String> = [], val aliases: Array<String> = [], val ignoreCase: Boolean = true, val channelPrivacyTypes: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP)
}