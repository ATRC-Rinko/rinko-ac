package com.rinko.auth.service

import com.rinko.auth.entity.Permission
import com.rinko.auth.event.PermissionChangedEvent
import com.rinko.auth.repository.PermissionRepository
import com.rinko.auth.repository.RoleRepository
import com.rinko.infra.exception.NotFoundException
import com.rinko.infra.exception.ValidationException
import com.rinko.infra.id.SnowflakeIdGenerator
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PermissionService(
    private val permissionRepository: PermissionRepository,
    private val roleRepository: RoleRepository,
    private val snowflakeIdGenerator: SnowflakeIdGenerator,
    private val databaseClient: DatabaseClient,
    private val eventPublisher: ApplicationEventPublisher
) {
    companion object {
        private val log = LoggerFactory.getLogger(PermissionService::class.java)
        private val CODE_PATTERN = Regex("^[a-z]+(:[a-z*]+)+$")
    }

    // ===== T028: createPermission with format validation =====
    fun createPermission(code: String, description: String?): Mono<Permission> {
        if (!CODE_PATTERN.matches(code)) {
            return Mono.error(ValidationException("Invalid permission code format: $code"))
        }
        val permission = Permission(
            id = snowflakeIdGenerator.nextId(),
            code = code,
            description = description
        )
        return permissionRepository.save(permission)
    }

    fun updatePermission(id: Long, code: String, description: String?): Mono<Permission> {
        if (!CODE_PATTERN.matches(code)) {
            return Mono.error(ValidationException("Invalid permission code format: $code"))
        }
        return permissionRepository.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Permission not found: $id")))
            .flatMap { p ->
                permissionRepository.save(p.copy(code = code, description = description))
            }
    }

    fun deletePermission(id: Long): Mono<Void> {
        return permissionRepository.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Permission not found: $id")))
            .flatMap {
                databaseClient.sql("DELETE FROM role_permissions WHERE permission_id = :permissionId")
                    .bind("permissionId", id)
                    .fetch()
                    .rowsUpdated()
                    .then(permissionRepository.deleteById(id))
            }
    }

    fun listPermissions(): Flux<Permission> = permissionRepository.findAll()

    fun getPermissionById(id: Long): Mono<Permission> {
        return permissionRepository.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Permission not found: $id")))
    }

    // ===== T030: assignPermissionsToRole (batch) =====
    @Transactional
    fun assignPermissionsToRole(roleId: Long, permissionIds: List<Long>): Mono<Void> {
        return roleRepository.findById(roleId)
            .switchIfEmpty(Mono.error(NotFoundException("Role not found: $roleId")))
            .thenMany(Flux.fromIterable(permissionIds))
            .flatMap { permId ->
                permissionRepository.findById(permId)
                    .switchIfEmpty(Mono.error(NotFoundException("Permission not found: $permId")))
                    .then(databaseClient.sql(
                        "INSERT INTO role_permissions (role_id, permission_id) VALUES (:roleId, :permId) ON CONFLICT DO NOTHING"
                    )
                        .bind("roleId", roleId)
                        .bind("permId", permId)
                        .fetch()
                        .rowsUpdated()
                    )
            }
            .then(Mono.fromRunnable {
                // T032: publish PermissionChangedEvent via Spring ApplicationEventPublisher
                eventPublisher.publishEvent(PermissionChangedEvent(roleId))
                log.debug("Published PermissionChangedEvent for roleId={}", roleId)
            })
    }

    // ===== T031: removePermissionFromRole =====
    @Transactional
    fun removePermissionFromRole(roleId: Long, permissionId: Long): Mono<Void> {
        return roleRepository.findById(roleId)
            .switchIfEmpty(Mono.error(NotFoundException("Role not found: $roleId")))
            .then(permissionRepository.findById(permissionId))
            .switchIfEmpty(Mono.error(NotFoundException("Permission not found: $permissionId")))
            .then(
                databaseClient.sql(
                    "DELETE FROM role_permissions WHERE role_id = :roleId AND permission_id = :permId"
                )
                    .bind("roleId", roleId)
                    .bind("permId", permissionId)
                    .fetch()
                    .rowsUpdated()
            )
            .flatMap { rowsAffected ->
                if (rowsAffected == 0L) {
                    return@flatMap Mono.error<Void>(NotFoundException("Permission not assigned to this role"))
                }
                eventPublisher.publishEvent(PermissionChangedEvent(roleId))
                log.debug("Published PermissionChangedEvent for roleId={}", roleId)
                Mono.empty()
            }
    }
}
