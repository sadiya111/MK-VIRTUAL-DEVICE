package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey
    val id: String,
    val profileId: String,
    val title: String,
    val url: String,
    val iconEmoji: String = "🌐",
    val orderIndex: Int = 0
)
