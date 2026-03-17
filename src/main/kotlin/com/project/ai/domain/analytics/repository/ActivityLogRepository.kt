package com.project.ai.domain.analytics.repository

import com.project.ai.domain.analytics.entity.ActivityLog
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ActivityLogRepository : JpaRepository<ActivityLog, Long> {
    fun countByActivityTypeAndCreatedAtAfter(
        activityType: String,
        after: LocalDateTime,
    ): Long
}
