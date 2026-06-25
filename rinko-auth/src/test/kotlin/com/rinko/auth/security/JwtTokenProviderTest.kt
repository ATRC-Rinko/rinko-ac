package com.rinko.auth.security

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class JwtTokenProviderTest : StringSpec({

    val secret = "test-secret-key-for-jwt-testing-minimum-32-bytes!!"
    val blacklistService = Mockito.mock(TokenBlacklistService::class.java)

    beforeTest {
        doReturn(Mono.just(false)).`when`(blacklistService).isBlacklisted(
            org.mockito.ArgumentMatchers.anyString()
        )
    }

    val provider = JwtTokenProvider(secret, 900L, 604800L, blacklistService)

    "generateAccessToken 应包含 userId, username, roles" {
        val token = provider.generateAccessToken(100L, "user1", listOf("admin", "user"))
        token shouldNotBe null

        val auth = provider.getAuthentication(token)
        auth.name shouldBe "user1"
        auth.authorities.map { it.authority } shouldContain "ROLE_admin"
        auth.authorities.map { it.authority } shouldContain "ROLE_user"

        val userId = provider.getUserId(token)
        userId shouldBe 100L
    }

    "generateRefreshToken 应有 7 天有效期" {
        val token = provider.generateRefreshToken(100L, "user1")
        token shouldNotBe null

        val expiration = provider.getTokenExpiration(token)
        val diff = expiration.time - System.currentTimeMillis()
        (diff > 600000000) shouldBe true
    }

    "validateToken 对有效 token 返回 true" {
        val token = provider.generateAccessToken(100L, "user1", listOf("admin"))
        StepVerifier.create(provider.validateToken(token))
            .consumeNextWith { valid: Boolean -> valid.shouldBeTrue() }
            .verifyComplete()
    }

    "validateToken 对黑名单 token 返回 false" {
        val token = provider.generateAccessToken(100L, "user1", listOf("admin"))
        doReturn(Mono.just(true)).`when`(blacklistService).isBlacklisted(token)

        StepVerifier.create(provider.validateToken(token))
            .consumeNextWith { valid: Boolean -> valid.shouldBeFalse() }
            .verifyComplete()
    }

    "getAuthentication 返回正确的 authorities" {
        val token = provider.generateAccessToken(100L, "user1", listOf("admin", "user"))
        val auth = provider.getAuthentication(token)

        auth.isAuthenticated shouldBe true
        auth.authorities.size shouldBe 2
    }
})
