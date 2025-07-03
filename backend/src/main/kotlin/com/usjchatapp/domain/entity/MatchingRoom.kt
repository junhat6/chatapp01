package com.usjchatapp.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "matching_rooms")
data class MatchingRoom(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
        @Column(name = "matching_request_id", nullable = false, unique = true)
        val matchingRequestId: Long,
        @Column(name = "participant_user_ids")
        @JdbcTypeCode(SqlTypes.ARRAY)
        val participantUserIds: Array<Long> = arrayOf(),
        @Enumerated(EnumType.STRING) val status: MatchingRoomStatus = MatchingRoomStatus.WAITING,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now(),

// 関連データは必要に応じてサービス層で取得
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatchingRoom

        if (id != other.id) return false
        if (matchingRequestId != other.matchingRequestId) return false
        if (!participantUserIds.contentEquals(other.participantUserIds)) return false
        if (status != other.status) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + matchingRequestId.hashCode()
        result = 31 * result + participantUserIds.contentHashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}

enum class MatchingRoomStatus {
    WAITING, // 待機中
    CONFIRMED, // 確定済み
    DISBANDED // 解散
}
