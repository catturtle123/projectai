package com.project.ai.domain.chat.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.ai.domain.chat.dto.OpenAiMessage
import com.project.ai.domain.chat.dto.OpenAiRequest
import com.project.ai.domain.chat.dto.OpenAiResponse
import com.project.ai.domain.chat.dto.OpenAiStreamResponse
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.TimeoutException

@Service
class OpenAiService(
    private val openAiWebClient: WebClient,
    private val objectMapper: ObjectMapper,
    @Value("\${openai.model}") private val defaultModel: String,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private val REQUEST_TIMEOUT: Duration = Duration.ofSeconds(30)
        private const val MAX_STREAM_PARSE_FAILURES = 5
    }

    fun chat(
        messages: List<OpenAiMessage>,
        model: String? = null,
    ): String {
        val request =
            OpenAiRequest(
                model = model ?: defaultModel,
                messages = messages,
                stream = false,
            )

        return try {
            val response =
                openAiWebClient
                    .post()
                    .uri("/v1/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono<OpenAiResponse>()
                    .timeout(REQUEST_TIMEOUT)
                    .block()
                    ?: throw AppException(ErrorCode.OPENAI_API_ERROR)

            response.choices
                .firstOrNull()
                ?.message
                ?.content
                ?: throw AppException(ErrorCode.OPENAI_API_ERROR)
        } catch (e: AppException) {
            throw e
        } catch (e: WebClientResponseException) {
            log.error("OpenAI API 호출 실패: status={}", e.statusCode)
            throw AppException(ErrorCode.OPENAI_API_ERROR)
        } catch (e: WebClientRequestException) {
            log.error("OpenAI API 연결 실패: {}", e.message)
            throw AppException(ErrorCode.OPENAI_API_ERROR)
        } catch (e: TimeoutException) {
            log.error("OpenAI API 타임아웃: {}ms 초과", REQUEST_TIMEOUT.toMillis())
            throw AppException(ErrorCode.OPENAI_API_ERROR)
        } catch (e: IllegalStateException) {
            log.error("OpenAI API 응답 처리 중 오류: {}", e.message)
            throw AppException(ErrorCode.OPENAI_API_ERROR)
        }
    }

    fun chatStream(
        messages: List<OpenAiMessage>,
        model: String? = null,
    ): Flux<String> {
        val request =
            OpenAiRequest(
                model = model ?: defaultModel,
                messages = messages,
                stream = true,
            )

        var parseFailureCount = 0

        return openAiWebClient
            .post()
            .uri("/v1/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToFlux<ServerSentEvent<String>>()
            .filter { event ->
                val data = event.data()
                data != null && data != "[DONE]"
            }
            .map { event ->
                try {
                    val streamResponse = objectMapper.readValue(event.data(), OpenAiStreamResponse::class.java)
                    streamResponse.choices.firstOrNull()?.delta?.content ?: ""
                } catch (e: com.fasterxml.jackson.core.JsonProcessingException) {
                    parseFailureCount++
                    log.warn("OpenAI 스트림 응답 파싱 실패 ({}/{}): {}", parseFailureCount, MAX_STREAM_PARSE_FAILURES, e.message)
                    if (parseFailureCount >= MAX_STREAM_PARSE_FAILURES) {
                        throw AppException(ErrorCode.OPENAI_API_ERROR)
                    }
                    ""
                }
            }
            .filter { it.isNotEmpty() }
            .onErrorMap { e ->
                if (e is AppException) {
                    e
                } else {
                    log.error("OpenAI 스트림 호출 중 오류 발생", e)
                    AppException(ErrorCode.OPENAI_API_ERROR)
                }
            }
    }
}
