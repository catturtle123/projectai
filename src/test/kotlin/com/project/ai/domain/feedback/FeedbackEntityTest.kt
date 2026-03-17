package com.project.ai.domain.feedback

import com.project.ai.domain.feedback.entity.Feedback
import com.project.ai.domain.feedback.entity.FeedbackStatus
import com.project.ai.domain.feedback.repository.FeedbackRepository
import com.project.ai.domain.user.entity.User
import com.project.ai.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class FeedbackEntityTest {
    @Autowired
    private lateinit var feedbackRepository: FeedbackRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @AfterEach
    fun tearDown() {
        feedbackRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun createUser(email: String = "test@test.com"): User =
        userRepository.save(User(email = email, password = "password123", name = "테스트"))

    @Test
    fun `피드백을 생성하고 조회할 수 있어야 한다`() {
        // given
        val user = createUser()
        val feedback =
            feedbackRepository.save(
                Feedback(chatId = 1L, isPositive = true, user = user),
            )

        // when
        val found = feedbackRepository.findById(feedback.id)

        // then
        assertThat(found.isPresent).isTrue()
        assertThat(found.get().chatId).isEqualTo(1L)
        assertThat(found.get().isPositive).isTrue()
        assertThat(found.get().status).isEqualTo(FeedbackStatus.PENDING)
        assertThat(found.get().userId).isEqualTo(user.id)
    }

    @Test
    fun `동일한 userId와 chatId로 중복 피드백 저장 시 예외가 발생해야 한다`() {
        // given
        val user = createUser()
        feedbackRepository.save(
            Feedback(chatId = 1L, isPositive = true, user = user),
        )

        // when & then
        assertThrows<DataIntegrityViolationException> {
            feedbackRepository.saveAndFlush(
                Feedback(chatId = 1L, isPositive = false, user = user),
            )
        }
    }

    @Test
    fun `findByUserIdAndChatId로 피드백을 조회할 수 있어야 한다`() {
        // given
        val user = createUser()
        feedbackRepository.save(
            Feedback(chatId = 100L, isPositive = true, user = user),
        )

        // when
        val found = feedbackRepository.findByUserIdAndChatId(user.id, 100L)

        // then
        assertThat(found).isNotNull
        assertThat(found!!.chatId).isEqualTo(100L)
        assertThat(found.userId).isEqualTo(user.id)
    }

    @Test
    fun `existsByUserIdAndChatId로 피드백 존재 여부를 확인할 수 있어야 한다`() {
        // given
        val user = createUser()
        feedbackRepository.save(
            Feedback(chatId = 1L, isPositive = true, user = user),
        )

        // when & then
        assertThat(feedbackRepository.existsByUserIdAndChatId(user.id, 1L)).isTrue()
        assertThat(feedbackRepository.existsByUserIdAndChatId(user.id, 999L)).isFalse()
    }

    @Test
    fun `findAllByUserId로 유저별 피드백을 페이지네이션 조회할 수 있어야 한다`() {
        // given
        val user = createUser()
        for (chatId in 1L..5L) {
            feedbackRepository.save(
                Feedback(chatId = chatId, isPositive = chatId % 2 == 0L, user = user),
            )
        }

        // when
        val page = feedbackRepository.findAllByUserId(user.id, PageRequest.of(0, 3))

        // then
        assertThat(page.totalElements).isEqualTo(5)
        assertThat(page.content).hasSize(3)
        assertThat(page.totalPages).isEqualTo(2)
    }

    @Test
    fun `findAllByUserIdAndIsPositive로 긍정 피드백만 조회할 수 있어야 한다`() {
        // given
        val user = createUser()
        feedbackRepository.save(Feedback(chatId = 1L, isPositive = true, user = user))
        feedbackRepository.save(Feedback(chatId = 2L, isPositive = false, user = user))
        feedbackRepository.save(Feedback(chatId = 3L, isPositive = true, user = user))

        // when
        val page = feedbackRepository.findAllByUserIdAndIsPositive(user.id, true, PageRequest.of(0, 10))

        // then
        assertThat(page.totalElements).isEqualTo(2)
        assertThat(page.content).allMatch { it.isPositive }
    }

    @Test
    fun `findAllByIsPositive로 전체 피드백을 필터링 조회할 수 있어야 한다`() {
        // given
        val user1 = createUser("user1@test.com")
        val user2 = createUser("user2@test.com")
        feedbackRepository.save(Feedback(chatId = 1L, isPositive = true, user = user1))
        feedbackRepository.save(Feedback(chatId = 2L, isPositive = false, user = user1))
        feedbackRepository.save(Feedback(chatId = 3L, isPositive = false, user = user2))

        // when
        val page = feedbackRepository.findAllByIsPositive(false, PageRequest.of(0, 10))

        // then
        assertThat(page.totalElements).isEqualTo(2)
        assertThat(page.content).allMatch { !it.isPositive }
    }
}
