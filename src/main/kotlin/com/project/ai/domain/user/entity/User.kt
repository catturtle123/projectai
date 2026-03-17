package com.project.ai.domain.user.entity

import com.project.ai.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false)
    var password: String,
    @Column(nullable = false, length = 50)
    val name: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val role: Role = Role.MEMBER,
) : BaseTimeEntity()

enum class Role {
    MEMBER,
    ADMIN,
}
