package com.rinko.auth.dto

/**
 * Role VO — API response representation, excludes DB-internal fields.
 */
data class RoleVO(
    val id: Long,
    val name: String,
    val description: String?
)

/**
 * Permission VO — API response representation.
 */
data class PermissionVO(
    val id: Long,
    val code: String,
    val description: String?
)

/**
 * RoleHierarchy VO — API response representation.
 */
data class RoleHierarchyVO(
    val ancestor: Long,
    val descendant: Long,
    val depth: Int
)
