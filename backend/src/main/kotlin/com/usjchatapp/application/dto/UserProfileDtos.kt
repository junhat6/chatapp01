package com.usjchatapp.application.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserProfileDto(
    val userId: Long,
    val displayName: String,
    val profileImage: String?,
    val bio: String?,
    val age: Int?,
    val gender: String?,
    val hasUsjAnnualPass: Boolean,
    val favoriteAttractions: Set<String>,
    val locationPrefecture: String?,
    val hobbies: Set<String>,
    val createdAt: String,
    val updatedAt: String
)

data class CreateUserProfileRequest(
    @field:NotBlank(message = "表示名は必須です")
    @field:Size(min = 1, max = 50, message = "表示名は1文字以上50文字以下で入力してください")
    val displayName: String,
    
    val profileImage: String? = null,
    
    val bio: String? = null,
    
    @field:Min(value = 13, message = "年齢は13歳以上で入力してください")
    @field:Max(value = 120, message = "年齢は120歳以下で入力してください")
    val age: Int? = null,
    
    val gender: String? = null,
    
    val hasUsjAnnualPass: Boolean = false,
    
    @field:Size(max = 10, message = "好きなアトラクションは10個まで選択できます")
    val favoriteAttractions: Set<String> = setOf(),
    
    val locationPrefecture: String? = null,
    
    @field:Size(max = 10, message = "趣味は10個まで選択できます")
    val hobbies: Set<String> = setOf()
)

data class UpdateUserProfileRequest(
    @field:Size(min = 1, max = 50, message = "表示名は1文字以上50文字以下で入力してください")
    val displayName: String? = null,
    
    val profileImage: String? = null,
    
    val bio: String? = null,
    
    @field:Min(value = 13, message = "年齢は13歳以上で入力してください")
    @field:Max(value = 120, message = "年齢は120歳以下で入力してください")
    val age: Int? = null,
    
    val gender: String? = null,
    
    val hasUsjAnnualPass: Boolean? = null,
    
    @field:Size(max = 10, message = "好きなアトラクションは10個まで選択できます")
    val favoriteAttractions: Set<String>? = null,
    
    val locationPrefecture: String? = null,
    
    @field:Size(max = 10, message = "趣味は10個まで選択できます")
    val hobbies: Set<String>? = null
)

data class UserProfileSearchRequest(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val gender: String? = null,
    val hasUsjAnnualPass: Boolean? = null,
    val favoriteAttraction: String? = null,
    val locationPrefecture: String? = null,
    val displayName: String? = null
) 