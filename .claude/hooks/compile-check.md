---
description: 대화 종료 시 compileKotlin 자동 실행
trigger: Stop
---

# 컴파일 체크

## 동작
Claude 대화가 종료될 때 `./gradlew compileKotlin`이 자동 실행됩니다.

## 설정 위치
`.claude/settings.json` → `hooks.Stop`

## 명령어
```bash
./gradlew compileKotlin --quiet 2>/dev/null || true
```

## 목적
- 대화 종료 전 컴파일 에러가 없는지 최종 확인
- 에러가 있어도 대화는 종료됨 (`|| true`)
- 다음 대화에서 깨진 상태로 시작하는 것을 방지
