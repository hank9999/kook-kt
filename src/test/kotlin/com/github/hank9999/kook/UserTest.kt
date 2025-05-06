package com.github.hank9999.kook;

import com.github.hank9999.kook.common.entity.Optional
import com.github.hank9999.kook.common.entity.user.GuildUser
import com.github.hank9999.kook.common.entity.user.TagInfo
import com.github.hank9999.kook.common.utils.JSON
import kotlin.test.Test
import kotlin.test.assertEquals

class UserTest {
    @Test
    fun guildUserTest() {
        val json = JSON.defaultJson()
        val guildUserListDecodedTestText = """[{"id":"1234","username":"1234","identify_num":"1234","online":false,"os":"Websocket","status":0,"avatar":"https://img.kookapp.cn/","vip_avatar":"https://img.kookapp.cn/","banner":"","nickname":"1234","roles":[1234,5678],"is_vip":false,"vip_amp":false,"bot":true,"nameplate":[],"bot_status":1,"tag_info":{"color":"#1","bg_color":"#1","text":"机器人"},"mobile_verified":true,"is_sys":false,"client_id":"11111111","verified":false,"joined_at":1710000000000,"active_time":1710000000000},{"id":"5678","username":"5678","identify_num":"5678","online":false,"os":"Websocket","status":1,"avatar":"https://img.kookapp.cn/","vip_avatar":"https://img.kookapp.cn/","banner":"https://img.kookapp.cn/","nickname":"5678","roles":[7890],"is_vip":true,"vip_amp":true,"bot":false,"nameplate":[],"decorations_id_map":null,"mobile_verified":true,"is_sys":false,"joined_at":1710000000000,"active_time":1710000000000}]"""
        val guildUserListEncodedTestText = """[{"id":"1234","username":"1234","nickname":"1234","identify_num":"1234","online":false,"os":"Websocket","status":0,"avatar":"https://img.kookapp.cn/","vip_avatar":"https://img.kookapp.cn/","banner":"","is_vip":false,"vip_amp":false,"bot":true,"roles":[1234,5678],"is_sys":false,"mobile_verified":true,"joined_at":1710000000000,"active_time":1710000000000,"bot_status":1,"tag_info":{"color":"#1","bg_color":"#1","text":"机器人"},"client_id":"11111111","verified":false},{"id":"5678","username":"5678","nickname":"5678","identify_num":"5678","online":false,"os":"Websocket","status":1,"avatar":"https://img.kookapp.cn/","vip_avatar":"https://img.kookapp.cn/","banner":"https://img.kookapp.cn/","is_vip":true,"vip_amp":true,"bot":false,"roles":[7890],"is_sys":false,"mobile_verified":true,"joined_at":1710000000000,"active_time":1710000000000}]"""
        val guildUserList = json.decodeFromString<List<GuildUser>>(guildUserListDecodedTestText)
        val encoded = json.encodeToString(guildUserList)
        assertEquals(2, guildUserList.size)
        assertEquals(GuildUser(
            id="1234", username="1234", nickname="1234", identifyNum="1234", online=false, os="Websocket",
            status=0, avatar="https://img.kookapp.cn/", vipAvatar="https://img.kookapp.cn/", banner="", isVip=false,
            vipAmp=false, bot=true, roles=listOf(1234, 5678), isSys=false, mobileVerified=true,
            joinedAt=1710000000000, activeTime=1710000000000, botStatus=Optional.Value(1),
            tagInfo=Optional.Value(TagInfo(color = "#1", bgColor = "#1", text = "机器人")),
            clientId=Optional.Value("11111111"), verified=Optional.Value(false)
        ), guildUserList[0])
        assertEquals(GuildUser(
            id="5678", username="5678", nickname="5678", identifyNum="5678", online=false, os="Websocket",
            status=1, avatar="https://img.kookapp.cn/", vipAvatar="https://img.kookapp.cn/", banner="https://img.kookapp.cn/", isVip=true,
            vipAmp=true, bot=false, roles=listOf(7890), isSys=false, mobileVerified=true,
            joinedAt=1710000000000, activeTime=1710000000000, botStatus=Optional.Missing(),
            tagInfo=Optional.Missing(), clientId=Optional.Missing(), verified=Optional.Missing()
        ), guildUserList[1])
        assertEquals(guildUserListEncodedTestText, encoded)
    }
}