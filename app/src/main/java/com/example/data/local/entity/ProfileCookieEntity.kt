package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_cookies")
data class ProfileCookieEntity(
    @PrimaryKey
    val id: String, // composite "${profileId}_${domain}"
    val profileId: String,
    val domain: String,
    val cookieString: String,
    val updatedAt: Long = System.currentTimeMillis()
)
