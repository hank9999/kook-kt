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
 * 通过订阅事件流来监听特定类型的事件并执行处理逻辑
 * 生命周期由外部通过 [attach]/[cancel] 控制
 *
 * @param flow 事件流
 * @param name 处理器名称，用于日志标识
 * @param dispatcher 协程调度器
 */
internal abstract class Handler(
    val flow: Flow<Event>,
    val name: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineScope {
    @Volatile
    private var job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = job + dispatcher

    /** 启动处理器，子类重写此方法以注册事件监听 */
    open fun start() {
    }

    /**
     * 启动处理器：创建新的协程作用域并注册事件监听
     * 如果之前已启动，会先取消旧的作用域
     */
    fun attach() {
        job.cancel()
        job = SupervisorJob()
        launch { start() }
    }

    /**
     * 停止处理器：取消协程作用域内的所有协程
     * 子类可重写以执行额外的清理逻辑
     */
    open fun cancel() {
        job.cancel()
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

