package com.project.ai.domain.feedback.repository

import com.project.ai.domain.feedback.entity.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun findByUserIdAndChatId(
        userId: Long,
        chatId: Long,
    ): Feedback?

    fun findAllByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<Feedback>

    fun findAllByUserIdAndIsPositive(
        userId: Long,
        isPositive: Boolean,
        pageable: Pageable,
    ): Page<Feedback>

    fun findAllByIsPositive(
        isPositive: Boolean,
        pageable: Pageable,
    ): Page<Feedback>

    fun existsByUserIdAndChatId(
        userId: Long,
        chatId: Long,
    ): Boolean
}
