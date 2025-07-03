package com.usjchatapp.application.dto

import com.usjchatapp.domain.entity.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

// MatchingRequest関連
data class MatchingRequestDto(
        val id: Long,
        val hostUserId: Long,
        val hostDisplayName: String,
        val attraction: String,
        val preferredDateTime: LocalDateTime,
        val maxParticipants: Int,
        val description: String?,
        val status: MatchingRequestStatus,
        val currentApplications: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
)

data class CreateMatchingRequestRequest(
        @field:NotBlank(message = "アトラクションは必須です") val attraction: String,
        @field:Future(message = "希望時間は現在時刻より後に設定してください") val preferredDateTime: LocalDateTime,
        @field:Min(value = 2, message = "最大参加者数は2人以上で設定してください")
        @field:Max(value = 8, message = "最大参加者数は8人以下で設定してください")
        val maxParticipants: Int,
        @field:Size(max = 500, message = "説明は500文字以内で入力してください") val description: String?
)

data class UpdateMatchingRequestRequest(
        @field:Size(max = 500, message = "説明は500文字以内で入力してください") val description: String?,
        @field:Future(message = "希望時間は現在時刻より後に設定してください") val preferredDateTime: LocalDateTime?,
        @field:Min(value = 2, message = "最大参加者数は2人以上で設定してください")
        @field:Max(value = 8, message = "最大参加者数は8人以下で設定してください")
        val maxParticipants: Int?
)

data class MatchingRequestSearchRequest(
        val attraction: String?,
        val dateFrom: LocalDateTime?,
        val dateTo: LocalDateTime?,
        val maxParticipants: Int?,
        val status: MatchingRequestStatus?
)

// MatchingApplication関連
data class MatchingApplicationDto(
        val id: Long,
        val matchingRequestId: Long,
        val applicantUserId: Long,
        val applicantDisplayName: String,
        val message: String?,
        val status: MatchingApplicationStatus,
        val appliedAt: LocalDateTime,
        val updatedAt: LocalDateTime
)

data class CreateMatchingApplicationRequest(
        @field:Size(max = 200, message = "メッセージは200文字以内で入力してください") val message: String?
)

data class UpdateApplicationStatusRequest(
        @field:NotNull(message = "ステータスは必須です") val status: MatchingApplicationStatus
)

// MatchingRoom関連
data class MatchingRoomDto(
        val id: Long,
        val matchingRequestId: Long,
        val participants: List<ParticipantDto>,
        val status: MatchingRoomStatus,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
)

data class ParticipantDto(
        val userId: Long,
        val displayName: String,
        val profileImage: String?,
        val isHost: Boolean
)

// ChatRoom関連
data class ChatRoomDto(
        val id: Long,
        val name: String,
        val matchingRequestId: Long,
        val participants: List<ParticipantDto>,
        val lastMessage: ChatMessageDto?,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
)

data class CreateChatRoomRequest(
        @field:NotBlank(message = "チャットルーム名は必須です")
        @field:Size(min = 1, max = 100, message = "チャットルーム名は1文字以上100文字以下で入力してください")
        val name: String,
        @field:NotNull(message = "募集IDは必須です") val matchingRequestId: Long,
        @field:NotEmpty(message = "参加者は最低1人必要です") val participantUserIds: List<Long>
)

// ChatMessage関連
data class ChatMessageDto(
        val id: Long,
        val chatRoomId: Long,
        val senderUserId: Long,
        val senderDisplayName: String,
        val senderProfileImage: String?,
        val content: String,
        val messageType: ChatMessageType,
        val sentAt: LocalDateTime
)

data class SendMessageRequest(
        @field:NotBlank(message = "メッセージ内容は必須です")
        @field:Size(min = 1, max = 1000, message = "メッセージは1文字以上1000文字以下で入力してください")
        val content: String,
        val messageType: ChatMessageType = ChatMessageType.TEXT
)

// マッチング概要
data class MatchingSummaryDto(
        val totalRequests: Int,
        val myRequests: Int,
        val myApplications: Int,
        val activeRooms: Int,
        val activeChatRooms: Int
)

// リアルタイム通信用
data class MatchingRoomUpdateDto(
        val type: String, // "JOIN", "LEAVE", "STATUS_CHANGE"
        val matchingRoomId: Long,
        val participants: List<ParticipantDto>,
        val status: MatchingRoomStatus,
        val message: String?
)

data class ChatMessageUpdateDto(
        val type: String, // "NEW_MESSAGE", "USER_JOIN", "USER_LEAVE"
        val chatRoomId: Long,
        val message: ChatMessageDto?,
        val systemMessage: String?
)
