package com.project.ai.domain.analytics

import com.project.ai.domain.analytics.repository.ActivityLogRepository
import com.project.ai.domain.analytics.service.AnalyticsService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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
        given(activityLogRepository.countByActivityTypeAndCreatedAtAfter(eq("SIGNUP"), any()))
            .willReturn(5L)
        given(activityLogRepository.countByActivityTypeAndCreatedAtAfter(eq("LOGIN"), any()))
            .willReturn(10L)
        given(activityLogRepository.countByActivityTypeAndCreatedAtAfter(eq("CHAT_CREATE"), any()))
            .willReturn(3L)

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
        given(activityLogRepository.countByActivityTypeAndCreatedAtAfter(eq("SIGNUP"), any()))
            .willReturn(0L)
        given(activityLogRepository.countByActivityTypeAndCreatedAtAfter(eq("LOGIN"), any()))
            .willReturn(0L)
        given(activityLogRepository.countByActivityTypeAndCreatedAtAfter(eq("CHAT_CREATE"), any()))
            .willReturn(0L)

        // when
        val result = analyticsService.getActivitySummary()

        // then
        assertThat(result.signupCount).isEqualTo(0L)
        assertThat(result.loginCount).isEqualTo(0L)
        assertThat(result.chatCreateCount).isEqualTo(0L)
    }
}
