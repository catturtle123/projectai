---
globs: src/main/kotlin/**/controller/*.kt, src/main/kotlin/**/dto/*.kt
---

# API 설계 규칙

API 컨트롤러와 DTO에 적용되는 규칙입니다.

## URL 규칙

- RESTful: `GET /api/v1/users/{id}`
- 복수형 사용: `/users`, `/orders`
- 소문자 + 하이픈: `/api/v1/user-profiles`

## DTO 구조

### Request
```kotlin
data class MyRequest(
    @field:NotBlank(message = "필수 입력입니다")
    val content: String,

    @field:Size(min = 1)
    val secretKey: String
)
```

### Response
```kotlin
data class MyResponse(
    val resultField: String,
    val createdAt: LocalDateTime
)
```

## 응답 형식

모든 API 응답은 다음 형식을 따릅니다:

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": { ... }
}
```

## 컨트롤러 패턴

```kotlin
@RestController
@RequestMapping("/api")
@Tag(name = "My API", description = "API 설명")
class MyController(
    private val myService: MyService
) {
    @PostMapping("/endpoint")
    @Operation(summary = "엔드포인트 설명")
    fun myEndpoint(
        @Valid @RequestBody request: MyRequest
    ): ResponseEntity<BaseResponse<MyResponse>> {
        val result = myService.process(request)
        return ResponseEntity.ok(BaseResponse.success(result))
    }
}
```

## 에러 코드

| 코드 | HTTP | 용도 |
|------|------|------|
| AUTH_001 | 401 | 인증 실패 |
| VALIDATION_001 | 400 | 유효하지 않은 입력 |
| COMMON400 | 400 | 일반 요청 오류 |
| COMMON500 | 500 | 서버 오류 |
