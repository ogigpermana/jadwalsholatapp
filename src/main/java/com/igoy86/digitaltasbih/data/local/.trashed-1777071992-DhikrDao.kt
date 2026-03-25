package com.igoy86.digitaltasbih.data.local

import androidx.room.*
import com.igoy86.digitaltasbih.data.model.DhikrEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {

    @Query("SELECT * FROM dhikr ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM dhikr WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun observeFavorites(): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM dhikr WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): DhikrEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dhikr: DhikrEntity)

    @Update
    suspend fun update(dhikr: DhikrEntity)

    @Delete
    suspend fun delete(dhikr: DhikrEntity)
}