package com.project.ai.domain.user.service

import com.project.ai.domain.analytics.service.ActivityLogService
import com.project.ai.domain.user.dto.LoginRequest
import com.project.ai.domain.user.dto.LoginResponse
import com.project.ai.domain.user.dto.SignupRequest
import com.project.ai.domain.user.dto.SignupResponse
import com.project.ai.domain.user.entity.User
import com.project.ai.domain.user.repository.UserRepository
import com.project.ai.global.config.JwtProvider
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val activityLogService: ActivityLogService,
) {
    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw AppException(ErrorCode.DUPLICATE_EMAIL)
        }

        val user =
            userRepository.save(
                User(
                    email = request.email,
                    password = passwordEncoder.encode(request.password),
                    name = request.name,
                ),
            )

        activityLogService.log("SIGNUP", user.id)

        return SignupResponse(
            id = user.id,
            email = user.email,
            name = user.name,
            role = user.role.name.lowercase(),
            createdAt = user.createdAt,
        )
    }

    fun login(request: LoginRequest): LoginResponse {
        val user =
            userRepository.findByEmail(request.email)
                ?: throw AppException(ErrorCode.INVALID_CREDENTIALS)

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw AppException(ErrorCode.INVALID_CREDENTIALS)
        }

        activityLogService.log("LOGIN", user.id)

        val token =
            jwtProvider.generateToken(
                userId = user.id,
                email = user.email,
                role = user.role.name.lowercase(),
            )

        return LoginResponse(accessToken = token)
    }
}
