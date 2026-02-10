package com.github.hank9999.kook.gateway.handler

import com.github.hank9999.kook.gateway.entity.Event
import com.github.hank9999.kook.gateway.entity.ReconnectEvent
import kotlinx.coroutines.flow.Flow

/**
 * 重连处理器
 * 监听服务端下发的重连信令（信令 5），收到后触发重连回调
 *
 * @param reconnect 收到重连信令时执行的回调，参数为服务端下发的重连事件
 */
internal class ReconnectHandler(
    flow: Flow<Event>,
    private val reconnect: suspend (ReconnectEvent) -> Unit,
) : Handler(flow, "ReconnectHandler") {

    override fun start() {
        on<ReconnectEvent> { event ->
            reconnect(event)
        }
    }
}

