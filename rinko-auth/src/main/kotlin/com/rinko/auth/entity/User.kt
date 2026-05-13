package com.rinko.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class User(
    @Id
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) : Persistable<Long> {

    @Transient
    var isNewRecord: Boolean = false

    override fun getId(): Long = id

    override fun isNew(): Boolean = isNewRecord

}

enum class UserStatus {
    ACTIVE, DISABLED, LOCKED
}
