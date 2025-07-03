package com.usjchatapp.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_messages")
data class ChatMessage(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
        @Column(name = "chat_room_id", nullable = false) val chatRoomId: Long,
        @Column(name = "sender_user_id", nullable = false) val senderUserId: Long,
        @Column(columnDefinition = "TEXT", nullable = false) val content: String,
        @Enumerated(EnumType.STRING) val messageType: ChatMessageType = ChatMessageType.TEXT,
        @Column(name = "sent_at", nullable = false) val sentAt: LocalDateTime = LocalDateTime.now(),

        // リレーション
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "chat_room_id", insertable = false, updatable = false)
        val chatRoom: ChatRoom? = null,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sender_user_id", insertable = false, updatable = false)
        val senderUser: User? = null
)

enum class ChatMessageType {
    TEXT, // テキストメッセージ
    SYSTEM, // システムメッセージ（入室・退室など）
    IMAGE, // 画像（将来的な拡張用）
    LOCATION // 位置情報（将来的な拡張用）
}
