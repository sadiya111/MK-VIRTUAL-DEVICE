package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey
    val id: String,
    val profileId: String,
    val title: String,
    val url: String,
    val faviconUrl: String = "",
    val isPrivate: Boolean = false,
    val isDesktopSite: Boolean = false,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
