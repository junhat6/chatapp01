package com.usjchatapp.presentation.controller

import com.usjchatapp.application.dto.*
import com.usjchatapp.application.service.UserProfileService
import com.usjchatapp.domain.entity.User
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profiles")
class UserProfileController(
    private val userProfileService: UserProfileService
) {

    @PostMapping("", "/")
    fun createProfile(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody request: CreateUserProfileRequest
    ): ResponseEntity<ApiResponse<UserProfileDto>> {
        return try {
            val profile = userProfileService.createProfile(user.id, request)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = profile,
                    message = "プロフィールが作成されました"
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = e.message ?: "プロフィール作成に失敗しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "サーバーエラーが発生しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        }
    }

    @GetMapping("/me")
    fun getMyProfile(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ApiResponse<UserProfileDto?>> {
        return try {
            val profile = userProfileService.getProfile(user.id)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = profile,
                    message = if (profile != null) "プロフィールを取得しました" else "プロフィールが見つかりません"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "サーバーエラーが発生しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        }
    }

    @GetMapping("/{userId}")
    fun getUserProfile(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<UserProfileDto?>> {
        return try {
            val profile = userProfileService.getProfile(userId)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = profile,
                    message = if (profile != null) "プロフィールを取得しました" else "プロフィールが見つかりません"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "サーバーエラーが発生しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        }
    }

    @PutMapping("", "/")
    fun updateProfile(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<ApiResponse<UserProfileDto>> {
        return try {
            val profile = userProfileService.updateProfile(user.id, request)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = profile,
                    message = "プロフィールが更新されました"
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = e.message ?: "プロフィール更新に失敗しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "サーバーエラーが発生しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        }
    }

    @DeleteMapping("", "/")
    fun deleteProfile(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ApiResponse<Boolean>> {
        return try {
            val result = userProfileService.deleteProfile(user.id)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = result,
                    message = "プロフィールが削除されました"
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = e.message ?: "プロフィール削除に失敗しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "サーバーエラーが発生しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        }
    }

    @GetMapping("/search")
    fun searchProfiles(
        @RequestParam(required = false) minAge: Int?,
        @RequestParam(required = false) maxAge: Int?,
        @RequestParam(required = false) gender: String?,
        @RequestParam(required = false) hasUsjAnnualPass: Boolean?,
        @RequestParam(required = false) favoriteAttraction: String?,
        @RequestParam(required = false) locationPrefecture: String?
    ): ResponseEntity<ApiResponse<List<UserProfileDto>>> {
        return try {
            val searchRequest = UserProfileSearchRequest(
                minAge = minAge,
                maxAge = maxAge,
                gender = gender,
                hasUsjAnnualPass = hasUsjAnnualPass,
                favoriteAttraction = favoriteAttraction,
                locationPrefecture = locationPrefecture
            )
            val profiles = userProfileService.searchProfiles(searchRequest)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = profiles,
                    message = "${profiles.size}件のプロフィールが見つかりました"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "サーバーエラーが発生しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        }
    }

    @GetMapping("/annual-pass-holders")
    fun getAnnualPassHolders(): ResponseEntity<ApiResponse<List<UserProfileDto>>> {
        return try {
            val profiles = userProfileService.getUsersWithAnnualPass()
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = profiles,
                    message = "${profiles.size}人の年間パス保有者が見つかりました"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "サーバーエラーが発生しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        }
    }

    @GetMapping("/attraction/{attraction}")
    fun getUsersByFavoriteAttraction(
        @PathVariable attraction: String
    ): ResponseEntity<ApiResponse<List<UserProfileDto>>> {
        return try {
            val profiles = userProfileService.getUsersByFavoriteAttraction(attraction)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = profiles,
                    message = "${profiles.size}人のユーザーが「$attraction」を好きなアトラクションに設定しています"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "サーバーエラーが発生しました",
                    errors = listOf(e.message ?: "不明なエラー")
                )
            )
        }
    }
} 