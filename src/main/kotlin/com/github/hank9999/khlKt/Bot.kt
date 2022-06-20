package com.github.hank9999.khlKt

import com.github.hank9999.khlKt.handler.KhlHandler
import com.github.hank9999.khlKt.http.HttpApi
import com.github.hank9999.khlKt.types.KhlEvent
import com.github.hank9999.khlKt.types.KhlMessage
import com.github.hank9999.khlKt.types.types.EventTypes
import com.github.hank9999.khlKt.types.types.MessageTypes

class Bot(config: Config) {
    companion object {
        lateinit var config: Config
        lateinit var httpApi: HttpApi
        lateinit var khlHandler: KhlHandler
    }
    init {
        Companion.config = config
        httpApi = HttpApi()
        khlHandler = KhlHandler()
        WebHook().initialize()
    }

    fun <T : Any> registerClass(t: T) {
        khlHandler.registerClassHandler(t)
    }

    fun registerMessageFunc(type: MessageTypes, func: (msg: KhlMessage) -> Unit) {
        khlHandler.registerMessageFuncHandler(type, func)
    }

    fun registerEventFunc(type: EventTypes, func: (event: KhlEvent) -> Unit) {
        khlHandler.registerEventFuncHandler(type, func)
    }

    @Target(AnnotationTarget.FUNCTION)
    annotation class OnMessage(val type: MessageTypes)

    @Target(AnnotationTarget.FUNCTION)
    annotation class OnEvent(val type: EventTypes)

    @Target(AnnotationTarget.FUNCTION)
    annotation class OnFilter(val startWith: String = "", val keyword: String = "", val regex: String = "", val ignoreCase: Boolean = true)
}