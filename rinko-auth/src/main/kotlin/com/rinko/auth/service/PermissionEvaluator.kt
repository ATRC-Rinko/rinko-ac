package com.rinko.auth.service

import com.rinko.auth.repository.PermissionRepository
import com.rinko.auth.repository.RoleHierarchyRepository
import com.rinko.auth.repository.RoleRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PermissionEvaluator(
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
    private val roleHierarchyRepository: RoleHierarchyRepository,
    private val wildcardMatcher: WildcardMatcher
) {
    fun checkPermission(userId: Long, requiredPermission: String): Mono<Boolean> {
        return roleRepository.findByUserId(userId)
            .collectList()
            .flatMap { userRoles ->
                // Get all roles including inherited
                val roleIds = userRoles.map { it.id }
                expandRoles(roleIds)
            }
            .flatMap { allRoleIds ->
                permissionRepository.matchWildcard(allRoleIds.toList(), requiredPermission)
                    .hasElements()
            }
    }

    fun getUserPermissions(userId: Long): Mono<Set<String>> {
        return roleRepository.findByUserId(userId)
            .collectList()
            .flatMap { userRoles ->
                val roleIds = userRoles.map { it.id }
                expandRoles(roleIds)
            }
            .flatMapMany { allRoleIds ->
                permissionRepository.findByRoleId(allRoleIds.first())
                    .collectList()
                    .map { it.map { p -> p.code }.toSet() }
            }
            .collectList()
            .map { it.flatten().toSet() }
    }

    private fun expandRoles(roleIds: List<Long>): Mono<Set<Long>> {
        val allRoles = roleIds.toMutableSet()
        return roleHierarchyRepository.findAll()
            .filter { rh -> allRoles.contains(rh.descendant) || allRoles.contains(rh.ancestor) }
            .collectList()
            .map { hierarchies ->
                hierarchies.forEach { rh ->
                    allRoles.add(rh.ancestor)
                    allRoles.add(rh.descendant)
                }
                allRoles
            }
    }
}
