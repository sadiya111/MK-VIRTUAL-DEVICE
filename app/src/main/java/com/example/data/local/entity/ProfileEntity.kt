package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val colorHex: String,
    val avatarIcon: String, // e.g. "person", "work", "school", "shield", "rocket", "palette", "code"
    val searchEngine: String, // "Google", "DuckDuckGo", "Bing", "Yahoo"
    val customHomeUrl: String = "",
    val isDefault: Boolean = false,
    val isPrivateOnly: Boolean = false,
    val pinCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
