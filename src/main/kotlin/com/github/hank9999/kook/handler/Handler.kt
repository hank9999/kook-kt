package com.github.hank9999.kook.handler

import com.github.hank9999.kook.Bot
import com.github.hank9999.kook.Config
import com.github.hank9999.kook.handler.types.*
import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.types.Event
import com.github.hank9999.kook.types.Message
import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import com.github.hank9999.kook.types.types.EventTypes
import com.github.hank9999.kook.types.types.MessageTypes
import com.github.hank9999.kook.utils.NamedThreadFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.full.declaredFunctions

class Handler(config: Config) {
    private val logger: Logger = LoggerFactory.getLogger(Handler::class.java)
    private val messageClassHandlers: MutableMap<MessageTypes, MutableList<MessageClassHandler>> = mutableMapOf()
    private val messageFuncHandlers: MutableMap<MessageTypes, MutableList<MessageFuncHandler>> = mutableMapOf()
    private val filterClassHandlers: MutableList<FilterClassHandler> = mutableListOf()
    private val filterFuncHandlers: MutableList<FilterFuncHandler> = mutableListOf()
    private val eventClassHandlers: MutableMap<EventTypes, MutableList<EventClassHandler>> = mutableMapOf()
    private val eventFuncHandlers: MutableMap<EventTypes, MutableList<(event: Event, cs: CoroutineScope) -> Unit>> = mutableMapOf()
    private val commandClassHandlers: MutableList<CommandClassHandler> = mutableListOf()
    private val commandFuncHandlers: MutableList<CommandFuncHandler> = mutableListOf()
    private val messageQueue: MutableList<JsonElement> = mutableListOf()
    private val eventQueue: MutableList<JsonElement> = mutableListOf()
    private val threadNumber = Runtime.getRuntime().availableProcessors() + 1
    private val threadPool = ThreadPoolExecutor(threadNumber, threadNumber,0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue(10240), NamedThreadFactory("KookHandler"))
    private val coroutineScope = CoroutineScope(threadPool.asCoroutineDispatcher())
    private var config: Config

    fun addMessageQueue(data: JsonElement) {
        messageQueue.add(data)
    }

    fun addEventQueue(data: JsonElement) {
        eventQueue.add(data)
    }

    init {
        this.config = config
        coroutineScope.launch {
            launch {
                while (true) {
                    if (messageQueue.size != 0) {
                        val data = messageQueue.removeAt(0)
                        messageHandler(data)
                    } else {
                        delay(1)
                    }
                }
            }
            launch {
                while (true) {
                    if (eventQueue.size != 0) {
                        val data = eventQueue.removeAt(0)
                        eventHandler(data)
                    } else {
                        delay(1)
                    }
                }
            }
        }
    }

