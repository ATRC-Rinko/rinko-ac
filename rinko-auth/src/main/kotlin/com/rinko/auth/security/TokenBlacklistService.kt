package com.rinko.auth.security

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class TokenBlacklistService(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    companion object {
        private const val BLACKLIST_PREFIX = "jwt:blacklist:"
        private val objectMapper = ObjectMapper()
    }

    fun add(tokenId: String, expiration: Date): Mono<Boolean> {
        val key = "$BLACKLIST_PREFIX$tokenId"
        val ttl = Duration.between(Instant.now(), expiration.toInstant())
        return if (ttl.isPositive) {
            redisTemplate.opsForValue().set(key, "revoked", ttl)
        } else {
            Mono.just(true)
        }
    }

    fun isBlacklisted(token: String): Mono<Boolean> {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return Mono.just(true)
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val jti = extractJti(payload)
            if (jti == null) return Mono.just(true)
            redisTemplate.hasKey("$BLACKLIST_PREFIX$jti")
        } catch (e: Exception) {
            Mono.just(false)
        }
    }

    /** Parse JWT payload JSON to extract the jti claim. Uses Jackson instead of regex for correctness. */
    private fun extractJti(payload: String): String? {
        return try {
            val node = objectMapper.readTree(payload)
            node.get("jti")?.asText()
        } catch (e: Exception) {
            null
        }
    }
}
