package com.project.ai.domain.analytics

import com.project.ai.domain.analytics.service.ReportService
import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.user.entity.User
import com.project.ai.global.common.BaseTimeEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ReportServiceTest {
    @Mock
    private lateinit var chatRepository: ChatRepository

    @InjectMocks
    private lateinit var reportService: ReportService

    private fun createUser(
        email: String = "test@test.com",
        name: String = "테스터",
    ): User = User(id = 1L, email = email, password = "encoded", name = name)

    private fun createThread(user: User): Thread = Thread(id = 1L, user = user)

    private fun setCreatedAt(
        entity: BaseTimeEntity,
        time: LocalDateTime,
    ) {
        val field = BaseTimeEntity::class.java.getDeclaredField("createdAt")
        field.isAccessible = true
        field.set(entity, time)
    }

    private fun createChat(
        thread: Thread,
        question: String = "질문",
        answer: String = "답변",
        createdAt: LocalDateTime = LocalDateTime.now(),
    ): Chat {
        val chat = Chat(id = 1L, thread = thread, question = question, answer = answer)
        setCreatedAt(chat, createdAt)
        return chat
    }

    @Test
    fun `데이터가 있으면 올바른 CSV를 생성해야 한다`() {
        // given
        val user = createUser(email = "user@test.com", name = "홍길동")
        val thread = createThread(user)
        val now = LocalDateTime.of(2026, 3, 17, 10, 0, 0)
        val chat = createChat(thread, question = "안녕하세요", answer = "반갑습니다", createdAt = now)

        given(chatRepository.findAllWithUserAfter(any<LocalDateTime>())).willReturn(listOf(chat))

        // when
        val result = reportService.generateChatReport()
        val csv = String(result, Charsets.UTF_8)

        // then
        assertThat(csv).startsWith("\uFEFF")
        assertThat(csv).contains("사용자이메일,사용자이름,질문,답변,생성일시")
        assertThat(csv).contains("user@test.com,홍길동,안녕하세요,반갑습니다,2026-03-17T10:00")
    }

    @Test
    fun `데이터가 없으면 헤더만 포함된 CSV를 반환해야 한다`() {
        // given
        given(chatRepository.findAllWithUserAfter(any<LocalDateTime>())).willReturn(emptyList())

        // when
        val result = reportService.generateChatReport()
        val csv = String(result, Charsets.UTF_8)

        // then
        assertThat(csv).startsWith("\uFEFF")
        assertThat(csv).contains("사용자이메일,사용자이름,질문,답변,생성일시")
        val lines = csv.trim().lines()
        assertThat(lines).hasSize(1) // BOM + header only
    }

    @Test
    fun `CSV에서 쉼표, 따옴표, 줄바꿈이 올바르게 이스케이프되어야 한다`() {
        // given
        val user = createUser(email = "user@test.com", name = "홍,길동")
        val thread = createThread(user)
        val now = LocalDateTime.of(2026, 3, 17, 10, 0, 0)
        val chatWithComma = createChat(thread, question = "안녕,하세요", answer = "반갑습니다", createdAt = now)

        val user2 = createUser(email = "user2@test.com", name = "김\"철수")
        val thread2 = Thread(id = 2L, user = user2)
        val chatWithQuote =
            Chat(id = 2L, thread = thread2, question = "질문입니다", answer = "답\"변")
        setCreatedAt(chatWithQuote, now)

        val user3 = createUser(email = "user3@test.com", name = "박영희")
        val thread3 = Thread(id = 3L, user = user3)
        val chatWithNewline =
            Chat(id = 3L, thread = thread3, question = "줄바꿈\n질문", answer = "답변")
        setCreatedAt(chatWithNewline, now)

        given(chatRepository.findAllWithUserAfter(any<LocalDateTime>()))
            .willReturn(listOf(chatWithComma, chatWithQuote, chatWithNewline))

        // when
        val result = reportService.generateChatReport()
        val csv = String(result, Charsets.UTF_8)

        // then
        // 쉼표가 포함된 값은 따옴표로 감싸져야 한다
        assertThat(csv).contains("\"홍,길동\"")
        assertThat(csv).contains("\"안녕,하세요\"")
        // 따옴표가 포함된 값은 따옴표를 이중으로 이스케이프해야 한다
        assertThat(csv).contains("\"김\"\"철수\"")
        assertThat(csv).contains("\"답\"\"변\"")
        // 줄바꿈이 포함된 값은 따옴표로 감싸져야 한다
        assertThat(csv).contains("\"줄바꿈\n질문\"")
    }

    @Test
    fun `UTF-8 BOM이 존재해야 한다`() {
        // given
        given(chatRepository.findAllWithUserAfter(any<LocalDateTime>())).willReturn(emptyList())

        // when
        val result = reportService.generateChatReport()

        // then
        // UTF-8 BOM: EF BB BF
        assertThat(result[0]).isEqualTo(0xEF.toByte())
        assertThat(result[1]).isEqualTo(0xBB.toByte())
        assertThat(result[2]).isEqualTo(0xBF.toByte())
    }
}
