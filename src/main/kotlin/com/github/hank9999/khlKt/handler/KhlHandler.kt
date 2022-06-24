package com.github.hank9999.khlKt.handler

import com.github.hank9999.khlKt.Bot
import com.github.hank9999.khlKt.handler.types.*
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
    private val filterFuncHandlers: MutableList<FilterFuncHandler> = mutableListOf()
    private val eventClassHandlers: MutableMap<EventTypes, MutableList<EventClassHandler>> = mutableMapOf()
    private val eventFuncHandlers: MutableMap<EventTypes, MutableList<(event: KhlEvent) -> Unit>> = mutableMapOf()
    private val commandClassHandlers: MutableList<CommandClassHandler> = mutableListOf()
    private val commandFuncHandlers: MutableList<CommandFuncHandler> = mutableListOf()

    fun <T : Any> registerClassHandler(t: T) {
        val declaredFunctions = t::class.declaredFunctions
        for (f in declaredFunctions) {
            // 处理注解，使用其中的元数据
            f.annotations.forEach {
                when (it) {
                    is Bot.OnMessage -> {
                        if (!messageClassHandlers.containsKey(it.type)) messageClassHandlers[it.type] = mutableListOf()
                        val data = MessageClassHandler(t, f)
                        if (!messageClassHandlers[it.type]!!.contains(data)) {
                            messageClassHandlers[it.type]!!.add(data)
                            logger.debug("[Handler] Class ${t.javaClass.name} ${it.type} handler ${f.name} added")
                        }
                    }
                    is Bot.OnEvent -> {
                        if (!eventClassHandlers.containsKey(it.type)) eventClassHandlers[it.type] = mutableListOf()
                        val data = EventClassHandler(t, f)
                        if (!eventClassHandlers[it.type]!!.contains(data)) {
                            eventClassHandlers[it.type]!!.add(data)
                            logger.debug("[Handler] Class ${t.javaClass.name} ${it.type} handler ${f.name} added")
                        }
                    }
                    is Bot.OnFilter -> {
                        val data = when (it.type) {
                            FilterTypes.START_WITH -> FilterClassHandler(FilterTypes.START_WITH, t, f, it.startWith, it.ignoreCase)
                            FilterTypes.KEYWORD -> FilterClassHandler(FilterTypes.KEYWORD, t, f, it.keyword, it.ignoreCase)
                            FilterTypes.REGEX -> FilterClassHandler(FilterTypes.REGEX, t, f, filterRegex = Regex(it.regex))
                        }
                        if (!filterClassHandlers.contains(data)) {
                            filterClassHandlers.add(data)
                            logger.debug("[Handler] Class ${t.javaClass.name} ${it.type} handler ${f.name} added")
                        }
                    }
                    is Bot.OnCommand -> {
                        val startWith = mutableListOf<String>()
                        val prefixes = if (it.prefixes.isEmpty())  Bot.config.cmd_prefix else it.prefixes.toList()
                        prefixes.forEach { prefix ->
                            startWith.add(prefix + it.name)
                            it.aliases.forEach { name -> startWith.add(prefix + name) }
                        }
                        val data = CommandClassHandler(t, f, startWith, it.ignoreCase)
                        if (!commandClassHandlers.contains(data)) {
                            commandClassHandlers.add(data)
                            logger.debug("[Handler] Class ${t.javaClass.name} Command handler ${f.name} added")
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun registerMessageFuncHandler(type: MessageTypes, func: (msg: KhlMessage) -> Unit) {
        if (!messageFuncHandlers.containsKey(type)) messageFuncHandlers[type] = mutableListOf()
        if (!messageFuncHandlers[type]!!.contains(func)) {
            messageFuncHandlers[type]!!.add(func)
            logger.debug("[Handler] Function $type handler ${func.javaClass.name} added")
        }
    }

    fun registerEventFuncHandler(type: EventTypes, func: (event: KhlEvent) -> Unit) {
        if (!eventFuncHandlers.containsKey(type)) eventFuncHandlers[type] = mutableListOf()
        if (!eventFuncHandlers[type]!!.contains(func)) {
            eventFuncHandlers[type]!!.add(func)
            logger.debug("[Handler] Function $type handler ${func.javaClass.name} added")
        }
    }

    fun registerFilterFuncHandler(type: FilterTypes, startWith: String, keyword: String, regex: String, ignoreCase: Boolean, func: (msg: KhlMessage) -> Unit) {
        val data = when (type) {
            FilterTypes.START_WITH -> { FilterFuncHandler(FilterTypes.START_WITH, func, startWith, ignoreCase) }
            FilterTypes.KEYWORD -> { FilterFuncHandler(FilterTypes.KEYWORD, func, keyword, ignoreCase) }
            FilterTypes.REGEX -> { FilterFuncHandler(FilterTypes.REGEX, func, filterRegex = Regex(regex)) }
        }
        if (!filterFuncHandlers.contains(data)) {
            filterFuncHandlers.add(data)
            logger.debug("[Handler] Function $type handler ${func.javaClass.name} added")
        }
    }

    fun registerCommandFuncHandler(name: String, prefixes: Array<String>, aliases: Array<String>, ignoreCase: Boolean, func: (msg: KhlMessage) -> Unit) {
        val startWith = mutableListOf<String>()
        val newPrefixes = if (prefixes.isEmpty())  Bot.config.cmd_prefix else prefixes.toList()
        newPrefixes.forEach { prefix ->
            startWith.add(prefix + name)
            aliases.forEach { name -> startWith.add(prefix + name) }
        }
        val data = CommandFuncHandler(func, startWith, ignoreCase)
        if (!commandFuncHandlers.contains(data)) {
            commandFuncHandlers.add(data)
            logger.debug("[Handler] Function Command handler ${func.javaClass.name} added")
        }
    }

    private fun whetherCommandTriggered(text: String, startWith: List<String>, ignoreCase: Boolean): Boolean {
        logger.debug("[Handler] Command List: $startWith")
        startWith.forEach { prefix -> if (text.startsWith(prefix, ignoreCase)) return true }
        return false
    }

    // TODO: 线程池

    fun messageHandler(element: JsonElement) {
        val data = json.decodeFromJsonElement<KhlMessage>(element)
        logger.debug("[Handler] Received Message: $data")
        logger.debug("[Handler] Message Function Handler Processing")
        messageFuncHandlers.forEach { m -> if (m.key == data.type || m.key == MessageTypes.ALL) { m.value.forEach { func -> func(data) } } }
        logger.debug("[Handler] Message Class Handler Processing")
        messageClassHandlers.forEach { m -> if (m.key == data.type  || m.key == MessageTypes.ALL) { m.value.forEach { h -> h.function.call(h.classInstance, data) } } }
        if (data.type == MessageTypes.TEXT || data.type == MessageTypes.KMD) {
            logger.debug("[Handler] Filter Class Handler Processing")
            filterClassHandlers.forEach { m -> when (m.type) {
                FilterTypes.START_WITH -> if (data.content.startsWith(m.filterString, m.ignoreCase)) m.function.call(m.classInstance, data)
                FilterTypes.KEYWORD -> if (data.content.indexOf(m.filterString, ignoreCase = m.ignoreCase) != -1) m.function.call(m.classInstance, data)
                FilterTypes.REGEX -> if (m.filterRegex.matches(data.content)) m.function.call(m.classInstance, data)
            } }
            logger.debug("[Handler] Filter Function Handler Processing")
            filterFuncHandlers.forEach { m -> when (m.type) {
                FilterTypes.START_WITH -> if (data.content.startsWith(m.filterString, m.ignoreCase)) m.function(data)
                FilterTypes.KEYWORD -> if (data.content.indexOf(m.filterString, ignoreCase = m.ignoreCase) != -1) m.function(data)
                FilterTypes.REGEX -> if (m.filterRegex.matches(data.content)) m.function(data)
            } }
            logger.debug("[Handler] Command Class Handler Processing")
            commandClassHandlers.forEach { m -> if (whetherCommandTriggered(data.content, m.startWith, m.ignoreCase)) m.function.call(m.classInstance, data) }
            logger.debug("[Handler] Command Function Handler Processing")
            commandFuncHandlers.forEach { m -> if (whetherCommandTriggered(data.content, m.startWith, m.ignoreCase)) m.function(data) }
        }
    }

    fun eventHandler(element: JsonElement) {
        val data = json.decodeFromJsonElement<KhlEvent>(element)
        logger.debug("[Handler] Received Event: $data")
        logger.debug("[Handler] Event Function Handler Processing")
        eventFuncHandlers.forEach { e -> if (e.key == data.extra.type  || e.key == EventTypes.ALL) { e.value.forEach { func -> func(data) } } }
        logger.debug("[Handler] Event Class Handler Processing")
        eventClassHandlers.forEach { e -> if (e.key == data.extra.type  || e.key == EventTypes.ALL) { e.value.forEach { h -> h.function.call(h.classInstance, data) } } }
    }
}