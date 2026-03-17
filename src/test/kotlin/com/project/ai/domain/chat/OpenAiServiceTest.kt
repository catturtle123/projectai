package com.project.ai.domain.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.project.ai.domain.chat.dto.OpenAiMessage
import com.project.ai.domain.chat.service.OpenAiService
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

class OpenAiServiceTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var openAiService: OpenAiService
    private val objectMapper =
        ObjectMapper().registerModule(
            KotlinModule.Builder().build(),
        )

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val webClient =
            WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer test-key")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()

        openAiService = OpenAiService(webClient, objectMapper, "gpt-4o-mini")
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `chat 성공 시 응답 문자열을 반환해야 한다`() {
        // given
        val responseBody =
            """
            {
              "choices": [
                {
                  "message": {
                    "role": "assistant",
                    "content": "안녕하세요!"
                  },
                  "finish_reason": "stop"
                }
              ]
            }
            """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"),
        )

        val messages = listOf(OpenAiMessage(role = "user", content = "안녕"))

        // when
        val result = openAiService.chat(messages)

        // then
        assertThat(result).isEqualTo("안녕하세요!")

        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.path).isEqualTo("/v1/chat/completions")
        assertThat(recordedRequest.method).isEqualTo("POST")

        val requestBody = objectMapper.readTree(recordedRequest.body.readUtf8())
        assertThat(requestBody["model"].asText()).isEqualTo("gpt-4o-mini")
        assertThat(requestBody["stream"].asBoolean()).isFalse()
    }

    @Test
    fun `chat에서 model 파라미터를 지정하면 해당 모델을 사용해야 한다`() {
        // given
        val responseBody =
            """
            {
              "choices": [
                {
                  "message": {
                    "role": "assistant",
                    "content": "응답"
                  },
                  "finish_reason": "stop"
                }
              ]
            }
            """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"),
        )

        val messages = listOf(OpenAiMessage(role = "user", content = "테스트"))

        // when
        openAiService.chat(messages, model = "gpt-4o")

        // then
        val recordedRequest = mockWebServer.takeRequest()
        val requestBody = objectMapper.readTree(recordedRequest.body.readUtf8())
        assertThat(requestBody["model"].asText()).isEqualTo("gpt-4o")
    }

    @Test
    fun `chat에서 API 오류 시 AppException을 던져야 한다`() {
        // given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error": {"message": "Internal Server Error"}}""")
                .addHeader("Content-Type", "application/json"),
        )

        val messages = listOf(OpenAiMessage(role = "user", content = "안녕"))

        // when & then
        val exception =
            assertThrows<AppException> {
                openAiService.chat(messages)
            }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.OPENAI_API_ERROR)
    }

    @Test
    fun `chatStream 성공 시 콘텐츠 청크를 반환해야 한다`() {
        // given
        val sseBody =
            """
            data: {"choices":[{"delta":{"role":"assistant","content":"안녕"},"finish_reason":null}]}

            data: {"choices":[{"delta":{"content":"하세요"},"finish_reason":null}]}

            data: {"choices":[{"delta":{"content":"!"},"finish_reason":"stop"}]}

            data: [DONE]

            """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(sseBody)
                .addHeader("Content-Type", "text/event-stream"),
        )

        val messages = listOf(OpenAiMessage(role = "user", content = "안녕"))

        // when
        val flux = openAiService.chatStream(messages)

        // then
        StepVerifier.create(flux)
            .expectNext("안녕")
            .expectNext("하세요")
            .expectNext("!")
            .verifyComplete()
    }

    @Test
    fun `chatStream에서 model 파라미터를 지정하면 해당 모델을 사용해야 한다`() {
        // given
        val sseBody =
            """
            data: {"choices":[{"delta":{"content":"응답"},"finish_reason":"stop"}]}

            data: [DONE]

            """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(sseBody)
                .addHeader("Content-Type", "text/event-stream"),
        )

        val messages = listOf(OpenAiMessage(role = "user", content = "테스트"))

        // when
        val flux = openAiService.chatStream(messages, model = "gpt-4o")

        // then
        StepVerifier.create(flux)
            .expectNext("응답")
            .verifyComplete()

        val recordedRequest = mockWebServer.takeRequest()
        val requestBody = objectMapper.readTree(recordedRequest.body.readUtf8())
        assertThat(requestBody["model"].asText()).isEqualTo("gpt-4o")
        assertThat(requestBody["stream"].asBoolean()).isTrue()
    }
}
