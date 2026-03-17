# Fix GitHub Issue Skill

GitHub 이슈를 확인하고, 코드를 작성하고, PR까지 올리는 전체 플로우입니다.

## 전체 플로우

### 1. 이슈 확인
- `gh issue view <이슈번호>`로 이슈 내용 파악
- 이슈 라벨 확인 (feat, fix, refactor 등)

### 2. 브랜치 준비
- 현재 브랜치가 `main`이 아니면 `git stash`로 임시 저장
- `main`에서 최신 pull 후 새 브랜치 생성 (`feat/설명` 또는 `fix/설명`)

### 3. 코드 구현
- 요구사항 분석
- 엔티티, 리포지토리, 서비스, 컨트롤러, DTO 순서로 구현
- 구현 완료 후 컴파일 확인: `./gradlew compileKotlin`

### 4. 테스트 작성
- 구현한 코드에 대한 테스트 작성
- 단위 테스트 (Service: Mock 기반)
- 통합 테스트 (API: MockMvc)
- given-when-then 패턴

### 5. 테스트 실행
```bash
./gradlew test
```
- 모든 테스트 통과 확인
- 실패 시 코드 수정 후 재실행

### 6. 린트 확인 및 수정
```bash
./gradlew ktlintCheck       # 확인
./gradlew ktlintFormat      # 자동 수정
```

### 7. 빌드 검증
```bash
./gradlew build
```

### 8. 커밋 및 Push
- Tidy First: 구조적 변경과 행동적 변경 분리 커밋
- 커밋 메시지는 한글로 작성, `Closes #이슈번호` 포함
- 브랜치 push

### 9. PR 생성
- PR 생성 형식은 `.claude/skills/create-pr/skill.md`를 따름
- PR 타이틀: `type: 설명` (이모지 절대 금지, CI 실패 원인)
- PR 본문에 `Closes #이슈번호`로 이슈 자동 연결
- base 브랜치: `main`

### 10. 원래 브랜치 복원
- stash한 내용이 있으면 원래 브랜치로 돌아가서 `git stash pop`
- 충돌 시 수동 해결 안내
