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

        // アクティブな募集を取得（OPEN, WAITING）- 論理削除されていない募集のみ
        fun findByStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(
                status: List<MatchingRequestStatus>
        ): List<MatchingRequest>

        // 特定のアトラクションの募集を取得 - 論理削除されていない募集のみ
        fun findByAttractionAndStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(
                attraction: String,
                status: List<MatchingRequestStatus>
        ): List<MatchingRequest>

        // ホストユーザーの募集を取得 - 論理削除されていない募集のみ
        fun findByHostUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                hostUserId: Long
        ): List<MatchingRequest>

        // 指定日時以降の募集を取得 - 論理削除されていない募集のみ
        fun findByPreferredDateTimeAfterAndStatusInAndDeletedAtIsNullOrderByPreferredDateTimeAsc(
                dateTime: LocalDateTime,
                status: List<MatchingRequestStatus>
        ): List<MatchingRequest>

        // 期限切れの募集を検索 - 論理削除されていない募集のみ
        fun findByPreferredDateTimeBeforeAndStatusInAndDeletedAtIsNull(
                dateTime: LocalDateTime,
                status: List<MatchingRequestStatus>
        ): List<MatchingRequest>

        // アトラクションと日時範囲での検索 - 論理削除されていない募集のみ
        @Query(
                """
        SELECT mr FROM MatchingRequest mr 
        WHERE mr.attraction = :attraction 
        AND mr.preferredDateTime BETWEEN :startDateTime AND :endDateTime 
        AND mr.status IN :statuses
        AND mr.deletedAt IS NULL
        ORDER BY mr.preferredDateTime ASC
    """
        )
        fun findByAttractionAndDateTimeRange(
                @Param("attraction") attraction: String,
                @Param("startDateTime") startDateTime: LocalDateTime,
                @Param("endDateTime") endDateTime: LocalDateTime,
                @Param("statuses") statuses: List<MatchingRequestStatus>
        ): List<MatchingRequest>

        // 論理削除対象の募集を検索（期限切れから24時間経過した募集）
        @Query(
                """
        SELECT mr FROM MatchingRequest mr 
        WHERE mr.preferredDateTime < :expiredThreshold
        AND mr.deletedAt IS NULL
        AND mr.status IN :expiredStatuses
    """
        )
        fun findExpiredRequestsForDeletion(
                @Param("expiredThreshold") expiredThreshold: LocalDateTime,
                @Param("expiredStatuses") expiredStatuses: List<MatchingRequestStatus>
        ): List<MatchingRequest>

        // 論理削除実行
        @Query(
                """
        UPDATE MatchingRequest mr 
        SET mr.deletedAt = :deletedAt, mr.updatedAt = :updatedAt
        WHERE mr.id IN :ids
    """
        )
        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        fun softDeleteByIds(
                @Param("ids") ids: List<Long>,
                @Param("deletedAt") deletedAt: LocalDateTime,
                @Param("updatedAt") updatedAt: LocalDateTime
        ): Int
}
