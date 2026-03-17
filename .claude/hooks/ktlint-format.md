---
description: .kt 파일 수정 시 ktlintFormat 자동 실행
trigger: PostToolUse
matcher: Write|Edit
---

# ktlint 자동 포맷팅

## 동작
`.kt` 파일을 Write 또는 Edit할 때 `./gradlew ktlintFormat`이 자동 실행됩니다.

## 설정 위치
`.claude/settings.json` → `hooks.PostToolUse`

## 명령어
```bash
file_path=$(echo $CLAUDE_TOOL_INPUT | jq -r '.file_path // empty')
if [[ "$file_path" == *.kt ]]; then
  ./gradlew ktlintFormat --quiet 2>/dev/null || true
fi
```

## 목적
- 코드 스타일 일관성 유지
- 수동 포맷팅 실행 불필요
- `.kt` 파일에만 적용 (`.md`, `.yml` 등은 무시)
