package com.project.ai.domain.chat.service

import com.project.ai.domain.analytics.entity.ActivityType
import com.project.ai.domain.analytics.service.ActivityLogService
import com.project.ai.domain.chat.dto.ChatCreateRequest
import com.project.ai.domain.chat.dto.ChatCreateResponse
import com.project.ai.domain.chat.dto.ChatResponse
import com.project.ai.domain.chat.dto.OpenAiMessage
import com.project.ai.domain.chat.dto.ThreadResponse
import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.chat.entity.Thread
import com.project.ai.domain.chat.repository.ChatRepository
import com.project.ai.domain.chat.repository.ThreadRepository
import com.project.ai.domain.user.entity.Role
import com.project.ai.domain.user.entity.User
import com.project.ai.domain.user.repository.UserRepository
import com.project.ai.global.error.AppException
import com.project.ai.global.error.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ChatService(
    private val chatRepository: ChatRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
    private val openAiService: OpenAiService,
    private val transactionTemplate: TransactionTemplate,
    private val activityLogService: ActivityLogService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val MAX_CONTEXT_MESSAGES = 10
        private val VALID_SORT_VALUES = setOf("asc", "desc")
    }

    @Transactional
    fun createChat(
        userId: Long,
        request: ChatCreateRequest,
    ): ChatCreateResponse {
        val user = findUser(userId)
        val thread = resolveThread(user)
        val previousChats = chatRepository.findAllByThreadOrderByCreatedAtAsc(thread)
        val messages = buildMessages(previousChats, request.question)
        val answer = openAiService.chat(messages, request.model)

        val chat =
            chatRepository.save(
                Chat(
                    thread = thread,
                    question = request.question,
                    answer = answer,
                ),
            )

        activityLogService.log(ActivityType.CHAT_CREATE, userId)

        return ChatCreateResponse(
            chatId = chat.id,
            threadId = thread.id,
            question = chat.question,
            answer = chat.answer,
            createdAt = chat.createdAt,
        )
    }

    @Transactional
    fun createChatStream(
        userId: Long,
        request: ChatCreateRequest,
    ): Pair<Long, Flux<String>> {
        val user = findUser(userId)
        val thread = resolveThread(user)
        val previousChats = chatRepository.findAllByThreadOrderByCreatedAtAsc(thread)
        val messages = buildMessages(previousChats, request.question)
        val contentFlux = openAiService.chatStream(messages, request.model)

        val buffer = StringBuilder()
        val threadId = thread.id
        val question = request.question

        val resultFlux =
            contentFlux
                .doOnNext { content -> buffer.append(content) }
                .doOnComplete {
                    transactionTemplate.execute {
                        chatRepository.save(
                            Chat(
                                thread = thread,
                                question = question,
                                answer = buffer.toString(),
                            ),
                        )
                        activityLogService.log(ActivityType.CHAT_CREATE, userId)
                    }
                    log.info("스트리밍 대화 저장 완료: threadId={}", threadId)
                }

        return Pair(threadId, resultFlux)
    }

    private fun findUser(userId: Long): User =
        userRepository.findById(userId).orElseThrow {
            AppException(ErrorCode.USER_NOT_FOUND)
        }

    private fun resolveThread(user: User): Thread {
        val latestThread = threadRepository.findTopByUserIdOrderByCreatedAtDesc(user.id)
        if (latestThread != null) {
            val latestChat = chatRepository.findTopByThreadOrderByCreatedAtDesc(latestThread)
            val thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30)
            if (latestChat != null && latestChat.createdAt.isAfter(thirtyMinutesAgo)) {
                return latestThread
            }
            if (latestChat == null && latestThread.createdAt.isAfter(thirtyMinutesAgo)) {
                return latestThread
            }
        }
        return threadRepository.save(Thread(user = user))
    }

    fun getChats(
        userId: Long,
        role: Role,
        page: Int,
        size: Int,
        sort: String,
    ): Page<ThreadResponse> {
        if (sort !in VALID_SORT_VALUES) {
            throw AppException(ErrorCode.COMMON400)
        }

        val sortDirection = if (sort == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, "createdAt"))

        val threads =
            if (role == Role.ADMIN) {
                threadRepository.findAll(pageable)
            } else {
                threadRepository.findAllByUserId(userId, pageable)
            }

        val threadIds = threads.content.map { it.id }
        val chatsByThreadId =
            if (threadIds.isNotEmpty()) {
                chatRepository.findAllByThreadIdInOrderByCreatedAtAsc(threadIds)
                    .groupBy { it.thread.id }
            } else {
                emptyMap()
            }

        return threads.map { thread ->
            val chats = chatsByThreadId[thread.id] ?: emptyList()
            ThreadResponse(
                threadId = thread.id,
                createdAt = thread.createdAt,
                chats =
                    chats.map { chat ->
                        ChatResponse(
                            chatId = chat.id,
                            question = chat.question,
                            answer = chat.answer,
                            createdAt = chat.createdAt,
                        )
                    },
            )
        }
    }

    private fun buildMessages(
        previousChats: List<Chat>,
        newQuestion: String,
    ): List<OpenAiMessage> {
        val messages =
            mutableListOf(
                OpenAiMessage(role = "system", content = "You are a helpful assistant."),
            )
        for (chat in previousChats.takeLast(MAX_CONTEXT_MESSAGES)) {
            messages.add(OpenAiMessage(role = "user", content = chat.question))
            messages.add(OpenAiMessage(role = "assistant", content = chat.answer))
        }
        messages.add(OpenAiMessage(role = "user", content = newQuestion))
        return messages
    }
}
