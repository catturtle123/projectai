# TDD & Tidy First Skill

TDD(Test-Driven Development)와 Tidy First 원칙을 적용하는 스킬입니다.

## TDD 워크플로우

### 1. Red (실패하는 테스트 작성)
```kotlin
@Test
fun `사용자를 생성해야 한다`() {
    // given
    val request = CreateUserRequest(name = "홍길동", email = "hong@test.com")

    // when
    val result = userService.create(request)

    // then
    assertThat(result.name).isEqualTo("홍길동")
    assertThat(result.email).isEqualTo("hong@test.com")
}
```

### 2. Green (최소 구현)
- 테스트를 통과하는 최소한의 코드만 작성
- 과도한 설계 금지

### 3. Refactor (코드 개선)
- 테스트는 계속 통과하는 상태 유지
- 중복 제거, 네이밍 개선

## Tidy First 커밋 전략

구조적 변경과 행동적 변경을 분리하여 커밋합니다.

### 구조적 변경 (Structure)
행동 변경 없이 코드 구조만 변경:
- 파일/디렉토리/패키지 생성, 이동, 삭제
- 클래스/함수 이름 변경, 패키지 분할/병합
- import 경로 변경, 접근 제어자 변경

```
🏗️ structure: AiService를 domain/ai/ 패키지로 분리
```

### 행동적 변경 (Behavior)
기능이나 로직을 변경:
- 새 기능 추가, 버그 수정, 검증 규칙 변경

```
✨ feat: [기능 설명]
🐛 fix: [버그 설명]
♻️ refactor: [리팩토링 내용]
```

### 커밋 순서 원칙
1. **먼저 구조적 변경** → 테스트 통과 확인
2. **그 다음 행동적 변경** → 테스트 통과 확인
3. **절대 같은 커밋에 혼합하지 않음**

## 테스트 작성 가이드

### 단위 테스트
- Service 레이어: Mock을 사용한 격리 테스트
- 빠른 실행, 외부 의존성 없음

### 통합 테스트
- `@SpringBootTest` 사용
- 실제 DB 연결 (테스트용 DB)
- API 엔드포인트 전체 흐름 테스트
