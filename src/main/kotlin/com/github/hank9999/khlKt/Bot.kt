package com.github.hank9999.khlKt

import com.github.hank9999.khlKt.handler.KhlHandler
import com.github.hank9999.khlKt.handler.types.FilterTypes
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

    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnMessage(val type: MessageTypes)

    @Target(AnnotationTarget.FUNCTION) @Repeatable
    annotation class OnEvent(val type: EventTypes)

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
}