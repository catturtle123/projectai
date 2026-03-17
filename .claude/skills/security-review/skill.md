# Security Review Skill

코드 변경사항에 대한 보안 리뷰를 수행합니다.

## 언제 이 스킬을 사용하나요?

- 인증/인가 관련 코드 변경 시
- 사용자 입력을 처리하는 코드 작성 시
- 외부 API 연동 코드 작성 시
- DB 쿼리 관련 코드 변경 시

## 보안 점검 항목

### 1. SQL Injection
- JPA/JPQL 파라미터 바인딩 사용 여부 확인
- 네이티브 쿼리에서 문자열 연결 사용 금지
```kotlin
// 나쁜 예
@Query("SELECT u FROM User u WHERE u.name = '$name'")  // 위험!

// 좋은 예
@Query("SELECT u FROM User u WHERE u.name = :name")
fun findByName(@Param("name") name: String): User?
```

### 2. XSS (Cross-Site Scripting)
- 사용자 입력 데이터 이스케이프 처리
- `@Valid`로 입력값 검증

### 3. 인증/인가
- 민감한 엔드포인트에 적절한 인증 확인
- 권한 체크 누락 여부

### 4. 민감 정보 노출
- 로그에 비밀번호, 토큰 등 출력 금지
- 응답에 불필요한 내부 정보 포함 금지
- `application.yml`에 하드코딩된 비밀 정보 금지 → 환경 변수 사용

### 5. CORS / CSRF
- CORS 설정 적절성
- CSRF 보호 설정

## 보안 체크리스트

- [ ] SQL Injection 가능성 없음
- [ ] XSS 가능성 없음
- [ ] 인증/인가가 적절히 적용됨
- [ ] 민감 정보가 로그/응답에 노출되지 않음
- [ ] 환경변수로 비밀 정보 관리
- [ ] 입력값 검증이 적용됨
