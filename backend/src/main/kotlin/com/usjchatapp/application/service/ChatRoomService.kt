package com.usjchatapp.application.service

import com.usjchatapp.application.dto.*
import com.usjchatapp.domain.entity.*
import com.usjchatapp.domain.repository.*
import java.time.LocalDateTime
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ChatRoomService(
        private val chatRoomRepository: ChatRoomRepository,
        private val chatMessageRepository: ChatMessageRepository,
        private val matchingRequestRepository: MatchingRequestRepository,
        private val matchingRoomRepository: MatchingRoomRepository,
        private val userProfileRepository: UserProfileRepository
) {

    // チャットルーム作成（待機室確定時に自動作成）
    fun createChatRoom(matchingRequestId: Long): ChatRoomDto {
        // 既存のチャットルームがあるかチェック
        val existingChatRoom = chatRoomRepository.findByMatchingRequestId(matchingRequestId)
        if (existingChatRoom != null) {
            return convertToDto(existingChatRoom)
        }

        val matchingRequest =
                matchingRequestRepository.findById(matchingRequestId).orElseThrow {
                    IllegalArgumentException("募集が見つかりません")
                }

        val matchingRoom =
                matchingRoomRepository.findByMatchingRequestId(matchingRequestId)
                        ?: throw IllegalArgumentException("待機室が見つかりません")

        if (matchingRoom.status != MatchingRoomStatus.CONFIRMED) {
            throw IllegalArgumentException("確定済みの待機室からのみチャットルームを作成できます")
        }

        val chatRoomName = "${matchingRequest.attraction} - ${matchingRequest.preferredDateTime}"

        val chatRoom =
                ChatRoom(
                        name = chatRoomName,
                        matchingRequestId = matchingRequestId,
                        participantUserIds = matchingRoom.participantUserIds
                )

        val saved = chatRoomRepository.save(chatRoom)

        // システムメッセージでチャットルーム作成を通知
        createSystemMessage(saved.id, "チャットルームが作成されました！皆さんで楽しいUSJ体験を計画しましょう🎢")

        return convertToDto(saved)
    }

    // チャットルーム取得
    fun getChatRoom(chatRoomId: Long, userId: Long): ChatRoomDto {
        val chatRoom =
                chatRoomRepository.findById(chatRoomId).orElseThrow {
                    IllegalArgumentException("チャットルームが見つかりません")
                }

        if (!chatRoomRepository.isUserParticipant(chatRoomId, userId)) {
            throw IllegalArgumentException("参加していないチャットルームです")
        }

        return convertToDto(chatRoom)
    }

    // ユーザーのチャットルーム一覧
    fun getUserChatRooms(userId: Long): List<ChatRoomDto> {
        return chatRoomRepository.findByParticipantUserId(userId).map { convertToDto(it) }
    }

    // メッセージ送信
    fun sendMessage(chatRoomId: Long, senderId: Long, request: SendMessageRequest): ChatMessageDto {
        val chatRoom =
                chatRoomRepository.findById(chatRoomId).orElseThrow {
                    IllegalArgumentException("チャットルームが見つかりません")
                }

        if (!chatRoomRepository.isUserParticipant(chatRoomId, senderId)) {
            throw IllegalArgumentException("参加していないチャットルームです")
        }

        val message =
                ChatMessage(
                        chatRoomId = chatRoomId,
                        senderUserId = senderId,
                        content = request.content,
                        messageType = request.messageType
                )

        val saved = chatMessageRepository.save(message)
        return convertToMessageDto(saved)
    }

    // チャットルームのメッセージ一覧取得
    fun getChatMessages(
            chatRoomId: Long,
            userId: Long,
            page: Int = 0,
            size: Int = 50
    ): List<ChatMessageDto> {
        if (!chatRoomRepository.isUserParticipant(chatRoomId, userId)) {
            throw IllegalArgumentException("参加していないチャットルームです")
        }

        val pageable = PageRequest.of(page, size)
        return chatMessageRepository
                .findByChatRoomIdOrderBySentAtDesc(chatRoomId, pageable)
                .content
                .reversed() // 時系列順に並び替え
                .map { convertToMessageDto(it) }
    }

    // 最新メッセージ以降を取得（リアルタイム更新用）
    fun getMessagesSince(
            chatRoomId: Long,
            userId: Long,
            since: LocalDateTime
    ): List<ChatMessageDto> {
        if (!chatRoomRepository.isUserParticipant(chatRoomId, userId)) {
            throw IllegalArgumentException("参加していないチャットルームです")
        }

        return chatMessageRepository.findByChatRoomIdAndSentAtAfterOrderBySentAtAsc(
                        chatRoomId,
                        since
                )
                .map { convertToMessageDto(it) }
    }

    // システムメッセージ作成
    fun createSystemMessage(chatRoomId: Long, content: String): ChatMessageDto {
        val message =
                ChatMessage(
                        chatRoomId = chatRoomId,
                        senderUserId = 0, // システムメッセージは送信者ID 0
                        content = content,
                        messageType = ChatMessageType.SYSTEM
                )

        val saved = chatMessageRepository.save(message)
        return convertToMessageDto(saved)
    }

    // ユーザーのチャットルーム参加通知
    fun notifyUserJoined(chatRoomId: Long, userId: Long): ChatMessageDto {
        val profile = userProfileRepository.findById(userId).orElse(null)
        val displayName = profile?.displayName ?: "ユーザー"

        return createSystemMessage(chatRoomId, "${displayName}さんが参加しました")
    }

    // ユーザーのチャットルーム退出通知
    fun notifyUserLeft(chatRoomId: Long, userId: Long): ChatMessageDto {
        val profile = userProfileRepository.findById(userId).orElse(null)
        val displayName = profile?.displayName ?: "ユーザー"

        return createSystemMessage(chatRoomId, "${displayName}さんが退出しました")
    }

    // チャットルーム検索
    fun searchChatRooms(query: String): List<ChatRoomDto> {
        return chatRoomRepository.findByNameContainingOrderByCreatedAtDesc(query).map {
            convertToDto(it)
        }
    }

    // チャットルーム削除（管理者用）
    fun deleteChatRoom(chatRoomId: Long): Boolean {
        val chatRoom =
                chatRoomRepository.findById(chatRoomId).orElseThrow {
                    IllegalArgumentException("チャットルームが見つかりません")
                }

        chatRoomRepository.delete(chatRoom)
        return true
    }

    // チャットルームDTO変換
    private fun convertToDto(chatRoom: ChatRoom): ChatRoomDto {
        val participants =
                chatRoom.participantUserIds.mapNotNull { userId ->
                    val profile = userProfileRepository.findById(userId).orElse(null)
                    profile?.let {
                        val matchingRequest =
                                matchingRequestRepository
                                        .findById(chatRoom.matchingRequestId)
                                        .orElse(null)
                        ParticipantDto(
                                userId = it.userId,
                                displayName = it.displayName,
                                profileImage = it.profileImage,
                                isHost = userId == matchingRequest?.hostUserId
                        )
                    }
                }

        val lastMessage =
                chatMessageRepository.findTopByChatRoomIdOrderBySentAtDesc(chatRoom.id)?.let {
                    convertToMessageDto(it)
                }

        return ChatRoomDto(
                id = chatRoom.id,
                name = chatRoom.name,
                matchingRequestId = chatRoom.matchingRequestId,
                participants = participants,
                lastMessage = lastMessage,
                createdAt = chatRoom.createdAt,
                updatedAt = chatRoom.updatedAt
        )
    }

    // メッセージDTO変換
    private fun convertToMessageDto(message: ChatMessage): ChatMessageDto {
        val senderProfile =
                if (message.senderUserId == 0L) {
                    null // システムメッセージ
                } else {
                    userProfileRepository.findById(message.senderUserId).orElse(null)
                }

        return ChatMessageDto(
                id = message.id,
                chatRoomId = message.chatRoomId,
                senderUserId = message.senderUserId,
                senderDisplayName = senderProfile?.displayName ?: "システム",
                senderProfileImage = senderProfile?.profileImage,
                content = message.content,
                messageType = message.messageType,
                sentAt = message.sentAt
        )
    }
}
