package com.rinko.auth.service

import org.springframework.stereotype.Service

@Service
class WildcardMatcher {

    fun matches(permissionCode: String, requiredPermission: String): Boolean {
        val codeParts = permissionCode.split(":")
        val requiredParts = requiredPermission.split(":")

        if (codeParts.size != requiredParts.size) return false

        for (i in codeParts.indices) {
            if (codeParts[i] == "*") continue
            if (requiredParts[i] == "*") continue
            if (codeParts[i] != requiredParts[i]) return false
        }
        return true
    }
}
