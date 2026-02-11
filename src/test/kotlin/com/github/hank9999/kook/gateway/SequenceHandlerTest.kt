package com.github.hank9999.kook.gateway

import com.github.hank9999.kook.gateway.entity.ResumeAckEvent
import com.github.hank9999.kook.gateway.handler.SequenceHandler
import com.github.hank9999.kook.gateway.handler.SequenceHandler.ProcessResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** SequenceHandler 序列号处理器测试 */
class SequenceHandlerTest {

    /** 验证乱序到达的事件会被缓冲，待前序事件到达后一起按序输出 */
    @Test
    fun sequenceHandlerBuffersOutOfOrderEvents() = runTest {
        val handler = SequenceHandler()

        val outOfOrder = handler.process(ResumeAckEvent(2))
        assertIs<ProcessResult.Events>(outOfOrder)
        assertTrue(outOfOrder.events.isEmpty())
        assertTrue(outOfOrder.buffering)

        val ordered = handler.process(ResumeAckEvent(1))
        assertIs<ProcessResult.Events>(ordered)
        assertEquals(listOf(1, 2), ordered.events.mapNotNull { it.sn })
        assertFalse(ordered.buffering)
        assertEquals(2, handler.value)
    }

    /** 验证重复序列号的事件被丢弃，返回空列表 */
    @Test
    fun sequenceHandlerDropsDuplicatedEvents() = runTest {
        val handler = SequenceHandler()
        handler.process(ResumeAckEvent(1))

        val duplicated = handler.process(ResumeAckEvent(1))
        assertIs<ProcessResult.Events>(duplicated)
        assertTrue(duplicated.events.isEmpty())
        assertFalse(duplicated.buffering)
        assertEquals(1, handler.value)
    }

    /** 验证无序列号的事件直接透传，不影响序列号计数 */
    @Test
    fun sequenceHandlerPassesThroughEventsWithoutSn() = runTest {
        val handler = SequenceHandler()
        val result = handler.process(ResumeAckEvent(null))

        assertIs<ProcessResult.Events>(result)
        assertEquals(1, result.events.size)
        assertEquals(null, result.events.first().sn)
        assertFalse(result.buffering)
        assertEquals(0, handler.value)
    }

    /** 验证缓冲区超过上限时返回 BufferOverflow 信号 */
    @Test
    fun sequenceHandlerReturnsBufferOverflowWhenLimitExceeded() = runTest {
        val handler = SequenceHandler(bufferLimit = 3)

        // sn=2,3,4 乱序到达（缺 sn=1），buffer 逐渐填充
        for (sn in 2..4) {
            val result = handler.process(ResumeAckEvent(sn))
            assertIs<ProcessResult.Events>(result)
            assertTrue(result.events.isEmpty())
            assertTrue(result.buffering)
        }

        // sn=5 到达，buffer 大小超过 limit=3，触发溢出
        val overflow = handler.process(ResumeAckEvent(5))
        assertIs<ProcessResult.BufferOverflow>(overflow)
    }

    /** 验证顺序事件部分释放缓冲区后，仍有缺号时 buffering 为 true */
    @Test
    fun sequenceHandlerReportsBufferingWhenGapRemains() = runTest {
        val handler = SequenceHandler()

        // sn=2, sn=4 到达，缺 sn=1, sn=3
        handler.process(ResumeAckEvent(2))
        handler.process(ResumeAckEvent(4))

        // sn=1 到达，释放 sn=1,2，但 sn=4 仍在缓冲区（缺 sn=3）
        val partial = handler.process(ResumeAckEvent(1))
        assertIs<ProcessResult.Events>(partial)
        assertEquals(listOf(1, 2), partial.events.mapNotNull { it.sn })
        assertTrue(partial.buffering)
        assertEquals(2, handler.value)

        // sn=3 到达，释放 sn=3,4，缓冲区清空
        val filled = handler.process(ResumeAckEvent(3))
        assertIs<ProcessResult.Events>(filled)
        assertEquals(listOf(3, 4), filled.events.mapNotNull { it.sn })
        assertFalse(filled.buffering)
        assertEquals(4, handler.value)
    }
}
