package com.usjchatapp.domain.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    val email: String = "",
    
    @Column(nullable = false)
    private val password: String = "",
    
    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.USER,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val isActive: Boolean = true
) : UserDetails {
    
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    }
    
    override fun getPassword(): String = password
    
    override fun getUsername(): String = email
    
    override fun isAccountNonExpired(): Boolean = isActive
    
    override fun isAccountNonLocked(): Boolean = isActive
    
    override fun isCredentialsNonExpired(): Boolean = isActive
    
    override fun isEnabled(): Boolean = isActive
}

enum class UserRole {
    USER,
    ADMIN
} 