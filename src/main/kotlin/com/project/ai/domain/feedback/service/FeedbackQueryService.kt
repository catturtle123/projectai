package com.project.ai.domain.feedback.service

import com.project.ai.domain.feedback.dto.FeedbackResponse
import com.project.ai.domain.feedback.repository.FeedbackRepository
import com.project.ai.domain.user.entity.Role
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FeedbackQueryService(
    private val feedbackRepository: FeedbackRepository,
) {
    fun getFeedbacks(
        userId: Long,
        role: Role,
        isPositive: Boolean?,
        page: Int,
        size: Int,
        sort: String,
    ): Page<FeedbackResponse> {
        if (sort !in setOf("asc", "desc")) throw AppException(ErrorCode.COMMON400)
        val direction = if (sort == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"))

        val feedbacks =
            when {
                role == Role.ADMIN && isPositive != null -> feedbackRepository.findAllByIsPositive(isPositive, pageable)
                role == Role.ADMIN -> feedbackRepository.findAll(pageable)
                isPositive != null -> feedbackRepository.findAllByUserIdAndIsPositive(userId, isPositive, pageable)
                else -> feedbackRepository.findAllByUserId(userId, pageable)
            }

        return feedbacks.map { feedback ->
            FeedbackResponse(
                feedbackId = feedback.id,
                chatId = feedback.chat.id,
                isPositive = feedback.isPositive,
                status = feedback.status.name.lowercase(),
                createdAt = feedback.createdAt,
            )
        }
    }
}
