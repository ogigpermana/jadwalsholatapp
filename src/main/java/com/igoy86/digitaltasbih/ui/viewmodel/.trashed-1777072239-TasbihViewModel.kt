package com.igoy86.digitaltasbih.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igoy86.digitaltasbih.data.model.AppSettings
import com.igoy86.digitaltasbih.domain.usecase.GetCountUseCase
import com.igoy86.digitaltasbih.domain.usecase.GetSettingsUseCase
import com.igoy86.digitaltasbih.domain.usecase.SaveCountUseCase
import com.igoy86.digitaltasbih.domain.usecase.SaveDhikrUseCase
import com.igoy86.digitaltasbih.domain.usecase.UpdateDhikrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasbihViewModel @Inject constructor(
    private val getCountUseCase: GetCountUseCase,
    private val saveCountUseCase: SaveCountUseCase,
    private val saveDhikrUseCase: SaveDhikrUseCase,
    private val updateDhikrUseCase: UpdateDhikrUseCase,
    private val getSettings: GetSettingsUseCase,
) : ViewModel() {

    private val settings: StateFlow<AppSettings> = getSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val settingsState: StateFlow<AppSettings> = settings
    val isHapticEnabled: Boolean get() = settings.value.isHapticEnabled
	val isBeepEnabled: Boolean get() = settings.value.isBeepEnabled

    companion object {
        private const val MAX_COUNT = 99999
    }

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _target = MutableStateFlow(0)
    val target: StateFlow<Int> = _target.asStateFlow()

    private val _saveStatus = MutableStateFlow<Boolean?>(null)
    val saveStatus: StateFlow<Boolean?> = _saveStatus.asStateFlow()

    private val _activeDhikrId = MutableStateFlow<Int?>(null)
    val activeDhikrId: StateFlow<Int?> = _activeDhikrId.asStateFlow()

    private val _activeDhikrName = MutableStateFlow<String?>(null)
    val activeDhikrName: StateFlow<String?> = _activeDhikrName.asStateFlow()

    private val _targetReached = MutableSharedFlow<Unit>()
    val targetReached: SharedFlow<Unit> = _targetReached.asSharedFlow()

    private val _saveEvent = MutableSharedFlow<SaveEvent>()
    val saveEvent: SharedFlow<SaveEvent> = _saveEvent.asSharedFlow()

    private var hasUnsavedChanges = false

    init {
        viewModelScope.launch {
            getCountUseCase().collect { savedCount ->
                if (_activeDhikrId.value == null) {
                    _count.value = savedCount
                }
            }
        }
    }

    // =========================================================
    // COUNTER
    // =========================================================

    fun increment() {
        if (_target.value > 0 && _count.value >= _target.value) return
        if (_count.value >= MAX_COUNT) return
        _count.value++
        hasUnsavedChanges = true
        if (_target.value > 0 && _count.value == _target.value) {
            viewModelScope.launch { _targetReached.emit(Unit) }
            if (settings.value.isBeepEnabled) playBeep()
            saveIfNeeded()
        }
    }

    fun reset() {
        _count.value = 0
        hasUnsavedChanges = true
        saveIfNeeded()
    }

    fun setTarget(target: Int) { _target.value = target }
    fun clearTarget() { _target.value = 0 }

    fun saveIfNeeded() {
        if (!hasUnsavedChanges) return
        viewModelScope.launch {
            val success = saveCountUseCase(_count.value)
            _saveStatus.value = success
            if (success) hasUnsavedChanges = false
        }
    }

    private fun playBeep() {
        // beep dihandle di MainActivity via targetReached event
        // fungsi ini placeholder jika ingin dipindah ke ViewModel kelak
    }

    // =========================================================
    // LOAD
    // =========================================================

    fun loadDhikr(id: Int, name: String, count: Int, target: Int) {
        _activeDhikrId.value = id
        _activeDhikrName.value = name
        _count.value = count
        _target.value = target
        hasUnsavedChanges = false
    }

    // =========================================================
    // SAVE
    // =========================================================

    fun saveDhikr(name: String) {
        viewModelScope.launch {
            val id = _activeDhikrId.value
            val success = if (id != null) {
                updateDhikrUseCase(id, name, _count.value, _target.value)
                    .also { if (it) _activeDhikrName.value = name }
            } else {
                saveDhikrUseCase(name, _count.value, _target.value)
            }

            if (success) clearActiveDhikr()

            _saveEvent.emit(
                if (success) {
                    if (id != null) SaveEvent.Updated else SaveEvent.Saved
                } else {
                    SaveEvent.Failed
                }
            )
        }
    }

    // =========================================================
    // RESET MODE
    // =========================================================

    fun clearActiveDhikr() {
        _activeDhikrId.value = null
        _activeDhikrName.value = null
        _count.value = 0
        _target.value = 0
        hasUnsavedChanges = false
    }
}

sealed class SaveEvent {
    object Saved   : SaveEvent()
    object Updated : SaveEvent()
    object Failed  : SaveEvent()
}
