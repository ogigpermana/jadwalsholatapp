package com.igoy86.digitaltasbih.data.repository

import com.igoy86.digitaltasbih.data.local.TasbihDao
import com.igoy86.digitaltasbih.data.model.TasbihCounter
import com.igoy86.digitaltasbih.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasbihRepositoryImpl @Inject constructor(
    private val dao: TasbihDao
) : TasbihRepository {
    override fun observeCount(): Flow<Int> =
        dao.observeCounter().map { it?.value ?: 0 }

    override suspend fun saveCount(count: Int): Boolean {
        return try {
            dao.saveCounter(TasbihCounter(id = 1, value = count))
            true
        } catch (e: Exception) {
            false
        }
    }
}
