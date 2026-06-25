package com.rinko.auth.repository

import com.rinko.auth.entity.Permission
import com.rinko.auth.entity.Role
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface RoleRepository : ReactiveCrudRepository<Role, Long> {

    fun findByName(name: String): Mono<Role>

    @Query(
        """
        SELECT r.* FROM roles r
        INNER JOIN user_roles ur ON r.id = ur.role_id
        WHERE ur.user_id = :userId
    """
    )
    fun findByUserId(userId: Long): Flux<Role>

    @Query(
        """
        SELECT p.* FROM permissions p
        INNER JOIN role_permissions rp ON p.id = rp.permission_id
        WHERE rp.role_id = :roleId
    """
    )
    fun findPermissionsByRoleId(roleId: Long): Flux<Permission>
}
