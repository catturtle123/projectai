# Engineering Conventions Skill

프로젝트 엔지니어링 컨벤션을 정의합니다.

## Git 규칙

### 브랜치 전략
- `main` - 프로덕션
- `dev` - 개발
- `feat/{설명}` - 기능 개발
- `fix/{설명}` - 버그 수정
- `refactor/{설명}` - 리팩토링

### 커밋 메시지
- 한글 작성
- Tidy First: 구조/행동 분리
- 타입: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`

## DB 규칙 (PostgreSQL)

### 테이블 네이밍
- 소문자 + 언더스코어: `user_profiles`
- 복수형: `users`, `orders`

### 컬럼 네이밍
- 소문자 + 언더스코어: `created_at`, `user_name`
- PK: `id` (BIGSERIAL)
- FK: `{참조테이블단수}_id`

### 마이그레이션
- Flyway 또는 Liquibase 사용 권장
- 수동 DDL 직접 실행 금지
