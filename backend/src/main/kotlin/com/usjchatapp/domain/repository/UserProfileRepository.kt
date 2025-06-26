package com.usjchatapp.domain.repository

import com.usjchatapp.domain.entity.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserProfileRepository : JpaRepository<UserProfile, Long> {
    
    fun existsByUserId(userId: Long): Boolean
    
    @Query("SELECT up FROM UserProfile up WHERE up.hasUsjAnnualPass = true")
    fun findUsersWithAnnualPass(): List<UserProfile>
    
    @Query(
        value = "SELECT * FROM user_profiles up WHERE :attraction = ANY(up.favorite_attractions)",
        nativeQuery = true
    )
    fun findByFavoriteAttraction(@Param("attraction") attraction: String): List<UserProfile>
    
    @Query("SELECT up FROM UserProfile up WHERE up.age BETWEEN :minAge AND :maxAge")
    fun findByAgeRange(@Param("minAge") minAge: Int, @Param("maxAge") maxAge: Int): List<UserProfile>
    
    fun findByGender(gender: String): List<UserProfile>
    
    fun findByLocationPrefecture(prefecture: String): List<UserProfile>
    
    fun findByDisplayNameContaining(displayName: String): List<UserProfile>
} 