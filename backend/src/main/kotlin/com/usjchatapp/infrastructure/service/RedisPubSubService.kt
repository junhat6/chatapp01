package com.usjchatapp.infrastructure.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.usjchatapp.application.dto.*
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisPubSubService(
        private val redisTemplate: RedisTemplate<String, Any>,
        private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(RedisPubSubService::class.java)

    companion object {
        const val WAITING_ROOM_CHANNEL_PREFIX = "waiting-room-"
    }

    /** 待機室参加通知をRedisに送信 */
    fun publishJoinRoom(requestId: Long, joinMessage: JoinRoomMessage) {
        val channel = "$WAITING_ROOM_CHANNEL_PREFIX$requestId"
        val message = objectMapper.writeValueAsString(joinMessage)
        redisTemplate.convertAndSend(channel, message)
        logger.info("待機室参加通知を送信: requestId=$requestId, userId=${joinMessage.userId}")
    }

    /** 待機室退出通知をRedisに送信 */
    fun publishLeaveRoom(requestId: Long, leaveMessage: LeaveRoomMessage) {
        val channel = "$WAITING_ROOM_CHANNEL_PREFIX$requestId"
        val message = objectMapper.writeValueAsString(leaveMessage)
        redisTemplate.convertAndSend(channel, message)
        logger.info("待機室退出通知を送信: requestId=$requestId, userId=${leaveMessage.userId}")
    }

    /** 待機室確定通知をRedisに送信 */
    fun publishConfirmRoom(requestId: Long, confirmMessage: ConfirmRoomMessage) {
        val channel = "$WAITING_ROOM_CHANNEL_PREFIX$requestId"
        val message = objectMapper.writeValueAsString(confirmMessage)
        redisTemplate.convertAndSend(channel, message)
        logger.info("待機室確定通知を送信: requestId=$requestId")
    }

    /** 待機室解散通知をRedisに送信 */
    fun publishDisbandRoom(requestId: Long, disbandMessage: DisbandRoomMessage) {
        val channel = "$WAITING_ROOM_CHANNEL_PREFIX$requestId"
        val message = objectMapper.writeValueAsString(disbandMessage)
        redisTemplate.convertAndSend(channel, message)
        logger.info("待機室解散通知を送信: requestId=$requestId")
    }

    /** 待機室状態更新通知をRedisに送信 */
    fun publishRoomStateUpdate(requestId: Long, stateUpdate: RoomStateUpdateMessage) {
        val channel = "$WAITING_ROOM_CHANNEL_PREFIX$requestId"
        val message = objectMapper.writeValueAsString(stateUpdate)
        redisTemplate.convertAndSend(channel, message)
        logger.info("待機室状態更新通知を送信: requestId=$requestId")
    }
}
