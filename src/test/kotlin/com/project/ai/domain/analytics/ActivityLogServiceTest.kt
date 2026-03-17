package com.project.ai.domain.analytics

import com.project.ai.domain.analytics.entity.ActivityLog
import com.project.ai.domain.analytics.entity.ActivityType
import com.project.ai.domain.analytics.repository.ActivityLogRepository
import com.project.ai.domain.analytics.service.ActivityLogService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then

@ExtendWith(MockitoExtension::class)
class ActivityLogServiceTest {
    @Mock
    private lateinit var activityLogRepository: ActivityLogRepository

    @InjectMocks
    private lateinit var activityLogService: ActivityLogService

    @Test
    fun `활동 로그를 저장해야 한다`() {
        // given
        given(activityLogRepository.save(any<ActivityLog>()))
            .willAnswer { it.arguments[0] as ActivityLog }

        // when
        activityLogService.log(ActivityType.LOGIN, 1L)

        // then
        then(activityLogRepository).should().save(any<ActivityLog>())
    }

    @Test
    fun `저장 실패해도 예외를 던지지 않아야 한다`() {
        // given
        given(activityLogRepository.save(any<ActivityLog>()))
            .willThrow(RuntimeException("DB error"))

        // when — should not throw
        activityLogService.log(ActivityType.SIGNUP, 1L)

        // then
        then(activityLogRepository).should().save(any<ActivityLog>())
    }
}
