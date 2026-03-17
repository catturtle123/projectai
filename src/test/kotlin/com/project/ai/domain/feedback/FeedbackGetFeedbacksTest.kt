package com.project.ai.domain.feedback

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@ExtendWith(MockitoExtension::class)
class FeedbackGetFeedbacksTest {
    @Mock
    private lateinit var feedbackRepository: FeedbackRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var feedbackService: FeedbackService

    private fun createUser(
        id: Long = 1L,
        role: Role = Role.MEMBER,
    ): User = User(id = id, email = "test@test.com", password = "password", name = "tester", role = role)

    private fun createThread(
        id: Long = 1L,
        user: User,
    ): Thread = Thread(id = id, user = user)

    private fun createChat(
        id: Long = 1L,
        thread: Thread,
    ): Chat = Chat(id = id, thread = thread, question = "질문", answer = "답변")

    private fun createFeedback(
        id: Long = 1L,
        isPositive: Boolean = true,
        status: FeedbackStatus = FeedbackStatus.PENDING,
        user: User,
        chat: Chat,
    ): Feedback = Feedback(id = id, isPositive = isPositive, status = status, user = user, chat = chat, chatId = chat.id)

    @Test
    fun `일반 유저는 본인의 피드백만 조회해야 한다`() {
        val user = createUser()
        val thread = createThread(user = user)
        val chat = createChat(thread = thread)
        val feedback = createFeedback(user = user, chat = chat)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(feedbackRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(listOf(feedback), pageable, 1))

        val result = feedbackService.getFeedbacks(userId = 1L, role = Role.MEMBER, isPositive = null, page = 0, size = 20, sort = "desc")

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content[0].feedbackId).isEqualTo(1L)
        assertThat(result.content[0].status).isEqualTo(FeedbackStatus.PENDING)
    }

    @Test
    fun `관리자는 모든 피드백을 조회해야 한다`() {
        val user1 = createUser(id = 1L)
        val user2 = createUser(id = 2L)
        val thread = createThread(user = user1)
        val chat1 = createChat(id = 1L, thread = thread)
        val chat2 = createChat(id = 2L, thread = thread)
        val feedback1 = createFeedback(id = 1L, user = user1, chat = chat1)
        val feedback2 = createFeedback(id = 2L, user = user2, chat = chat2, isPositive = false)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(feedbackRepository.findAll(pageable))
            .willReturn(PageImpl(listOf(feedback1, feedback2), pageable, 2))

        val result = feedbackService.getFeedbacks(userId = 1L, role = Role.ADMIN, isPositive = null, page = 0, size = 20, sort = "desc")

        assertThat(result.totalElements).isEqualTo(2)
    }

    @Test
    fun `isPositive 필터가 올바르게 동작해야 한다`() {
        val user = createUser()
        val thread = createThread(user = user)
        val chat = createChat(thread = thread)
        val feedback = createFeedback(user = user, chat = chat, isPositive = true)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(feedbackRepository.findAllByUserIdAndIsPositive(1L, true, pageable))
            .willReturn(PageImpl(listOf(feedback), pageable, 1))

        val result = feedbackService.getFeedbacks(userId = 1L, role = Role.MEMBER, isPositive = true, page = 0, size = 20, sort = "desc")

        assertThat(result.content[0].isPositive).isTrue()
    }

    @Test
    fun `빈 결과는 빈 페이지를 반환해야 한다`() {
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(feedbackRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(emptyList(), pageable, 0))

        val result = feedbackService.getFeedbacks(userId = 1L, role = Role.MEMBER, isPositive = null, page = 0, size = 20, sort = "desc")

        assertThat(result.totalElements).isEqualTo(0)
    }

    @Test
    fun `잘못된 sort 값이면 예외를 던져야 한다`() {
        val exception =
            assertThrows<AppException> {
                feedbackService.getFeedbacks(userId = 1L, role = Role.MEMBER, isPositive = null, page = 0, size = 20, sort = "invalid")
            }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.VALIDATION_001)
    }
}
