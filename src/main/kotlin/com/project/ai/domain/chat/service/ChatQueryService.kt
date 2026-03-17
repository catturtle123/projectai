package com.project.ai.domain.chat.service

import com.project.ai.domain.chat.dto.ChatResponse
import com.project.ai.domain.chat.dto.ThreadResponse
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
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
class ChatQueryService(
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
) {
    companion object {
        private val VALID_SORT_VALUES = setOf("asc", "desc")
    }

    fun getChats(
        userId: Long,
        role: Role,
        page: Int,
        size: Int,
        sort: String,
    ): Page<ThreadResponse> {
        if (sort !in VALID_SORT_VALUES) {
            throw AppException(ErrorCode.COMMON400)
        }

        val sortDirection = if (sort == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, "createdAt"))

        val threads =
            if (role == Role.ADMIN) {
                threadRepository.findAll(pageable)
            } else {
                threadRepository.findAllByUserId(userId, pageable)
            }

        val threadIds = threads.content.map { it.id }
        val chatsByThreadId =
            if (threadIds.isNotEmpty()) {
                chatRepository.findAllByThreadIdInOrderByCreatedAtAsc(threadIds)
                    .groupBy { it.thread.id }
            } else {
                emptyMap()
            }

        return threads.map { thread ->
            val chats = chatsByThreadId[thread.id] ?: emptyList()
            ThreadResponse(
                threadId = thread.id,
                createdAt = thread.createdAt,
                chats =
                    chats.map { chat ->
                        ChatResponse(
                            chatId = chat.id,
                            question = chat.question,
                            answer = chat.answer,
                            createdAt = chat.createdAt,
                        )
                    },
            )
        }
    }
}
