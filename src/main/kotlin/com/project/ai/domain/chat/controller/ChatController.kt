package com.project.ai.domain.chat.controller

import com.project.ai.domain.chat.dto.ChatCreateRequest
import com.project.ai.domain.chat.dto.ThreadResponse
import com.project.ai.domain.chat.service.ChatService
import com.project.ai.domain.user.entity.Role
import com.project.ai.global.common.BaseResponse
import com.project.ai.global.config.AuthenticatedUser
import com.project.ai.global.config.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import org.springframework.data.domain.Page
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors

@RestController
@RequestMapping("/api/v1/chats")
@Tag(name = "Chat", description = "대화 API")
class ChatController(
    private val chatService: ChatService,
) {
    private val executor = Executors.newCachedThreadPool()

    @PostMapping
    @Operation(summary = "대화 생성", description = "isStreaming=true 시 SSE 스트리밍 응답")
    fun createChat(
        @CurrentUser user: AuthenticatedUser,
        @Valid @RequestBody request: ChatCreateRequest,
    ): Any {
        if (request.isStreaming) {
            val emitter = SseEmitter(60_000L)
            val (_, flux) = chatService.createChatStream(user.id, request)

            executor.execute {
                flux.subscribe(
                    { content ->
                        try {
                            emitter.send(SseEmitter.event().data(content, MediaType.TEXT_PLAIN))
                        } catch (e: Exception) {
                            emitter.completeWithError(e)
                        }
                    },
                    { error -> emitter.completeWithError(error) },
                    { emitter.complete() },
                )
            }

            return emitter
        }
        val result = chatService.createChat(user.id, request)
        return ResponseEntity.ok(BaseResponse.success(result))
    }

    @GetMapping
    @Operation(summary = "대화 목록 조회", description = "스레드 단위로 그룹화된 대화 목록을 조회합니다")
    fun getChats(
        @CurrentUser user: AuthenticatedUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") @Max(100) size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
    ): ResponseEntity<BaseResponse<Page<ThreadResponse>>> {
        val role = Role.valueOf(user.role.uppercase())
        val result = chatService.getChats(user.id, role, page, size, sort)
        return ResponseEntity.ok(BaseResponse.success(result))
    }
}
