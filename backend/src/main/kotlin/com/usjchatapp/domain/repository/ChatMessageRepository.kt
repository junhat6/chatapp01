package com.usjchatapp.domain.repository

import com.usjchatapp.domain.entity.ChatMessage
import com.usjchatapp.domain.entity.ChatMessageType
import java.time.LocalDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {

    // チャットルームのメッセージを時系列順に取得
    fun findByChatRoomIdOrderBySentAtAsc(chatRoomId: Long): List<ChatMessage>

    // チャットルームのメッセージをページング
    fun findByChatRoomIdOrderBySentAtDesc(chatRoomId: Long, pageable: Pageable): Page<ChatMessage>

    // 特定の時間以降のメッセージを取得
    fun findByChatRoomIdAndSentAtAfterOrderBySentAtAsc(
            chatRoomId: Long,
            since: LocalDateTime
    ): List<ChatMessage>

    // 特定のメッセージタイプのメッセージを取得
    fun findByChatRoomIdAndMessageTypeOrderBySentAtAsc(
            chatRoomId: Long,
            messageType: ChatMessageType
    ): List<ChatMessage>

    // ユーザーが送信したメッセージを取得
    fun findBySenderUserIdOrderBySentAtDesc(senderUserId: Long): List<ChatMessage>

    // 特定の期間のメッセージ数をカウント
    @Query(
            """
        SELECT COUNT(cm) FROM ChatMessage cm 
        WHERE cm.chatRoomId = :chatRoomId 
        AND cm.sentAt BETWEEN :startTime AND :endTime
    """
    )
    fun countByChatRoomIdAndTimePeriod(
            @Param("chatRoomId") chatRoomId: Long,
            @Param("startTime") startTime: LocalDateTime,
            @Param("endTime") endTime: LocalDateTime
    ): Long

    // 最新のメッセージを取得
    fun findTopByChatRoomIdOrderBySentAtDesc(chatRoomId: Long): ChatMessage?
}
