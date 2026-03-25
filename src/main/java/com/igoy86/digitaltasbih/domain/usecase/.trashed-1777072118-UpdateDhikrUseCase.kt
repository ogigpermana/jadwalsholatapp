package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.domain.repository.DhikrRepository
import javax.inject.Inject

class UpdateDhikrUseCase @Inject constructor(
    private val repository: DhikrRepository
) {
    /**
     * Update record dzikir yang sudah ada.
     * Ambil data lama dari DB, lalu copy dengan nilai baru.
     * isFavorite & savedAt tetap dipertahankan.
     */
    suspend operator fun invoke(
        id: Int,
        name: String,
        count: Int,
        target: Int
    ): Boolean {
        val existing = repository.getById(id) ?: return false
        return repository.update(
            existing.copy(
                name = name,
                count = count,
                target = target
                // savedAt & isFavorite tidak diubah
            )
        )
    }
}