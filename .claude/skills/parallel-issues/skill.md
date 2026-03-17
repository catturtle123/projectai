# Parallel Issues Skill

여러 이슈를 git worktree를 활용해 병렬로 구현하고, 각각 PR을 올리는 스킬입니다.

## 언제 이 스킬을 사용하나요?

- 독립적인 이슈 여러 개를 동시에 처리할 때
- 이슈 간 의존성이 없어 병렬 작업이 가능할 때

## 전체 플로우

### 1. 이슈 분석
- 전달받은 이슈 목록을 확인
- 이슈 간 의존성 파악 (의존성 있으면 순서 조정)
- 각 이슈별 브랜치명 결정 (`feat/설명`, `fix/설명` 등)

### 2. Agent worktree로 병렬 구현
각 이슈마다 별도의 Agent를 `isolation: "worktree"`로 실행합니다.

```
Agent(
  isolation: "worktree",
  prompt: "이슈 내용 + 구현 지시"
)
```

각 Agent는 독립된 worktree에서:
1. `main` 기준으로 새 브랜치 생성
2. 코드 구현
3. `./gradlew ktlintFormat` — 린트 자동 수정
4. `./gradlew ktlintCheck` — 린트 통과 확인
5. `./gradlew test` — 전체 테스트 통과 확인
6. `./gradlew build` — 빌드 확인
7. 커밋 (한글, `Closes #이슈번호` 포함)
8. 브랜치 push

### 3. PR 생성
각 Agent의 작업 완료 후:
- `.claude/skills/create-pr/skill.md` 형식에 따라 PR 생성
- base 브랜치: `main`
- PR 타이틀: `type: 설명` (이모지 금지)
- PR 본문에 `Closes #이슈번호`

### 4. 결과 보고
모든 Agent 완료 후 요약:
- 이슈별 PR 링크
- 성공/실패 여부
- 실패 시 원인

## 사용 예시

```
이슈 #5, #6, #7을 병렬로 구현해줘
```

## 주의사항

- 이슈 간 같은 파일을 수정하면 PR 머지 시 충돌 가능 → 독립적인 이슈만 병렬 처리
- 공통 코드(global/) 변경이 필요한 이슈는 먼저 단독 처리 후 나머지 병렬
- 각 worktree는 `main` 기준이므로 이전 이슈의 변경사항이 반영되지 않음
- 의존성이 있는 이슈는 순차 처리하거나, 앞선 PR 머지 후 진행
