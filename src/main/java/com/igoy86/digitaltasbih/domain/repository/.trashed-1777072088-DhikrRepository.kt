package com.igoy86.digitaltasbih.domain.repository

import com.igoy86.digitaltasbih.data.model.DhikrEntity
import kotlinx.coroutines.flow.Flow

interface DhikrRepository {
    fun observeAll(): Flow<List<DhikrEntity>>
    fun observeFavorites(): Flow<List<DhikrEntity>>
    suspend fun getById(id: Int): DhikrEntity?
    suspend fun save(dhikr: DhikrEntity): Boolean
    suspend fun update(dhikr: DhikrEntity): Boolean
    suspend fun delete(dhikr: DhikrEntity): Boolean
}