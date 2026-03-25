package com.igoy86.digitaltasbih.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.igoy86.digitaltasbih.data.model.DhikrEntity
import com.igoy86.digitaltasbih.data.model.TasbihCounter

@Database(
    entities = [TasbihCounter::class, DhikrEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TasbihDatabase : RoomDatabase() {
    abstract fun tasbihDao(): TasbihDao
	abstract fun dhikrDao(): DhikrDao
}