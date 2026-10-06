package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val lastModified: Long,
    val durationMs: Long,
    val aspectRatioName: String,
    val clipCount: Int,
    val thumbnailUri: String?,
    val serializedData: String
)
