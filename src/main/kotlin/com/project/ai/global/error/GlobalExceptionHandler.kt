package com.project.ai.global.error

import com.project.ai.global.common.BaseResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(AppException::class)
    fun handleAppException(e: AppException): ResponseEntity<BaseResponse<Nothing>> {
        log.warn("AppException: {} - {}", e.errorCode.code, e.errorCode.message)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(BaseResponse.error(e.errorCode.code, e.errorCode.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<BaseResponse<Nothing>> {
        val message =
            e.bindingResult.fieldErrors
                .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity
            .badRequest()
            .body(BaseResponse.error(ErrorCode.VALIDATION_001.code, message))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<BaseResponse<Nothing>> {
        log.error("Unhandled exception", e)
        return ResponseEntity
            .internalServerError()
            .body(BaseResponse.error(ErrorCode.COMMON500.code, ErrorCode.COMMON500.message))
    }
}
