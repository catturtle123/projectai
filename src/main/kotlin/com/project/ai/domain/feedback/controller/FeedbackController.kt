package com.project.ai.domain.feedback.controller

import com.project.ai.domain.feedback.dto.FeedbackResponse
import com.project.ai.domain.feedback.dto.FeedbackStatusUpdateRequest
import com.project.ai.domain.feedback.service.FeedbackService
import com.project.ai.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/feedbacks")
@Tag(name = "Feedback", description = "피드백 API")
class FeedbackController(
    private val feedbackService: FeedbackService,
) {
    @PatchMapping("/{feedbackId}/status")
    @Operation(summary = "피드백 상태 변경", description = "관리자만 가능합니다")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateFeedbackStatus(
        @PathVariable feedbackId: Long,
        @Valid @RequestBody request: FeedbackStatusUpdateRequest,
    ): ResponseEntity<BaseResponse<FeedbackResponse>> {
        val result = feedbackService.updateFeedbackStatus(feedbackId, request)
        return ResponseEntity.ok(BaseResponse.success(result))
    }
}
