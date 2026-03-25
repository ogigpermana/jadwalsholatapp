package com.igoy86.digitaltasbih.data.repository

import com.igoy86.digitaltasbih.data.local.ThemeDataStore
import com.igoy86.digitaltasbih.data.model.AppTheme
import com.igoy86.digitaltasbih.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: ThemeDataStore
) : ThemeRepository {

    override val selectedTheme: Flow<AppTheme> = dataStore.selectedTheme
    override val customPrimary: Flow<String>   = dataStore.customPrimary
    override val customDark: Flow<String>      = dataStore.customDark

    override suspend fun saveTheme(theme: AppTheme) {
        dataStore.saveTheme(theme)
    }

    override suspend fun saveCustomColor(primary: String, dark: String) {
        dataStore.saveCustomColor(primary, dark)
    }
}