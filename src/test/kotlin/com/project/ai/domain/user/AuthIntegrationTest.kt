package com.project.ai.domain.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.ai.domain.user.dto.LoginRequest
import com.project.ai.domain.user.dto.SignupRequest
import com.project.ai.domain.user.repository.UserRepository
import com.project.ai.global.config.JwtProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtProvider: JwtProvider

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `회원가입 API가 정상 동작해야 한다`() {
        val request = SignupRequest("test@test.com", "password123", "홍길동")

        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.isSuccess") { value(true) }
            jsonPath("$.result.email") { value("test@test.com") }
            jsonPath("$.result.name") { value("홍길동") }
            jsonPath("$.result.role") { value("member") }
        }
    }

    @Test
    fun `로그인 API가 정상 동작해야 한다`() {
        // 회원가입
        val signup = SignupRequest("test@test.com", "password123", "홍길동")
        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signup)
        }

        // 로그인
        val login = LoginRequest("test@test.com", "password123")
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(login)
        }.andExpect {
            status { isOk() }
            jsonPath("$.isSuccess") { value(true) }
            jsonPath("$.result.accessToken") { isNotEmpty() }
        }
    }

    @Test
    fun `인증 없이 보호된 엔드포인트 접근 시 401이 반환되어야 한다`() {
        mockMvc.get("/api/v1/some-protected-resource")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `유효한 토큰으로 보호된 엔드포인트 접근이 가능해야 한다`() {
        val token = jwtProvider.generateToken(1L, "test@test.com", "member")

        mockMvc.get("/api/v1/health") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `헬스 체크는 토큰 없이도 접근 가능해야 한다`() {
        mockMvc.get("/api/v1/health")
            .andExpect {
                status { isOk() }
                jsonPath("$.isSuccess") { value(true) }
                jsonPath("$.result.status") { value("UP") }
            }
    }
}
