package com.usjchatapp.infrastructure.exception

import com.usjchatapp.application.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
            ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiResponse<Any>> {
        val errors =
                ex.bindingResult.fieldErrors.map { fieldError: FieldError ->
                    "${fieldError.field}: ${fieldError.defaultMessage}"
                }

        return ResponseEntity.badRequest()
                .body(ApiResponse(success = false, message = "バリデーションエラーが発生しました", errors = errors))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
            ex: IllegalArgumentException
    ): ResponseEntity<ApiResponse<Any>> {
        return ResponseEntity.badRequest()
                .body(
                        ApiResponse(
                                success = false,
                                message = ex.message ?: "リクエストが無効です",
                                errors = listOf(ex.message ?: "不明なエラー")
                        )
                )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Any>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse(
                                success = false,
                                message = "サーバーエラーが発生しました",
                                errors = listOf(ex.message ?: "不明なエラー")
                        )
                )
    }
}
