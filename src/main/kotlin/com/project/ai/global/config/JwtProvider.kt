package com.project.ai.global.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
) {
    init {
        require(secret.isNotBlank()) { "JWT_SECRET 환경변수가 설정되지 않았습니다." }
        require(secret.toByteArray().size >= 32) { "JWT_SECRET은 최소 32바이트 이상이어야 합니다." }
    }

    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun generateToken(
        userId: Long,
        email: String,
        role: String,
    ): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(Date(now.time + expiration))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean =
        try {
            parseClaims(token)
            true
        } catch (e: ExpiredJwtException) {
            false
        } catch (e: JwtException) {
            false
        }

    fun getUserId(token: String): Long = parseClaims(token).subject.toLong()

    fun getEmail(token: String): String = parseClaims(token)["email"] as String

    fun getRole(token: String): String = parseClaims(token)["role"] as String

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
