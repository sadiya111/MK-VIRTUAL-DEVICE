package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val profileId: String,
    val title: String,
    val url: String,
    val folder: String = "Mobile Bookmarks",
    val createdAt: Long = System.currentTimeMillis()
)
