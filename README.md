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
