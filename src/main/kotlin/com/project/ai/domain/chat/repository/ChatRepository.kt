package com.project.ai.domain.chat.repository

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findAllByThreadOrderByCreatedAtAsc(thread: Thread): List<Chat>

    fun findAllByThreadId(threadId: Long): List<Chat>
}
