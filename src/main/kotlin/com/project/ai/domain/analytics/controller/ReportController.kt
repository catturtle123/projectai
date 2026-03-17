package com.project.ai.domain.analytics.controller

import com.project.ai.domain.analytics.service.ReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/admin/reports")
@Tag(name = "Reports", description = "보고서 API (관리자 전용)")
class ReportController(
    private val reportService: ReportService,
) {
    @GetMapping("/chats")
    @Operation(summary = "대화 보고서 생성", description = "최근 24시간 대화 목록 CSV 다운로드")
    @PreAuthorize("hasRole('ADMIN')")
    fun generateChatReport(): ResponseEntity<ByteArray> {
        val csv = reportService.generateChatReport()
        val filename = "chat-report-${LocalDate.now()}.csv"
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csv)
    }
}
