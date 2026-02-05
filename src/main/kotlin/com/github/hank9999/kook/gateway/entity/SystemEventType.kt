package com.github.hank9999.kook.gateway.entity

enum class SystemEventType(val value: String) {
    // 消息反应
    ADDED_REACTION("added_reaction"),
    DELETED_REACTION("deleted_reaction"),

    // 消息操作
    UPDATED_MESSAGE("updated_message"),
    EMBEDS_APPEND("embeds_append"),
    DELETED_MESSAGE("deleted_message"),
    PINNED_MESSAGE("pinned_message"),
    UNPINNED_MESSAGE("unpinned_message"),

    // 频道
    ADDED_CHANNEL("added_channel"),
    UPDATED_CHANNEL("updated_channel"),
    UPDATED_SERVER_TYPE("updated_server_type"),
    SORT_CHANNEL("sort_channel"),
    DELETED_CHANNEL("deleted_channel"),
    BATCH_ADDED_CHANNEL("batch_added_channel"),
    BATCH_UPDATED_CHANNEL("batch_updated_channel"),
    BATCH_DELETED_CHANNEL("batch_deleted_channel"),

    // 服务器成员
    JOINED_GUILD("joined_guild"),
    EXITED_GUILD("exited_guild"),
    UPDATED_GUILD_MEMBER("updated_guild_member"),
    GUILD_MEMBER_ONLINE("guild_member_online"),
    GUILD_MEMBER_OFFLINE("guild_member_offline"),

    // 服务器
    UPDATED_GUILD("updated_guild"),
    DELETED_GUILD("deleted_guild"),

    // 角色
    ADDED_ROLE("added_role"),
    DELETED_ROLE("deleted_role"),
    UPDATED_ROLE("updated_role"),

    // 封禁
    ADDED_BLOCK_LIST("added_block_list"),
    DELETED_BLOCK_LIST("deleted_block_list"),

    // 表情
    ADDED_EMOJI("added_emoji"),
    REMOVED_EMOJI("removed_emoji"),
    DELETED_EMOJI("deleted_emoji"),
    UPDATED_EMOJI("updated_emoji"),

    // 语音
    JOINED_CHANNEL("joined_channel"),
    EXITED_CHANNEL("exited_channel"),
    LIVE_STATUS_CHANGED("live_status_changed"),
    ADD_GUILD_MUTE("add_guild_mute"),
    DELETE_GUILD_MUTE("delete_guild_mute"),

    // 私聊
    UPDATED_PRIVATE_MESSAGE("updated_private_message"),
    DELETED_PRIVATE_MESSAGE("deleted_private_message"),
    PRIVATE_ADDED_REACTION("private_added_reaction"),
    PRIVATE_DELETED_REACTION("private_deleted_reaction"),

    // 用户
    USER_UPDATED("user_updated"),
    SELF_JOINED_GUILD("self_joined_guild"),
    SELF_EXITED_GUILD("self_exited_guild"),

    // 交互
    MESSAGE_BTN_CLICK("message_btn_click"),

    UNKNOWN("unknown");

    companion object {
        fun fromValue(value: String): SystemEventType =
            entries.firstOrNull { it.value == value } ?: UNKNOWN
    }
}
