package com.usjchatapp.domain.repository

import com.usjchatapp.domain.entity.MatchingApplication
import com.usjchatapp.domain.entity.MatchingApplicationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MatchingApplicationRepository : JpaRepository<MatchingApplication, Long> {

    // 特定の募集への応募を取得
    fun findByMatchingRequestIdOrderByAppliedAtAsc(
            matchingRequestId: Long
    ): List<MatchingApplication>

    // 特定の応募者の応募を取得
    fun findByApplicantUserIdOrderByAppliedAtDesc(applicantUserId: Long): List<MatchingApplication>

    // 特定の募集への特定ユーザーの応募を確認
    fun findByMatchingRequestIdAndApplicantUserId(
            matchingRequestId: Long,
            applicantUserId: Long
    ): MatchingApplication?

    // 特定の募集の応募数を取得
    fun countByMatchingRequestIdAndStatus(
            matchingRequestId: Long,
            status: MatchingApplicationStatus
    ): Int

    // 特定のステータスの応募を取得
    fun findByMatchingRequestIdAndStatusOrderByAppliedAtAsc(
            matchingRequestId: Long,
            status: MatchingApplicationStatus
    ): List<MatchingApplication>

    // ユーザーが既に応募済みかチェック
    fun existsByMatchingRequestIdAndApplicantUserId(
            matchingRequestId: Long,
            applicantUserId: Long
    ): Boolean

    // 特定ユーザーの特定ステータスの応募数
    @Query(
            """
        SELECT COUNT(ma) FROM MatchingApplication ma 
        WHERE ma.applicantUserId = :userId 
        AND ma.status = :status
    """
    )
    fun countByUserIdAndStatus(
            @Param("userId") userId: Long,
            @Param("status") status: MatchingApplicationStatus
    ): Int
}
