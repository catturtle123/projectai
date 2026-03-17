package com.project.ai.domain.analytics.repository

import com.project.ai.domain.analytics.entity.ActivityLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ActivityLogRepository : JpaRepository<ActivityLog, Long> {
    @Query(
        """
        SELECT a.activityType, COUNT(a)
        FROM ActivityLog a
        WHERE a.createdAt > :after
        GROUP BY a.activityType
    """,
    )
    fun countByActivityTypeSince(
        @Param("after") after: LocalDateTime,
    ): List<Array<Any>>
}
