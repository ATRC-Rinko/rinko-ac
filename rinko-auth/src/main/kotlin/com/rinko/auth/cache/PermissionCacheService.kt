package com.rinko.auth.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.rinko.auth.service.PermissionEvaluator
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class PermissionCacheService(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val permissionEvaluator: PermissionEvaluator
) {
    companion object {
        private val log = LoggerFactory.getLogger(PermissionCacheService::class.java)
        private const val REDIS_KEY_PREFIX = "auth:permissions:"
        private const val L2_TTL_MINUTES = 5L
    }

    private val localCache: Cache<Long, Set<String>> = Caffeine.newBuilder()
        .maximumSize(1_000)
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .recordStats()
        .build()

    fun getPermissions(userId: Long): Mono<Set<String>> {
        val cached: Set<String>? = localCache.getIfPresent(userId)
        if (cached != null) {
            log.debug("L1 cache hit for userId={}", userId)
            return Mono.just(cached)
        }
        val redisKey: String = "$REDIS_KEY_PREFIX$userId"
        return redisTemplate.opsForValue().get(redisKey)
            .flatMap { value: String ->
                log.debug("L2 cache hit for userId={}", userId)
                val permissions: Set<String> = parsePermissionSet(value)
                localCache.put(userId, permissions)
                Mono.just(permissions)
            }
            .switchIfEmpty(
                permissionEvaluator.getUserPermissions(userId)
                    .flatMap { permissions: Set<String> ->
                        log.debug("L3 DB lookup for userId={}", userId)
                        backfillCache(userId, permissions, redisKey)
                            .flatMap { Mono.just(permissions) }
                    }
            )
    }

    private fun backfillCache(userId: Long, permissions: Set<String>, redisKey: String): Mono<Boolean> {
        localCache.put(userId, permissions)
        return redisTemplate.opsForValue()
            .set(redisKey, permissions.joinToString(","), Duration.ofMinutes(L2_TTL_MINUTES))
    }

    fun evictByRole(roleId: Long, userIds: List<Long>): Mono<Void> {
        userIds.forEach { userId: Long -> localCache.invalidate(userId) }
        log.debug("L1 evicted for {} user(s) (roleId={})", userIds.size, roleId)
        if (userIds.isEmpty()) {
            return Mono.empty()
        }
        val keys: Array<String> = userIds.map { "$REDIS_KEY_PREFIX$it" }.toTypedArray()
        return redisTemplate.delete(*keys).then()
    }

    fun evictByUser(userId: Long): Mono<Void> {
        localCache.invalidate(userId)
        log.debug("L1 evicted for userId={}", userId)
        return redisTemplate.delete("$REDIS_KEY_PREFIX$userId").then()
    }

    fun evictByHierarchyChange(roleId: Long, affectedUserIds: List<Long>): Mono<Void> {
        if (affectedUserIds.isEmpty()) {
            return Mono.empty()
        }
        affectedUserIds.forEach { userId: Long -> localCache.invalidate(userId) }
        log.debug("L1 evicted for {} user(s) (hierarchy change roleId={})", affectedUserIds.size, roleId)
        val keys: Array<String> = affectedUserIds.map { "$REDIS_KEY_PREFIX$it" }.toTypedArray()
        return redisTemplate.delete(*keys).then()
    }

    fun cacheStats(): Mono<Map<String, Any>> {
        val stats: Map<String, Any> = mapOf(
            "l1_size" to localCache.estimatedSize(),
            "l1_hitRate" to localCache.stats().hitRate(),
            "l1_hitCount" to localCache.stats().hitCount(),
            "l1_missCount" to localCache.stats().missCount()
        )
        return Mono.just(stats)
    }

    private fun parsePermissionSet(value: String): Set<String> {
        if (value.isBlank()) {
            return emptySet()
        }
        return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }
}
