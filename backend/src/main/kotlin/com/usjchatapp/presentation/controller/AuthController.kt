package com.usjchatapp.presentation.controller

import com.usjchatapp.application.dto.*
import com.usjchatapp.application.service.UserService
import com.usjchatapp.common.constants.ApiPaths
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiPaths.Auth.BASE)
class AuthController(private val userService: UserService) {

    @PostMapping(ApiPaths.Auth.SIGNUP)
    fun signUp(
            @Valid @RequestBody request: SignUpRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            val response = userService.signUp(request)
            ResponseEntity.ok(
                    ApiResponse(success = true, data = response, message = "ユーザー登録が完了しました")
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                    .body(
                            ApiResponse(
                                    success = false,
                                    message = e.message ?: "登録に失敗しました",
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

    @PostMapping(ApiPaths.Auth.SIGNIN)
    fun signIn(
            @Valid @RequestBody request: SignInRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            val response = userService.signIn(request)
            ResponseEntity.ok(ApiResponse(success = true, data = response, message = "ログインに成功しました"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            ApiResponse(
                                    success = false,
                                    message = "メールアドレスまたはパスワードが正しくありません",
                                    errors = listOf(e.message ?: "認証に失敗しました")
                            )
                    )
        }
    }

    @GetMapping(ApiPaths.Auth.ME)
    fun getCurrentUser(authentication: Authentication): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            val email = authentication.name
            val user = userService.getCurrentUser(email)
            ResponseEntity.ok(ApiResponse(success = true, data = user, message = "ユーザー情報を取得しました"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            ApiResponse(
                                    success = false,
                                    message = "ユーザー情報の取得に失敗しました",
                                    errors = listOf(e.message ?: "不明なエラー")
                            )
                    )
        }
    }
}
