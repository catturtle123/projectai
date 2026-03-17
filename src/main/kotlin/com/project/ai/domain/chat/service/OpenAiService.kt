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
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux

@Service
class OpenAiService(
    private val openAiWebClient: WebClient,
    private val objectMapper: ObjectMapper,
    @Value("\${openai.model}") private val defaultModel: String,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

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
            log.error("OpenAI API 호출 실패: status={}, body={}", e.statusCode, e.responseBodyAsString)
            throw AppException(ErrorCode.OPENAI_API_ERROR)
        } catch (e: Exception) {
            log.error("OpenAI API 호출 중 예상치 못한 오류 발생", e)
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
                } catch (e: Exception) {
                    log.warn("OpenAI 스트림 응답 파싱 실패: {}", e.message)
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
