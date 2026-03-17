package com.project.ai.domain.feedback

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.feedback.dto.FeedbackCreateRequest
import com.project.ai.domain.feedback.entity.Feedback
import com.project.ai.domain.feedback.entity.FeedbackStatus
import com.project.ai.domain.feedback.repository.FeedbackRepository
import com.project.ai.domain.feedback.service.FeedbackService
import com.project.ai.domain.user.entity.Role
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
import org.mockito.kotlin.any
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class FeedbackServiceTest {
    @Mock
    private lateinit var feedbackRepository: FeedbackRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var feedbackService: FeedbackService

    private val testUser = User(id = 1L, email = "test@test.com", password = "password123", name = "테스트")
    private val otherUser = User(id = 2L, email = "other@test.com", password = "password123", name = "다른유저")
    private val testThread = Thread(id = 1L, user = testUser)
    private val testChat = Chat(id = 1L, thread = testThread, question = "질문", answer = "답변")

    @Test
    fun `피드백을 정상적으로 생성할 수 있어야 한다`() {
        // given
        val request = FeedbackCreateRequest(chatId = 1L, isPositive = true)
        val feedback = Feedback(id = 1L, isPositive = true, user = testUser, chat = testChat)

        given(chatRepository.findById(1L)).willReturn(Optional.of(testChat))
        given(feedbackRepository.existsByUserIdAndChatId(1L, 1L)).willReturn(false)
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser))
        given(feedbackRepository.save(any())).willReturn(feedback)

        // when
        val result = feedbackService.createFeedback(1L, Role.MEMBER, request)

        // then
        assertThat(result.feedbackId).isEqualTo(1L)
        assertThat(result.chatId).isEqualTo(1L)
        assertThat(result.isPositive).isTrue()
        assertThat(result.status).isEqualTo(FeedbackStatus.PENDING)
    }

    @Test
    fun `중복 피드백 생성 시 DUPLICATE_FEEDBACK 에러가 발생해야 한다`() {
        // given
        val request = FeedbackCreateRequest(chatId = 1L, isPositive = true)

        given(chatRepository.findById(1L)).willReturn(Optional.of(testChat))
        given(feedbackRepository.existsByUserIdAndChatId(1L, 1L)).willReturn(true)

        // when
        val exception =
            assertThrows<AppException> {
                feedbackService.createFeedback(1L, Role.MEMBER, request)
            }

        // then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.DUPLICATE_FEEDBACK)
    }

    @Test
    fun `일반 유저가 다른 유저의 대화에 피드백 시 FEEDBACK_ACCESS_DENIED 에러가 발생해야 한다`() {
        // given
        val request = FeedbackCreateRequest(chatId = 1L, isPositive = true)

        given(chatRepository.findById(1L)).willReturn(Optional.of(testChat))

        // when
        val exception =
            assertThrows<AppException> {
                feedbackService.createFeedback(otherUser.id, Role.MEMBER, request)
            }

        // then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.FEEDBACK_ACCESS_DENIED)
    }

    @Test
    fun `관리자는 다른 유저의 대화에도 피드백을 생성할 수 있어야 한다`() {
        // given
        val request = FeedbackCreateRequest(chatId = 1L, isPositive = false)
        val adminUser = User(id = 3L, email = "admin@test.com", password = "password123", name = "관리자", role = Role.ADMIN)
        val feedback = Feedback(id = 2L, isPositive = false, user = adminUser, chat = testChat)

        given(chatRepository.findById(1L)).willReturn(Optional.of(testChat))
        given(feedbackRepository.existsByUserIdAndChatId(3L, 1L)).willReturn(false)
        given(userRepository.findById(3L)).willReturn(Optional.of(adminUser))
        given(feedbackRepository.save(any())).willReturn(feedback)

        // when
        val result = feedbackService.createFeedback(3L, Role.ADMIN, request)

        // then
        assertThat(result.feedbackId).isEqualTo(2L)
        assertThat(result.isPositive).isFalse()
    }

    @Test
    fun `존재하지 않는 대화에 피드백 시 CHAT_NOT_FOUND 에러가 발생해야 한다`() {
        // given
        val request = FeedbackCreateRequest(chatId = 999L, isPositive = true)

        given(chatRepository.findById(999L)).willReturn(Optional.empty())

        // when
        val exception =
            assertThrows<AppException> {
                feedbackService.createFeedback(1L, Role.MEMBER, request)
            }

        // then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.CHAT_NOT_FOUND)
    }
}
