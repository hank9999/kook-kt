package com.github.hank9999.kook.gateway.handler

import com.github.hank9999.kook.gateway.entity.Event
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 序列号处理器
 * 维护事件的有序性：对乱序到达的事件进行缓冲，丢弃重复事件，
 * 按序列号递增顺序输出连续的事件列表
 */
internal class SequenceHandler(
    private val bufferLimit: Int = 50,
) {

    /** 事件处理结果 */
    sealed class ProcessResult {
        /** 正常结果，包含按序排列的可输出事件列表；buffering 表示处理后缓冲区是否仍有等待中的事件 */
        data class Events(val events: List<Event>, val buffering: Boolean = false) : ProcessResult()
        /** 缓冲区溢出，需要触发重连 */
        data object BufferOverflow : ProcessResult()
    }

    private val lock = Mutex()
    /** 当前已确认的最大序列号 */
    @Volatile
    private var maxSn: Int = 0
    /** 乱序事件缓冲区，按序列号排序 */
    private val buffer = sortedMapOf<Int, Event>()

    /** 当前已处理的最大序列号 */
    val value: Int
        get() = maxSn

    /**
     * 处理收到的事件，维护事件有序性
     *
     * 处理逻辑：
     * - 无序列号的事件直接透传
     * - 重复事件（sn <= maxSn）被丢弃
     * - 乱序事件（sn > expected）暂存到缓冲区，超过上限返回溢出信号
     * - 顺序事件被输出，同时释放缓冲区中连续的后续事件
     *
     * @param event 待处理的事件
     * @return 处理结果：按序事件列表或缓冲区溢出信号
     */
    suspend fun process(event: Event): ProcessResult {
        val sn = event.sn ?: return ProcessResult.Events(listOf(event))

        return lock.withLock {
            // 丢弃已处理过的重复事件
            if (sn <= maxSn) {
                return@withLock ProcessResult.Events(emptyList())
            }

            val expected = maxSn + 1
            if (sn > expected) {
                // 事件乱序到达，暂存到缓冲区等待前序事件
                buffer.putIfAbsent(sn, event)
                if (buffer.size > bufferLimit) {
                    return@withLock ProcessResult.BufferOverflow
                }
                return@withLock ProcessResult.Events(emptyList(), buffering = true)
            }

            // 顺序事件，输出并尝试释放缓冲区中连续的后续事件
            val readyEvents = mutableListOf<Event>()
            readyEvents += event
            maxSn = sn

            while (true) {
                val nextSn = maxSn + 1
                val next = buffer.remove(nextSn) ?: break
                readyEvents += next
                maxSn = nextSn
            }

            ProcessResult.Events(readyEvents, buffering = buffer.isNotEmpty())
        }
    }

    /** 重置序列号和缓冲区，用于全量重连 */
    suspend fun reset() {
        lock.withLock {
            maxSn = 0
            buffer.clear()
        }
    }
}
