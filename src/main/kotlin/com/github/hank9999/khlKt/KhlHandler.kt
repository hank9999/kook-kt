package com.github.hank9999.khlKt

import com.github.hank9999.khlKt.types.KhlMessage
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import com.github.hank9999.khlKt.json.JSON.Companion.json
import com.github.hank9999.khlKt.types.KhlEvent
import com.github.hank9999.khlKt.types.types.EventTypes
import com.github.hank9999.khlKt.types.types.MessageTypes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

class KhlHandler {
    private val logger: Logger = LoggerFactory.getLogger(KhlHandler::class.java)
    private val messageClassHandlers: MutableMap<MessageTypes, MutableMap<Any, MutableList<KFunction<*>>>> = mutableMapOf()
    private val messageFuncHandlers: MutableMap<MessageTypes, MutableList<(msg: KhlMessage) -> Unit>> = mutableMapOf()
    private val eventClassHandlers: MutableMap<EventTypes, MutableMap<Any, MutableList<KFunction<*>>>> = mutableMapOf()
    private val eventFuncHandlers: MutableMap<EventTypes, MutableList<(event: KhlEvent) -> Unit>> = mutableMapOf()

    fun <T : Any> registerClassHandler(t: T) {
        val declaredFunctions = t::class.declaredFunctions
        for (f in declaredFunctions) {
            // 处理 TestCase 注解，使用其中的元数据
            f.annotations.forEach {
                if (it is Bot.OnMessage) {
                    if (!messageClassHandlers.containsKey(it.type)) messageClassHandlers[it.type] = mutableMapOf()
                    if (!messageClassHandlers[it.type]!!.containsKey(t)) messageClassHandlers[it.type]!![t] = mutableListOf()
                    if (!messageClassHandlers[it.type]!![t]!!.contains(f)) messageClassHandlers[it.type]!![t]!!.add(f)
                } else if (it is Bot.OnEvent) {
                    if (!eventClassHandlers.containsKey(it.type)) eventClassHandlers[it.type] = mutableMapOf()
                    if (!eventClassHandlers[it.type]!!.containsKey(t)) eventClassHandlers[it.type]!![t] = mutableListOf()
                    if (!eventClassHandlers[it.type]!![t]!!.contains(f)) eventClassHandlers[it.type]!![t]!!.add(f)
                }
            }
        }
    }

    fun registerMessageFuncHandler(type: MessageTypes, func: (msg: KhlMessage) -> Unit) {
        if (!messageFuncHandlers.containsKey(type)) messageFuncHandlers[type] = mutableListOf()
        if (!messageFuncHandlers[type]!!.contains(func)) messageFuncHandlers[type]!!.add(func)
    }

    fun registerEventFuncHandler(type: EventTypes, func: (event: KhlEvent) -> Unit) {
        if (!eventFuncHandlers.containsKey(type)) eventFuncHandlers[type] = mutableListOf()
        if (!eventFuncHandlers[type]!!.contains(func)) eventFuncHandlers[type]!!.add(func)
    }

    fun messageHandler(element: JsonElement) {
        val data = json.decodeFromJsonElement<KhlMessage>(element)
        messageFuncHandlers.forEach {
            if (data.type == it.key || it.key == MessageTypes.ALL) {
                it.value.forEach { func ->
                    thread(name = "MessageFuncHandler") { func(data) }
                }
            }
        }
        messageClassHandlers.forEach {
            it.value.forEach { c ->
                if (data.type == it.key || it.key == MessageTypes.ALL) {
                    c.value.forEach { func ->
                        thread(name = "MessageClassHandler") { func.call(c.key, data) }
                    }
                }
            }
        }
    }

    fun eventHandler(element: JsonElement) {
        val data = json.decodeFromJsonElement<KhlEvent>(element)
        eventFuncHandlers.forEach {
            if (data.extra.type == it.key || it.key == EventTypes.ALL) {
                it.value.forEach { func ->
                    thread(name = "MessageFuncHandler") { func(data) }
                }
            }
        }
        eventClassHandlers.forEach {
            it.value.forEach { c ->
                if (data.extra.type == it.key || it.key == EventTypes.ALL) {
                    c.value.forEach { func ->
                        thread(name = "MessageClassHandler") { func.call(c.key, data) }
                    }
                }
            }
        }
    }
}