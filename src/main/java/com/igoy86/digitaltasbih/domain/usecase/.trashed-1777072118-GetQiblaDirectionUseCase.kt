package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.data.model.QiblaData
import com.igoy86.digitaltasbih.domain.repository.QiblaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQiblaDirectionUseCase @Inject constructor(
    private val repository: QiblaRepository
) {
    operator fun invoke(): Flow<QiblaData> = repository.getQiblaDirection()
}