package com.github.hank9999.kook

import com.github.hank9999.kook.common.utils.JSON
import com.github.hank9999.kook.gateway.ChallengeEvent
import com.github.hank9999.kook.gateway.Event
import kotlin.test.Test
import kotlin.test.assertEquals

class EventTest {
    @Test
    fun decodeChallengeTest() {
        val rawText = """{"s":0,"d":{"type":255,"channel_type":"WEBHOOK_CHALLENGE","challenge":"bkes654x09XY","verify_token":"xxxxxx"}}"""
        val decoded = JSON.defaultJson().decodeFromString<Event>(rawText)
        assertEquals(ChallengeEvent::class, decoded::class)
        println(decoded)
    }
}