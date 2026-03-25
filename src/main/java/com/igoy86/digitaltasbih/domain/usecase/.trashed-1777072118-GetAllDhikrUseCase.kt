package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.domain.repository.DhikrRepository
import javax.inject.Inject

class GetAllDhikrUseCase @Inject constructor(
    private val repository: DhikrRepository
) {
    operator fun invoke() = repository.observeAll()
}