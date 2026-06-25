package com.rinko.auth.security

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.mockito.Mockito.*
import org.springframework.data.redis.core.ReactiveHashOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class TokenBlacklistServiceTest : StringSpec({

    val valueOps = mock(ReactiveValueOperations::class.java) as ReactiveValueOperations<String, String>
    val redisTemplate = mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
    `when`(redisTemplate.opsForValue()).thenReturn(valueOps)

    val service = TokenBlacklistService(redisTemplate)

    "extractJti 应使用 ObjectMapper 正确解析 JWT payload 中的 jti" {
        // Given: a JWT token with known jti
        val jti = "550e8400-e29b-41d4-a716-446655440000"
        val header = "{\"alg\":\"HS256\"}"
        val payload = "{\"jti\":\"$jti\",\"sub\":\"user1\",\"roles\":[\"admin\"]}"

        val headerB64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(header.toByteArray())
        val payloadB64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
        val token = "$headerB64.$payloadB64.signature"

        `when`(redisTemplate.hasKey("jwt:blacklist:$jti")).thenReturn(Mono.just(false))

        StepVerifier.create(service.isBlacklisted(token))
            .consumeNextWith { blacklisted ->
                blacklisted.shouldBeFalse()
            }
            .verifyComplete()
    }

    "isBlacklisted 对非法的 token payload 应返回 false（安全降级）" {
        val token = "bad.part.only"

        StepVerifier.create(service.isBlacklisted(token))
            .consumeNextWith { blacklisted ->
                blacklisted.shouldBeFalse()
            }
            .verifyComplete()
    }

    "isBlacklisted 对缺少 jti 的 token 应返回 true（视为黑名单）" {
        val payload = "{\"sub\":\"user1\"}"
        val headerB64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\"}".toByteArray())
        val payloadB64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
        val token = "$headerB64.$payloadB64.sig"

        StepVerifier.create(service.isBlacklisted(token))
            .consumeNextWith { blacklisted ->
                blacklisted.shouldBeTrue() // no jti → 视为不可信
            }
            .verifyComplete()
    }

    "isBlacklisted 对已黑名单 token 应返回 true" {
        val jti = "blacklisted-jti-001"
        val payload = "{\"jti\":\"$jti\"}"
        val headerB64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\"}".toByteArray())
        val payloadB64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
        val token = "$headerB64.$payloadB64.sig"

        `when`(redisTemplate.hasKey("jwt:blacklist:$jti")).thenReturn(Mono.just(true))

        StepVerifier.create(service.isBlacklisted(token))
            .consumeNextWith { blacklisted ->
                blacklisted.shouldBeTrue()
            }
            .verifyComplete()
    }
})
