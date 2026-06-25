package com.rinko.auth.controller

import com.rinko.auth.dto.*
import com.rinko.auth.service.PermissionService
import com.rinko.auth.service.RoleService
import com.rinko.infra.dto.ApiResponse
import com.rinko.infra.dto.PageRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Role Management", description = "角色与用户-角色管理接口")
class RoleController(
    private val roleService: RoleService,
    private val permissionService: PermissionService
) {

    @GetMapping("/roles")
    @Operation(summary = "分页查询角色列表")
    fun listRoles(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Mono<ApiResponse<Any>> {
        return roleService.listRoles(PageRequest(page, size, null))
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/roles")
    @Operation(summary = "创建角色")
    fun createRole(@Valid @RequestBody req: CreateRoleRequest, exchange: ServerWebExchange): Mono<ApiResponse<RoleVO>> {
        exchange.response.statusCode = HttpStatus.valueOf(201)
        return roleService.createRole(req.name, req.description)
            .map { RoleVO(it.id, it.name, it.description) }
            .map { ApiResponse.success(it) }
    }

    @PutMapping("/roles/{roleId}")
    @Operation(summary = "更新角色")
    fun updateRole(@PathVariable roleId: Long, @Valid @RequestBody req: UpdateRoleRequest): Mono<ApiResponse<RoleVO>> {
        return roleService.updateRole(roleId, req.name, req.description)
            .map { RoleVO(it.id, it.name, it.description) }
            .map { ApiResponse.success(it) }
    }

    @DeleteMapping("/roles/{roleId}")
    @Operation(summary = "删除角色")
    fun deleteRole(@PathVariable roleId: Long, exchange: ServerWebExchange): Mono<ApiResponse<Void>> {
        return roleService.deleteRole(roleId)
            .map {
                exchange.response.statusCode = HttpStatus.valueOf(204)
                ApiResponse.success(null)
            }
    }

    @PostMapping("/roles/{roleId}/permissions")
    @Operation(summary = "批量分配权限给角色")
    fun assignPermissions(
        @PathVariable roleId: Long,
        @Valid @RequestBody req: AssignPermissionsRequest
    ): Mono<ApiResponse<Void>> {
        return permissionService.assignPermissionsToRole(roleId, req.permissionIds)
            .map { ApiResponse.success(null) }
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @Operation(summary = "移除角色的单个权限")
    fun removePermission(
        @PathVariable roleId: Long,
        @PathVariable permissionId: Long,
        exchange: ServerWebExchange
    ): Mono<ApiResponse<Void>> {
        return permissionService.removePermissionFromRole(roleId, permissionId)
            .map {
                exchange.response.statusCode = HttpStatus.valueOf(204)
                ApiResponse.success(null)
            }
    }

    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "批量分配角色给用户")
    fun assignRoles(
        @PathVariable userId: Long,
        @Valid @RequestBody req: AssignRolesRequest
    ): Mono<ApiResponse<Void>> {
        return roleService.assignRolesToUser(userId, req.roleIds)
            .map { ApiResponse.success(null) }
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @Operation(summary = "移除用户的角色")
    fun removeRole(
        @PathVariable userId: Long,
        @PathVariable roleId: Long,
        exchange: ServerWebExchange
    ): Mono<ApiResponse<Void>> {
        return roleService.removeRoleFromUser(userId, roleId)
            .map {
                exchange.response.statusCode = HttpStatus.valueOf(204)
                ApiResponse.success(null)
            }
    }
}
