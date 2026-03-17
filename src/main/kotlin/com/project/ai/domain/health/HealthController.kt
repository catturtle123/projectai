package com.project.ai.domain.health

import com.project.ai.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "헬스 체크 API")
class HealthController {
    @GetMapping
    @Operation(summary = "서버 상태 확인")
    fun health(): ResponseEntity<BaseResponse<Map<String, String>>> = ResponseEntity.ok(BaseResponse.success(mapOf("status" to "UP")))
}
