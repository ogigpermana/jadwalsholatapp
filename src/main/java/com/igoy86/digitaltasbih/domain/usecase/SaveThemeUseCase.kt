package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.data.model.AppTheme
import com.igoy86.digitaltasbih.domain.repository.ThemeRepository
import javax.inject.Inject

class SaveThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    suspend operator fun invoke(theme: AppTheme, customPrimary: String = "", customDark: String = "") {
        repository.saveTheme(theme)
        if (theme == AppTheme.CUSTOM && customPrimary.isNotBlank()) {
            repository.saveCustomColor(customPrimary, customDark)
        }
    }
}