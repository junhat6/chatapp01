package com.usjchatapp.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "chat_rooms")
data class ChatRoom(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
        @Column(nullable = false) val name: String,
        @Column(name = "matching_request_id", nullable = false, unique = true)
        val matchingRequestId: Long,
        @Column(name = "participant_user_ids")
        @JdbcTypeCode(SqlTypes.ARRAY)
        val participantUserIds: Array<Long> = arrayOf(),
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now(),

        // リレーション
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "matching_request_id", insertable = false, updatable = false)
        val matchingRequest: MatchingRequest? = null,
        @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
        val messages: List<ChatMessage> = listOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatRoom

        if (id != other.id) return false
        if (name != other.name) return false
        if (matchingRequestId != other.matchingRequestId) return false
        if (!participantUserIds.contentEquals(other.participantUserIds)) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + matchingRequestId.hashCode()
        result = 31 * result + participantUserIds.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}
