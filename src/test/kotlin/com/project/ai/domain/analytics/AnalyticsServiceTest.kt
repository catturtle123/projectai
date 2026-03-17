package com.project.ai.domain.analytics

import com.project.ai.domain.analytics.entity.ActivityType
import com.project.ai.domain.analytics.repository.ActivityLogRepository
import com.project.ai.domain.analytics.service.AnalyticsService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class AnalyticsServiceTest {
    @Mock
    private lateinit var activityLogRepository: ActivityLogRepository

    @InjectMocks
    private lateinit var analyticsService: AnalyticsService

    @Test
    fun `활동 통계가 올바르게 반환되어야 한다`() {
        // given
        given(activityLogRepository.countByActivityTypeSince(any()))
            .willReturn(
                listOf(
                    arrayOf(ActivityType.SIGNUP as Any, 5L as Any),
                    arrayOf(ActivityType.LOGIN as Any, 10L as Any),
                    arrayOf(ActivityType.CHAT_CREATE as Any, 3L as Any),
                ),
            )

        // when
        val result = analyticsService.getActivitySummary()

        // then
        assertThat(result.signupCount).isEqualTo(5L)
        assertThat(result.loginCount).isEqualTo(10L)
        assertThat(result.chatCreateCount).isEqualTo(3L)
        assertThat(result.periodStart).isBefore(result.periodEnd)
    }

    @Test
    fun `활동이 없을 때 모든 카운트가 0이어야 한다`() {
        // given
        given(activityLogRepository.countByActivityTypeSince(any()))
            .willReturn(emptyList())

        // when
        val result = analyticsService.getActivitySummary()

        // then
        assertThat(result.signupCount).isEqualTo(0L)
        assertThat(result.loginCount).isEqualTo(0L)
        assertThat(result.chatCreateCount).isEqualTo(0L)
    }
}
