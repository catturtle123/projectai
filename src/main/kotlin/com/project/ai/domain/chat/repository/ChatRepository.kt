package com.project.ai.domain.chat.repository

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ChatRepository : JpaRepository<Chat, Long> {
    @Query(
        "SELECT c FROM Chat c JOIN FETCH c.thread t JOIN FETCH t.user " +
            "WHERE c.createdAt > :after ORDER BY c.createdAt ASC",
    )
    fun findAllWithUserAfter(
        @Param("after") after: LocalDateTime,
    ): List<Chat>

    fun findAllByThreadOrderByCreatedAtAsc(thread: Thread): List<Chat>

    fun findAllByThreadIdOrderByCreatedAtAsc(threadId: Long): List<Chat>

    fun findAllByThreadIdInOrderByCreatedAtAsc(threadIds: List<Long>): List<Chat>

    fun findTopByThreadOrderByCreatedAtDesc(thread: Thread): Chat?

    @Modifying
    @Query("DELETE FROM Chat c WHERE c.thread.id = :threadId")
    fun deleteAllByThreadId(
        @Param("threadId") threadId: Long,
    )
}
