package com.project.ai.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    // Common
    COMMON400(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    COMMON500(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 오류가 발생했습니다."),

    // Auth
    AUTH_001(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증에 실패했습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_002", "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_003", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "유효하지 않은 토큰입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH_006", "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),

    // Chat
    THREAD_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "스레드를 찾을 수 없습니다."),
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_002", "대화를 찾을 수 없습니다."),
    THREAD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_003", "해당 스레드에 접근 권한이 없습니다."),

    // Feedback
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK_001", "피드백을 찾을 수 없습니다."),
    DUPLICATE_FEEDBACK(HttpStatus.CONFLICT, "FEEDBACK_002", "이미 해당 대화에 피드백을 작성했습니다."),
    FEEDBACK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FEEDBACK_003", "피드백에 대한 접근 권한이 없습니다."),

    // OpenAI
    OPENAI_API_ERROR(HttpStatus.BAD_GATEWAY, "OPENAI_001", "AI 응답 생성에 실패했습니다."),

    // Validation
    VALIDATION_001(HttpStatus.BAD_REQUEST, "VALIDATION_001", "유효하지 않은 입력입니다."),
}
