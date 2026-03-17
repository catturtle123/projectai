package com.project.ai.domain.chat.controller

import com.project.ai.domain.chat.service.ThreadService
import com.project.ai.global.common.BaseResponse
import com.project.ai.global.config.AuthenticatedUser
import com.project.ai.global.config.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/threads")
@Tag(name = "Thread", description = "스레드 API")
class ThreadController(
    private val threadService: ThreadService,
) {
    @DeleteMapping("/{threadId}")
    @Operation(summary = "스레드 삭제", description = "본인 스레드를 삭제합니다. 하위 대화와 피드백도 함께 삭제됩니다.")
    fun deleteThread(
        @CurrentUser user: AuthenticatedUser,
        @PathVariable threadId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        threadService.deleteThread(user.id, threadId)
        return ResponseEntity.ok(BaseResponse.success(Unit))
    }
}
