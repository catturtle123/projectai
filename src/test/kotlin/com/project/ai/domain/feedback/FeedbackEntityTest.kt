package com.project.ai.domain.feedback

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
import com.project.ai.domain.feedback.entity.Feedback
import com.project.ai.domain.feedback.entity.FeedbackStatus
import com.project.ai.domain.feedback.repository.FeedbackRepository
import com.project.ai.domain.user.entity.User
import com.project.ai.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class FeedbackEntityTest {
    @Autowired
    private lateinit var feedbackRepository: FeedbackRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var threadRepository: ThreadRepository

    @Autowired
    private lateinit var chatRepository: ChatRepository

    private lateinit var testUser: User
    private lateinit var testThread: Thread

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(User(email = "test@test.com", password = "password123", name = "테스트"))
        testThread = threadRepository.save(Thread(user = testUser))
    }

    private fun createChat(question: String = "질문"): Chat =
        chatRepository.save(Chat(thread = testThread, question = question, answer = "답변"))

    private fun createUser(email: String): User = userRepository.save(User(email = email, password = "password123", name = "테스트"))

    @Test
    fun `피드백을 생성하고 조회할 수 있어야 한다`() {
        // given
        val chat = createChat()
        val feedback =
            feedbackRepository.save(
                Feedback(isPositive = true, user = testUser, chat = chat),
            )

        // when
        val found = feedbackRepository.findById(feedback.id)

        // then
        assertThat(found.isPresent).isTrue()
        assertThat(found.get().chat.id).isEqualTo(chat.id)
        assertThat(found.get().isPositive).isTrue()
        assertThat(found.get().status).isEqualTo(FeedbackStatus.PENDING)
        assertThat(found.get().user.id).isEqualTo(testUser.id)
    }

    @Test
    fun `동일한 userId와 chatId로 중복 피드백 저장 시 예외가 발생해야 한다`() {
        // given
        val chat = createChat()
        feedbackRepository.save(
            Feedback(isPositive = true, user = testUser, chat = chat),
        )

        // when & then
        assertThrows<DataIntegrityViolationException> {
            feedbackRepository.saveAndFlush(
                Feedback(isPositive = false, user = testUser, chat = chat),
            )
        }
    }

    @Test
    fun `findByUserIdAndChatId로 피드백을 조회할 수 있어야 한다`() {
        // given
        val chat = createChat()
        feedbackRepository.save(
            Feedback(isPositive = true, user = testUser, chat = chat),
        )

        // when
        val found = feedbackRepository.findByUserIdAndChatId(testUser.id, chat.id)

        // then
        assertThat(found).isNotNull
        assertThat(found!!.chat.id).isEqualTo(chat.id)
        assertThat(found.user.id).isEqualTo(testUser.id)
    }

    @Test
    fun `existsByUserIdAndChatId로 피드백 존재 여부를 확인할 수 있어야 한다`() {
        // given
        val chat = createChat()
        feedbackRepository.save(
            Feedback(isPositive = true, user = testUser, chat = chat),
        )

        // when & then
        assertThat(feedbackRepository.existsByUserIdAndChatId(testUser.id, chat.id)).isTrue()
        assertThat(feedbackRepository.existsByUserIdAndChatId(testUser.id, 999L)).isFalse()
    }

    @Test
    fun `findAllByUserId로 유저별 피드백을 페이지네이션 조회할 수 있어야 한다`() {
        // given
        for (i in 1..5) {
            val chat = createChat("질문$i")
            feedbackRepository.save(
                Feedback(isPositive = i % 2 == 0, user = testUser, chat = chat),
            )
        }

        // when
        val page = feedbackRepository.findAllByUserId(testUser.id, PageRequest.of(0, 3))

        // then
        assertThat(page.totalElements).isEqualTo(5)
        assertThat(page.content).hasSize(3)
        assertThat(page.totalPages).isEqualTo(2)
    }

    @Test
    fun `findAllByUserIdAndIsPositive로 긍정 피드백만 조회할 수 있어야 한다`() {
        // given
        val chat1 = createChat("질문1")
        val chat2 = createChat("질문2")
        val chat3 = createChat("질문3")
        feedbackRepository.save(Feedback(isPositive = true, user = testUser, chat = chat1))
        feedbackRepository.save(Feedback(isPositive = false, user = testUser, chat = chat2))
        feedbackRepository.save(Feedback(isPositive = true, user = testUser, chat = chat3))

        // when
        val page = feedbackRepository.findAllByUserIdAndIsPositive(testUser.id, true, PageRequest.of(0, 10))

        // then
        assertThat(page.totalElements).isEqualTo(2)
        assertThat(page.content).allMatch { it.isPositive }
    }

    @Test
    fun `findAllByIsPositive로 전체 피드백을 필터링 조회할 수 있어야 한다`() {
        // given
        val user2 = createUser("user2@test.com")
        val thread2 = threadRepository.save(Thread(user = user2))
        val chat1 = createChat("질문1")
        val chat2 = createChat("질문2")
        val chat3 = chatRepository.save(Chat(thread = thread2, question = "질문3", answer = "답변3"))
        feedbackRepository.save(Feedback(isPositive = true, user = testUser, chat = chat1))
        feedbackRepository.save(Feedback(isPositive = false, user = testUser, chat = chat2))
        feedbackRepository.save(Feedback(isPositive = false, user = user2, chat = chat3))

        // when
        val page = feedbackRepository.findAllByIsPositive(false, PageRequest.of(0, 10))

        // then
        assertThat(page.totalElements).isEqualTo(2)
        assertThat(page.content).allMatch { !it.isPositive }
    }
}
