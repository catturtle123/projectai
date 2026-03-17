package com.project.ai.domain.user

import com.project.ai.domain.user.dto.LoginRequest
import com.project.ai.domain.user.dto.SignupRequest
import com.project.ai.domain.user.repository.UserRepository
import com.project.ai.domain.user.service.AuthService
import com.project.ai.global.config.JwtProvider
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {
    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtProvider: JwtProvider

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    // === 회원가입 ===

    @Test
    fun `회원가입이 정상적으로 동작해야 한다`() {
        // given
        val request =
            SignupRequest(
                email = "test@test.com",
                password = "password123",
                name = "홍길동",
            )

        // when
        val result = authService.signup(request)

        // then
        assertThat(result.email).isEqualTo("test@test.com")
        assertThat(result.name).isEqualTo("홍길동")
        assertThat(result.role).isEqualTo("member")
        assertThat(result.id).isGreaterThan(0)
    }

    @Test
    fun `중복 이메일로 가입 시 예외가 발생해야 한다`() {
        // given
        val request =
            SignupRequest(
                email = "test@test.com",
                password = "password123",
                name = "홍길동",
            )
        authService.signup(request)

        // when & then
        val exception =
            assertThrows<AppException> {
                authService.signup(request.copy(name = "김철수"))
            }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.DUPLICATE_EMAIL)
    }

    @Test
    fun `가입 시 비밀번호가 암호화되어 저장되어야 한다`() {
        // given
        val request =
            SignupRequest(
                email = "test@test.com",
                password = "password123",
                name = "홍길동",
            )

        // when
        authService.signup(request)

        // then
        val user = userRepository.findByEmail("test@test.com")!!
        assertThat(user.password).isNotEqualTo("password123")
    }

    // === 로그인 ===

    @Test
    fun `로그인이 정상적으로 동작해야 한다`() {
        // given
        authService.signup(SignupRequest("test@test.com", "password123", "홍길동"))
        val request = LoginRequest(email = "test@test.com", password = "password123")

        // when
        val result = authService.login(request)

        // then
        assertThat(result.accessToken).isNotBlank()
        assertThat(jwtProvider.validateToken(result.accessToken)).isTrue()
        assertThat(jwtProvider.getEmail(result.accessToken)).isEqualTo("test@test.com")
        assertThat(jwtProvider.getRole(result.accessToken)).isEqualTo("member")
    }

    @Test
    fun `존재하지 않는 이메일로 로그인 시 예외가 발생해야 한다`() {
        // given
        val request = LoginRequest(email = "nobody@test.com", password = "password123")

        // when & then
        val exception =
            assertThrows<AppException> {
                authService.login(request)
            }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.INVALID_CREDENTIALS)
    }

    @Test
    fun `잘못된 비밀번호로 로그인 시 예외가 발생해야 한다`() {
        // given
        authService.signup(SignupRequest("test@test.com", "password123", "홍길동"))
        val request = LoginRequest(email = "test@test.com", password = "wrongpassword")

        // when & then
        val exception =
            assertThrows<AppException> {
                authService.login(request)
            }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.INVALID_CREDENTIALS)
    }
}
