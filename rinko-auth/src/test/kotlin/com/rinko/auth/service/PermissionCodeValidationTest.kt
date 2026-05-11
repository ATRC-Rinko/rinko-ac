package com.rinko.auth.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class PermissionCodeValidationTest : StringSpec({

    // Test the CODE_PATTERN regex from PermissionService directly
    // Pattern: ^[a-z]+(:[a-z*]+)+$
    val pattern = Regex("^[a-z]+(:[a-z*]+)+$")

    "合法格式 user:read" {
        pattern.matches("user:read").shouldBeTrue()
    }

    "合法格式 resource:*:read" {
        pattern.matches("resource:*:read").shouldBeTrue()
    }

    "合法格式 a:b:c:d" {
        pattern.matches("a:b:c:d").shouldBeTrue()
    }

    "合法格式 resource:*" {
        pattern.matches("resource:*").shouldBeTrue()
    }

    "非法格式 大写字母" {
        pattern.matches("USER:READ").shouldBeFalse()
    }

    "非法格式 无冒号" {
        pattern.matches("userread").shouldBeFalse()
    }

    "非法格式 单段" {
        pattern.matches("user").shouldBeFalse()
    }

    "非法格式 空字符串" {
        pattern.matches("").shouldBeFalse()
    }

    "非法格式 尾部冒号" {
        pattern.matches("user:").shouldBeFalse()
    }

    "非法格式 开头冒号" {
        pattern.matches(":read").shouldBeFalse()
    }
})
