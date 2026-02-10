package com.github.hank9999.kook.gateway.handler

import com.github.hank9999.kook.gateway.entity.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger("[Handler]")

/**
 * 事件处理器基类
 * 通过订阅事件流来监听特定类型的事件并执行处理逻辑，在初始化时自动启动
 *
 * @param flow 事件流
 * @param name 处理器名称，用于日志标识
 * @param dispatcher 协程调度器
 */
internal abstract class Handler(
    val flow: Flow<Event>,
    val name: String,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatcher

    init {
        launch { start() }
    }

    /** 启动处理器，子类重写此方法以注册事件监听 */
    open fun start() {
    }

    /**
     * 注册指定类型事件的监听回调
     * 从事件流中过滤出 [T] 类型的事件，并在独立协程中执行 [block] 处理
     */
    inline fun <reified T> on(crossinline block: suspend (T) -> Unit) {
        flow
            .filterIsInstance<T>()
            .onEach {
                try {
                    block(it)
                } catch (exception: Exception) {
                    logger.error(exception) { "[$name]" }
                }
            }
            .launchIn(this)
    }
}

