package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.data.model.DhikrEntity
import com.igoy86.digitaltasbih.domain.repository.DhikrRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: DhikrRepository
) {
    suspend operator fun invoke(dhikr: DhikrEntity) =
        repository.update(dhikr.copy(isFavorite = !dhikr.isFavorite))
}