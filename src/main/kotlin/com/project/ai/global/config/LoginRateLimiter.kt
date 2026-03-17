package com.project.ai.global.config

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class LoginRateLimiter {
    companion object {
        private const val MAX_ATTEMPTS = 5
        private const val BLOCK_DURATION_MS = 300_000L // 5분
    }

    private val attempts = ConcurrentHashMap<String, AttemptInfo>()

    fun isBlocked(email: String): Boolean {
        val info = attempts[email] ?: return false
        if (System.currentTimeMillis() - info.firstAttemptTime > BLOCK_DURATION_MS) {
            attempts.remove(email)
            return false
        }
        return info.count.get() >= MAX_ATTEMPTS
    }

    fun recordFailure(email: String) {
        attempts.compute(email) { _, existing ->
            if (existing == null || System.currentTimeMillis() - existing.firstAttemptTime > BLOCK_DURATION_MS) {
                AttemptInfo(AtomicInteger(1), System.currentTimeMillis())
            } else {
                existing.count.incrementAndGet()
                existing
            }
        }
    }

    fun resetAttempts(email: String) {
        attempts.remove(email)
    }

    data class AttemptInfo(
        val count: AtomicInteger,
        val firstAttemptTime: Long,
    )
}
