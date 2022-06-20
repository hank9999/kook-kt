package com.github.hank9999.khlKt.handler

import com.github.hank9999.khlKt.Bot
import com.github.hank9999.khlKt.handler.types.EventClassHandler
import com.github.hank9999.khlKt.handler.types.FilterClassHandler
import com.github.hank9999.khlKt.handler.types.FilterTypes
import com.github.hank9999.khlKt.handler.types.MessageClassHandler
import com.github.hank9999.khlKt.types.KhlMessage
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import com.github.hank9999.khlKt.json.JSON.Companion.json
import com.github.hank9999.khlKt.types.KhlEvent
import com.github.hank9999.khlKt.types.types.EventTypes
import com.github.hank9999.khlKt.types.types.MessageTypes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.declaredFunctions

class KhlHandler {
    private val logger: Logger = LoggerFactory.getLogger(KhlHandler::class.java)
    private val messageClassHandlers: MutableMap<MessageTypes, MutableList<MessageClassHandler>> = mutableMapOf()
    private val messageFuncHandlers: MutableMap<MessageTypes, MutableList<(msg: KhlMessage) -> Unit>> = mutableMapOf()
    private val filterClassHandlers: MutableList<FilterClassHandler> = mutableListOf()
    private val eventClassHandlers: MutableMap<EventTypes, MutableList<EventClassHandler>> = mutableMapOf()
    private val eventFuncHandlers: MutableMap<EventTypes, MutableList<(event: KhlEvent) -> Unit>> = mutableMapOf()

    fun <T : Any> registerClassHandler(t: T) {
        val declaredFunctions = t::class.declaredFunctions
        for (f in declaredFunctions) {
            // 处理注解，使用其中的元数据
            f.annotations.forEach {
                when (it) {
                    is Bot.OnMessage -> {
                        if (!messageClassHandlers.containsKey(it.type)) messageClassHandlers[it.type] = mutableListOf()
                        messageClassHandlers[it.type]!!.add(MessageClassHandler(t, f))
                        logger.debug("[Handler] Class ${it.type} handler ${f.name} added")
                    }
                    is Bot.OnEvent -> {
                        if (!eventClassHandlers.containsKey(it.type)) eventClassHandlers[it.type] = mutableListOf()
                        eventClassHandlers[it.type]!!.add(EventClassHandler(t, f))
                        logger.debug("[Handler] Class ${it.type} handler ${f.name} added")
                    }
                    is Bot.OnFilter -> {
                        when (it.type) {
                            FilterTypes.START_WITH -> filterClassHandlers.add(FilterClassHandler(FilterTypes.START_WITH, t, f, it.startWith, it.ignoreCase))
                            FilterTypes.KEYWORD -> filterClassHandlers.add(FilterClassHandler(FilterTypes.KEYWORD, t, f, it.keyword, it.ignoreCase))
                            FilterTypes.REGEX -> filterClassHandlers.add(FilterClassHandler(FilterTypes.REGEX, t, f, filterRegex = Regex(it.regex)))
                        }
                        logger.debug("[Handler] Class ${it.type} handler ${f.name} added")
                    }
                    else -> {}
                }
            }
        }
    }

    fun registerMessageFuncHandler(type: MessageTypes, func: (msg: KhlMessage) -> Unit) {
        if (!messageFuncHandlers.containsKey(type)) messageFuncHandlers[type] = mutableListOf()
        if (!messageFuncHandlers[type]!!.contains(func)) messageFuncHandlers[type]!!.add(func)
        logger.debug("[Handler] Function $type handler ${func.javaClass.name} added")
    }

    fun registerEventFuncHandler(type: EventTypes, func: (event: KhlEvent) -> Unit) {
        if (!eventFuncHandlers.containsKey(type)) eventFuncHandlers[type] = mutableListOf()
        if (!eventFuncHandlers[type]!!.contains(func)) eventFuncHandlers[type]!!.add(func)
        logger.debug("[Handler] Function $type handler ${func.javaClass.name} added")
    }

    // TODO: 线程池

    fun messageHandler(element: JsonElement) {
        val data = json.decodeFromJsonElement<KhlMessage>(element)
        logger.debug("[Handler] Received Message: $data")
        messageFuncHandlers.forEach { m -> if (m.key == data.type || m.key == MessageTypes.ALL) { m.value.forEach { func -> func(data) } } }
        messageClassHandlers.forEach { m -> if (m.key == data.type  || m.key == MessageTypes.ALL) { m.value.forEach { h -> h.function.call(h.classInstance, data) } } }
        filterClassHandlers.forEach { m -> if (data.type == MessageTypes.TEXT || data.type == MessageTypes.KMD) {
            when (m.type) {
                FilterTypes.START_WITH -> if (data.content.startsWith(m.filterString, m.ignoreCase)) m.function.call(m.classInstance, data)
                FilterTypes.KEYWORD -> if (data.content.indexOf(m.filterString, ignoreCase = m.ignoreCase) != -1) m.function.call(m.classInstance, data)
                FilterTypes.REGEX -> if (m.filterRegex.matches(data.content)) m.function.call(m.classInstance, data)
            }
        } }
    }

    fun eventHandler(element: JsonElement) {
        val data = json.decodeFromJsonElement<KhlEvent>(element)
        logger.debug("[Handler] Received Event: $data")
        eventFuncHandlers.forEach { e -> if (e.key == data.extra.type  || e.key == EventTypes.ALL) { e.value.forEach { func -> func(data) } } }
        eventClassHandlers.forEach { e -> if (e.key == data.extra.type  || e.key == EventTypes.ALL) { e.value.forEach { h -> h.function.call(h.classInstance, data) } } }
    }
}