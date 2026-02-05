package com.github.hank9999.kook

import com.github.hank9999.kook.common.entity.ChannelPrivacyType
import com.github.hank9999.kook.common.entity.MessageType
import com.github.hank9999.kook.common.utils.JSON
import com.github.hank9999.kook.gateway.entity.ChallengeEvent
import com.github.hank9999.kook.gateway.entity.Event
import com.github.hank9999.kook.gateway.entity.HandshakeAckEvent
import com.github.hank9999.kook.gateway.entity.MemberJoinedGuildEvent
import com.github.hank9999.kook.gateway.entity.PongEvent
import com.github.hank9999.kook.gateway.entity.ReactionAddedEvent
import com.github.hank9999.kook.gateway.entity.ReconnectEvent
import com.github.hank9999.kook.gateway.entity.ResumeAckEvent
import com.github.hank9999.kook.gateway.entity.UnknownEvent
import com.github.hank9999.kook.gateway.entity.UnknownSystemEvent
import com.github.hank9999.kook.gateway.entity.UserMessageEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class EventTest {
    private val json = JSON.defaultJson()

    @Test
    fun decodeChallengeTest() {
        val rawText = """{"s":0,"d":{"type":255,"channel_type":"WEBHOOK_CHALLENGE","challenge":"bkes654x09XY","verify_token":"xxxxxx"}}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<ChallengeEvent>(decoded)
        assertEquals("bkes654x09XY", decoded.challenge)
        assertEquals("xxxxxx", decoded.verifyToken)
    }

    @Test
    fun decodeGroupKMarkdownMessageTest() {
        val rawText = """{"s":0,"sn":1,"d":{
            "channel_type":"GROUP","type":9,"target_id":"1234567890",
            "author_id":"9876543210","content":"Hello World",
            "msg_id":"msg-001","msg_timestamp":1700000000000,"nonce":"abc",
            "verify_token":"token123",
            "extra":{"type":9,"guild_id":"guild001","channel_name":"general",
                "mention":[],"mention_all":false,"mention_roles":[],"mention_here":false,
                "author":{"id":"9876543210","username":"TestUser","nickname":"Test",
                    "identify_num":"0001","online":true,"os":"Websocket","status":0,
                    "avatar":"https://example.com/avatar.png","vip_avatar":"","banner":"",
                    "is_vip":false,"vip_amp":false,"bot":false,"roles":[],"is_sys":false},
                "kmarkdown":{"raw_content":"Hello World","mention_part":[],"mention_role_part":[]}}
        }}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<UserMessageEvent>(decoded)
        assertEquals(ChannelPrivacyType.GROUP, decoded.channelType)
        assertEquals(MessageType.KMD, decoded.messageType)
        assertEquals("Hello World", decoded.content)
        assertEquals("guild001", decoded.guildId)
        assertEquals("general", decoded.channelName)
        assertEquals(1, decoded.sn)
        assertEquals(1700000000000L, decoded.msgTimestamp)
        assertNotNull(decoded.author)
        assertEquals("TestUser", decoded.author!!.username)
        assertNotNull(decoded.kmarkdown)
        assertEquals("Hello World", decoded.kmarkdown!!.rawContent)
    }

    @Test
    fun decodePersonMessageTest() {
        val rawText = """{"s":0,"sn":3,"d":{
            "channel_type":"PERSON","type":1,"target_id":"user123",
            "author_id":"user456","content":"私聊消息",
            "msg_id":"msg-003","msg_timestamp":1700000000000,"nonce":"ghi",
            "extra":{"type":1,"code":"","author":{"id":"user456","username":"PrivateUser",
                "nickname":"PM","identify_num":"0002","online":true,"os":"Websocket",
                "status":0,"avatar":"","vip_avatar":"","banner":"","is_vip":false,
                "vip_amp":false,"bot":false,"roles":[],"is_sys":false}}
        }}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<UserMessageEvent>(decoded)
        assertEquals(ChannelPrivacyType.PERSON, decoded.channelType)
        assertEquals(MessageType.TEXT, decoded.messageType)
        assertEquals("私聊消息", decoded.content)
        assertEquals("", decoded.guildId)
        assertEquals(3, decoded.sn)
        assertNotNull(decoded.author)
        assertEquals("PrivateUser", decoded.author!!.username)
    }

    @Test
    fun decodeImageMessageTest() {
        val rawText = """{"s":0,"sn":4,"d":{
            "channel_type":"GROUP","type":2,"target_id":"ch001",
            "author_id":"user001","content":"https://example.com/image.png",
            "msg_id":"msg-004","msg_timestamp":1700000000000,"nonce":"jkl",
            "extra":{"type":2,"guild_id":"guild001","channel_name":"images",
                "mention":[],"mention_all":false,"mention_roles":[],"mention_here":false,
                "author":{"id":"user001","username":"ImageUser","nickname":"Img",
                    "identify_num":"0003","online":true,"os":"Websocket","status":0,
                    "avatar":"","vip_avatar":"","banner":"","is_vip":false,
                    "vip_amp":false,"bot":false,"roles":[],"is_sys":false},
                "attachments":{"type":"image","url":"https://example.com/image.png",
                    "name":"image.png","size":12345}}
        }}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<UserMessageEvent>(decoded)
        assertEquals(MessageType.IMAGE, decoded.messageType)
        assertNotNull(decoded.attachment)
        assertEquals("image", decoded.attachment!!.type)
        assertEquals("https://example.com/image.png", decoded.attachment!!.url)
        assertEquals(12345, decoded.attachment!!.size)
    }

    @Test
    fun decodeSystemEventAddedReactionTest() {
        val rawText = """{"s":0,"sn":2,"d":{
            "channel_type":"GROUP","type":255,"target_id":"1234567890",
            "author_id":"1","content":"[系统消息]",
            "msg_id":"msg-002","msg_timestamp":1700000000000,"nonce":"def",
            "verify_token":"token123",
            "extra":{"type":"added_reaction","body":{
                "channel_id":"1234567890","emoji":{"id":"1234","name":"thumbsup"},
                "user_id":"9876543210","msg_id":"msg-001"}}
        }}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<ReactionAddedEvent>(decoded)
        assertEquals(ChannelPrivacyType.GROUP, decoded.context.channelType)
        assertEquals(2, decoded.sn)
        // 验证强类型数据
        assertEquals("1234567890", decoded.data.channelId)
        assertEquals("1234", decoded.data.emoji.id)
        assertEquals("thumbsup", decoded.data.emoji.name)
        assertEquals("9876543210", decoded.data.userId)
        assertEquals("msg-001", decoded.data.msgId)
    }

    @Test
    fun decodeSystemEventJoinedGuildTest() {
        val rawText = """{"s":0,"sn":5,"d":{
            "channel_type":"GROUP","type":255,"target_id":"guild001",
            "author_id":"1","content":"[系统消息]",
            "msg_id":"msg-005","msg_timestamp":1700000000000,"nonce":"mno",
            "extra":{"type":"joined_guild","body":{
                "user_id":"user999","joined_at":1700000000000}}
        }}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<MemberJoinedGuildEvent>(decoded)
        // 验证强类型数据
        assertEquals("user999", decoded.data.userId)
        assertEquals(1700000000000L, decoded.data.joinedAt)
    }

    @Test
    fun decodeUnknownSystemEventTypeTest() {
        val rawText = """{"s":0,"sn":1,"d":{
            "channel_type":"GROUP","type":255,"target_id":"123",
            "author_id":"1","content":"","msg_id":"m1","msg_timestamp":0,"nonce":"",
            "extra":{"type":"some_future_event","body":{}}
        }}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<UnknownSystemEvent>(decoded)
        assertEquals("some_future_event", decoded.eventTypeName)
    }

    @Test
    fun decodeHandshakeAckTest() {
        val rawText = """{"s":1,"d":{"code":0,"session_id":"abc123"}}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<HandshakeAckEvent>(decoded)
        assertNotNull(decoded.data)
    }

    @Test
    fun decodePongTest() {
        val rawText = """{"s":3}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<PongEvent>(decoded)
    }

    @Test
    fun decodeReconnectTest() {
        val rawText = """{"s":5,"d":{"code":41008,"err":"Missing params"}}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<ReconnectEvent>(decoded)
        assertNotNull(decoded.data)
    }

    @Test
    fun decodeResumeAckTest() {
        val rawText = """{"s":6,"sn":100}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<ResumeAckEvent>(decoded)
        assertEquals(100, decoded.sn)
    }

    @Test
    fun decodeUnknownSignalTypeTest() {
        val rawText = """{"s":99}"""
        val decoded = json.decodeFromString<Event>(rawText)
        assertIs<UnknownEvent>(decoded)
    }
}
