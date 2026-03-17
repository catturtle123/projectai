package com.project.ai.domain.analytics.service

import com.project.ai.domain.analytics.dto.ActivityResponse
import com.project.ai.domain.analytics.repository.ActivityLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AnalyticsService(
    private val activityLogRepository: ActivityLogRepository,
) {
    fun getActivitySummary(): ActivityResponse {
        val now = LocalDateTime.now()
        val oneDayAgo = now.minusDays(1)

        return ActivityResponse(
            signupCount = activityLogRepository.countByActivityTypeAndCreatedAtAfter("SIGNUP", oneDayAgo),
            loginCount = activityLogRepository.countByActivityTypeAndCreatedAtAfter("LOGIN", oneDayAgo),
            chatCreateCount = activityLogRepository.countByActivityTypeAndCreatedAtAfter("CHAT_CREATE", oneDayAgo),
            periodStart = oneDayAgo,
            periodEnd = now,
        )
    }
}
