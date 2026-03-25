package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.domain.repository.TasbihRepository
import javax.inject.Inject

class SaveCountUseCase @Inject constructor(
    private val repository: TasbihRepository
) {
    companion object {
        const val MAX_COUNT = 99999
    }

    // ✅ Business rule: nilai valid 0 - 99999
    suspend operator fun invoke(count: Int): Boolean {
        if (count < 0 || count > MAX_COUNT) return false
        return repository.saveCount(count)
    }
}