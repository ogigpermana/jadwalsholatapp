package com.igoy86.digitaltasbih.data.repository

import com.igoy86.digitaltasbih.data.local.SettingsDataStore
import com.igoy86.digitaltasbih.data.model.AppSettings
import com.igoy86.digitaltasbih.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {
    override val settings: Flow<AppSettings> = dataStore.settings
    override suspend fun save(settings: AppSettings) = dataStore.save(settings)
}