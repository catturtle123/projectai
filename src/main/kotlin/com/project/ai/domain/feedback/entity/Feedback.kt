package com.project.ai.domain.feedback.entity

import com.project.ai.domain.chat.entity.Chat
import com.project.ai.domain.user.entity.User
import com.project.ai.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "chat_id"]),
    ],
)
class Feedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "is_positive", nullable = false)
    val isPositive: Boolean,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: FeedbackStatus = FeedbackStatus.PENDING,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,
) : BaseTimeEntity()

enum class FeedbackStatus {
    PENDING,
    RESOLVED,
}
