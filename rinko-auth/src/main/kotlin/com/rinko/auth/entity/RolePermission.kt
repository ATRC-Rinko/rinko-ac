package com.rinko.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("roles")
data class Role(
    @Id
    val id: Long,
    val name: String,
    val description: String? = null,
    val isSystem: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Table("permissions")
data class Permission(
    @Id
    val id: Long,
    val code: String,
    val description: String? = null
)

@Table("user_roles")
data class UserRole(
    val userId: Long,
    val roleId: Long
)

@Table("role_permissions")
data class RolePermission(
    val roleId: Long,
    val permissionId: Long
)

@Table("role_hierarchy")
data class RoleHierarchy(
    val ancestor: Long,
    val descendant: Long,
    val depth: Int
)
