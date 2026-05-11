package com.rinko.auth.service

import com.rinko.auth.entity.RoleHierarchy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RoleHierarchyEntityTest : StringSpec({

    "RoleHierarchy 实体字段测试" {
        val rh = RoleHierarchy(ancestor = 1L, descendant = 2L, depth = 3)
        rh.ancestor shouldBe 1L
        rh.descendant shouldBe 2L
        rh.depth shouldBe 3
    }

    "RoleHierarchy 设置 depth=0" {
        val rh = RoleHierarchy(ancestor = 10L, descendant = 20L, depth = 0)
        rh.depth shouldBe 0
    }

    "RoleHierarchy copy 更新 depth" {
        val rh = RoleHierarchy(1L, 2L, 1)
        val copied = rh.copy(depth = 2)
        copied.depth shouldBe 2
        copied.ancestor shouldBe 1L
        copied.descendant shouldBe 2L
    }
})
