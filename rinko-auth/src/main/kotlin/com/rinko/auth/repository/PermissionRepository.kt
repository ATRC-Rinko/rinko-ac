package com.rinko.auth.repository

import com.rinko.auth.entity.Permission
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PermissionRepository : ReactiveCrudRepository<Permission, Long> {

    @Query("""
        SELECT p.* FROM permissions p
        INNER JOIN role_permissions rp ON p.id = rp.permission_id
        WHERE rp.role_id = :roleId
    """)
    fun findByRoleId(roleId: Long): Flux<Permission>

    /**
     * 通配符匹配：检查是否存在匹配 requiredPermission 模式的权限
     * 支持: * 匹配单段, *:* 匹配两段, resource:*:action
     */
    @Query("""
        SELECT p.* FROM permissions p
        INNER JOIN role_permissions rp ON p.id = rp.permission_id
        WHERE rp.role_id IN (:roleIds)
        AND :permissionCode LIKE REPLACE(REPLACE(p.code, '*', '%'), '?', '_')
    """)
    fun matchWildcard(roleIds: List<Long>, permissionCode: String): Flux<Permission>
}
