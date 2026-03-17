# ProjectAI - AI 챗봇 API 서버

Kotlin + Spring Boot 기반의 AI 챗봇 백엔드 서비스입니다.

## 기술 스택

- Kotlin 1.9.x / Spring Boot 3.4.x / Gradle (Kotlin DSL)
- Java 17 / PostgreSQL 15.8+ / JPA
- JWT 인증 / OpenAI API 연동
- Swagger UI (API 문서)

## 빠른 시작

### 1. 사전 요구사항

- Java 17+
- PostgreSQL 15.8+
- OpenAI API Key

### 2. 데이터베이스 준비

```bash
createdb projectai
```

### 3. 환경변수 설정

```bash
# 필수
export JWT_SECRET="your-secret-key-must-be-at-least-32-bytes"
export OPENAI_API_KEY="sk-your-openai-api-key"

# 관리자 계정 (설정 시 앱 시작 시 자동 생성)
export ADMIN_EMAIL="admin@example.com"
export ADMIN_PASSWORD="admin1234"

# 선택 (기본값 사용 가능)
export DB_URL="jdbc:postgresql://localhost:5432/projectai"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
```

### 4. 실행

```bash
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 시작됩니다.

### 5. API 문서 확인

Swagger UI: http://localhost:8080/swagger-ui/index.html

## API 사용 가이드

### 인증

#### 회원가입
```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","name":"사용자"}'
```

#### 로그인
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

응답의 `result.accessToken`을 이후 요청의 `Authorization: Bearer {token}` 헤더에 사용합니다.

### AI 대화

#### 대화 생성 (일반)
```bash
curl -X POST http://localhost:8080/api/v1/chats \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"question":"AI란 무엇인가요?"}'
```

#### 대화 생성 (스트리밍)
```bash
curl -X POST http://localhost:8080/api/v1/chats \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"question":"AI란 무엇인가요?","isStreaming":true}'
```

#### 대화 생성 (모델 지정)
```bash
curl -X POST http://localhost:8080/api/v1/chats \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"question":"질문","model":"gpt-4o"}'
```

#### 대화 목록 조회
```bash
curl http://localhost:8080/api/v1/chats?page=0&size=20&sort=desc \
  -H "Authorization: Bearer {token}"
```

#### 스레드 삭제
```bash
curl -X DELETE http://localhost:8080/api/v1/threads/{threadId} \
  -H "Authorization: Bearer {token}"
```

### 피드백

#### 피드백 생성
```bash
curl -X POST http://localhost:8080/api/v1/feedbacks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"chatId":1,"isPositive":true}'
```

#### 피드백 목록 조회
```bash
curl "http://localhost:8080/api/v1/feedbacks?isPositive=true&page=0&size=20&sort=desc" \
  -H "Authorization: Bearer {token}"
```

#### 피드백 상태 변경 (관리자)
```bash
curl -X PATCH http://localhost:8080/api/v1/feedbacks/{feedbackId}/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {admin_token}" \
  -d '{"status":"RESOLVED"}'
```

### 분석/보고 (관리자)

#### 활동 기록 조회
```bash
curl http://localhost:8080/api/v1/admin/analytics/activity \
  -H "Authorization: Bearer {admin_token}"
```

#### CSV 보고서 다운로드
```bash
curl http://localhost:8080/api/v1/admin/reports/chats \
  -H "Authorization: Bearer {admin_token}" \
  -o report.csv
```

## 스레드 규칙

- 유저의 첫 질문이거나 마지막 질문 후 30분이 지난 시점에 새 스레드가 생성됩니다.
- 30분 이내 재질문 시 기존 스레드가 유지되며, 이전 대화 컨텍스트가 AI에 전달됩니다.

## 권한

| 역할 | 설명 |
|------|------|
| member | 본인의 대화/피드백 생성/조회, 본인 스레드 삭제 |
| admin | 전체 대화/피드백 조회, 피드백 상태 변경, 활동 기록/보고서 조회 |

## 테스트

```bash
./gradlew test
```

---

## 과제 수행 과정

### 1. 과제를 어떻게 분석했나요?

요구사항을 **필수 기능**(인증, 대화, 피드백, 분석)과 **부가 기능**(스트리밍, 모델 선택, CSV 보고서)으로 분류하고, 각 기능의 세부 요구사항(스레드 30분 규칙, 권한별 접근 제어, 페이지네이션 등)을 정리했습니다.

이를 GitHub 이슈로 나누어 구현 계획을 세웠고, 의존성을 고려해 Phase별로 그룹화했습니다:
- **Phase 1**: 엔티티/리포지토리 (Thread, Chat, Feedback)
- **Phase 2**: OpenAI 연동 + 대화 API
- **Phase 3**: 피드백 API
- **Phase 4**: 분석/보고 API

각 Phase에서 가장 필수적인 부분을 우선으로 구현하고, 이슈별로 브랜치를 분리하여 PR → 리뷰 → 머지 흐름을 유지했습니다.

### 2. AI를 어떻게 활용했나요? 어떤 어려움이 있었나요?

Claude Code를 활용하여 다음 작업을 수행했습니다:
- **이슈 생성 및 구현 계획**: 요구사항 분석 후 11개 이슈를 edge case까지 포함하여 작성
- **병렬 구현**: Phase별로 독립적인 이슈를 worktree 기반 병렬 구현으로 속도를 높임
- **PR 리뷰 반영**: 별도 AI(Claude, Codex)로 코드 리뷰를 진행하고, N+1 쿼리 문제, 벌크 삭제 최적화, 트랜잭션 이슈 등의 피드백을 반영
- **E2E 테스트**: 실제 서버에 요청을 보내 전체 요구사항을 검증하고, 누락된 에러 처리(AccessDeniedException 403) 발견 및 수정
- **보안 리뷰**: JWT 시크릿 하드코딩, brute-force 방어, Swagger 노출 등 보안 취약점을 점검하고 개선

어려웠던 점은 **AI가 생성한 코드의 일관성 관리**였습니다. 병렬로 여러 이슈를 구현하면 브랜치 간 충돌이 발생하고, 서로 다른 패턴(QueryService 분리 등)이 생기기도 했습니다. 이를 머지 후 리팩터링으로 통일하는 과정이 필요했습니다.

### 3. 가장 어려웠던 기능

**OpenAI 외부 API 연동과 스트리밍 처리**가 가장 까다로웠습니다.

- **타임아웃/에러 처리**: 외부 API 호출은 네트워크 지연, 타임아웃, 응답 파싱 실패 등 다양한 실패 시나리오가 있어 구체적 예외별 처리가 필요했습니다
- **SSE 스트리밍**: Spring MVC 환경에서 `Flux` 기반 스트리밍을 `SseEmitter`로 전환하는 과정에서 호환성 문제가 있었고, 스트리밍 완료 후 트랜잭션 바깥에서 DB 저장을 해야 하는 `@Transactional` + `doOnComplete` 이슈도 해결이 필요했습니다
- **스레드 30분 타임아웃**: 마지막 대화 시점 기반으로 스레드를 재사용할지 판단하는 로직에서 빈 스레드, 경계값, 동시 요청 등 edge case를 고려해야 했습니다
