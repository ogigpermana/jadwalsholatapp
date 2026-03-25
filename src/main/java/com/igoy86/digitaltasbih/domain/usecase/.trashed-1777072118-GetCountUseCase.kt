package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCountUseCase @Inject constructor(
    private val repository: TasbihRepository
) {
    // ✅ Business rule: nilai tidak boleh negatif
    operator fun invoke(): Flow<Int> =
        repository.observeCount().map { count -> maxOf(count, 0) }
}