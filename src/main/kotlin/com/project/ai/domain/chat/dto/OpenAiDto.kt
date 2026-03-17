package com.project.ai.domain.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAiMessage(
    val role: String,
    val content: String,
)

data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val stream: Boolean = false,
)

data class OpenAiResponse(
    val choices: List<OpenAiChoice>,
) {
    data class OpenAiChoice(
        val message: OpenAiMessage,
        @JsonProperty("finish_reason")
        val finishReason: String?,
    )
}

data class OpenAiStreamResponse(
    val choices: List<OpenAiStreamChoice>,
) {
    data class OpenAiStreamChoice(
        val delta: OpenAiDelta,
        @JsonProperty("finish_reason")
        val finishReason: String?,
    )

    data class OpenAiDelta(
        val role: String? = null,
        val content: String? = null,
    )
}
