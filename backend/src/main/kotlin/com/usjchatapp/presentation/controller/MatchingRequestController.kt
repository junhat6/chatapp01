package com.usjchatapp.presentation.controller

import com.usjchatapp.application.dto.*
import com.usjchatapp.application.service.MatchingRequestService
import com.usjchatapp.common.constants.ApiPaths
import com.usjchatapp.domain.entity.MatchingRequestStatus
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
@RequestMapping(ApiPaths.MatchingRequest.BASE)
@RequireCompleteProfile
class MatchingRequestController(private val matchingRequestService: MatchingRequestService) {

        // 募集作成
        @PostMapping(ApiPaths.MatchingRequest.CREATE)
        fun createMatchingRequest(
                @AuthenticationPrincipal user: User,
                @Valid @RequestBody request: CreateMatchingRequestRequest
        ): ResponseEntity<ApiResponse<MatchingRequestDto>> {
                return try {
                        val matchingRequest =
                                matchingRequestService.createMatchingRequest(user.id, request)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = matchingRequest,
                                        message = "募集を作成しました"
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "募集作成に失敗しました",
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

        // アクティブな募集一覧取得
        @GetMapping(ApiPaths.MatchingRequest.LIST)
        fun getActiveMatchingRequests(
                @RequestParam(required = false) attraction: String?,
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                dateFrom: LocalDateTime?,
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                dateTo: LocalDateTime?,
                @RequestParam(required = false) maxParticipants: Int?,
                @RequestParam(required = false) status: MatchingRequestStatus?
        ): ResponseEntity<ApiResponse<List<MatchingRequestDto>>> {
                return try {
                        val searchRequest =
                                MatchingRequestSearchRequest(
                                        attraction = attraction,
                                        dateFrom = dateFrom,
                                        dateTo = dateTo,
                                        maxParticipants = maxParticipants,
                                        status = status
                                )
                        val requests =
                                matchingRequestService.getActiveMatchingRequests(searchRequest)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = requests,
                                        message = "${requests.size}件の募集が見つかりました"
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

        // 募集詳細取得
        @GetMapping(ApiPaths.MatchingRequest.BY_ID)
        fun getMatchingRequest(
                @PathVariable id: Long
        ): ResponseEntity<ApiResponse<MatchingRequestDto>> {
                return try {
                        val request = matchingRequestService.getMatchingRequest(id)
                        ResponseEntity.ok(
                                ApiResponse(success = true, data = request, message = "募集詳細を取得しました")
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "募集が見つかりません",
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

        // ユーザーの募集一覧取得
        @GetMapping(ApiPaths.MatchingRequest.MY)
        fun getMyMatchingRequests(
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<List<MatchingRequestDto>>> {
                return try {
                        val requests = matchingRequestService.getUserMatchingRequests(user.id)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = requests,
                                        message = "${requests.size}件の募集があります"
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

        // 募集更新
        @PutMapping(ApiPaths.MatchingRequest.UPDATE)
        fun updateMatchingRequest(
                @PathVariable id: Long,
                @AuthenticationPrincipal user: User,
                @Valid @RequestBody request: UpdateMatchingRequestRequest
        ): ResponseEntity<ApiResponse<MatchingRequestDto>> {
                return try {
                        val updated =
                                matchingRequestService.updateMatchingRequest(id, user.id, request)
                        ResponseEntity.ok(
                                ApiResponse(success = true, data = updated, message = "募集を更新しました")
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "募集更新に失敗しました",
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

        // 募集キャンセル
        @DeleteMapping(ApiPaths.MatchingRequest.DELETE)
        fun cancelMatchingRequest(
                @PathVariable id: Long,
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<MatchingRequestDto>> {
                return try {
                        val cancelled = matchingRequestService.cancelMatchingRequest(id, user.id)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = cancelled,
                                        message = "募集をキャンセルしました"
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "募集キャンセルに失敗しました",
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

        // ステータス更新（内部利用）
        @PutMapping(ApiPaths.MatchingRequest.UPDATE_STATUS)
        fun updateStatus(
                @PathVariable id: Long,
                @RequestParam status: MatchingRequestStatus
        ): ResponseEntity<ApiResponse<MatchingRequestDto>> {
                return try {
                        val updated = matchingRequestService.updateStatus(id, status)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = updated,
                                        message = "ステータスを更新しました"
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
