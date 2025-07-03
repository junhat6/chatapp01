package com.usjchatapp.presentation.controller

import com.usjchatapp.application.dto.*
import com.usjchatapp.application.service.MatchingApplicationService
import com.usjchatapp.application.service.MatchingRoomService
import com.usjchatapp.common.constants.ApiPaths
import com.usjchatapp.domain.entity.MatchingApplicationStatus
import com.usjchatapp.domain.entity.User
import com.usjchatapp.infrastructure.annotation.RequireCompleteProfile
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiPaths.MatchingApplication.BASE)
@RequireCompleteProfile
class MatchingApplicationController(
        private val matchingApplicationService: MatchingApplicationService,
        private val matchingRoomService: MatchingRoomService
) {

        // 募集に応募
        @PostMapping("/requests/{requestId}")
        fun applyToMatching(
                @PathVariable requestId: Long,
                @AuthenticationPrincipal user: User,
                @Valid @RequestBody request: CreateMatchingApplicationRequest
        ): ResponseEntity<ApiResponse<MatchingApplicationDto>> {
                return try {
                        val application =
                                matchingApplicationService.applyToMatching(
                                        requestId,
                                        user.id,
                                        request
                                )
                        ResponseEntity.ok(
                                ApiResponse(success = true, data = application, message = "応募しました")
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "応募に失敗しました",
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

        // 応募取り消し
        @DeleteMapping("/requests/{requestId}")
        fun withdrawApplication(
                @PathVariable requestId: Long,
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<Boolean>> {
                return try {
                        val result =
                                matchingApplicationService.withdrawApplication(requestId, user.id)
                        ResponseEntity.ok(
                                ApiResponse(success = true, data = result, message = "応募を取り消しました")
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "応募取り消しに失敗しました",
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

        // 募集への応募一覧取得（ホスト用）
        @GetMapping("/requests/{requestId}")
        fun getApplicationsForRequest(
                @PathVariable requestId: Long
        ): ResponseEntity<ApiResponse<List<MatchingApplicationDto>>> {
                return try {
                        val applications =
                                matchingApplicationService.getApplicationsForRequest(requestId)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = applications,
                                        message = "${applications.size}件の応募があります"
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

        // ユーザーの応募一覧取得
        @GetMapping("/my")
        fun getMyApplications(
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<List<MatchingApplicationDto>>> {
                return try {
                        val applications = matchingApplicationService.getUserApplications(user.id)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = applications,
                                        message = "${applications.size}件の応募があります"
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

        // 応募ステータス更新（ホストが承認/拒否）
        @PutMapping("/{applicationId}/status")
        fun updateApplicationStatus(
                @PathVariable applicationId: Long,
                @AuthenticationPrincipal user: User,
                @Valid @RequestBody request: UpdateApplicationStatusRequest
        ): ResponseEntity<ApiResponse<MatchingApplicationDto>> {
                return try {
                        val updated =
                                matchingApplicationService.updateApplicationStatus(
                                        applicationId,
                                        user.id,
                                        request
                                )

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = updated,
                                        message =
                                                when (request.status) {
                                                        MatchingApplicationStatus.ACCEPTED ->
                                                                "応募を承認しました"
                                                        MatchingApplicationStatus.REJECTED ->
                                                                "応募を拒否しました"
                                                        else -> "応募ステータスを更新しました"
                                                }
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "ステータス更新に失敗しました",
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

        // 承認済み参加者取得
        @GetMapping("/requests/{requestId}/accepted")
        fun getAcceptedParticipants(
                @PathVariable requestId: Long
        ): ResponseEntity<ApiResponse<List<MatchingApplicationDto>>> {
                return try {
                        val participants =
                                matchingApplicationService.getAcceptedParticipants(requestId)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = participants,
                                        message = "${participants.size}人の参加者が承認されています"
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

        // 応募可能かチェック
        @GetMapping("/requests/{requestId}/can-apply")
        fun canApply(
                @PathVariable requestId: Long,
                @AuthenticationPrincipal user: User
        ): ResponseEntity<ApiResponse<Boolean>> {
                return try {
                        val canApply = matchingApplicationService.canApply(requestId, user.id)
                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        data = canApply,
                                        message = if (canApply) "応募可能です" else "応募できません"
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
