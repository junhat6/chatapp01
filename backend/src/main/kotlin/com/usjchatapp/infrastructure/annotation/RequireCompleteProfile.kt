package com.usjchatapp.infrastructure.annotation

import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget

/**
 * プロフィール完了が必要なAPIエンドポイントに付与するアノテーション
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireCompleteProfile 