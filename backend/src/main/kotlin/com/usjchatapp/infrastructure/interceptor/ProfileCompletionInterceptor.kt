package com.usjchatapp.infrastructure.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.usjchatapp.application.dto.ApiResponse
import com.usjchatapp.application.service.UserProfileService
import com.usjchatapp.domain.entity.User
import com.usjchatapp.infrastructure.annotation.RequireCompleteProfile
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ProfileCompletionInterceptor(
    private val userProfileService: UserProfileService,
    private val objectMapper: ObjectMapper
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // ハンドラーがメソッドでない場合はスキップ
        if (handler !is HandlerMethod) {
            return true
        }

        // RequireCompleteProfileアノテーションがない場合はスキップ
        val requireCompleteProfile = handler.getMethodAnnotation(RequireCompleteProfile::class.java)
            ?: handler.beanType.getAnnotation(RequireCompleteProfile::class.java)
        
        if (requireCompleteProfile == null) {
            return true
        }

        // 認証されたユーザーを取得
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return true // 認証チェックは別の場所で行う
        }

        val user = authentication.principal as? User
        if (user == null) {
            return true
        }

        // プロフィール完了チェック
        val isComplete = userProfileService.isProfileComplete(user.id)
        if (!isComplete) {
            val completionStatus = userProfileService.getProfileCompletionStatus(user.id)
            
            response.status = HttpStatus.FORBIDDEN.value()
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            
            val errorResponse = ApiResponse<Any>(
                success = false,
                message = "プロフィールの設定が完了していません。必要な項目を入力してください。",
                errors = completionStatus.missingFields
            )
            
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
            return false
        }

        return true
    }
} 