package com.project.ai.domain.chat.service

import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
import com.project.ai.domain.feedback.repository.FeedbackRepository
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val feedbackRepository: FeedbackRepository,
) {
    @Transactional
    fun deleteThread(
        userId: Long,
        threadId: Long,
    ) {
        val thread =
            threadRepository.findById(threadId)
                .orElseThrow { AppException(ErrorCode.THREAD_NOT_FOUND) }

        if (thread.userId != userId) {
            throw AppException(ErrorCode.THREAD_ACCESS_DENIED)
        }

        feedbackRepository.deleteAllByThreadId(threadId)
        chatRepository.deleteAllByThreadId(threadId)
        threadRepository.delete(thread)
    }
}
