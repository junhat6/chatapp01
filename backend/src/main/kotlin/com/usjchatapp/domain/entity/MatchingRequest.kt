package com.usjchatapp.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "matching_requests")
data class MatchingRequest(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
        @Column(name = "host_user_id", nullable = false) val hostUserId: Long,
        @Column(nullable = false) val attraction: String,
        @Column(name = "preferred_date_time", nullable = false)
        val preferredDateTime: LocalDateTime,
        @Column(name = "max_participants", nullable = false) val maxParticipants: Int = 4,
        @Column(columnDefinition = "TEXT") val description: String? = null,
        @Enumerated(EnumType.STRING) val status: MatchingRequestStatus = MatchingRequestStatus.OPEN,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now(),

        // リレーション
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "host_user_id", insertable = false, updatable = false)
        val hostUser: User? = null,
// 関連データは必要に応じてサービス層で取得
)

enum class MatchingRequestStatus {
    OPEN, // 募集中
    WAITING, // 待機中（参加者が集まっている状態）
    CONFIRMED, // 確定済み（チャットルーム作成済み）
    CLOSED, // 終了
    EXPIRED // 期限切れ
}
