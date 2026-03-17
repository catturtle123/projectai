package com.project.ai.domain.feedback.service

import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.feedback.dto.FeedbackCreateRequest
import com.project.ai.domain.feedback.dto.FeedbackResponse
import com.project.ai.domain.feedback.dto.FeedbackStatusUpdateRequest
import com.project.ai.domain.feedback.entity.Feedback
import com.project.ai.domain.feedback.repository.FeedbackRepository
import com.project.ai.domain.user.entity.Role
import com.project.ai.domain.user.repository.UserRepository
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun createFeedback(
        userId: Long,
        role: Role,
        request: FeedbackCreateRequest,
    ): FeedbackResponse {
        val chat =
            chatRepository.findById(request.chatId)
                .orElseThrow { AppException(ErrorCode.CHAT_NOT_FOUND) }

        if (role == Role.MEMBER && chat.thread.userId != userId) {
            throw AppException(ErrorCode.FEEDBACK_ACCESS_DENIED)
        }

        if (feedbackRepository.existsByUserIdAndChatId(userId, request.chatId)) {
            throw AppException(ErrorCode.DUPLICATE_FEEDBACK)
        }

        val user =
            userRepository.findById(userId)
                .orElseThrow { AppException(ErrorCode.USER_NOT_FOUND) }

        val feedback =
            feedbackRepository.save(
                Feedback(
                    isPositive = request.isPositive,
                    user = user,
                    chat = chat,
                ),
            )

        return FeedbackResponse(
            feedbackId = feedback.id,
            chatId = chat.id,
            isPositive = feedback.isPositive,
            status = feedback.status,
            createdAt = feedback.createdAt,
            updatedAt = feedback.updatedAt,
        )
    }

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

    fun getFeedbacks(
        userId: Long,
        role: Role,
        isPositive: Boolean?,
        page: Int,
        size: Int,
        sort: String,
    ): Page<FeedbackResponse> {
        if (sort !in setOf("asc", "desc")) throw AppException(ErrorCode.VALIDATION_001)
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
                chatId = feedback.chatId,
                isPositive = feedback.isPositive,
                status = feedback.status,
                createdAt = feedback.createdAt,
                updatedAt = feedback.updatedAt,
            )
        }
    }
}
