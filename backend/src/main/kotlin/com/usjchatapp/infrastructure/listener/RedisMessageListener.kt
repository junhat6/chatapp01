package com.usjchatapp.infrastructure.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class RedisMessageListener(
        private val messagingTemplate: SimpMessagingTemplate,
        private val objectMapper: ObjectMapper
) : MessageListener {

    private val logger = LoggerFactory.getLogger(RedisMessageListener::class.java)

    companion object {
        const val WAITING_ROOM_CHANNEL_PREFIX = "waiting-room-"
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            val channel = String(message.channel)
            val messageBody = String(message.body)

            // チャンネル名からrequestIdを抽出
            val requestId = extractRequestIdFromChannel(channel)
            if (requestId != null) {
                handleRedisMessage(messageBody, requestId)
            }

            logger.debug("Redisメッセージを受信: channel=$channel, message=$messageBody")
        } catch (e: Exception) {
            logger.error("Redisメッセージ処理エラー", e)
        }
    }

    /** Redisからのメッセージを受信してWebSocketで配信 */
    private fun handleRedisMessage(message: String, requestId: Long) {
        try {
            // メッセージタイプを判定
            val messageType =
                    when {
                        message.contains("\"userId\"") &&
                                message.contains("\"userDisplayName\"") -> {
                            if (message.contains("\"isHost\"")) "STATE_UPDATE"
                            else "PARTICIPANT_ACTION"
                        }
                        message.contains("\"confirmedBy\"") -> "CONFIRM"
                        message.contains("\"disbandedBy\"") -> "DISBAND"
                        else -> "UNKNOWN"
                    }

            // WebSocketでクライアントに配信
            val topic = "/topic/rooms/$requestId"
            messagingTemplate.convertAndSend(topic, message)

            logger.info("WebSocketでメッセージを配信: topic=$topic, type=$messageType")
        } catch (e: Exception) {
            logger.error("Redisメッセージ処理エラー: $message", e)
        }
    }

    private fun extractRequestIdFromChannel(channel: String): Long? {
        return try {
            if (channel.startsWith(WAITING_ROOM_CHANNEL_PREFIX)) {
                val requestIdStr = channel.substring(WAITING_ROOM_CHANNEL_PREFIX.length)
                requestIdStr.toLong()
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            logger.warn("無効なチャンネル名: $channel")
            null
        }
    }
}
