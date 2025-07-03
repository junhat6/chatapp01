package com.usjchatapp.domain.repository

import com.usjchatapp.domain.entity.MatchingRequest
import com.usjchatapp.domain.entity.MatchingRequestStatus
import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MatchingRequestRepository : JpaRepository<MatchingRequest, Long> {

    // アクティブな募集を取得（OPEN, WAITING）
    fun findByStatusInOrderByCreatedAtDesc(
            status: List<MatchingRequestStatus>
    ): List<MatchingRequest>

    // 特定のアトラクションの募集を取得
    fun findByAttractionAndStatusInOrderByCreatedAtDesc(
            attraction: String,
            status: List<MatchingRequestStatus>
    ): List<MatchingRequest>

    // ホストユーザーの募集を取得
    fun findByHostUserIdOrderByCreatedAtDesc(hostUserId: Long): List<MatchingRequest>

    // 指定日時以降の募集を取得
    fun findByPreferredDateTimeAfterAndStatusInOrderByPreferredDateTimeAsc(
            dateTime: LocalDateTime,
            status: List<MatchingRequestStatus>
    ): List<MatchingRequest>

    // 期限切れの募集を検索
    fun findByPreferredDateTimeBeforeAndStatusIn(
            dateTime: LocalDateTime,
            status: List<MatchingRequestStatus>
    ): List<MatchingRequest>

    // アトラクションと日時範囲での検索
    @Query(
            """
        SELECT mr FROM MatchingRequest mr 
        WHERE mr.attraction = :attraction 
        AND mr.preferredDateTime BETWEEN :startDateTime AND :endDateTime 
        AND mr.status IN :statuses
        ORDER BY mr.preferredDateTime ASC
    """
    )
    fun findByAttractionAndDateTimeRange(
            @Param("attraction") attraction: String,
            @Param("startDateTime") startDateTime: LocalDateTime,
            @Param("endDateTime") endDateTime: LocalDateTime,
            @Param("statuses") statuses: List<MatchingRequestStatus>
    ): List<MatchingRequest>
}
