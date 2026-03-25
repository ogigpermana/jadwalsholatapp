package com.igoy86.digitaltasbih.data.repository

import com.igoy86.digitaltasbih.data.local.DhikrDao
import com.igoy86.digitaltasbih.data.model.DhikrEntity
import com.igoy86.digitaltasbih.domain.repository.DhikrRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DhikrRepositoryImpl @Inject constructor(
    private val dao: DhikrDao
) : DhikrRepository {

    override fun observeAll(): Flow<List<DhikrEntity>> = dao.observeAll()

    override fun observeFavorites(): Flow<List<DhikrEntity>> = dao.observeFavorites()

    override suspend fun getById(id: Int): DhikrEntity? = dao.getById(id)

    override suspend fun save(dhikr: DhikrEntity): Boolean {
        return try { dao.insert(dhikr); true } catch (e: Exception) { false }
    }

    override suspend fun update(dhikr: DhikrEntity): Boolean {
        return try { dao.update(dhikr); true } catch (e: Exception) { false }
    }

    override suspend fun delete(dhikr: DhikrEntity): Boolean {
        return try { dao.delete(dhikr); true } catch (e: Exception) { false }
    }
}