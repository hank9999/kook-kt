package com.github.hank9999.kook.http.types.apiResponse.directMessage

import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.types.Quote
import com.github.hank9999.kook.types.Reaction
import com.github.hank9999.kook.types.kmd.MentionInfo
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement


@Serializable
data class DirectMessageList(
    @SerialName("id") val id: String = "",
    @SerialName("type") val type: MessageTypes = MessageTypes.KMD,
    @SerialName("content") val content: String = "",

    // TODO("嵌入解析待做")
    @SerialName("embeds") val embeds: JsonElement = json.parseToJsonElement("{}"),

    // TODO("KOOK返回类型不确定")
    @SerialName("attachments") val attachments: JsonElement = json.parseToJsonElement("{}"),

    @SerialName("create_at") val createAt: Long = 0,
    @SerialName("updated_at") val updatedAt: Int = 0,
    @SerialName("reactions") val reactions: List<Reaction> = emptyList(),
    @SerialName("author_id") val authorId: String = "0",
    @SerialName("image_name") val imageName: String = "",
    @SerialName("read_status") val readStatus: Boolean = false,
    @SerialName("quote") val quote: Quote = Quote(),
    @SerialName("mention_info") val mentionInfo: MentionInfo = MentionInfo(),
    @SerialName("from_type") val fromType: Int = 0,
    @SerialName("msg_icon") val msgIcon: String = "",
)