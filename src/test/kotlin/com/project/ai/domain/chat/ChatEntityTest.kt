package com.project.ai.domain.chat

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
import com.project.ai.domain.user.entity.User
import com.project.ai.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
class ChatEntityTest {
    @Autowired
    private lateinit var threadRepository: ThreadRepository

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser =
            userRepository.save(
                User(
                    email = "chattest@test.com",
                    password = "password123",
                    name = "테스트유저",
                ),
            )
    }

    @AfterEach
    fun tearDown() {
        chatRepository.deleteAll()
        threadRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `스레드를 생성하고 조회할 수 있어야 한다`() {
        // given
        val thread = Thread(user = testUser)

        // when
        val saved = threadRepository.save(thread)

        // then
        val found = threadRepository.findById(saved.id)
        assertThat(found).isPresent
        assertThat(found.get().user.id).isEqualTo(testUser.id)
        assertThat(found.get().createdAt).isNotNull()
    }

    @Test
    fun `스레드에 연결된 대화를 생성할 수 있어야 한다`() {
        // given
        val thread = threadRepository.save(Thread(user = testUser))
        val chat =
            Chat(
                thread = thread,
                question = "안녕하세요?",
                answer = "안녕하세요! 무엇을 도와드릴까요?",
            )

        // when
        val saved = chatRepository.save(chat)

        // then
        assertThat(saved.id).isGreaterThan(0)
        assertThat(saved.question).isEqualTo("안녕하세요?")
        assertThat(saved.answer).isEqualTo("안녕하세요! 무엇을 도와드릴까요?")
        assertThat(saved.thread.id).isEqualTo(thread.id)
    }

    @Test
    fun `findTopByUserIdOrderByCreatedAtDesc로 최신 스레드를 조회할 수 있어야 한다`() {
        // given
        threadRepository.save(Thread(user = testUser))
        val thread2 = threadRepository.save(Thread(user = testUser))

        // when
        val latest = threadRepository.findTopByUserIdOrderByCreatedAtDesc(testUser.id)

        // then
        assertThat(latest).isNotNull
        assertThat(latest!!.id).isEqualTo(thread2.id)
    }

    @Test
    fun `유저별 모든 스레드를 조회할 수 있어야 한다`() {
        // given
        threadRepository.save(Thread(user = testUser))
        threadRepository.save(Thread(user = testUser))

        // when
        val threads = threadRepository.findAllByUserId(testUser.id)

        // then
        assertThat(threads).hasSize(2)
    }

    @Test
    fun `스레드별 대화 목록을 생성 시간순으로 조회할 수 있어야 한다`() {
        // given
        val thread = threadRepository.save(Thread(user = testUser))
        chatRepository.save(Chat(thread = thread, question = "첫 번째 질문", answer = "첫 번째 답변"))
        chatRepository.save(Chat(thread = thread, question = "두 번째 질문", answer = "두 번째 답변"))

        // when
        val chats = chatRepository.findAllByThreadOrderByCreatedAtAsc(thread)

        // then
        assertThat(chats).hasSize(2)
        assertThat(chats[0].question).isEqualTo("첫 번째 질문")
        assertThat(chats[1].question).isEqualTo("두 번째 질문")
    }

    @Test
    fun `스레드 ID로 대화 목록을 조회할 수 있어야 한다`() {
        // given
        val thread = threadRepository.save(Thread(user = testUser))
        chatRepository.save(Chat(thread = thread, question = "질문1", answer = "답변1"))
        chatRepository.save(Chat(thread = thread, question = "질문2", answer = "답변2"))

        // when
        val chats = chatRepository.findAllByThreadId(thread.id)

        // then
        assertThat(chats).hasSize(2)
    }

    @Test
    fun `30분 이내 스레드는 재사용 가능 여부를 판단할 수 있어야 한다`() {
        // given
        threadRepository.save(Thread(user = testUser))

        // when
        val latest = threadRepository.findTopByUserIdOrderByCreatedAtDesc(testUser.id)

        // then
        assertThat(latest).isNotNull
        val thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30)
        val shouldReuse = latest!!.createdAt.isAfter(thirtyMinutesAgo)
        assertThat(shouldReuse).isTrue()
    }

    @Test
    fun `다른 유저의 스레드는 조회되지 않아야 한다`() {
        // given
        val otherUser =
            userRepository.save(
                User(
                    email = "other@test.com",
                    password = "password123",
                    name = "다른유저",
                ),
            )
        threadRepository.save(Thread(user = testUser))
        threadRepository.save(Thread(user = otherUser))

        // when
        val threads = threadRepository.findAllByUserId(testUser.id)

        // then
        assertThat(threads).hasSize(1)
        assertThat(threads[0].user.id).isEqualTo(testUser.id)
    }
}
