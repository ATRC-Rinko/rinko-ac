package com.rinko.auth.controller

import com.rinko.auth.dto.PermissionCheckRequest
import com.rinko.auth.service.PermissionEvaluator
import com.rinko.infra.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Permission Check", description = "权限校验接口")
class PermissionCheckController(
    private val permissionEvaluator: PermissionEvaluator
) {

    @PostMapping("/check")
    @Operation(summary = "权限校验：检查用户是否拥有指定权限")
    fun checkPermission(@Valid @RequestBody req: PermissionCheckRequest): Mono<ApiResponse<Map<String, Boolean>>> {
        return permissionEvaluator.checkPermission(req.userId, req.requiredPermission)
            .map { authorized -> ApiResponse.success(mapOf("authorized" to authorized)) }
    }

    @GetMapping("/users/{userId}/permissions")
    @Operation(summary = "获取用户的所有权限（含角色继承和通配符展开）")
    fun getUserPermissions(@PathVariable userId: Long): Mono<ApiResponse<Set<String>>> {
        return permissionEvaluator.getUserPermissions(userId)
            .map { ApiResponse.success(it) }
    }
}
