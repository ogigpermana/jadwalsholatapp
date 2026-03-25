package com.igoy86.digitaltasbih.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.igoy86.digitaltasbih.data.model.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_THEME        = stringPreferencesKey("selected_theme")
        private val KEY_CUSTOM_PRIMARY = stringPreferencesKey("custom_primary")
        private val KEY_CUSTOM_DARK    = stringPreferencesKey("custom_dark")
    }

    // Observe tema aktif
    val selectedTheme: Flow<AppTheme> = context.themeDataStore.data.map { prefs ->
        val name = prefs[KEY_THEME] ?: AppTheme.GREEN.name
        AppTheme.valueOf(name)
    }

    // Observe warna custom
    val customPrimary: Flow<String> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_PRIMARY] ?: "#2D763F"
    }
    val customDark: Flow<String> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_DARK] ?: "#1A531A"
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun saveCustomColor(primary: String, dark: String) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_CUSTOM_PRIMARY] = primary
            prefs[KEY_CUSTOM_DARK]    = dark
        }
    }
}