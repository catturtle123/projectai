package com.project.ai.domain.feedback.dto

import com.project.ai.domain.feedback.entity.FeedbackStatus
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class FeedbackStatusUpdateRequest(
    @field:NotNull(message = "상태는 필수 입력입니다")
    val status: FeedbackStatus,
)

data class FeedbackResponse(
    val feedbackId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: String,
    val createdAt: LocalDateTime,
)
