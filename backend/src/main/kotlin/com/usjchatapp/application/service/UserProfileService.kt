package com.usjchatapp.application.service

import com.usjchatapp.application.dto.*
import com.usjchatapp.domain.entity.UserProfile
import com.usjchatapp.domain.repository.UserProfileRepository
import com.usjchatapp.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
class UserProfileService(
    private val userProfileRepository: UserProfileRepository,
    private val userRepository: UserRepository
) {

    fun createProfile(userId: Long, request: CreateUserProfileRequest): UserProfileDto {
        // ユーザーが存在するかチェック
        if (!userRepository.existsById(userId)) {
            throw IllegalArgumentException("ユーザーが見つかりません")
        }
        
        // 既にプロフィールが存在するかチェック
        if (userProfileRepository.existsById(userId)) {
            throw IllegalArgumentException("このユーザーのプロフィールは既に存在します")
        }

        val profile = UserProfile(
            userId = userId,
            displayName = request.displayName,
            profileImage = request.profileImage,
            bio = request.bio,
            age = request.age,
            gender = request.gender,
            hasUsjAnnualPass = request.hasUsjAnnualPass,
            favoriteAttractions = if (request.favoriteAttractions.isNotEmpty()) request.favoriteAttractions.toTypedArray() else null,
            locationPrefecture = request.locationPrefecture,
            hobbies = if (request.hobbies.isNotEmpty()) request.hobbies.toTypedArray() else null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedProfile = userProfileRepository.save(profile)
        return mapToUserProfileDto(savedProfile)
    }

    fun getProfile(userId: Long): UserProfileDto? {
        return userProfileRepository.findById(userId)
            .map { mapToUserProfileDto(it) }
            .orElse(null)
    }

    fun updateProfile(userId: Long, request: UpdateUserProfileRequest): UserProfileDto {
        val existingProfile = userProfileRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("プロフィールが見つかりません") }

        val updatedProfile = existingProfile.copy(
            displayName = request.displayName ?: existingProfile.displayName,
            profileImage = request.profileImage ?: existingProfile.profileImage,
            bio = request.bio ?: existingProfile.bio,
            age = request.age ?: existingProfile.age,
            gender = request.gender ?: existingProfile.gender,
            hasUsjAnnualPass = request.hasUsjAnnualPass ?: existingProfile.hasUsjAnnualPass,
            favoriteAttractions = request.favoriteAttractions?.let { 
                if (it.isNotEmpty()) it.toTypedArray() else null 
            } ?: existingProfile.favoriteAttractions,
            locationPrefecture = request.locationPrefecture ?: existingProfile.locationPrefecture,
            hobbies = request.hobbies?.let { 
                if (it.isNotEmpty()) it.toTypedArray() else null 
            } ?: existingProfile.hobbies,
            updatedAt = LocalDateTime.now()
        )

        val savedProfile = userProfileRepository.save(updatedProfile)
        return mapToUserProfileDto(savedProfile)
    }

    fun deleteProfile(userId: Long): Boolean {
        val profile = userProfileRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("プロフィールが見つかりません") }
        
        userProfileRepository.delete(profile)
        return true
    }

    fun searchProfiles(searchRequest: UserProfileSearchRequest): List<UserProfileDto> {
        var profiles = userProfileRepository.findAll()

        // 年齢範囲でフィルタ
        if (searchRequest.minAge != null && searchRequest.maxAge != null) {
            profiles = userProfileRepository.findByAgeRange(searchRequest.minAge, searchRequest.maxAge)
        }

        // 性別でフィルタ
        if (searchRequest.gender != null) {
            profiles = profiles.filter { it.gender == searchRequest.gender }
        }

        // 年間パス保有でフィルタ
        if (searchRequest.hasUsjAnnualPass != null) {
            profiles = profiles.filter { it.hasUsjAnnualPass == searchRequest.hasUsjAnnualPass }
        }

        // 好きなアトラクションでフィルタ
        if (searchRequest.favoriteAttraction != null) {
            profiles = profiles.filter { 
                it.favoriteAttractions?.contains(searchRequest.favoriteAttraction) == true 
            }
        }

        // 都道府県でフィルタ
        if (searchRequest.locationPrefecture != null) {
            profiles = profiles.filter { it.locationPrefecture == searchRequest.locationPrefecture }
        }

        // 表示名でフィルタ
        if (searchRequest.displayName != null) {
            profiles = profiles.filter { it.displayName.contains(searchRequest.displayName!!, ignoreCase = true) }
        }

        return profiles.map { mapToUserProfileDto(it) }
    }

    fun getUsersWithAnnualPass(): List<UserProfileDto> {
        return userProfileRepository.findUsersWithAnnualPass()
            .map { mapToUserProfileDto(it) }
    }

    fun getUsersByFavoriteAttraction(attraction: String): List<UserProfileDto> {
        return userProfileRepository.findByFavoriteAttraction(attraction)
            .map { mapToUserProfileDto(it) }
    }

    /**
     * プロフィール完了ステータスをチェック
     * 必須項目: displayName, age, gender, locationPrefecture
     */
    fun isProfileComplete(userId: Long): Boolean {
        val profile = userProfileRepository.findById(userId).orElse(null)
        if (profile == null) {
            return false
        }
        
        return profile.displayName.isNotBlank() &&
               profile.age != null &&
               profile.gender != null &&
               profile.locationPrefecture != null
    }

    /**
     * プロフィール完了ステータス詳細を取得
     */
    fun getProfileCompletionStatus(userId: Long): ProfileCompletionStatusDto {
        val profile = userProfileRepository.findById(userId).orElse(null)
        if (profile == null) {
            return ProfileCompletionStatusDto(
                isComplete = false,
                hasProfile = false,
                missingFields = listOf("プロフィール未作成")
            )
        }
        
        val missingFields = mutableListOf<String>()
        
        if (profile.displayName.isBlank()) {
            missingFields.add("表示名")
        }
        if (profile.age == null) {
            missingFields.add("年齢")
        }
        if (profile.gender == null) {
            missingFields.add("性別")
        }
        if (profile.locationPrefecture == null) {
            missingFields.add("都道府県")
        }
        
        return ProfileCompletionStatusDto(
            isComplete = missingFields.isEmpty(),
            hasProfile = true,
            missingFields = missingFields
        )
    }

    private fun mapToUserProfileDto(profile: UserProfile): UserProfileDto {
        return UserProfileDto(
            userId = profile.userId,
            displayName = profile.displayName,
            profileImage = profile.profileImage,
            bio = profile.bio,
            age = profile.age,
            gender = profile.gender,
            hasUsjAnnualPass = profile.hasUsjAnnualPass,
            favoriteAttractions = profile.favoriteAttractions?.toSet() ?: setOf(),
            locationPrefecture = profile.locationPrefecture,
            hobbies = profile.hobbies?.toSet() ?: setOf(),
            createdAt = profile.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            updatedAt = profile.updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
} 