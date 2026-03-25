package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.data.model.AppTheme
import com.igoy86.digitaltasbih.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class ThemeState(
    val theme: AppTheme,
    val primaryColor: String,
    val darkColor: String
)

class GetThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    operator fun invoke(): Flow<ThemeState> =
        combine(
            repository.selectedTheme,
            repository.customPrimary,
            repository.customDark
        ) { theme, customPrimary, customDark ->
            ThemeState(
                theme        = theme,
                primaryColor = if (theme == AppTheme.CUSTOM) customPrimary else theme.primary,
                darkColor    = if (theme == AppTheme.CUSTOM) customDark    else theme.dark
            )
        }
}