package com.project.ai.domain.chat.controller

import com.project.ai.domain.chat.dto.ChatCreateRequest
import com.project.ai.domain.chat.dto.ChatCreateResponse
import com.project.ai.domain.chat.service.ChatService
import com.project.ai.global.common.BaseResponse
import com.project.ai.global.config.AuthenticatedUser
import com.project.ai.global.config.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/chats")
@Tag(name = "Chat", description = "대화 API")
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping
    @Operation(summary = "대화 생성")
    fun createChat(
        @CurrentUser user: AuthenticatedUser,
        @Valid @RequestBody request: ChatCreateRequest,
    ): ResponseEntity<BaseResponse<ChatCreateResponse>> {
        val result = chatService.createChat(user.id, request)
        return ResponseEntity.ok(BaseResponse.success(result))
    }

    @PostMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @Operation(summary = "대화 생성 (스트리밍)")
    fun createChatStream(
        @CurrentUser user: AuthenticatedUser,
        @Valid @RequestBody request: ChatCreateRequest,
    ): Flux<ServerSentEvent<String>> {
        val (_, flux) = chatService.createChatStream(user.id, request)
        return flux.map { content ->
            ServerSentEvent.builder(content).build()
        }
    }
}
