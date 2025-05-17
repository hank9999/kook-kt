package com.github.hank9999.kook.gateway
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SignalType.SignalTypeSerializer::class)
enum class SignalType(val code: Int) {
    /** server->client 消息(包含聊天和通知消息) */
    SERVER_MESSAGE(0),
    /** server->client 客户端连接 ws 时, 服务端返回握手结果 */
    SERVER_HANDSHAKE_ACK(1),
    /** client->server 客户端发送心跳包 ping */
    CLIENT_PING(2),
    /** server->client 服务端响应心跳包 pong */
    SERVER_PONG(3),
    /** client->server 客户端请求恢复会话 */
    CLIENT_REQUEST_RESUME(4),
    /** server->client 服务端要求客户端断开当前连接并重新连接 */
    SERVER_REQUEST_RECONNECT(5),
    /** server->client 服务端确认会话恢复请求 */
    SERVER_RESUME_ACK(6),
    /** 未知 */
    UNKNOWN(-1);

    companion object {
        fun fromCode(value: Int): SignalType = entries.firstOrNull { it.code == value } ?: UNKNOWN
    }

    internal object SignalTypeSerializer : KSerializer<SignalType> {
        override val descriptor = PrimitiveSerialDescriptor("com.github.hank9999.kook.gateway.SignalType", PrimitiveKind.INT)
        override fun serialize(encoder: Encoder, value: SignalType) = encoder.encodeInt(value.code)
        override fun deserialize(decoder: Decoder): SignalType = SignalType.fromCode(decoder.decodeInt())
    }
}