package com.igoy86.digitaltasbih.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igoy86.digitaltasbih.data.model.TasbihCounter
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbihDao {

    // ✅ Flow — otomatis emit nilai baru setiap ada perubahan DB
    @Query("SELECT * FROM counters WHERE id = 1")
    fun observeCounter(): Flow<TasbihCounter?>

    // ✅ Suspend — berjalan di Coroutine, tidak blocking UI
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCounter(counter: TasbihCounter)

    @Query("SELECT * FROM counters WHERE id = 1")
    suspend fun getCounter(): TasbihCounter?
}