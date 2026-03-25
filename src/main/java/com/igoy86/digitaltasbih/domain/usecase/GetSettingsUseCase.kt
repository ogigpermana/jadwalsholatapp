package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.data.model.AppSettings
import com.igoy86.digitaltasbih.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repo: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = repo.settings
}
