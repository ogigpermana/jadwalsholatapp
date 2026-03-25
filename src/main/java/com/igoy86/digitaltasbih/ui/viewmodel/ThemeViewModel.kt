package com.igoy86.digitaltasbih.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igoy86.digitaltasbih.data.model.AppTheme
import com.igoy86.digitaltasbih.domain.usecase.GetThemeUseCase
import com.igoy86.digitaltasbih.domain.usecase.SaveThemeUseCase
import com.igoy86.digitaltasbih.domain.usecase.ThemeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val saveThemeUseCase: SaveThemeUseCase
) : ViewModel() {

    val themeState: StateFlow<ThemeState> = getThemeUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ThemeState(AppTheme.GREEN, "#2D763F", "#1A531A")
        )

    fun saveTheme(theme: AppTheme, customPrimary: String = "", customDark: String = "") {
        viewModelScope.launch {
            saveThemeUseCase(theme, customPrimary, customDark)
        }
    }
}