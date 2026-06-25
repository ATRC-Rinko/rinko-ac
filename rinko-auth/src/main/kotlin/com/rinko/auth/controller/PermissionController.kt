package com.rinko.auth.controller

import com.rinko.auth.dto.CreatePermissionRequest
import com.rinko.auth.dto.PermissionVO
import com.rinko.auth.dto.UpdatePermissionRequest
import com.rinko.auth.service.PermissionService
import com.rinko.infra.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
    fun listPermissions(): Mono<ApiResponse<List<PermissionVO>>> {
        return permissionService.listPermissions()
            .map { PermissionVO(it.id, it.code, it.description) }
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @PostMapping
    @Operation(summary = "创建权限")
    fun createPermission(
        @Valid @RequestBody req: CreatePermissionRequest,
        exchange: ServerWebExchange
    ): Mono<ApiResponse<PermissionVO>> {
        exchange.response.statusCode = HttpStatus.valueOf(201)
        return permissionService.createPermission(req.code, req.description)
            .map { PermissionVO(it.id, it.code, it.description) }
            .map { ApiResponse.success(it) }
    }

    @PutMapping("/{permissionId}")
    @Operation(summary = "更新权限")
    fun updatePermission(
        @PathVariable permissionId: Long,
        @Valid @RequestBody req: UpdatePermissionRequest
    ): Mono<ApiResponse<PermissionVO>> {
        return permissionService.updatePermission(permissionId, req.code, req.description)
            .map { PermissionVO(it.id, it.code, it.description) }
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
