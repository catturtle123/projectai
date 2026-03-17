package com.project.ai.domain.analytics.service

import com.project.ai.domain.analytics.entity.ActivityLog
import com.project.ai.domain.analytics.entity.ActivityType
import com.project.ai.domain.analytics.repository.ActivityLogRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class ActivityLogService(
    private val activityLogRepository: ActivityLogRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun log(
        activityType: ActivityType,
        userId: Long,
    ) {
        try {
            activityLogRepository.save(ActivityLog(activityType = activityType, userId = userId))
        } catch (e: Exception) {
            log.warn("활동 로그 저장 실패: type={}, userId={}, error={}", activityType, userId, e.message)
        }
    }
}
