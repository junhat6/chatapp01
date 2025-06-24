package com.usjchatapp.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:Email(message = "有効なメールアドレスを入力してください")
    @field:NotBlank(message = "メールアドレスは必須です")
    val email: String,
    
    @field:NotBlank(message = "パスワードは必須です")
    @field:Size(min = 6, max = 20, message = "パスワードは6文字以上20文字以下で入力してください")
    val password: String,
    
    @field:NotBlank(message = "表示名は必須です")
    @field:Size(min = 1, max = 50, message = "表示名は1文字以上50文字以下で入力してください")
    val displayName: String,
    
    val profileImage: String? = null,
    val bio: String? = null
)

data class SignInRequest(
    @field:Email(message = "有効なメールアドレスを入力してください")
    @field:NotBlank(message = "メールアドレスは必須です")
    val email: String,
    
    @field:NotBlank(message = "パスワードは必須です")
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: Long,
    val email: String,
    val displayName: String,
    val profileImage: String?,
    val bio: String?,
    val createdAt: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = "",
    val errors: List<String> = emptyList()
) 