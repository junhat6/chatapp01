package com.usjchatapp.application.service

import com.usjchatapp.application.dto.*
import com.usjchatapp.domain.entity.User
import com.usjchatapp.domain.entity.UserRole
import com.usjchatapp.domain.repository.UserRepository
import com.usjchatapp.infrastructure.security.JwtUtils
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
    @Lazy private val authenticationManager: AuthenticationManager
) : UserDetailsService {

    fun signUp(request: SignUpRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("このメールアドレスは既に使用されています")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            displayName = request.displayName,
            profileImage = request.profileImage,
            bio = request.bio,
            role = UserRole.USER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(user)
        val token = jwtUtils.generateToken(savedUser)

        return AuthResponse(
            token = token,
            user = mapToUserDto(savedUser)
        )
    }

    fun signIn(request: SignInRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )

        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UsernameNotFoundException("ユーザーが見つかりません") }

        val token = jwtUtils.generateToken(user)

        return AuthResponse(
            token = token,
            user = mapToUserDto(user)
        )
    }

    fun getCurrentUser(email: String): UserDto {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("ユーザーが見つかりません") }
        return mapToUserDto(user)
    }

    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("ユーザーが見つかりません: $username") }
    }

    private fun mapToUserDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            profileImage = user.profileImage,
            bio = user.bio,
            createdAt = user.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
} 