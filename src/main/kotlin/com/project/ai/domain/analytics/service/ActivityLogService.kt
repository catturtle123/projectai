package com.project.ai.domain.analytics.service

import com.project.ai.domain.analytics.entity.ActivityLog
import com.project.ai.domain.analytics.repository.ActivityLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ActivityLogService(
    private val activityLogRepository: ActivityLogRepository,
) {
    @Transactional
    fun log(
        activityType: String,
        userId: Long,
    ) {
        activityLogRepository.save(ActivityLog(activityType = activityType, userId = userId))
    }
}
