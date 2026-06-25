package com.rinko.auth.dto

import jakarta.validation.constraints.NotBlank

/** Create/update permission request. */
data class CreatePermissionRequest(
    @field:NotBlank val code: String,
    val description: String? = null
)

data class UpdatePermissionRequest(
    @field:NotBlank val code: String,
    val description: String? = null
)

/** Create/update role request. */
data class CreateRoleRequest(
    @field:NotBlank val name: String,
    val description: String? = null
)

data class UpdateRoleRequest(
    @field:NotBlank val name: String,
    val description: String? = null
)

/** Bulk assign permissions/roles request. */
data class AssignPermissionsRequest(
    val permissionIds: List<Long>
)

data class AssignRolesRequest(
    val roleIds: List<Long>
)

/** Token refresh/revoke request. */
data class RefreshTokenRequest(
    @field:NotBlank val refreshToken: String
)

data class RevokeTokenRequest(
    @field:NotBlank val refreshToken: String
)

/** Permission check request. */
data class PermissionCheckRequest(
    val userId: Long,
    @field:NotBlank val requiredPermission: String
)
