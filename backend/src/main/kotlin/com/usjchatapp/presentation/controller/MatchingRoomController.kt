package com.usjchatapp.presentation.controller

import com.usjchatapp.application.dto.*
import com.usjchatapp.application.service.ChatRoomService
import com.usjchatapp.application.service.MatchingRoomService
import com.usjchatapp.common.constants.ApiPaths
import com.usjchatapp.domain.entity.User
import com.usjchatapp.infrastructure.annotation.RequireCompleteProfile
import com.usjchatapp.infrastructure.service.RedisPubSubService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiPaths.MatchingRoom.BASE)
@RequireCompleteProfile
class MatchingRoomController(
        private val matchingRoomService: MatchingRoomService,
        private val chatRoomService: ChatRoomService,
        private val redisPubSubService: RedisPubSubService
) {

        // 待機室取得
        @GetMapping("/requests/{requestId}")
        fun getMatchingRoom(
                @PathVariable requestId: Long
        ): ResponseEntity<ApiResponse<MatchingRoomDto?>> {
                return try {
                        val room = matchingRoomService.getMatchingRoom(requestId)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = room,
                                        message = if (room != null) "待機室を取得しました" else "待機室が見つかりません"
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

        // 待機室作成
        @PostMapping("/requests/{requestId}")
        fun createMatchingRoom(
                @PathVariable requestId: Long
        ): ResponseEntity<ApiResponse<MatchingRoomDto>> {
                return try {
                        val room = matchingRoomService.createMatchingRoom(requestId)
                        ResponseEntity.ok(
                                ApiResponse(success = true, data = room, message = "待機室を作成しました")
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "待機室作成に失敗しました",
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

        // 待機室参加
        @PostMapping("/requests/{requestId}/join")
        fun joinMatchingRoom(
                @PathVariable requestId: Long,
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<MatchingRoomDto>> {
                return try {
                        val room = matchingRoomService.joinMatchingRoom(requestId, user.id)
                        ResponseEntity.ok(
                                ApiResponse(success = true, data = room, message = "待機室に参加しました")
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "待機室参加に失敗しました",
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

        // 待機室退出
        @PostMapping("/requests/{requestId}/leave")
        fun leaveMatchingRoom(
                @PathVariable requestId: Long,
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<MatchingRoomDto>> {
                return try {
                        val room = matchingRoomService.leaveMatchingRoom(requestId, user.id)
                        ResponseEntity.ok(
                                ApiResponse(success = true, data = room, message = "待機室を退出しました")
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "待機室退出に失敗しました",
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

        // 待機室確定（ホストが確定ボタンを押す）
        @PostMapping("/requests/{requestId}/confirm")
        fun confirmMatchingRoom(
                @PathVariable requestId: Long,
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<MatchingRoomDto>> {
                return try {
                        val room = matchingRoomService.confirmMatchingRoom(requestId, user.id)

                        // 確定時にチャットルームを自動作成
                        val chatRoom = chatRoomService.createChatRoom(requestId)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = room,
                                        message = "待機室を確定しました。チャットルームが作成されました！"
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "待機室確定に失敗しました",
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

        // 待機室解散
        @PostMapping("/requests/{requestId}/disband")
        fun disbandMatchingRoom(
                @PathVariable requestId: Long,
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<MatchingRoomDto>> {
                return try {
                        val room = matchingRoomService.disbandMatchingRoom(requestId, user.id)
                        ResponseEntity.ok(
                                ApiResponse(success = true, data = room, message = "待機室を解散しました")
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "待機室解散に失敗しました",
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

        // ユーザーの待機室一覧
        @GetMapping("/my")
        fun getMyMatchingRooms(
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<List<MatchingRoomDto>>> {
                return try {
                        val rooms = matchingRoomService.getUserMatchingRooms(user.id)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = rooms,
                                        message = "${rooms.size}個の待機室に参加しています"
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

        // アクティブな待機室一覧
        @GetMapping("/active")
        fun getActiveMatchingRooms(): ResponseEntity<ApiResponse<List<MatchingRoomDto>>> {
                return try {
                        val rooms = matchingRoomService.getActiveMatchingRooms()
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = rooms,
                                        message = "${rooms.size}個のアクティブな待機室があります"
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
