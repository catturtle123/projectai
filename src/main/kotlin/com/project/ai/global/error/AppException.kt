package com.project.ai.global.error

class AppException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
