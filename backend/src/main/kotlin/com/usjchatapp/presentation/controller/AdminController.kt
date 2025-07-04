package com.usjchatapp.presentation.controller

import com.usjchatapp.application.dto.ApiResponse
import com.usjchatapp.application.service.MatchingCleanupService
import com.usjchatapp.common.constants.ApiPaths
import com.usjchatapp.domain.entity.User
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("${ApiPaths.API_BASE}/admin")
class AdminController(private val matchingCleanupService: MatchingCleanupService) {

    /** 期限切れ募集の手動論理削除実行 管理者のみ実行可能 */
    @PostMapping("/cleanup/expired-requests")
    @PreAuthorize("hasRole('ADMIN')")
    fun manualCleanupExpiredRequests(
            @AuthenticationPrincipal user: User
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val deletedCount = matchingCleanupService.manualSoftDeleteExpiredRequests()

            ResponseEntity.ok(
                    ApiResponse(
                            success = true,
                            data =
                                    mapOf(
                                            "deletedCount" to deletedCount,
                                            "executedBy" to user.email,
                                            "executedAt" to System.currentTimeMillis()
                                    ),
                            message =
                                    if (deletedCount > 0) {
                                        "期限切れ募集を${deletedCount}件論理削除しました"
                                    } else {
                                        "論理削除対象の募集はありませんでした"
                                    }
                    )
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                    .body(
                            ApiResponse(
                                    success = false,
                                    message = "論理削除処理中にエラーが発生しました",
                                    errors = listOf(e.message ?: "不明なエラー")
                            )
                    )
        }
    }

    /** 論理削除統計情報取得 管理者のみ実行可能 */
    @GetMapping("/stats/deleted-requests")
    @PreAuthorize("hasRole('ADMIN')")
    fun getDeletedRequestsStats(
            @AuthenticationPrincipal user: User
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val deletedCount = matchingCleanupService.getDeletedRequestsCount()

            ResponseEntity.ok(
                    ApiResponse(
                            success = true,
                            data =
                                    mapOf(
                                            "deletedCount" to deletedCount,
                                            "requestedBy" to user.email,
                                            "timestamp" to System.currentTimeMillis()
                                    ),
                            message = "統計情報を取得しました"
                    )
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                    .body(
                            ApiResponse(
                                    success = false,
                                    message = "統計情報取得中にエラーが発生しました",
                                    errors = listOf(e.message ?: "不明なエラー")
                            )
                    )
        }
    }
}
