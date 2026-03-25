package com.igoy86.digitaltasbih.domain.repository

import com.igoy86.digitaltasbih.data.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val selectedTheme: Flow<AppTheme>
    val customPrimary: Flow<String>
    val customDark: Flow<String>
    suspend fun saveTheme(theme: AppTheme)
    suspend fun saveCustomColor(primary: String, dark: String)
}