package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.data.model.AppSettings
import com.igoy86.digitaltasbih.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveSettingsUseCase @Inject constructor(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(settings: AppSettings) = repo.save(settings)
}
