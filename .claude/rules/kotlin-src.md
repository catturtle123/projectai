---
globs: src/main/kotlin/**/*.kt
---

# Kotlin 소스 코드 규칙

이 규칙은 `src/main/kotlin/` 하위의 모든 `.kt` 파일에 적용됩니다.

## 네이밍 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `UserService`, `OrderController` |
| 함수/변수 | camelCase | `findById`, `userName` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 패키지 | lowercase | `com.project.ai.domain.user` |

## Kotlin 특화 규칙

- `!!` (non-null assertion) 사용 금지 — `?:`, `?.let`, `requireNotNull` 사용
- `var` 보다 `val` 우선 사용
- 생성자 주입 사용 (`@Autowired` / `lateinit var` 금지)
- data class는 DTO에만 사용, Entity에는 일반 class 사용

## 에러 처리

- 비즈니스 예외는 `AppException(ErrorCode.XXX)` 사용
- `GlobalExceptionHandler`에서 일괄 처리
- `catch (e: Exception)` 남발 금지 — 구체적 예외 타입 사용

## DTO / 직렬화

- DTO 필드는 `camelCase` (Jackson 기본)
- API 응답은 `BaseResponse` 래핑
- `@Valid`로 요청 검증

## 로깅

- SLF4J + Logback 사용 (`private val log = LoggerFactory.getLogger(this::class.java)`)
- 민감 정보(비밀번호, 토큰 등) 로그 출력 금지

## 레이어별 규칙

### Controller
- HTTP 요청/응답 처리만 담당, 비즈니스 로직 금지
- `@Valid`로 요청 검증, `BaseResponse`로 응답 래핑
- Swagger 어노테이션 필수 (`@Tag`, `@Operation`)

### Service
- `@Transactional(readOnly = true)` 기본, 변경 메서드에만 `@Transactional`
- 비즈니스 예외는 `AppException` throw
- 다른 도메인 Service 직접 참조 최소화

### Entity
- `class` 사용 (`data class` 금지)
- `BaseTimeEntity` 상속 (createdAt, updatedAt)
- `@Column` 명시적 지정
- 연관관계는 지연 로딩 (`FetchType.LAZY`) 기본

### Repository
- `JpaRepository<Entity, Long>` 상속
- 메서드 쿼리 우선, 복잡한 건 `@Query`
- 페이징: `Pageable` 파라미터 사용

## 코드 스타일

```kotlin
// 좋은 예
@RestController
@RequestMapping("/api")
class MyController(
    private val myService: MyService
) {
    @GetMapping("/items/{id}")
    fun getItem(@PathVariable id: Long): ResponseEntity<BaseResponse<ItemResponse>> {
        val result = myService.findById(id)
        return ResponseEntity.ok(BaseResponse.success(result))
    }
}

// 나쁜 예
@RestController
class MyController {
    @Autowired
    lateinit var myService: MyService

    @GetMapping("/items/{id}")
    fun getItem(@PathVariable id: Long): ItemResponse {
        return myService.findById(id)!!  // !! 사용 금지
    }
}
```
