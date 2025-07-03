package com.usjchatapp.domain.repository

import com.usjchatapp.domain.entity.MatchingRoom
import com.usjchatapp.domain.entity.MatchingRoomStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MatchingRoomRepository : JpaRepository<MatchingRoom, Long> {

    // 募集IDで待機室を取得
    fun findByMatchingRequestId(matchingRequestId: Long): MatchingRoom?

    // 特定ステータスの待機室を取得
    fun findByStatusOrderByCreatedAtDesc(status: MatchingRoomStatus): List<MatchingRoom>

    // ユーザーが参加している待機室を検索
    @Query(
            value = "SELECT * FROM matching_rooms mr WHERE :userId = ANY(mr.participant_user_ids)",
            nativeQuery = true
    )
    fun findByParticipantUserId(@Param("userId") userId: Long): List<MatchingRoom>

    // アクティブな待機室を検索（WAITING状態）
    fun findByStatusAndMatchingRequestIdIn(
            status: MatchingRoomStatus,
            matchingRequestIds: List<Long>
    ): List<MatchingRoom>

    // 参加者数によるフィルタリング
    @Query(
            value =
                    "SELECT * FROM matching_rooms WHERE array_length(participant_user_ids, 1) >= :minParticipants",
            nativeQuery = true
    )
    fun findByMinimumParticipants(
            @Param("minParticipants") minParticipants: Int
    ): List<MatchingRoom>
}
