package com.project.ai.domain.chat.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ChatCreateRequest(
    @field:NotBlank(message = "질문은 필수 입력입니다")
    @field:Size(max = 10000, message = "질문은 10000자 이내여야 합니다")
    val question: String,
    val isStreaming: Boolean = false,
    val model: String? = null,
)

data class ChatCreateResponse(
    val chatId: Long,
    val threadId: Long,
    val question: String,
    val answer: String,
    val createdAt: LocalDateTime,
)

data class ThreadResponse(
    val threadId: Long,
    val createdAt: LocalDateTime,
    val chats: List<ChatResponse>,
)

data class ChatResponse(
    val chatId: Long,
    val question: String,
    val answer: String,
    val createdAt: LocalDateTime,
)