    fun <T : Any> registerClassHandler(t: T) {
        val declaredFunctions = t::class.declaredFunctions
        for (f in declaredFunctions) {
            // 处理注解，使用其中的元数据
            f.annotations.forEach {
                when (it) {
                    is Bot.OnMessage -> {
                        if (!messageClassHandlers.containsKey(it.type)) messageClassHandlers[it.type] = mutableListOf()
                        val data = MessageClassHandler(t, it.channelPrivacyTypes, f)
                        if (!messageClassHandlers[it.type]!!.contains(data)) {
                            messageClassHandlers[it.type]!!.add(data)
                            logger.debug("Class ${t.javaClass.name} ${it.type} handler ${f.name} added")
                        }
                    }
                    is Bot.OnEvent -> {
                        if (!eventClassHandlers.containsKey(it.type)) eventClassHandlers[it.type] = mutableListOf()
                        val data = EventClassHandler(t, f)
                        if (!eventClassHandlers[it.type]!!.contains(data)) {
                            eventClassHandlers[it.type]!!.add(data)
                            logger.debug("Class ${t.javaClass.name} ${it.type} handler ${f.name} added")
                        }
                    }
                    is Bot.OnFilter -> {
                        val data = when (it.type) {
                            FilterTypes.START_WITH -> FilterClassHandler(FilterTypes.START_WITH, t, f, it.channelPrivacyTypes, it.startWith, it.ignoreCase)
                            FilterTypes.KEYWORD -> FilterClassHandler(FilterTypes.KEYWORD, t, f, it.channelPrivacyTypes, it.keyword, it.ignoreCase)
                            FilterTypes.REGEX -> FilterClassHandler(FilterTypes.REGEX, t, f, it.channelPrivacyTypes, filterRegex = Regex(it.regex))
                        }
                        if (!filterClassHandlers.contains(data)) {
                            filterClassHandlers.add(data)
                            logger.debug("Class ${t.javaClass.name} ${it.type} handler ${f.name} added")
                        }
                    }
                    is Bot.OnCommand -> {
                        val startWith = mutableListOf<String>()
                        val prefixes = if (it.prefixes.isEmpty())  config.cmd_prefix else it.prefixes.toList()
                        prefixes.forEach { prefix ->
                            startWith.add(prefix + it.name)
                            it.aliases.forEach { name -> startWith.add(prefix + name) }
                        }
                        val data = CommandClassHandler(t, f, startWith, it.ignoreCase, it.channelPrivacyTypes)
                        if (!commandClassHandlers.contains(data)) {
                            commandClassHandlers.add(data)
                            logger.debug("Class ${t.javaClass.name} Command handler ${f.name} added")
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun registerMessageFuncHandler(type: MessageTypes, channelPrivacyTypes: ChannelPrivacyTypes, func: (msg: Message, cs: CoroutineScope) -> Unit) {
        if (!messageFuncHandlers.containsKey(type)) messageFuncHandlers[type] = mutableListOf()
        val data = MessageFuncHandler(channelPrivacyTypes, func)
        if (!messageFuncHandlers[type]!!.contains(data)) {
            messageFuncHandlers[type]!!.add(data)
            logger.debug("Function $type handler ${func.javaClass.name} added")
        }
    }

    fun registerEventFuncHandler(type: EventTypes, func: (event: Event, cs: CoroutineScope) -> Unit) {
        if (!eventFuncHandlers.containsKey(type)) eventFuncHandlers[type] = mutableListOf()
        if (!eventFuncHandlers[type]!!.contains(func)) {
            eventFuncHandlers[type]!!.add(func)
            logger.debug("Function $type handler ${func.javaClass.name} added")
        }
    }

    fun registerFilterFuncHandler(type: FilterTypes, channelPrivacyTypes: ChannelPrivacyTypes, startWith: String, keyword: String, regex: String, ignoreCase: Boolean, func: (msg: Message, cs: CoroutineScope) -> Unit) {
        val data = when (type) {
            FilterTypes.START_WITH -> { FilterFuncHandler(FilterTypes.START_WITH, func, channelPrivacyTypes, startWith, ignoreCase) }
            FilterTypes.KEYWORD -> { FilterFuncHandler(FilterTypes.KEYWORD, func, channelPrivacyTypes, keyword, ignoreCase) }
            FilterTypes.REGEX -> { FilterFuncHandler(FilterTypes.REGEX, func, channelPrivacyTypes, filterRegex = Regex(regex)) }
        }
        if (!filterFuncHandlers.contains(data)) {
            filterFuncHandlers.add(data)
            logger.debug("Function $type handler ${func.javaClass.name} added")
        }
    }

    fun registerCommandFuncHandler(name: String, prefixes: Array<String>, aliases: Array<String>, ignoreCase: Boolean, channelPrivacyTypes: ChannelPrivacyTypes, func: (msg: Message, cs: CoroutineScope) -> Unit) {
        val startWith = mutableListOf<String>()
        val newPrefixes = if (prefixes.isEmpty()) config.cmd_prefix else prefixes.toList()
        newPrefixes.forEach { prefix ->
            startWith.add(prefix + name)
            aliases.forEach { name -> startWith.add(prefix + name) }
        }
        val data = CommandFuncHandler(func, startWith, ignoreCase, channelPrivacyTypes)
        if (!commandFuncHandlers.contains(data)) {
            commandFuncHandlers.add(data)
            logger.debug("Function Command handler ${func.javaClass.name} added")
        }
    }

    private fun whetherCommandTriggered(text: String, startWith: List<String>, ignoreCase: Boolean): Boolean {
        logger.debug("Command List: $startWith")
        startWith.forEach { prefix ->
            if (text.startsWith(prefix, ignoreCase)) {
                return true
            }
        }
        return false
    }

    private fun messageHandler(element: JsonElement) {
        coroutineScope.launch {
            val data = try {
                json.decodeFromJsonElement<Message>(element)
            } catch (ex: Exception) {
                logger.error("$element\n${ex.message}\n${ex.stackTraceToString()}")
                return@launch
            }
            logger.debug("Received Message: $data")
            logger.debug("Message Function Handler Processing")
            messageFuncHandlers.forEach { m ->
                logger.debug("Message Function Handler $m")
                if (m.key == data.type || m.key == MessageTypes.ALL) {
                    m.value.forEach { h ->
                        if (h.channelPrivacyTypes == data.channelType || h.channelPrivacyTypes == ChannelPrivacyTypes.ALL) {
                            launch { h.function(data, coroutineScope) }
                        }
                    }
                }
            }
            logger.debug("Message Class Handler Processing")
            messageClassHandlers.forEach { m ->
                logger.debug("Message Class Handler $m")
                if (m.key == data.type || m.key == MessageTypes.ALL) {
                    m.value.forEach { h ->
                        if (h.channelPrivacyTypes == data.channelType || h.channelPrivacyTypes == ChannelPrivacyTypes.ALL) {
                            launch { suspendCoroutine<Unit> { continuation -> h.function.call(h.classInstance, data, continuation) } }
                        }
                    }
                }
            }
            if (data.type == MessageTypes.TEXT || data.type == MessageTypes.KMD) {
                logger.debug("Filter Class Handler Processing")
                filterClassHandlers.forEach { m ->
                    logger.debug("Filter Class Handler $m")
                    val isMatchChannelPrivacyType = m.channelPrivacyTypes == data.channelType || m.channelPrivacyTypes == ChannelPrivacyTypes.ALL
                    when (m.type) {
                        FilterTypes.START_WITH -> if (data.content.startsWith(m.filterString, m.ignoreCase) && isMatchChannelPrivacyType) {
                            launch { suspendCoroutine<Unit> { continuation -> m.function.call(m.classInstance, data, continuation) } }
                        }
                        FilterTypes.KEYWORD -> if (data.content.indexOf(m.filterString, ignoreCase = m.ignoreCase) != -1 && isMatchChannelPrivacyType) {
                            launch { suspendCoroutine<Unit> { continuation -> m.function.call(m.classInstance, data, continuation) } }
                        }
                        FilterTypes.REGEX -> if (m.filterRegex.matches(data.content) && isMatchChannelPrivacyType) {
                            launch { suspendCoroutine<Unit> { continuation -> m.function.call(m.classInstance, data, continuation) } }
                        }
                    }
                }
                logger.debug("Filter Function Handler Processing")
                filterFuncHandlers.forEach { m ->
                    logger.debug("Filter Function Handler $m")
                    val isMatchChannelPrivacyType = m.channelPrivacyTypes == data.channelType || m.channelPrivacyTypes == ChannelPrivacyTypes.ALL
                    when (m.type) {
                        FilterTypes.START_WITH -> if (data.content.startsWith(m.filterString, m.ignoreCase) && isMatchChannelPrivacyType) {
                            launch { m.function(data, coroutineScope) }
                        }
                        FilterTypes.KEYWORD -> if (data.content.indexOf(m.filterString, ignoreCase = m.ignoreCase) != -1 && isMatchChannelPrivacyType) {
                            launch { m.function(data, coroutineScope) }
                        }
                        FilterTypes.REGEX -> if (m.filterRegex.matches(data.content) && isMatchChannelPrivacyType) {
                            launch { m.function(data, coroutineScope) }
                        }
                    }
                }
                logger.debug("Command Class Handler Processing")
                commandClassHandlers.forEach { m ->
                    logger.debug("Command Class Handler $m")
                    val isMatchChannelPrivacyType = m.channelPrivacyTypes == data.channelType || m.channelPrivacyTypes == ChannelPrivacyTypes.ALL
                    if (whetherCommandTriggered(data.content, m.startWith, m.ignoreCase) && isMatchChannelPrivacyType) {
                        launch { suspendCoroutine<Unit> { continuation -> m.function.call(m.classInstance, data, continuation) } }
                    }
                }
                logger.debug("Command Function Handler Processing")
                commandFuncHandlers.forEach { m ->
                    logger.debug("Command Function Handler $m")
                    val isMatchChannelPrivacyType = m.channelPrivacyTypes == data.channelType || m.channelPrivacyTypes == ChannelPrivacyTypes.ALL
                    if (whetherCommandTriggered(data.content, m.startWith, m.ignoreCase) && isMatchChannelPrivacyType) {
                        launch { m.function(data, coroutineScope) }
                    }
                }
            }
        }
    }

    private fun eventHandler(element: JsonElement) {
        coroutineScope.launch {
            val data = try {
                json.decodeFromJsonElement<Event>(element)
            } catch (ex: Exception) {
                logger.error("$element\n${ex.message}\n${ex.stackTraceToString()}")
                return@launch
            }
            logger.debug("Received Event: $data")
            logger.debug("Event Function Handler Processing")
            eventFuncHandlers.forEach { e ->
                logger.debug("Event Function Handler $e")
                if (e.key == data.extra.type || e.key == EventTypes.ALL) {
                    e.value.forEach { func ->
                        launch { func(data, coroutineScope) }
                    }
                }
            }
            logger.debug("Event Class Handler Processing")
            eventClassHandlers.forEach { e ->
                logger.debug("Event Class Handler $e")
                if (e.key == data.extra.type || e.key == EventTypes.ALL) {
                    e.value.forEach { h ->
                        launch { suspendCoroutine<Unit> { continuation -> h.function.call(h.classInstance, data, continuation) } }
                    }
                }
            }
        }
    }
}