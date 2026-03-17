package com.project.ai.domain.analytics.entity

import com.project.ai.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "activity_logs")
class ActivityLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 20)
    val activityType: String,
    @Column(nullable = false)
    val userId: Long,
) : BaseTimeEntity()
