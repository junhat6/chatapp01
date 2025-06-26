package com.usjchatapp.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "user_profiles")
data class UserProfile(
    @Id
    @Column(name = "user_id")
    val userId: Long,
    
    @Column(name = "display_name", nullable = false)
    val displayName: String,
    
    @Column(name = "profile_image")
    val profileImage: String? = null,
    
    @Column
    val bio: String? = null,
    
    @Column
    val age: Int? = null,
    
    @Column
    val gender: String? = null,
    
    @Column(name = "has_usj_annual_pass")
    val hasUsjAnnualPass: Boolean = false,
    
    @Column(name = "favorite_attractions")
    @JdbcTypeCode(SqlTypes.ARRAY)
    val favoriteAttractions: Array<String>? = null,
    
    @Column(name = "location_prefecture")
    val locationPrefecture: String? = null,
    
    @Column
    @JdbcTypeCode(SqlTypes.ARRAY)
    val hobbies: Array<String>? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null
) {
    // equals and hashCode override for data class with Array
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserProfile

        if (userId != other.userId) return false
        if (displayName != other.displayName) return false
        if (profileImage != other.profileImage) return false
        if (bio != other.bio) return false
        if (age != other.age) return false
        if (gender != other.gender) return false
        if (hasUsjAnnualPass != other.hasUsjAnnualPass) return false
        if (favoriteAttractions != null) {
            if (other.favoriteAttractions == null) return false
            if (!favoriteAttractions.contentEquals(other.favoriteAttractions)) return false
        } else if (other.favoriteAttractions != null) return false
        if (locationPrefecture != other.locationPrefecture) return false
        if (hobbies != null) {
            if (other.hobbies == null) return false
            if (!hobbies.contentEquals(other.hobbies)) return false
        } else if (other.hobbies != null) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + (profileImage?.hashCode() ?: 0)
        result = 31 * result + (bio?.hashCode() ?: 0)
        result = 31 * result + (age ?: 0)
        result = 31 * result + (gender?.hashCode() ?: 0)
        result = 31 * result + hasUsjAnnualPass.hashCode()
        result = 31 * result + (favoriteAttractions?.contentHashCode() ?: 0)
        result = 31 * result + (locationPrefecture?.hashCode() ?: 0)
        result = 31 * result + (hobbies?.contentHashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
} 