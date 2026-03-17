package com.project.ai.domain.analytics.dto

import java.time.LocalDateTime

data class ActivityResponse(
    val signupCount: Long,
    val loginCount: Long,
    val chatCreateCount: Long,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
)
