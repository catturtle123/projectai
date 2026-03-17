---
name: project_progress
description: 현재까지 구현 완료된 이슈 및 기능 목록
type: project
---

## 구현 완료

### Issue #1 — `chore: 프로젝트 기반 세팅 보완`
- Spring Security, JWT(jjwt), WebFlux, H2, springdoc-openapi, validation 의존성 추가
- application.yml: JWT, OpenAI 환경변수 설정
- application 프로필 분리 (local, dev, test)
- test 프로필: H2 인메모리 DB
- SecurityConfig: auth/health/swagger permitAll
- HealthController: `GET /api/v1/health`
- ktlint 플러그인 적용 + PostToolUse 훅 자동 포맷팅

### Issue #2 — `feat: User 엔티티 및 회원가입 API`
- User 엔티티 (email, password, name, role, BaseTimeEntity)
- UserRepository (findByEmail, existsByEmail)
- `POST /api/v1/auth/signup`
- BCrypt 암호화, 이메일 중복 검증
- 테스트 3개 (정상, 중복 이메일, 암호화 저장)

### Issue #3 — `feat: 로그인 및 JWT 발급 API`
- JwtProvider (생성, 검증, userId/email/role 파싱)
- `POST /api/v1/auth/login`
- 테스트 3개 (정상, 존재하지 않는 이메일, 잘못된 비밀번호)

### Issue #4 — `feat: JWT 인증 필터 및 SecurityConfig`
- JwtAuthenticationFilter (Bearer 토큰 추출, SecurityContext 설정)
- AuthenticatedUser (id, email, role)
- @CurrentUser 어노테이션
- AuthenticationEntryPoint (인증 실패 시 401)
- 통합 테스트 5개 (회원가입 API, 로그인 API, 401, 토큰 접근, 헬스체크)

## 미구현

- Issue #5: Thread/Chat 엔티티 및 대화 생성 API (OpenAI 연동)
- Issue #6: 대화 스트리밍 응답 (SSE)
- Issue #7: 대화 목록 조회 API
- Issue #8: 스레드 삭제 API
- Issue #9: 피드백 생성 API
- Issue #10: 피드백 목록 조회 API
- Issue #11: 피드백 상태 변경 API
- Issue #12: 사용자 활동 기록 조회 API
- Issue #13: CSV 보고서 생성 API
