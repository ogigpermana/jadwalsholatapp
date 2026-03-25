package com.igoy86.digitaltasbih.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igoy86.digitaltasbih.data.model.QiblaData
import com.igoy86.digitaltasbih.domain.usecase.GetQiblaDirectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val getQiblaDirectionUseCase: GetQiblaDirectionUseCase
) : ViewModel() {

    private val _qiblaState = MutableStateFlow<QiblaUiState>(QiblaUiState.Loading)
    val qiblaState: StateFlow<QiblaUiState> = _qiblaState

    fun startQibla() {
        viewModelScope.launch {
            getQiblaDirectionUseCase()
                .catch { e ->
                    _qiblaState.value = QiblaUiState.Error(e.message ?: "Terjadi kesalahan")
                }
                .collect { data ->
                    _qiblaState.value = QiblaUiState.Success(data)
                }
        }
    }
}

sealed class QiblaUiState {
    object Loading : QiblaUiState()
    data class Success(val data: QiblaData) : QiblaUiState()
    data class Error(val message: String) : QiblaUiState()
}