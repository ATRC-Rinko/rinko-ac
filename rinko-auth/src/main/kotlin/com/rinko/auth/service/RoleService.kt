package com.rinko.auth.service

import com.rinko.auth.entity.Role
import com.rinko.auth.event.UserRoleChangedEvent
import com.rinko.auth.repository.RoleRepository
import com.rinko.auth.repository.UserRepository
import com.rinko.infra.dto.PageRequest
import com.rinko.infra.dto.PageResponse
import com.rinko.infra.exception.ForbiddenException
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
class RoleService(
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val snowflakeIdGenerator: SnowflakeIdGenerator,
    private val databaseClient: DatabaseClient,
    private val eventPublisher: ApplicationEventPublisher
) {
    companion object {
        private val log = LoggerFactory.getLogger(RoleService::class.java)
    }

    // ===== T023: createRole =====
    fun createRole(name: String, description: String?): Mono<Role> {
        return roleRepository.findByName(name)
            .hasElement()
            .flatMap { exists ->
                if (exists) {
                    return@flatMap Mono.error<Role>(ValidationException("Role '$name' already exists"))
                }
                val role = Role(
                    id = snowflakeIdGenerator.nextId(),
                    name = name,
                    description = description
                )
                roleRepository.save(role)
            }
    }

    // ===== T024: updateRole =====
    fun updateRole(id: Long, name: String, description: String?): Mono<Role> {
        return roleRepository.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Role not found: $id")))
            .flatMap { role ->
                if (role.isSystem) {
                    return@flatMap Mono.error<Role>(ForbiddenException("Cannot modify system role"))
                }
                roleRepository.save(role.copy(name = name, description = description))
            }
    }

    // ===== T025: deleteRole =====
    @Transactional
    fun deleteRole(id: Long): Mono<Void> {
        return roleRepository.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Role not found: $id")))
            .flatMap { role ->
                if (role.isSystem) {
                    return@flatMap Mono.error<Void>(ForbiddenException("Cannot delete system role"))
                }
                // Check if role has associated users
                databaseClient.sql("SELECT COUNT(*) FROM user_roles WHERE role_id = :roleId")
                    .bind("roleId", id)
                    .mapValue(Long::class.java)
                    .one()
                    .flatMap { count ->
                        if (count > 0) {
                            return@flatMap Mono.error<Void>(
                                ValidationException("Cannot delete role: $count user(s) assigned")
                            )
                        }
                        // Remove role_permissions first
                        databaseClient.sql("DELETE FROM role_permissions WHERE role_id = :roleId")
                            .bind("roleId", id)
                            .fetch()
                            .rowsUpdated()
                            .then(roleRepository.deleteById(id))
                    }
            }
    }

    // ===== T027: listRoles (paginated) =====
    fun listRoles(pageRequest: PageRequest = PageRequest()): Mono<PageResponse<Role>> {
        val offset = pageRequest.offset
        val size = pageRequest.size
        val countQuery = "SELECT COUNT(*) FROM roles"
        val dataQuery = "SELECT * FROM roles ORDER BY id LIMIT :limit OFFSET :offset"

        return databaseClient.sql(countQuery)
            .mapValue(Long::class.java)
            .one()
            .flatMap { total ->
                databaseClient.sql(dataQuery)
                    .bind("limit", size)
                    .bind("offset", offset)
                    .map { row ->
                        Role(
                            id = row.get("id", Long::class.java)!!,
                            name = row.get("name", String::class.java)!!,
                            description = row.get("description", String::class.java),
                            isSystem = row.get("is_system", Boolean::class.java) ?: false
                        )
                    }
                    .all()
                    .collectList()
                    .map { content -> PageResponse(content, total, pageRequest.page, size) }
            }
    }

    fun getRoleById(id: Long): Mono<Role> {
        return roleRepository.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Role not found: $id")))
    }

    fun getAllRoles(): Flux<Role> = roleRepository.findAll()

    // ===== T033: assignRolesToUser =====
    @Transactional
    fun assignRolesToUser(userId: Long, roleIds: List<Long>): Mono<Void> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(NotFoundException("User not found: $userId")))
            .thenMany(Flux.fromIterable(roleIds))
            .flatMap { roleId ->
                roleRepository.findById(roleId)
                    .switchIfEmpty(Mono.error(NotFoundException("Role not found: $roleId")))
                    .then(
                        databaseClient.sql(
                            "INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId) ON CONFLICT DO NOTHING"
                        )
                            .bind("userId", userId)
                            .bind("roleId", roleId)
                            .fetch()
                            .rowsUpdated()
                    )
            }
            .then(Mono.fromRunnable {
                eventPublisher.publishEvent(UserRoleChangedEvent(userId))
                log.debug("Published UserRoleChangedEvent for userId={}", userId)
            })
    }

    // ===== T034: removeRoleFromUser =====
    @Transactional
    fun removeRoleFromUser(userId: Long, roleId: Long): Mono<Void> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(NotFoundException("User not found: $userId")))
            .then(roleRepository.findById(roleId))
            .switchIfEmpty(Mono.error(NotFoundException("Role not found: $roleId")))
            .then(
                databaseClient.sql("DELETE FROM user_roles WHERE user_id = :userId AND role_id = :roleId")
                    .bind("userId", userId)
                    .bind("roleId", roleId)
                    .fetch()
                    .rowsUpdated()
            )
            .flatMap { rowsAffected ->
                if (rowsAffected == 0L) {
                    return@flatMap Mono.error<Void>(NotFoundException("User does not have this role"))
                }
                eventPublisher.publishEvent(UserRoleChangedEvent(userId))
                log.debug("Published UserRoleChangedEvent for userId={}", userId)
                Mono.empty()
            }
    }
}
