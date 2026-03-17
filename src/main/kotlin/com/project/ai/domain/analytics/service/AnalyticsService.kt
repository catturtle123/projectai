package com.project.ai.domain.analytics.service

import com.project.ai.domain.analytics.dto.ActivityResponse
import com.project.ai.domain.analytics.entity.ActivityType
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

        val counts = activityLogRepository.countByActivityTypeSince(oneDayAgo)
        val countMap =
            counts.associate { row ->
                (row[0] as ActivityType) to (row[1] as Long)
            }

        return ActivityResponse(
            signupCount = countMap[ActivityType.SIGNUP] ?: 0L,
            loginCount = countMap[ActivityType.LOGIN] ?: 0L,
            chatCreateCount = countMap[ActivityType.CHAT_CREATE] ?: 0L,
            periodStart = oneDayAgo,
            periodEnd = now,
        )
    }
}
