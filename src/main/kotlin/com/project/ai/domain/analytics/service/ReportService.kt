package com.project.ai.domain.analytics.service

import com.project.ai.domain.chat.repository.ChatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ReportService(
    private val chatRepository: ChatRepository,
) {
    fun generateChatReport(): ByteArray {
        val oneDayAgo = LocalDateTime.now().minusDays(1)
        val chats = chatRepository.findAllWithUserAfter(oneDayAgo)

        val sb = StringBuilder()
        // UTF-8 BOM
        sb.append('\uFEFF')
        // Header
        sb.appendLine("사용자이메일,사용자이름,질문,답변,생성일시")

        for (chat in chats) {
            val user = chat.thread.user
            sb.appendLine(
                "${escapeCsv(user.email)},${escapeCsv(user.name)}," +
                    "${escapeCsv(chat.question)},${escapeCsv(chat.answer)},${chat.createdAt}",
            )
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
