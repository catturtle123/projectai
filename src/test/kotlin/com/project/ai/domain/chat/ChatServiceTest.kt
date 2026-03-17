package com.project.ai.domain.chat

import com.project.ai.domain.chat.dto.ChatCreateRequest
import com.project.ai.domain.chat.dto.OpenAiMessage
import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
import com.project.ai.domain.chat.service.ChatService
import com.project.ai.domain.chat.service.OpenAiService
import com.project.ai.domain.user.entity.User
import com.project.ai.domain.user.repository.UserRepository
import com.project.ai.global.common.BaseTimeEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.given
import org.mockito.kotlin.isNull
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ChatServiceTest {
    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var threadRepository: ThreadRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var openAiService: OpenAiService

    @Mock
    private lateinit var transactionTemplate: TransactionTemplate

    @InjectMocks
    private lateinit var chatService: ChatService

    private fun createUser(): User = User(id = 1L, email = "test@test.com", password = "encoded", name = "테스터")

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
    fun `이전 스레드가 없으면 새 스레드를 생성해야 한다`() {
        // given
        val user = createUser()
        val newThread = Thread(id = 2L, user = user)
        val savedChat = Chat(id = 1L, thread = newThread, question = "안녕", answer = "안녕하세요!")

        given(userRepository.findById(1L)).willReturn(Optional.of(user))
        given(threadRepository.findTopByUserIdOrderByCreatedAtDesc(1L)).willReturn(null)
        given(threadRepository.save(any<Thread>())).willReturn(newThread)
        given(chatRepository.findAllByThreadOrderByCreatedAtAsc(newThread)).willReturn(emptyList())
        given(openAiService.chat(any<List<OpenAiMessage>>(), isNull())).willReturn("안녕하세요!")
        given(chatRepository.save(any<Chat>())).willReturn(savedChat)

        val request = ChatCreateRequest(question = "안녕")

        // when
        val result = chatService.createChat(1L, request)

        // then
        assertThat(result.threadId).isEqualTo(2L)
        assertThat(result.question).isEqualTo("안녕")
        assertThat(result.answer).isEqualTo("안녕하세요!")
    }

    @Test
    fun `마지막 대화가 30분 이내이면 기존 스레드를 재사용해야 한다`() {
        // given
        val user = createUser()
        val existingThread = createThread(user)
        val recentChat = createChat(existingThread, createdAt = LocalDateTime.now().minusMinutes(10))
        val savedChat = Chat(id = 2L, thread = existingThread, question = "새 질문", answer = "새 답변")

        given(userRepository.findById(1L)).willReturn(Optional.of(user))
        given(threadRepository.findTopByUserIdOrderByCreatedAtDesc(1L)).willReturn(existingThread)
        given(chatRepository.findTopByThreadOrderByCreatedAtDesc(existingThread)).willReturn(recentChat)
        given(chatRepository.findAllByThreadOrderByCreatedAtAsc(existingThread)).willReturn(listOf(recentChat))
        given(openAiService.chat(any<List<OpenAiMessage>>(), isNull())).willReturn("새 답변")
        given(chatRepository.save(any<Chat>())).willReturn(savedChat)

        val request = ChatCreateRequest(question = "새 질문")

        // when
        val result = chatService.createChat(1L, request)

        // then
        assertThat(result.threadId).isEqualTo(1L)
    }

    @Test
    fun `마지막 대화가 30분 이상 지났으면 새 스레드를 생성해야 한다`() {
        // given
        val user = createUser()
        val oldThread = createThread(user)
        val oldChat = createChat(oldThread, createdAt = LocalDateTime.now().minusMinutes(60))
        val newThread = Thread(id = 2L, user = user)
        val savedChat = Chat(id = 2L, thread = newThread, question = "새 질문", answer = "새 답변")

        given(userRepository.findById(1L)).willReturn(Optional.of(user))
        given(threadRepository.findTopByUserIdOrderByCreatedAtDesc(1L)).willReturn(oldThread)
        given(chatRepository.findTopByThreadOrderByCreatedAtDesc(oldThread)).willReturn(oldChat)
        given(threadRepository.save(any<Thread>())).willReturn(newThread)
        given(chatRepository.findAllByThreadOrderByCreatedAtAsc(newThread)).willReturn(emptyList())
        given(openAiService.chat(any<List<OpenAiMessage>>(), isNull())).willReturn("새 답변")
        given(chatRepository.save(any<Chat>())).willReturn(savedChat)

        val request = ChatCreateRequest(question = "새 질문")

        // when
        val result = chatService.createChat(1L, request)

        // then
        assertThat(result.threadId).isEqualTo(2L)
    }

    @Test
    fun `이전 대화 컨텍스트가 올바르게 구성되어야 한다`() {
        // given
        val user = createUser()
        val thread = createThread(user)
        val previousChat = createChat(thread, question = "이전 질문", answer = "이전 답변", createdAt = LocalDateTime.now().minusMinutes(5))
        val savedChat = Chat(id = 2L, thread = thread, question = "새 질문", answer = "새 답변")

        given(userRepository.findById(1L)).willReturn(Optional.of(user))
        given(threadRepository.findTopByUserIdOrderByCreatedAtDesc(1L)).willReturn(thread)
        given(chatRepository.findTopByThreadOrderByCreatedAtDesc(thread)).willReturn(previousChat)
        given(chatRepository.findAllByThreadOrderByCreatedAtAsc(thread)).willReturn(listOf(previousChat))
        given(chatRepository.save(any<Chat>())).willReturn(savedChat)

        val messagesCaptor: ArgumentCaptor<List<OpenAiMessage>> =
            ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<OpenAiMessage>>
        given(openAiService.chat(capture(messagesCaptor), isNull())).willReturn("새 답변")

        val request = ChatCreateRequest(question = "새 질문")

        // when
        chatService.createChat(1L, request)

        // then
        val messages = messagesCaptor.value
        assertThat(messages).hasSize(4) // system + prev user + prev assistant + new user
        assertThat(messages[0].role).isEqualTo("system")
        assertThat(messages[1].role).isEqualTo("user")
        assertThat(messages[1].content).isEqualTo("이전 질문")
        assertThat(messages[2].role).isEqualTo("assistant")
        assertThat(messages[2].content).isEqualTo("이전 답변")
        assertThat(messages[3].role).isEqualTo("user")
        assertThat(messages[3].content).isEqualTo("새 질문")
    }

    @Test
    fun `OpenAI 응답이 올바르게 저장되어야 한다`() {
        // given
        val user = createUser()
        val thread = createThread(user)
        val expectedAnswer = "AI가 생성한 답변입니다."
        val savedChat = Chat(id = 1L, thread = thread, question = "테스트 질문", answer = expectedAnswer)

        setCreatedAt(thread, LocalDateTime.now())

        given(userRepository.findById(1L)).willReturn(Optional.of(user))
        given(threadRepository.findTopByUserIdOrderByCreatedAtDesc(1L)).willReturn(thread)
        given(chatRepository.findTopByThreadOrderByCreatedAtDesc(thread)).willReturn(null)
        given(chatRepository.findAllByThreadOrderByCreatedAtAsc(thread)).willReturn(emptyList())
        given(openAiService.chat(any<List<OpenAiMessage>>(), isNull())).willReturn(expectedAnswer)
        given(chatRepository.save(any<Chat>())).willReturn(savedChat)

        val request = ChatCreateRequest(question = "테스트 질문")

        // when
        val result = chatService.createChat(1L, request)

        // then
        assertThat(result.answer).isEqualTo(expectedAnswer)
        assertThat(result.chatId).isEqualTo(1L)
    }
}
