package com.igoy86.digitaltasbih.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igoy86.digitaltasbih.data.model.DhikrEntity
import com.igoy86.digitaltasbih.domain.usecase.DeleteDhikrUseCase
import com.igoy86.digitaltasbih.domain.usecase.GetAllDhikrUseCase
import com.igoy86.digitaltasbih.domain.usecase.ToggleFavoriteUseCase
import com.igoy86.digitaltasbih.domain.usecase.UpdateDhikrNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedDhikrViewModel @Inject constructor(
    private val getAllDhikrUseCase: GetAllDhikrUseCase,
    private val deleteDhikrUseCase: DeleteDhikrUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val updateDhikrNameUseCase: UpdateDhikrNameUseCase
) : ViewModel() {

    val dhikrList: StateFlow<List<DhikrEntity>> = getAllDhikrUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(dhikr: DhikrEntity) {
        viewModelScope.launch { deleteDhikrUseCase(dhikr) }
    }

    fun toggleFavorite(dhikr: DhikrEntity) {
        viewModelScope.launch { toggleFavoriteUseCase(dhikr) }
    }

    fun updateName(dhikr: DhikrEntity, newName: String) {
        viewModelScope.launch { updateDhikrNameUseCase(dhikr, newName) }
    }
}