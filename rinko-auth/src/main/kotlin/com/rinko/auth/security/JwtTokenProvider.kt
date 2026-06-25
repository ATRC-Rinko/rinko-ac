package com.rinko.auth.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${rinko.auth.jwt.secret}") secret: String,
    @Value("\${rinko.auth.jwt.access-token-expiration}") private val accessTokenExpiration: Long,
    @Value("\${rinko.auth.jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long,
    private val tokenBlacklistService: TokenBlacklistService
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray().let {
        if (it.size < 32) it.copyOf(32) else it
    })

    companion object {
        private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    }

    // ===== T014: generateAccessToken =====
    fun generateAccessToken(userId: Long, username: String, roles: List<String>): String {
        val now = Date()
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(username)
            .claim("userId", userId)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(Date(now.time + accessTokenExpiration * 1000))
            .signWith(key)
            .compact()
    }

    // ===== T015: generateRefreshToken =====
    fun generateRefreshToken(userId: Long, username: String): String {
        val now = Date()
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(username)
            .claim("userId", userId)
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(Date(now.time + refreshTokenExpiration * 1000))
            .signWith(key)
            .compact()
    }

    // ===== T016: validateToken =====
    fun validateToken(token: String): Mono<Boolean> {
        return tokenBlacklistService.isBlacklisted(token)
            .map { blacklisted ->
                if (blacklisted) {
                    log.debug("Token is blacklisted")
                    return@map false
                }
                try {
                    parseToken(token)
                    true
                } catch (e: ExpiredJwtException) {
                    log.debug("Token expired")
                    false
                } catch (e: JwtException) {
                    log.debug("Invalid token: {}", e.message)
                    false
                }
            }
    }

    // ===== T017: getAuthentication =====
    fun getAuthentication(token: String): Authentication {
        val claims = parseToken(token).payload
        val username = claims.subject

        @Suppress("UNCHECKED_CAST")
        val roles = claims["roles"] as? List<String> ?: emptyList()
        val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }
        val auth = UsernamePasswordAuthenticationToken(username, token, authorities)
        auth.details = mapOf("userId" to claims["userId"])
        return auth
    }

    fun getUserId(token: String): Long {
        return (parseToken(token).payload["userId"] as Number).toLong()
    }

    fun getTokenId(token: String): String {
        return parseToken(token).payload.id
    }

    fun getTokenExpiration(token: String): Date {
        return parseToken(token).payload.expiration
    }

    private fun parseToken(token: String): Jws<Claims> {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
    }
}
