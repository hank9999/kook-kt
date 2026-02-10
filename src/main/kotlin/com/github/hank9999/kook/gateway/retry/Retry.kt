package com.github.hank9999.kook.gateway.retry

/**
 * 重试策略接口
 * 定义重试行为的统一抽象，包括是否可继续重试、重置状态、执行重试等操作
 */
public interface Retry {
    /** 是否还有剩余重试次数 */
    public val hasNext: Boolean

    /** 重置重试状态，将计数器和退避时长恢复到初始值 */
    public fun reset()

    /** 执行一次重试，等待退避时长后递增计数器 */
    public suspend fun retry()
}

