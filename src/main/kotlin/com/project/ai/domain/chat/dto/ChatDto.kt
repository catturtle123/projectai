package com.project.ai.domain.chat.dto

import java.time.LocalDateTime

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
