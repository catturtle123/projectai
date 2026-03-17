package com.project.ai.domain.chat.repository

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findAllByThreadOrderByCreatedAtAsc(thread: Thread): List<Chat>

    fun findAllByThreadIdOrderByCreatedAtAsc(threadId: Long): List<Chat>

    @Modifying
    @Query("DELETE FROM Chat c WHERE c.thread.id = :threadId")
    fun deleteAllByThreadId(
        @Param("threadId") threadId: Long,
    )
}
