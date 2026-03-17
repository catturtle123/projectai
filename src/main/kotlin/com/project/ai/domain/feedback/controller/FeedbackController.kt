package com.project.ai.domain.feedback.controller

import com.project.ai.domain.feedback.dto.FeedbackResponse
import com.project.ai.domain.feedback.service.FeedbackQueryService
import com.project.ai.domain.user.entity.Role
import com.project.ai.global.common.BaseResponse
import com.project.ai.global.config.AuthenticatedUser
import com.project.ai.global.config.CurrentUser
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/feedbacks")
@Validated
@Tag(name = "Feedback", description = "피드백 API")
class FeedbackController(
    private val feedbackQueryService: FeedbackQueryService,
) {
    @GetMapping
    @Operation(summary = "피드백 목록 조회")
    fun getFeedbacks(
        @CurrentUser user: AuthenticatedUser,
        @RequestParam(required = false) isPositive: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") @Max(100) size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
    ): ResponseEntity<BaseResponse<Page<FeedbackResponse>>> {
        val role =
            try {
                Role.valueOf(user.role.uppercase())
            } catch (e: IllegalArgumentException) {
                throw AppException(ErrorCode.AUTH_001)
            }
        val result = feedbackQueryService.getFeedbacks(user.id, role, isPositive, page, size, sort)
        return ResponseEntity.ok(BaseResponse.success(result))
    }
}
