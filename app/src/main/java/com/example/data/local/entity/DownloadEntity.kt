package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val profileId: String,
    val fileName: String,
    val url: String,
    val mimeType: String = "",
    val fileSizeBytes: Long = 0,
    val status: String = "Completed", // "Downloading", "Completed", "Failed"
    val downloadedAt: Long = System.currentTimeMillis()
)
