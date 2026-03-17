package com.project.ai.domain.chat

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
import com.project.ai.domain.chat.service.ThreadService
import com.project.ai.domain.feedback.repository.FeedbackRepository
import com.project.ai.domain.user.entity.User
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.never
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ThreadServiceTest {
    @Mock
    private lateinit var threadRepository: ThreadRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var feedbackRepository: FeedbackRepository

    @InjectMocks
    private lateinit var threadService: ThreadService

    private val user = User(id = 1L, email = "test@test.com", password = "password", name = "테스트")
    private val otherUser = User(id = 2L, email = "other@test.com", password = "password", name = "다른유저")

    @Test
    fun `스레드를 정상적으로 삭제해야 한다`() {
        // given
        val thread = Thread(id = 10L, user = user)
        val chats =
            listOf(
                Chat(id = 100L, thread = thread, question = "질문1", answer = "답변1"),
                Chat(id = 101L, thread = thread, question = "질문2", answer = "답변2"),
            )

        given(threadRepository.findById(10L)).willReturn(Optional.of(thread))
        given(chatRepository.findAllByThreadIdOrderByCreatedAtAsc(10L)).willReturn(chats)

        // when
        threadService.deleteThread(userId = 1L, threadId = 10L)

        // then
        then(feedbackRepository).should().deleteAllByChatIdIn(listOf(100L, 101L))
        then(chatRepository).should().deleteAllByThreadId(10L)
        then(threadRepository).should().delete(thread)
    }

    @Test
    fun `존재하지 않는 스레드 삭제 시 THREAD_NOT_FOUND 예외가 발생해야 한다`() {
        // given
        given(threadRepository.findById(999L)).willReturn(Optional.empty())

        // when
        val exception =
            assertThrows<AppException> {
                threadService.deleteThread(userId = 1L, threadId = 999L)
            }

        // then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.THREAD_NOT_FOUND)
    }

    @Test
    fun `다른 사용자의 스레드 삭제 시 THREAD_ACCESS_DENIED 예외가 발생해야 한다`() {
        // given
        val thread = Thread(id = 10L, user = otherUser)
        given(threadRepository.findById(10L)).willReturn(Optional.of(thread))

        // when
        val exception =
            assertThrows<AppException> {
                threadService.deleteThread(userId = 1L, threadId = 10L)
            }

        // then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.THREAD_ACCESS_DENIED)
    }

    @Test
    fun `대화가 없는 스레드도 정상적으로 삭제해야 한다`() {
        // given
        val thread = Thread(id = 10L, user = user)

        given(threadRepository.findById(10L)).willReturn(Optional.of(thread))
        given(chatRepository.findAllByThreadIdOrderByCreatedAtAsc(10L)).willReturn(emptyList())

        // when
        threadService.deleteThread(userId = 1L, threadId = 10L)

        // then
        then(feedbackRepository).should(never()).deleteAllByChatIdIn(anyList())
        then(chatRepository).should().deleteAllByThreadId(10L)
        then(threadRepository).should().delete(thread)
    }
}
