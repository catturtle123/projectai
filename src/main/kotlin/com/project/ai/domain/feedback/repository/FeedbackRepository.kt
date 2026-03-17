package com.project.ai.domain.feedback.repository

import com.project.ai.domain.feedback.entity.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

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

    @Modifying
    @Query("DELETE FROM Feedback f WHERE f.chat.id IN (SELECT c.id FROM Chat c WHERE c.thread.id = :threadId)")
    fun deleteAllByThreadId(
        @Param("threadId") threadId: Long,
    )
}
