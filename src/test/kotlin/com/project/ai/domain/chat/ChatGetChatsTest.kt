package com.project.ai.domain.chat

import com.project.ai.domain.analytics.service.ActivityLogService
import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
import com.project.ai.domain.chat.service.ChatService
import com.project.ai.domain.chat.service.OpenAiService
import com.project.ai.domain.user.entity.Role
import com.project.ai.domain.user.entity.User
import com.project.ai.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.support.TransactionTemplate

@ExtendWith(MockitoExtension::class)
class ChatGetChatsTest {
    @Mock
    private lateinit var threadRepository: ThreadRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var openAiService: OpenAiService

    @Mock
    private lateinit var transactionTemplate: TransactionTemplate

    @Mock
    private lateinit var activityLogService: ActivityLogService

    @InjectMocks
    private lateinit var chatService: ChatService

    private fun createUser(
        id: Long = 1L,
        email: String = "test@test.com",
    ): User = User(id = id, email = email, password = "password", name = "tester", role = Role.MEMBER)

    private fun createThread(
        id: Long = 1L,
        user: User,
    ): Thread = Thread(id = id, user = user)

    private fun createChat(
        id: Long = 1L,
        thread: Thread,
        question: String = "질문",
        answer: String = "답변",
    ): Chat = Chat(id = id, thread = thread, question = question, answer = answer)

    @Test
    fun `일반 유저는 본인의 스레드만 조회해야 한다`() {
        val user = createUser()
        val thread = createThread(user = user)
        val chat = createChat(thread = thread)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(threadRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(listOf(thread), pageable, 1))
        given(chatRepository.findAllByThreadIdInOrderByCreatedAtAsc(listOf(1L)))
            .willReturn(listOf(chat))

        val result = chatService.getChats(userId = 1L, role = Role.MEMBER, page = 0, size = 20, sort = "desc")

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content[0].threadId).isEqualTo(1L)
        assertThat(result.content[0].chats).hasSize(1)
    }

    @Test
    fun `관리자는 모든 스레드를 조회해야 한다`() {
        val user1 = createUser(id = 1L, email = "user1@test.com")
        val user2 = createUser(id = 2L, email = "user2@test.com")
        val thread1 = createThread(id = 1L, user = user1)
        val thread2 = createThread(id = 2L, user = user2)
        val chat1 = createChat(id = 1L, thread = thread1)
        val chat2 = createChat(id = 2L, thread = thread2)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(threadRepository.findAll(pageable))
            .willReturn(PageImpl(listOf(thread1, thread2), pageable, 2))
        given(chatRepository.findAllByThreadIdInOrderByCreatedAtAsc(listOf(1L, 2L)))
            .willReturn(listOf(chat1, chat2))

        val result = chatService.getChats(userId = 1L, role = Role.ADMIN, page = 0, size = 20, sort = "desc")

        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.content).hasSize(2)
    }

    @Test
    fun `빈 결과는 빈 페이지를 반환해야 한다`() {
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(threadRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(emptyList(), pageable, 0))

        val result = chatService.getChats(userId = 1L, role = Role.MEMBER, page = 0, size = 20, sort = "desc")

        assertThat(result.totalElements).isEqualTo(0)
        assertThat(result.content).isEmpty()
    }

    @Test
    fun `스레드 내 대화가 그룹화되어야 한다`() {
        val user = createUser()
        val thread = createThread(user = user)
        val chat1 = createChat(id = 1L, thread = thread, question = "질문1", answer = "답변1")
        val chat2 = createChat(id = 2L, thread = thread, question = "질문2", answer = "답변2")
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(threadRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(listOf(thread), pageable, 1))
        given(chatRepository.findAllByThreadIdInOrderByCreatedAtAsc(listOf(1L)))
            .willReturn(listOf(chat1, chat2))

        val result = chatService.getChats(userId = 1L, role = Role.MEMBER, page = 0, size = 20, sort = "desc")

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].chats).hasSize(2)
    }
}
