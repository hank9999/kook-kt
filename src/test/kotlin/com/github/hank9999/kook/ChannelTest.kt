package com.github.hank9999.kook;

import com.github.hank9999.kook.common.entity.channel.DetailChannel
import com.github.hank9999.kook.common.entity.user.GuildBotUser
import com.github.hank9999.kook.common.entity.user.GuildUser
import com.github.hank9999.kook.common.utils.JSON
import kotlin.test.Test
import kotlin.test.assertEquals

class ChannelTest {
    @Test
    fun channelTest() {
        val rawText = """{"id":"4329657281000000","guild_id":"2498374620000000","master_id":"8542000000","parent_id":"5820913851000000","user_id":"8542000000","name":"量子实验室","topic":"","type":1,"level":3,"slow_mode":0,"last_msg_content":"Zaero：随机账号登录即可","last_msg_id":"91942812-c1d7-49ef-b9ac-271e00000000","has_password":false,"limit_amount":0,"is_category":false,"permission_sync":0,"permission_overwrites":[{"role_id":9980000,"allow":2048,"deny":0},{"role_id":1870000,"allow":0,"deny":2048},{"role_id":5840000,"allow":6144,"deny":0},{"role_id":0,"allow":0,"deny":6144}],"permission_users":[{"user":{"id":"3109000000","username":"AlphaBot","identify_num":"7600","online":false,"os":"Websocket","status":0,"avatar":"","vip_avatar":"","banner":"","nickname":"小音音","roles":[],"is_vip":false,"vip_amp":false,"bot":true,"nameplate":[],"bot_status":1,"tag_info":{"color":"#0096FF","bg_color":"#0096FF33","text":"机器人"},"mobile_verified":true,"is_sys":false,"client_id":"QfZpMt5D8rL7XXXX","verified":false,"joined_at":1715870000000,"active_time":1715870000000},"user_id":"3109000000","allow":6144,"deny":0},{"user":{"id":"5274000000","username":"ChatMind","identify_num":"4800","online":true,"os":"Websocket","status":0,"avatar":"","vip_avatar":"","banner":"","nickname":"消息达人","roles":[3674972,9982012,5840173],"is_vip":false,"vip_amp":false,"bot":true,"nameplate":[],"bot_status":1,"tag_info":{"color":"#0096FF","bg_color":"#0096FF33","text":"机器人"},"mobile_verified":true,"is_sys":false,"client_id":"GmawmXKE4rSjXXXX","verified":false,"joined_at":1670000000000,"active_time":1670000000000},"user_id":"5274000000","allow":2048,"deny":0},{"user":{"id":"2485900000","username":"RubyFrost","identify_num":"1420","online":false,"os":"Websocket","status":1,"avatar":"","vip_avatar":"","banner":"","nickname":"Frosty","roles":[5840173,1265479,5749201,20475830],"is_vip":true,"vip_amp":true,"bot":false,"nameplate":[],"decorations_id_map":null,"mobile_verified":true,"is_sys":false,"joined_at":1655000000000,"active_time":1655000000000},"user_id":"2485900000","allow":2048,"deny":0}],"voice_quality":"1","server_url":"kookapp.cn","children":[],"region":"","sync_guild_region":0}"""
        val decoded = JSON.defaultJson().decodeFromString<DetailChannel>(rawText)
        assertEquals(GuildBotUser::class, decoded.permissionUsers[0].user::class)
        assertEquals(GuildUser::class, decoded.permissionUsers[2].user::class)
        println(JSON.defaultJson().encodeToString(decoded))
    }
}