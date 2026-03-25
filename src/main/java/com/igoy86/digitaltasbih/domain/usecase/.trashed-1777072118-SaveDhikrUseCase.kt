package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.data.model.DhikrEntity
import com.igoy86.digitaltasbih.domain.repository.DhikrRepository
import javax.inject.Inject

class SaveDhikrUseCase @Inject constructor(
    private val repository: DhikrRepository
) {
    suspend operator fun invoke(name: String, count: Int, target: Int): Boolean {
        if (name.isBlank()) return false
        return repository.save(DhikrEntity(name = name, count = count, target = target))
    }
}