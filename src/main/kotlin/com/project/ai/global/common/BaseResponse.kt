package com.project.ai.global.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BaseResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T? = null,
) {
    companion object {
        fun <T> success(result: T): BaseResponse<T> =
            BaseResponse(
                isSuccess = true,
                code = "COMMON200",
                message = "성공입니다.",
                result = result,
            )

        fun <T> error(
            code: String,
            message: String,
        ): BaseResponse<T> =
            BaseResponse(
                isSuccess = false,
                code = code,
                message = message,
            )
    }
}
