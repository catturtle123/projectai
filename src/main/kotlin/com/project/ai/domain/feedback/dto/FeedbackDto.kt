package com.project.ai.domain.feedback.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class FeedbackCreateRequest(
    @field:NotNull
    val chatId: Long,
    @field:NotNull
    val isPositive: Boolean,
)

data class FeedbackResponse(
    val feedbackId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: String,
    val createdAt: LocalDateTime,
)
