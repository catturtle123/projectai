package com.project.ai.domain.chat

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
import com.project.ai.domain.chat.service.ChatQueryService
import com.project.ai.domain.user.entity.Role
import com.project.ai.domain.user.entity.User
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

@ExtendWith(MockitoExtension::class)
class ChatQueryServiceTest {
    @Mock
    private lateinit var threadRepository: ThreadRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @InjectMocks
    private lateinit var chatQueryService: ChatQueryService

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
        // given
        val user = createUser()
        val thread = createThread(user = user)
        val chat = createChat(thread = thread)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(threadRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(listOf(thread), pageable, 1))
        given(chatRepository.findAllByThreadIdInOrderByCreatedAtAsc(listOf(1L)))
            .willReturn(listOf(chat))

        // when
        val result = chatQueryService.getChats(userId = 1L, role = Role.MEMBER, page = 0, size = 20, sort = "desc")

        // then
        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content[0].threadId).isEqualTo(1L)
        assertThat(result.content[0].chats).hasSize(1)
        assertThat(result.content[0].chats[0].question).isEqualTo("질문")
    }

    @Test
    fun `관리자는 모든 스레드를 조회해야 한다`() {
        // given
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

        // when
        val result = chatQueryService.getChats(userId = 1L, role = Role.ADMIN, page = 0, size = 20, sort = "desc")

        // then
        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.content).hasSize(2)
    }

    @Test
    fun `페이지네이션이 올바르게 동작해야 한다`() {
        // given
        val user = createUser()
        val thread = createThread(user = user)
        val pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(threadRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(listOf(thread), pageable, 11))
        given(chatRepository.findAllByThreadIdInOrderByCreatedAtAsc(listOf(1L)))
            .willReturn(emptyList())

        // when
        val result = chatQueryService.getChats(userId = 1L, role = Role.MEMBER, page = 1, size = 10, sort = "desc")

        // then
        assertThat(result.totalElements).isEqualTo(11)
        assertThat(result.number).isEqualTo(1)
        assertThat(result.size).isEqualTo(10)
    }

    @Test
    fun `오름차순 정렬이 올바르게 적용되어야 한다`() {
        // given
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "createdAt"))

        given(threadRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(emptyList(), pageable, 0))

        // when
        val result = chatQueryService.getChats(userId = 1L, role = Role.MEMBER, page = 0, size = 20, sort = "asc")

        // then
        assertThat(result.totalElements).isEqualTo(0)
        assertThat(result.content).isEmpty()
    }

    @Test
    fun `결과가 없으면 빈 페이지를 반환해야 한다`() {
        // given
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(threadRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(emptyList(), pageable, 0))

        // when
        val result = chatQueryService.getChats(userId = 1L, role = Role.MEMBER, page = 0, size = 20, sort = "desc")

        // then
        assertThat(result.totalElements).isEqualTo(0)
        assertThat(result.content).isEmpty()
    }

    @Test
    fun `스레드 내 대화가 그룹화되어야 한다`() {
        // given
        val user = createUser()
        val thread = createThread(user = user)
        val chat1 = createChat(id = 1L, thread = thread, question = "질문1", answer = "답변1")
        val chat2 = createChat(id = 2L, thread = thread, question = "질문2", answer = "답변2")
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        given(threadRepository.findAllByUserId(1L, pageable))
            .willReturn(PageImpl(listOf(thread), pageable, 1))
        given(chatRepository.findAllByThreadIdInOrderByCreatedAtAsc(listOf(1L)))
            .willReturn(listOf(chat1, chat2))

        // when
        val result = chatQueryService.getChats(userId = 1L, role = Role.MEMBER, page = 0, size = 20, sort = "desc")

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].chats).hasSize(2)
        assertThat(result.content[0].chats[0].question).isEqualTo("질문1")
        assertThat(result.content[0].chats[1].question).isEqualTo("질문2")
    }
}
