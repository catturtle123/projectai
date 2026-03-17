package com.project.ai.domain.analytics.entity

import com.project.ai.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "activity_logs",
    indexes = [
        Index(name = "idx_activity_type_created_at", columnList = "activity_type, created_at"),
    ],
)
class ActivityLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 20)
    val activityType: ActivityType,
    @Column(nullable = false)
    val userId: Long,
) : BaseTimeEntity()

enum class ActivityType {
    SIGNUP,
    LOGIN,
    CHAT_CREATE,
}
