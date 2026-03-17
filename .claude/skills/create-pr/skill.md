# GitHub PR 생성 Skill

GitHub Pull Request를 프로젝트 템플릿에 맞춰 생성하는 스킬입니다.

## 언제 이 스킬을 사용하나요?

- 기능 구현 완료 후 PR을 생성할 때
- `fix-issue` 명령어에서 PR 생성 시

## PR 타이틀 규칙

- **형식**: `type: 설명`
- **허용 type**: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`
- **깃모지(이모지) 절대 금지** (CI 실패 원인)
- **70자 이내**

### 예시
```
feat: 사용자 회원가입 API 구현
fix: 토큰 만료 시 500 에러 수정
refactor: UserService 패키지 구조 개선
```

## PR 본문 양식

```bash
gh pr create \
  --title "type: 설명" \
  --base dev \
  --body "$(cat <<'EOF'
## Summary

- 변경 사항 요약

## Changes

- [ ] 변경 내용 1
- [ ] 변경 내용 2

## Test

- [ ] 단위 테스트 통과
- [ ] 통합 테스트 통과
- [ ] `./gradlew build` 성공

## Related Issues

- Closes #이슈번호
EOF
)"
```

## 체크리스트

PR 생성 전 확인:

- [ ] `./gradlew test` 통과
- [ ] `./gradlew ktlintCheck` 통과
- [ ] `./gradlew build` 성공
- [ ] 커밋 메시지가 Tidy First 원칙을 따르는가
- [ ] 불필요한 파일이 포함되지 않았는가
- [ ] base 브랜치가 `dev`인가

## 주의사항

1. PR 타이틀에 이모지 절대 금지
2. 커밋 메시지는 한글로 작성
3. base 브랜치는 `dev`
4. 이슈 연결 시 `Closes #번호` 사용
