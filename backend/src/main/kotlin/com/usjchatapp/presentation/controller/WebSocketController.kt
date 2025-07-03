package com.usjchatapp.presentation.controller

import com.usjchatapp.application.dto.*
import com.usjchatapp.application.service.MatchingRoomService
import com.usjchatapp.domain.entity.MatchingRoomStatus
import com.usjchatapp.infrastructure.service.RedisPubSubService
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller

/** WebSocket接続を介した待機室のリアルタイム通信を処理するコントローラー STOMP over WebSocketプロトコルを使用してリアルタイム更新を実現 */
@Controller
class WebSocketController(
        private val matchingRoomService: MatchingRoomService,
        private val redisPubSubService: RedisPubSubService
) {

    private val logger = LoggerFactory.getLogger(WebSocketController::class.java)

    companion object {
        private const val SUCCESS_RESPONSE = "SUCCESS"
        private const val ERROR_PREFIX = "ERROR: "
        private val DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    /** 待機室参加処理 クライアントが /app/rooms/{requestId}/join にメッセージを送信した際の処理 */
    @MessageMapping("/rooms/{requestId}/join")
    fun joinRoom(
            @DestinationVariable requestId: Long,
            @Payload joinRequest: JoinRoomMessage,
            headerAccessor: SimpMessageHeaderAccessor
    ): String {
        return executeWithErrorHandling("joinRoom", requestId, joinRequest.userId) {
            logger.info("WebSocket待機室参加開始: requestId=$requestId, userId=${joinRequest.userId}")

            // 待機室に参加
            val room = matchingRoomService.joinMatchingRoom(requestId, joinRequest.userId)

            // Redisに参加通知を送信
            redisPubSubService.publishJoinRoom(requestId, joinRequest)

            // 状態更新通知を送信
            publishRoomStateUpdate(requestId, room)

            logger.info("WebSocket待機室参加完了: requestId=$requestId, userId=${joinRequest.userId}")
            SUCCESS_RESPONSE
        }
    }

    /** 待機室退出処理 クライアントが /app/rooms/{requestId}/leave にメッセージを送信した際の処理 */
    @MessageMapping("/rooms/{requestId}/leave")
    fun leaveRoom(
            @DestinationVariable requestId: Long,
            @Payload leaveRequest: LeaveRoomMessage,
            headerAccessor: SimpMessageHeaderAccessor
    ): String {
        return executeWithErrorHandling("leaveRoom", requestId, leaveRequest.userId) {
            logger.info("WebSocket待機室退出開始: requestId=$requestId, userId=${leaveRequest.userId}")

            // 待機室から退出
            val room = matchingRoomService.leaveMatchingRoom(requestId, leaveRequest.userId)

            // Redisに退出通知を送信
            redisPubSubService.publishLeaveRoom(requestId, leaveRequest)

            // 状態更新通知を送信
            publishRoomStateUpdate(requestId, room)

            logger.info("WebSocket待機室退出完了: requestId=$requestId, userId=${leaveRequest.userId}")
            SUCCESS_RESPONSE
        }
    }

    /** 待機室確定処理 ホストが待機室を確定した際の処理 */
    @MessageMapping("/rooms/{requestId}/confirm")
    fun confirmRoom(
            @DestinationVariable requestId: Long,
            @Payload confirmRequest: ConfirmRoomMessage,
            headerAccessor: SimpMessageHeaderAccessor
    ): String {
        return executeWithErrorHandling("confirmRoom", requestId, confirmRequest.confirmedBy) {
            logger.info(
                    "WebSocket待機室確定開始: requestId=$requestId, confirmedBy=${confirmRequest.confirmedBy}"
            )

            // 待機室を確定
            val room =
                    matchingRoomService.confirmMatchingRoom(requestId, confirmRequest.confirmedBy)

            // Redisに確定通知を送信
            redisPubSubService.publishConfirmRoom(requestId, confirmRequest)

            // 状態更新通知を送信
            publishRoomStateUpdate(requestId, room)

            logger.info("WebSocket待機室確定完了: requestId=$requestId")
            SUCCESS_RESPONSE
        }
    }

    /** 待機室解散処理 ホストが待機室を解散した際の処理 */
    @MessageMapping("/rooms/{requestId}/disband")
    fun disbandRoom(
            @DestinationVariable requestId: Long,
            @Payload disbandRequest: DisbandRoomMessage,
            headerAccessor: SimpMessageHeaderAccessor
    ): String {
        return executeWithErrorHandling("disbandRoom", requestId, disbandRequest.disbandedBy) {
            logger.info(
                    "WebSocket待機室解散開始: requestId=$requestId, disbandedBy=${disbandRequest.disbandedBy}"
            )

            // 待機室を解散
            val room =
                    matchingRoomService.disbandMatchingRoom(requestId, disbandRequest.disbandedBy)

            // Redisに解散通知を送信
            redisPubSubService.publishDisbandRoom(requestId, disbandRequest)

            // 状態更新通知を送信
            publishRoomStateUpdate(requestId, room)

            logger.info("WebSocket待機室解散完了: requestId=$requestId")
            SUCCESS_RESPONSE
        }
    }

    // === Private Helper Methods ===

    /** エラーハンドリングを統一化したラッパーメソッド */
    private fun executeWithErrorHandling(
            operation: String,
            requestId: Long,
            userId: Long,
            action: () -> String
    ): String {
        return try {
            action()
        } catch (e: IllegalArgumentException) {
            logger.warn(
                    "WebSocket $operation 業務エラー: requestId=$requestId, userId=$userId, error=${e.message}"
            )
            "$ERROR_PREFIX${e.message}"
        } catch (e: Exception) {
            logger.error("WebSocket $operation システムエラー: requestId=$requestId, userId=$userId", e)
            "${ERROR_PREFIX}システムエラーが発生しました"
        }
    }

    /** 待機室の状態更新通知を送信 */
    private fun publishRoomStateUpdate(requestId: Long, room: MatchingRoomDto) {
        try {
            // 参加者情報を構築
            val participants = buildParticipantInfoList(room)

            // 募集情報から最大参加者数を取得
            val maxParticipants = getMaxParticipants(requestId)

            // 状態更新メッセージを構築
            val stateUpdate =
                    RoomStateUpdateMessage(
                            requestId = requestId,
                            participants = participants,
                            currentCount = participants.size,
                            maxParticipants = maxParticipants,
                            isConfirmed = room.status == MatchingRoomStatus.CONFIRMED
                    )

            // Redis経由で状態更新を通知
            redisPubSubService.publishRoomStateUpdate(requestId, stateUpdate)

            logger.debug("待機室状態更新通知送信: requestId=$requestId, participantCount=${participants.size}")
        } catch (e: Exception) {
            logger.error("待機室状態更新通知エラー: requestId=$requestId", e)
        }
    }

    /** ParticipantInfo リストを構築 */
    private fun buildParticipantInfoList(room: MatchingRoomDto): List<ParticipantInfo> {
        return room.participants.map { participant ->
            ParticipantInfo(
                    userId = participant.userId,
                    displayName = participant.displayName,
                    isHost = participant.isHost,
                    joinedAt = room.createdAt.format(DATE_TIME_FORMAT)
            )
        }
    }

    /** 募集情報から最大参加者数を取得 */
    private fun getMaxParticipants(requestId: Long): Int {
        return try {
            matchingRoomService.getMatchingRequest(requestId)?.maxParticipants ?: 4
        } catch (e: Exception) {
            logger.warn("最大参加者数取得エラー: requestId=$requestId, デフォルト値4を使用", e)
            4
        }
    }
}
