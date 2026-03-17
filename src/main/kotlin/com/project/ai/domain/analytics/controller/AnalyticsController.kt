package com.project.ai.domain.analytics.controller

import com.project.ai.domain.analytics.dto.ActivityResponse
import com.project.ai.domain.analytics.service.AnalyticsService
import com.project.ai.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/analytics")
@Tag(name = "Analytics", description = "분석 API (관리자 전용)")
class AnalyticsController(
    private val analyticsService: AnalyticsService,
) {
    @GetMapping("/activity")
    @Operation(summary = "사용자 활동 기록 조회", description = "최근 24시간 내 활동 통계")
    @PreAuthorize("hasRole('ADMIN')")
    fun getActivitySummary(): ResponseEntity<BaseResponse<ActivityResponse>> {
        val result = analyticsService.getActivitySummary()
        return ResponseEntity.ok(BaseResponse.success(result))
    }
}
