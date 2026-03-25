package com.igoy86.digitaltasbih.domain.repository

import kotlinx.coroutines.flow.Flow

interface TasbihRepository {
    fun observeCount(): Flow<Int>
    suspend fun saveCount(count: Int): Boolean
}