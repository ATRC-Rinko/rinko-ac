package com.rinko.auth.controller

import com.rinko.auth.service.PermissionEvaluator
import com.rinko.infra.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Permission Check", description = "权限校验接口")
class PermissionCheckController(
    private val permissionEvaluator: PermissionEvaluator
) {

    @PostMapping("/check")
    @Operation(summary = "权限校验：检查用户是否拥有指定权限")
    fun checkPermission(@RequestBody body: Map<String, Any>, exchange: ServerWebExchange): Mono<ApiResponse<Map<String, Boolean>>> {
        val userId = (body["userId"] as? Number)?.toLong()
        if (userId == null) {
            exchange.response.statusCode = HttpStatus.valueOf(400)
            return Mono.just(ApiResponse.error(400, "userId is required"))
        }
        val requiredPermission = body["requiredPermission"] as? String
        if (requiredPermission == null) {
            exchange.response.statusCode = HttpStatus.valueOf(400)
            return Mono.just(ApiResponse.error(400, "requiredPermission is required"))
        }
        return permissionEvaluator.checkPermission(userId, requiredPermission)
            .map { authorized -> ApiResponse.success(mapOf("authorized" to authorized)) }
    }

    @GetMapping("/users/{userId}/permissions")
    @Operation(summary = "获取用户的所有权限（含角色继承和通配符展开）")
    fun getUserPermissions(@PathVariable userId: Long): Mono<ApiResponse<Set<String>>> {
        return permissionEvaluator.getUserPermissions(userId)
            .map { ApiResponse.success(it) }
    }
}
