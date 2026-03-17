package com.project.ai.global.config

import com.project.ai.domain.user.entity.Role
import com.project.ai.domain.user.entity.User
import com.project.ai.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${admin.email:}") private val adminEmail: String,
    @Value("\${admin.password:}") private val adminPassword: String,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun run(args: ApplicationArguments) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("관리자 환경변수(ADMIN_EMAIL, ADMIN_PASSWORD)가 설정되지 않아 초기 관리자를 생성하지 않습니다.")
            return
        }

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("관리자 계정이 이미 존재합니다: {}", adminEmail)
            return
        }

        userRepository.save(
            User(
                email = adminEmail,
                password = passwordEncoder.encode(adminPassword),
                name = "Admin",
                role = Role.ADMIN,
            ),
        )
        log.info("초기 관리자 계정 생성 완료: {}", adminEmail)
    }
}
