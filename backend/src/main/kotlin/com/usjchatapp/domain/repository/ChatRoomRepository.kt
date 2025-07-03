package com.usjchatapp.domain.repository

import com.usjchatapp.domain.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {

    // 募集IDでチャットルームを取得
    fun findByMatchingRequestId(matchingRequestId: Long): ChatRoom?

    // ユーザーが参加しているチャットルームを取得
    @Query(
            value = "SELECT * FROM chat_rooms cr WHERE :userId = ANY(cr.participant_user_ids)",
            nativeQuery = true
    )
    fun findByParticipantUserId(@Param("userId") userId: Long): List<ChatRoom>

    // チャットルーム名で検索
    fun findByNameContainingOrderByCreatedAtDesc(name: String): List<ChatRoom>

    // 最近作成されたチャットルームを取得
    fun findAllByOrderByCreatedAtDesc(): List<ChatRoom>

    // ユーザーが特定のチャットルームに参加しているかチェック
    @Query(
            value =
                    "SELECT COUNT(*) > 0 FROM chat_rooms WHERE id = :chatRoomId AND :userId = ANY(participant_user_ids)",
            nativeQuery = true
    )
    fun isUserParticipant(
            @Param("chatRoomId") chatRoomId: Long,
            @Param("userId") userId: Long
    ): Boolean
}
