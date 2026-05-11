package com.rinko.auth.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class WildcardMatcherTest : StringSpec({

    val matcher = WildcardMatcher()

    "通配符 * 匹配单段" {
        matcher.matches("user:*", "user:read").shouldBeTrue()
        matcher.matches("user:*", "user:write").shouldBeTrue()
        matcher.matches("user:*", "admin:read").shouldBeFalse()
    }

    "*:* 匹配两段" {
        matcher.matches("*:*", "user:read").shouldBeTrue()
        matcher.matches("*:*", "admin:write").shouldBeTrue()
        matcher.matches("*:*", "user:read:detail").shouldBeFalse()
    }

    "三段通配符匹配" {
        matcher.matches("resource:*:read", "resource:foo:read").shouldBeTrue()
        matcher.matches("resource:*:read", "resource:bar:read").shouldBeTrue()
        matcher.matches("resource:*:read", "resource:foo:write").shouldBeFalse()
    }

    "不匹配时返回 false" {
        matcher.matches("user:read", "user:write").shouldBeFalse()
        matcher.matches("user:read", "admin:read").shouldBeFalse()
        matcher.matches("a:b:c", "a:b").shouldBeFalse()
    }

    "双向通配符匹配" {
        matcher.matches("*:read", "user:read").shouldBeTrue()
        matcher.matches("user:read", "*:read").shouldBeTrue()
    }
})
