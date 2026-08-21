package com.bsnutrition.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val email: String,
    val emailVerifiedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
