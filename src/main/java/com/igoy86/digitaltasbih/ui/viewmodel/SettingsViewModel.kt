package com.igoy86.digitaltasbih.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igoy86.digitaltasbih.data.model.AppLanguage
import com.igoy86.digitaltasbih.data.model.AppSettings
import com.igoy86.digitaltasbih.data.model.FontSizeOption
import com.igoy86.digitaltasbih.domain.usecase.GetSettingsUseCase
import com.igoy86.digitaltasbih.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase
) : ViewModel() {

    val settings: StateFlow<AppSettings> = getSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun toggleDarkMode(enabled: Boolean) = update { it.copy(isDarkMode = enabled) }
    fun setFontSize(option: FontSizeOption) = update { it.copy(fontSizeOption = option) }
    fun toggleBeep(enabled: Boolean) = update { it.copy(isBeepEnabled = enabled) }
    fun toggleHaptic(enabled: Boolean) = update { it.copy(isHapticEnabled = enabled) }
    fun setLanguage(lang: AppLanguage) = update { it.copy(language = lang) }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            saveSettings(transform(settings.value))
        }
    }
}
