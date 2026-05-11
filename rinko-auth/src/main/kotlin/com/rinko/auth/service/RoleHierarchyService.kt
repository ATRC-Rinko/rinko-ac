package com.rinko.auth.service

import com.rinko.auth.entity.RoleHierarchy
import com.rinko.auth.event.RoleHierarchyChangedEvent
import com.rinko.auth.repository.RoleHierarchyRepository
import com.rinko.auth.repository.RoleRepository
import com.rinko.infra.exception.NotFoundException
import com.rinko.infra.exception.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class RoleHierarchyService(
    private val roleHierarchyRepository: RoleHierarchyRepository,
    private val roleRepository: RoleRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    companion object {
        private val log = LoggerFactory.getLogger(RoleHierarchyService::class.java)
        private const val MAX_DEPTH = 3
    }

    // ===== T036/T037: addParent with closure table rebuild =====
    @Transactional
    fun addParent(roleId: Long, parentRoleId: Long): Mono<Void> {
        return roleRepository.findById(roleId)
            .switchIfEmpty(Mono.error(NotFoundException("Role not found: $roleId")))
            .then(roleRepository.findById(parentRoleId))
            .switchIfEmpty(Mono.error(NotFoundException("Parent role not found: $parentRoleId")))
            .then(calculateDepth(roleId, parentRoleId))
            .flatMap { newDepth ->
                if (newDepth > MAX_DEPTH) {
                    return@flatMap Mono.error<Void>(ValidationException("Role hierarchy cannot exceed $MAX_DEPTH levels"))
                }
                val hierarchy = RoleHierarchy(ancestor = parentRoleId, descendant = roleId, depth = 1)
                roleHierarchyRepository.save(hierarchy)
                    .then(rebuildClosureTable(roleId, parentRoleId))
            }
            .then(Mono.fromRunnable {
                // T039: publish event
                eventPublisher.publishEvent(RoleHierarchyChangedEvent(roleId))
                log.debug("Published RoleHierarchyChangedEvent for roleId={}", roleId)
            })
    }

    // ===== T038: removeParent =====
    @Transactional
    fun removeParent(roleId: Long, parentRoleId: Long): Mono<Void> {
        return roleHierarchyRepository.findAll()
            .filter { it.ancestor == parentRoleId && it.descendant == roleId && it.depth == 1 }
            .next()
            .switchIfEmpty(Mono.error(NotFoundException("Hierarchy not found: $parentRoleId -> $roleId")))
            .flatMap {
                roleHierarchyRepository.deleteAll(
                    roleHierarchyRepository.findAll()
                        .filter { rh -> isAffectedByRemoval(rh, roleId, parentRoleId) }
                )
            }
            .then(Mono.fromRunnable {
                // T039: publish event
                eventPublisher.publishEvent(RoleHierarchyChangedEvent(roleId))
                log.debug("Published RoleHierarchyChangedEvent for roleId={}", roleId)
            })
    }

    fun getDescendants(roleId: Long): Flux<RoleHierarchy> {
        return roleHierarchyRepository.findDescendantsByAncestor(roleId)
    }

    fun getAncestors(roleId: Long): Flux<RoleHierarchy> {
        return roleHierarchyRepository.findAncestorsByDescendant(roleId)
    }

    private fun calculateDepth(roleId: Long, parentRoleId: Long): Mono<Int> {
        return getAncestors(parentRoleId)
            .collectList()
            .map { ancestors ->
                val maxAncestorDepth = ancestors.maxOfOrNull { it.depth } ?: 0
                maxAncestorDepth + 1
            }
    }

    private fun rebuildClosureTable(roleId: Long, parentRoleId: Long): Mono<Void> {
        return getAncestors(parentRoleId)
            .concatMap { ancestor ->
                val rh = RoleHierarchy(ancestor = ancestor.ancestor, descendant = roleId, depth = ancestor.depth + 1)
                roleHierarchyRepository.save(rh)
            }
            .then()
    }

    private fun isAffectedByRemoval(rh: RoleHierarchy, roleId: Long, parentRoleId: Long): Boolean {
        return rh.descendant == roleId && rh.depth > 0
    }
}
