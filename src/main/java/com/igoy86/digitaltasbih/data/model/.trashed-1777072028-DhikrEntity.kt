package com.igoy86.digitaltasbih.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikr")
data class DhikrEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val count: Int,
    val target: Int,
    val savedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)