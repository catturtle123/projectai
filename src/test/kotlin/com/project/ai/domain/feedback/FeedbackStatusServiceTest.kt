package com.project.ai.domain.feedback

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.feedback.dto.FeedbackStatusUpdateRequest
import com.project.ai.domain.feedback.entity.Feedback
import com.project.ai.domain.feedback.entity.FeedbackStatus
import com.project.ai.domain.feedback.repository.FeedbackRepository
import com.project.ai.domain.feedback.service.FeedbackService
import com.project.ai.domain.user.entity.User
import com.project.ai.domain.user.repository.UserRepository
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class FeedbackStatusServiceTest {
    @Mock
    private lateinit var feedbackRepository: FeedbackRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var feedbackService: FeedbackService

    private val testUser = User(id = 1L, email = "admin@test.com", password = "password123", name = "관리자")
    private val testThread = Thread(id = 1L, user = testUser)
    private val testChat = Chat(id = 1L, thread = testThread, question = "질문", answer = "답변")

    @Test
    fun `관리자가 피드백 상태를 PENDING에서 RESOLVED로 변경할 수 있어야 한다`() {
        // given
        val feedback = Feedback(id = 1L, isPositive = true, user = testUser, chat = testChat, status = FeedbackStatus.PENDING)
        given(feedbackRepository.findById(1L)).willReturn(Optional.of(feedback))

        val request = FeedbackStatusUpdateRequest(status = FeedbackStatus.RESOLVED)

        // when
        val result = feedbackService.updateFeedbackStatus(1L, request)

        // then
        assertThat(result.feedbackId).isEqualTo(1L)
        assertThat(result.chatId).isEqualTo(1L)
        assertThat(result.isPositive).isTrue()
        assertThat(result.status).isEqualTo(FeedbackStatus.RESOLVED)
        assertThat(feedback.status).isEqualTo(FeedbackStatus.RESOLVED)
    }

    @Test
    fun `관리자가 피드백 상태를 RESOLVED에서 PENDING으로 변경할 수 있어야 한다`() {
        // given
        val feedback = Feedback(id = 2L, isPositive = false, user = testUser, chat = testChat, status = FeedbackStatus.RESOLVED)
        given(feedbackRepository.findById(2L)).willReturn(Optional.of(feedback))

        val request = FeedbackStatusUpdateRequest(status = FeedbackStatus.PENDING)

        // when
        val result = feedbackService.updateFeedbackStatus(2L, request)

        // then
        assertThat(result.feedbackId).isEqualTo(2L)
        assertThat(result.status).isEqualTo(FeedbackStatus.PENDING)
        assertThat(feedback.status).isEqualTo(FeedbackStatus.PENDING)
    }

    @Test
    fun `존재하지 않는 피드백 상태 변경 시 FEEDBACK_NOT_FOUND 예외가 발생해야 한다`() {
        // given
        given(feedbackRepository.findById(999L)).willReturn(Optional.empty())

        val request = FeedbackStatusUpdateRequest(status = FeedbackStatus.RESOLVED)

        // when
        val exception =
            assertThrows<AppException> {
                feedbackService.updateFeedbackStatus(999L, request)
            }

        // then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.FEEDBACK_NOT_FOUND)
    }

    @Test
    fun `동일한 상태로 변경해도 정상적으로 처리되어야 한다`() {
        // given
        val feedback = Feedback(id = 3L, isPositive = true, user = testUser, chat = testChat, status = FeedbackStatus.PENDING)
        given(feedbackRepository.findById(3L)).willReturn(Optional.of(feedback))

        val request = FeedbackStatusUpdateRequest(status = FeedbackStatus.PENDING)

        // when
        val result = feedbackService.updateFeedbackStatus(3L, request)

        // then
        assertThat(result.feedbackId).isEqualTo(3L)
        assertThat(result.status).isEqualTo(FeedbackStatus.PENDING)
        assertThat(feedback.status).isEqualTo(FeedbackStatus.PENDING)
    }
}
