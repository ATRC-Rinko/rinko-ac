package com.rinko.auth.entity

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EntityTest : StringSpec({

    "User 实体创建" {
        val user = User(1L, "test", "test@test.com", "hash", UserStatus.ACTIVE)
        user.id shouldBe 1L
        user.username shouldBe "test"
        user.status shouldBe UserStatus.ACTIVE
    }

    "Role 实体创建" {
        val role = Role(1L, "admin", "Administrator", isSystem = true)
        role.name shouldBe "admin"
        role.isSystem shouldBe true
    }

    "Role 实体 非系统角色默认" {
        val role = Role(2L, "user", "Normal User")
        role.isSystem shouldBe false
    }

    "Permission 实体创建" {
        val perm = Permission(1L, "user:read", "Read access")
        perm.code shouldBe "user:read"
        perm.description shouldBe "Read access"
    }
})
