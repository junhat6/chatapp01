package com.usjchatapp.infrastructure.config

import com.usjchatapp.common.constants.ApiPaths
import com.usjchatapp.infrastructure.interceptor.ProfileCompletionInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(private val profileCompletionInterceptor: ProfileCompletionInterceptor) :
        WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(profileCompletionInterceptor)
                .addPathPatterns("${ApiPaths.API_BASE}/**")
                .excludePathPatterns(
                        "${ApiPaths.Auth.BASE}/**",
                        ApiPaths.Health.BASE,
                        "${ApiPaths.Profile.BASE}/**" // プロフィール関連のAPIは除外（設定・取得のため）
                )
    }
}
