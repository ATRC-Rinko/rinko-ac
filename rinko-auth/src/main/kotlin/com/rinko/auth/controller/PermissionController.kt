package com.rinko.auth.controller

import com.rinko.auth.entity.Permission
import com.rinko.auth.service.PermissionService
import com.rinko.infra.dto.ApiResponse
import com.rinko.infra.exception.ValidationException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth/permissions")
@Tag(name = "Permission Management", description = "权限管理接口")
class PermissionController(
    private val permissionService: PermissionService
) {

    @GetMapping
    @Operation(summary = "查询所有权限")
    fun listPermissions(): Mono<ApiResponse<List<Permission>>> {
        return permissionService.listPermissions()
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @PostMapping
    @Operation(summary = "创建权限")
    fun createPermission(@RequestBody body: Map<String, String>, exchange: ServerWebExchange): Mono<ApiResponse<Permission>> {
        val code = body["code"]
            ?: throw ValidationException("code is required")
        exchange.response.statusCode = HttpStatus.valueOf(201)
        return permissionService.createPermission(code, body["description"])
            .map { ApiResponse.success(it) }
    }

    @PutMapping("/{permissionId}")
    @Operation(summary = "更新权限")
    fun updatePermission(
        @PathVariable permissionId: Long,
        @RequestBody body: Map<String, String>
    ): Mono<ApiResponse<Permission>> {
        val code = body["code"]
            ?: throw ValidationException("code is required")
        return permissionService.updatePermission(permissionId, code, body["description"])
            .map { ApiResponse.success(it) }
    }

    @DeleteMapping("/{permissionId}")
    @Operation(summary = "删除权限")
    fun deletePermission(@PathVariable permissionId: Long, exchange: ServerWebExchange): Mono<ApiResponse<Void>> {
        return permissionService.deletePermission(permissionId)
            .map {
                exchange.response.statusCode = HttpStatus.valueOf(204)
                ApiResponse.success(null)
            }
    }
}
