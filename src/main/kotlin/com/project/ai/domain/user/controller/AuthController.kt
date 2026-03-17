package com.project.ai.domain.user.controller

import com.project.ai.domain.user.dto.LoginRequest
import com.project.ai.domain.user.dto.LoginResponse
import com.project.ai.domain.user.dto.SignupRequest
import com.project.ai.domain.user.dto.SignupResponse
import com.project.ai.domain.user.service.AuthService
import com.project.ai.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증 API")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<BaseResponse<SignupResponse>> {
        val result = authService.signup(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(result))
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<BaseResponse<LoginResponse>> {
        val result = authService.login(request)
        return ResponseEntity.ok(BaseResponse.success(result))
    }
}
