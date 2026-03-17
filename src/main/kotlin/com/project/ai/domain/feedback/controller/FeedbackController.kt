package com.project.ai.domain.feedback.controller

import com.project.ai.domain.feedback.dto.FeedbackCreateRequest
import com.project.ai.domain.feedback.dto.FeedbackResponse
import com.project.ai.domain.feedback.service.FeedbackService
import com.project.ai.domain.user.entity.Role
import com.project.ai.global.common.BaseResponse
import com.project.ai.global.config.AuthenticatedUser
import com.project.ai.global.config.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/feedbacks")
@Tag(name = "Feedback", description = "피드백 API")
class FeedbackController(
    private val feedbackService: FeedbackService,
) {
    @PostMapping
    @Operation(summary = "피드백 생성")
    fun createFeedback(
        @CurrentUser user: AuthenticatedUser,
        @Valid @RequestBody request: FeedbackCreateRequest,
    ): ResponseEntity<BaseResponse<FeedbackResponse>> {
        val role = Role.valueOf(user.role.uppercase())
        val result = feedbackService.createFeedback(user.id, role, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(result))
    }
}
