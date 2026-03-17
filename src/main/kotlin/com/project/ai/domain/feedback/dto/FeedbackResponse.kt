package com.project.ai.domain.feedback.dto

import java.time.LocalDateTime

data class FeedbackResponse(
    val feedbackId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: String,
    val createdAt: LocalDateTime,
)
