package com.rinko.auth.controller

import com.rinko.auth.dto.RoleHierarchyVO
import com.rinko.auth.service.RoleHierarchyService
import com.rinko.infra.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Role Hierarchy", description = "角色继承管理接口")
class RoleHierarchyController(
    private val roleHierarchyService: RoleHierarchyService
) {

    @PostMapping("/roles/{roleId}/parents/{parentRoleId}")
    @Operation(summary = "建立角色继承关系")
    fun addParent(
        @PathVariable roleId: Long,
        @PathVariable parentRoleId: Long
    ): Mono<ApiResponse<Void>> {
        return roleHierarchyService.addParent(roleId, parentRoleId)
            .map { ApiResponse.success(null) }
    }

    @DeleteMapping("/roles/{roleId}/parents/{parentRoleId}")
    @Operation(summary = "移除角色继承关系")
    fun removeParent(
        @PathVariable roleId: Long,
        @PathVariable parentRoleId: Long,
        exchange: ServerWebExchange
    ): Mono<ApiResponse<Void>> {
        return roleHierarchyService.removeParent(roleId, parentRoleId)
            .map {
                exchange.response.statusCode = HttpStatus.valueOf(204)
                ApiResponse.success(null)
            }
    }

    @GetMapping("/roles/{roleId}/descendants")
    @Operation(summary = "查询角色的所有后代")
    fun getDescendants(@PathVariable roleId: Long): Mono<ApiResponse<List<RoleHierarchyVO>>> {
        return roleHierarchyService.getDescendants(roleId)
            .map { RoleHierarchyVO(it.ancestor, it.descendant, it.depth) }
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/roles/{roleId}/ancestors")
    @Operation(summary = "查询角色的所有祖先")
    fun getAncestors(@PathVariable roleId: Long): Mono<ApiResponse<List<RoleHierarchyVO>>> {
        return roleHierarchyService.getAncestors(roleId)
            .map { RoleHierarchyVO(it.ancestor, it.descendant, it.depth) }
            .collectList()
            .map { ApiResponse.success(it) }
    }
}
