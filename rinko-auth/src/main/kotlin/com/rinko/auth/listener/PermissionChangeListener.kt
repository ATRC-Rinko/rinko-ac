package com.rinko.auth.listener

import com.rinko.auth.cache.PermissionCacheService
import com.rinko.auth.event.PermissionChangedEvent
import com.rinko.auth.event.RoleHierarchyChangedEvent
import com.rinko.auth.event.UserRoleChangedEvent
import com.rinko.auth.repository.RoleHierarchyRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class PermissionChangeListener(
    private val permissionCacheService: PermissionCacheService,
    private val roleHierarchyRepository: RoleHierarchyRepository,
    private val databaseClient: DatabaseClient
) {
    companion object {
        private val log = LoggerFactory.getLogger(PermissionChangeListener::class.java)
    }

    @EventListener
    fun onPermissionChanged(event: PermissionChangedEvent) {
        log.info("Received PermissionChangedEvent for roleId={}", event.roleId)
        findUsersByRole(event.roleId)
            .flatMap { userIds ->
                permissionCacheService.evictByRole(event.roleId, userIds)
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                { log.debug("T047: Cache evicted for roleId={}", event.roleId) },
                { log.error("Failed to evict cache for roleId={}: {}", event.roleId, it.message) }
            )
    }

    @EventListener
    fun onUserRoleChanged(event: UserRoleChangedEvent) {
        log.info("Received UserRoleChangedEvent for userId={}", event.userId)
        permissionCacheService.evictByUser(event.userId)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                { log.debug("T048: Cache evicted for userId={}", event.userId) },
                { log.error("Failed to evict cache for userId={}: {}", event.userId, it.message) }
            )
    }

    @EventListener
    fun onRoleHierarchyChanged(event: RoleHierarchyChangedEvent) {
        log.info("Received RoleHierarchyChangedEvent for roleId={}", event.roleId)
        findAffectedUsers(event.roleId)
            .flatMap { affectedUserIds ->
                permissionCacheService.evictByHierarchyChange(event.roleId, affectedUserIds)
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                { log.debug("T049: Cache evicted for hierarchy change roleId={}", event.roleId) },
                { log.error("Failed to evict cache for hierarchy change roleId={}: {}", event.roleId, it.message) }
            )
    }

    private fun findUsersByRole(roleId: Long): Mono<List<Long>> {
        return roleHierarchyRepository.findDescendantsByAncestor(roleId)
            .collectList()
            .flatMap { descendants ->
                val allRoleIds = mutableListOf(roleId)
                descendants.forEach { allRoleIds.add(it.descendant) }
                collectUsersByRoles(allRoleIds)
            }
    }

    private fun findAffectedUsers(roleId: Long): Mono<List<Long>> {
        return roleHierarchyRepository.findDescendantsByAncestor(roleId)
            .collectList()
            .flatMap { descendants ->
                val allRoleIds = mutableListOf(roleId)
                descendants.forEach { allRoleIds.add(it.descendant) }
                collectUsersByRoles(allRoleIds)
            }
    }

    private fun collectUsersByRoles(roleIds: List<Long>): Mono<List<Long>> {
        if (roleIds.isEmpty()) return Mono.just(emptyList())
        val placeholders = roleIds.mapIndexed { i, _ -> ":r$i" }.joinToString(",")
        var spec = databaseClient.sql(
            "SELECT DISTINCT user_id FROM user_roles WHERE role_id IN ($placeholders)"
        )
        for ((i, roleId) in roleIds.withIndex()) {
            spec = spec.bind("r$i", roleId)
        }
        return spec.mapValue(Long::class.java)
            .all()
            .collectList()
    }
}
