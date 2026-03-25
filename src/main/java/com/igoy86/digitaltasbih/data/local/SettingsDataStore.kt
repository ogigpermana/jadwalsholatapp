package com.igoy86.digitaltasbih.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.igoy86.digitaltasbih.data.model.AppLanguage
import com.igoy86.digitaltasbih.data.model.AppSettings
import com.igoy86.digitaltasbih.data.model.FontSizeOption
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Top-level — bisa diakses dari Activity
val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_FONT_SIZE = stringPreferencesKey("font_size")
        val KEY_BEEP      = booleanPreferencesKey("beep_enabled")
        val KEY_HAPTIC    = booleanPreferencesKey("haptic_enabled")
        val KEY_LANGUAGE  = stringPreferencesKey("language")
    }
	
	// Simpan state notif per prayer (key = nama prayer, misal "Subuh", "Zuhur", dll)
    fun getPrayerNotifEnabled(prayerKey: String): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey("notif_$prayerKey")] ?: true
        }

    suspend fun setPrayerNotifEnabled(prayerKey: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("notif_$prayerKey")] = enabled
        }
    }

    // Untuk BootReceiver — simpan seluruh prayer keys yang aktif
    suspend fun saveActivePrayerKeys(keys: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("active_prayer_keys")] = keys.joinToString(",")
        }
    }

    fun getActivePrayerKeys(): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[stringPreferencesKey("active_prayer_keys")] ?: ""
            if (raw.isBlank()) emptyList() else raw.split(",")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            isDarkMode      = prefs[KEY_DARK_MODE] ?: false,
            fontSizeOption  = FontSizeOption.valueOf(
                                  prefs[KEY_FONT_SIZE] ?: FontSizeOption.MEDIUM.name),
            isBeepEnabled   = prefs[KEY_BEEP]      ?: true,
            isHapticEnabled = prefs[KEY_HAPTIC]    ?: true,
            language        = AppLanguage.valueOf(
                                  prefs[KEY_LANGUAGE] ?: AppLanguage.INDONESIA.name)
        )
    }

    suspend fun save(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = settings.isDarkMode
            prefs[KEY_FONT_SIZE] = settings.fontSizeOption.name
            prefs[KEY_BEEP]      = settings.isBeepEnabled
            prefs[KEY_HAPTIC]    = settings.isHapticEnabled
            prefs[KEY_LANGUAGE]  = settings.language.name
        }
    }
}
