package com.project.ai.domain.chat.service

import com.project.ai.domain.chat.dto.ChatResponse
import com.project.ai.domain.chat.dto.ThreadResponse
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
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
    fun getChats(
        userId: Long,
        role: String,
        page: Int,
        size: Int,
        sort: String,
    ): Page<ThreadResponse> {
        val sortDirection = if (sort == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, "createdAt"))

        val threads =
            if (role == "admin") {
                threadRepository.findAll(pageable)
            } else {
                threadRepository.findAllByUserId(userId, pageable)
            }

        return threads.map { thread ->
            val chats = chatRepository.findAllByThreadIdOrderByCreatedAtAsc(thread.id)
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
