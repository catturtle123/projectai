package com.project.ai.domain.feedback.service

import com.project.ai.domain.feedback.dto.FeedbackResponse
import com.project.ai.domain.feedback.dto.FeedbackStatusUpdateRequest
import com.project.ai.domain.feedback.repository.FeedbackRepository
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
) {
    @Transactional
    fun updateFeedbackStatus(
        feedbackId: Long,
        request: FeedbackStatusUpdateRequest,
    ): FeedbackResponse {
        val feedback =
            feedbackRepository.findById(feedbackId)
                .orElseThrow { AppException(ErrorCode.FEEDBACK_NOT_FOUND) }

        feedback.status = request.status

        return FeedbackResponse(
            feedbackId = feedback.id,
            chatId = feedback.chat.id,
            isPositive = feedback.isPositive,
            status = feedback.status,
            createdAt = feedback.createdAt,
            updatedAt = feedback.updatedAt,
        )
    }
}
