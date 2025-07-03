package com.usjchatapp.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "matching_applications")
data class MatchingApplication(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
        @Column(name = "matching_request_id", nullable = false) val matchingRequestId: Long,
        @Column(name = "applicant_user_id", nullable = false) val applicantUserId: Long,
        @Column(columnDefinition = "TEXT") val message: String? = null,
        @Enumerated(EnumType.STRING)
        val status: MatchingApplicationStatus = MatchingApplicationStatus.PENDING,
        @Column(name = "applied_at", nullable = false)
        val appliedAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now(),

        // リレーション
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "matching_request_id", insertable = false, updatable = false)
        val matchingRequest: MatchingRequest? = null,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "applicant_user_id", insertable = false, updatable = false)
        val applicantUser: User? = null
)

enum class MatchingApplicationStatus {
    PENDING, // 保留中
    ACCEPTED, // 承認済み
    REJECTED // 拒否済み
}
