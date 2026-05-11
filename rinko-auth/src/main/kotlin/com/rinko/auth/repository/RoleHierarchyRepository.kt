package com.rinko.auth.repository

import com.rinko.auth.entity.RoleHierarchy
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface RoleHierarchyRepository : ReactiveCrudRepository<RoleHierarchy, Long> {

    @Query("""
        SELECT * FROM role_hierarchy
        WHERE ancestor = :ancestor AND depth > 0
        ORDER BY depth
    """)
    fun findDescendantsByAncestor(ancestor: Long): Flux<RoleHierarchy>

    @Query("""
        SELECT * FROM role_hierarchy
        WHERE descendant = :descendant AND depth > 0
        ORDER BY depth
    """)
    fun findAncestorsByDescendant(descendant: Long): Flux<RoleHierarchy>

    @Query("""
        SELECT * FROM role_hierarchy
        WHERE descendant = :roleId
    """)
    fun findAllAncestors(roleId: Long): Flux<RoleHierarchy>
}
