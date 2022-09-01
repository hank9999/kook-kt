package com.github.hank9999.kook.utils

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

// Modified from java DefaultThreadFactory
class NamedThreadFactory(name: String): ThreadFactory {
    private val group: ThreadGroup = Thread.currentThread().threadGroup
    private val threadNumber = AtomicInteger(1)
    private val namePrefix: String = name

    override fun newThread(runnable: Runnable): Thread {
        val thread = Thread(
            group,
            runnable,
            "$namePrefix-${threadNumber.getAndIncrement()}",
            0
        )
        if (thread.isDaemon) {
            thread.isDaemon = false
        }
        if (thread.priority != Thread.NORM_PRIORITY) {
            thread.priority = Thread.NORM_PRIORITY
        }
        return thread
    }
}