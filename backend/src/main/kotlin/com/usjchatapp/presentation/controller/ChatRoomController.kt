package com.usjchatapp.presentation.controller

import com.usjchatapp.application.dto.*
import com.usjchatapp.application.service.ChatRoomService
import com.usjchatapp.common.constants.ApiPaths
import com.usjchatapp.domain.entity.User
import com.usjchatapp.infrastructure.annotation.RequireCompleteProfile
import jakarta.validation.Valid
import java.time.LocalDateTime
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiPaths.ChatRoom.BASE)
@RequireCompleteProfile
class ChatRoomController(private val chatRoomService: ChatRoomService) {

        // チャットルーム取得
        @GetMapping("/{chatRoomId}")
        fun getChatRoom(
                @PathVariable chatRoomId: Long,
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<ChatRoomDto>> {
                return try {
                        val chatRoom = chatRoomService.getChatRoom(chatRoomId, user.id)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = chatRoom,
                                        message = "チャットルームを取得しました"
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "チャットルームにアクセスできません",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "サーバーエラーが発生しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                }
        }

        // ユーザーのチャットルーム一覧
        @GetMapping("/my")
        fun getMyChatRooms(
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<List<ChatRoomDto>>> {
                return try {
                        val chatRooms = chatRoomService.getUserChatRooms(user.id)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = chatRooms,
                                        message = "${chatRooms.size}個のチャットルームに参加しています"
                                )
                        )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "サーバーエラーが発生しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                }
        }

        // メッセージ送信
        @PostMapping("/{chatRoomId}/messages")
        fun sendMessage(
                @PathVariable chatRoomId: Long,
                @AuthenticationPrincipal user: User,
                @Valid @RequestBody request: SendMessageRequest
        ): ResponseEntity<ApiResponse<ChatMessageDto>> {
                return try {
                        val message = chatRoomService.sendMessage(chatRoomId, user.id, request)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = message,
                                        message = "メッセージを送信しました"
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "メッセージ送信に失敗しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "サーバーエラーが発生しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                }
        }

        // チャットメッセージ一覧取得
        @GetMapping("/{chatRoomId}/messages")
        fun getChatMessages(
                @PathVariable chatRoomId: Long,
                @AuthenticationPrincipal user: User,
                @RequestParam(defaultValue = "0") page: Int,
                @RequestParam(defaultValue = "50") size: Int
        ): ResponseEntity<ApiResponse<List<ChatMessageDto>>> {
                return try {
                        val messages =
                                chatRoomService.getChatMessages(chatRoomId, user.id, page, size)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = messages,
                                        message = "${messages.size}件のメッセージを取得しました"
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "メッセージにアクセスできません",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "サーバーエラーが発生しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                }
        }

        // 最新メッセージ以降を取得（リアルタイム更新用）
        @GetMapping("/{chatRoomId}/messages/since")
        fun getMessagesSince(
                @PathVariable chatRoomId: Long,
                @AuthenticationPrincipal user: User,
                @RequestParam
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                since: LocalDateTime
        ): ResponseEntity<ApiResponse<List<ChatMessageDto>>> {
                return try {
                        val messages = chatRoomService.getMessagesSince(chatRoomId, user.id, since)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = messages,
                                        message = "${messages.size}件の新しいメッセージがあります"
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "メッセージにアクセスできません",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "サーバーエラーが発生しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                }
        }

        // システムメッセージ作成（管理者用）
        @PostMapping("/{chatRoomId}/system-messages")
        fun createSystemMessage(
                @PathVariable chatRoomId: Long,
                @RequestParam content: String
        ): ResponseEntity<ApiResponse<ChatMessageDto>> {
                return try {
                        val message = chatRoomService.createSystemMessage(chatRoomId, content)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = message,
                                        message = "システムメッセージを作成しました"
                                )
                        )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "サーバーエラーが発生しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                }
        }

        // チャットルーム検索
        @GetMapping("/search")
        fun searchChatRooms(
                @RequestParam query: String
        ): ResponseEntity<ApiResponse<List<ChatRoomDto>>> {
                return try {
                        val chatRooms = chatRoomService.searchChatRooms(query)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = chatRooms,
                                        message = "${chatRooms.size}個のチャットルームが見つかりました"
                                )
                        )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "サーバーエラーが発生しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                }
        }

        // チャットルーム削除（管理者用）
        @DeleteMapping("/{chatRoomId}")
        fun deleteChatRoom(@PathVariable chatRoomId: Long): ResponseEntity<ApiResponse<Boolean>> {
                return try {
                        val result = chatRoomService.deleteChatRoom(chatRoomId)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = result,
                                        message = "チャットルームを削除しました"
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "チャットルームが見つかりません",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "サーバーエラーが発生しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                }
        }

        // 募集からチャットルーム作成（内部使用）
        @PostMapping("/from-request/{requestId}")
        fun createChatRoomFromRequest(
                @PathVariable requestId: Long
        ): ResponseEntity<ApiResponse<ChatRoomDto>> {
                return try {
                        val chatRoom = chatRoomService.createChatRoom(requestId)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = chatRoom,
                                        message = "チャットルームを作成しました"
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "チャットルーム作成に失敗しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "サーバーエラーが発生しました",
                                                errors = listOf(e.message ?: "不明なエラー")
                                        )
                                )
                }
        }
}
