package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.data.model.DhikrEntity
import com.igoy86.digitaltasbih.domain.repository.DhikrRepository
import javax.inject.Inject

class UpdateDhikrNameUseCase @Inject constructor(
    private val repository: DhikrRepository
) {
    suspend operator fun invoke(dhikr: DhikrEntity, newName: String): Boolean {
        if (newName.isBlank()) return false
        return repository.update(dhikr.copy(name = newName))
    }
}