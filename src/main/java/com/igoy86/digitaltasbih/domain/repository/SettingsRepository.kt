package com.igoy86.digitaltasbih.domain.repository

import com.igoy86.digitaltasbih.data.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun save(settings: AppSettings)
}
