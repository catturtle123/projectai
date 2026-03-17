package com.project.ai.domain.chat.repository

import com.project.ai.domain.chat.entity.Thread
import org.springframework.data.jpa.repository.JpaRepository

interface ThreadRepository : JpaRepository<Thread, Long> {
    fun findTopByUserIdOrderByCreatedAtDesc(userId: Long): Thread?

    fun findAllByUserId(userId: Long): List<Thread>
}
