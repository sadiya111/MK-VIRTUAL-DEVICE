package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: String,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)
