package com.igoy86.digitaltasbih.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.igoy86.digitaltasbih.data.model.PrayerScheduleInfo
import com.igoy86.digitaltasbih.data.model.PrayerTime
import com.igoy86.digitaltasbih.domain.usecase.GetPrayerScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PrayerScheduleViewModel @Inject constructor(
    application: Application,
    private val getPrayerScheduleUseCase: GetPrayerScheduleUseCase
) : AndroidViewModel(application) {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val info: PrayerScheduleInfo) : UiState()
        data class Error(val message: String) : UiState()
        object LocationPermissionRequired : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    // Simpan data prayer terakhir untuk di-refresh tanpa hit API
    private var lastPrayerInfo: PrayerScheduleInfo? = null
    private var tickerJob: Job? = null

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    fun loadPrayerTimes() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                var location = fusedLocationClient.lastLocation.await()
                if (location == null) {
                    location = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY, null
                    ).await()
                }

                if (location == null) {
                    _uiState.value = UiState.Error("Gagal mendapatkan lokasi")
                    return@launch
                }

                val result = getPrayerScheduleUseCase(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                result.fold(
                    onSuccess = {
                        lastPrayerInfo = it
                        _uiState.value = UiState.Success(it)
                        startTicker() // Mulai ticker setelah data berhasil dimuat
                    },
                    onFailure = {
                        val msg = "${it.javaClass.simpleName}: ${it.message}"
                        _uiState.value = UiState.Error(msg)
                    }
                )

            } catch (e: SecurityException) {
                _uiState.value = UiState.LocationPermissionRequired
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel() // Batalkan ticker sebelumnya jika ada
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000L) // Setiap 1 menit default 60s
                refreshPrayerStatus()
            }
        }
    }

    // Recalculate isPast, isNext, statusText tanpa hit API
    private fun refreshPrayerStatus() {
        val info = lastPrayerInfo ?: return

        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val minutesList = info.prayers.map { it.timeInMinutes }

        // Cek apakah semua sudah lewat
        val allPast = minutesList.all { it <= nowMinutes }

        var nextIndex = if (allPast) 1 else 0  // default ke Subuh jika semua lewat
        if (!allPast) {
            for (i in info.prayers.indices) {
                if (minutesList[i] > nowMinutes &&
                    info.prayers[i].key != "imsak" &&
                    info.prayers[i].key != "sunrise") {
                    nextIndex = i
                    break
                }
            }
        }

        // Update isPast & isNext
        val updatedPrayers = info.prayers.mapIndexed { index, prayer ->
            prayer.copy(
                isNext = index == nextIndex,
                isPast = prayer.timeInMinutes <= nowMinutes
            )
        }

        // Update statusText
        val updatedStatus = buildStatusText(info.prayers, minutesList, nowMinutes, nextIndex)

        val updatedInfo = info.copy(
            prayers = updatedPrayers,
            statusText = updatedStatus
        )

        lastPrayerInfo = updatedInfo
        _uiState.value = UiState.Success(updatedInfo)
    }

    private fun buildStatusText(
        prayers: List<PrayerTime>,
        minutesList: List<Int>,
        nowMinutes: Int,
        nextIndex: Int
    ): String {
        val pastIndex = if (nextIndex > 0) nextIndex - 1 else prayers.size - 1
        val pastMinutes = minutesList[pastIndex]
        val pastName = prayers[pastIndex].name

        return if (nextIndex == 0 && nowMinutes > minutesList.last()) {
            val diff = nowMinutes - minutesList.last()
            "Waktu ${prayers.last().name} Sudah Lewat · ${formatDiff(diff)} yang lalu"
        } else if (nextIndex == 0) {
            val diff = minutesList[0] - nowMinutes
            "Menuju ${prayers[0].name} · ${formatDiff(diff)} lagi"
        } else {
            val diffPast = nowMinutes - pastMinutes
            val diffNext = minutesList[nextIndex] - nowMinutes
            if (diffPast < diffNext) {
                "Waktu $pastName Sudah Lewat · ${formatDiff(diffPast)} yang lalu"
            } else {
                "Menuju ${prayers[nextIndex].name} · ${formatDiff(diffNext)} lagi"
            }
        }
    }

    private fun formatDiff(minutes: Int): String {
        return if (minutes < 60) {
            "$minutes menit"
        } else {
            val h = minutes / 60
            val m = minutes % 60
            if (m == 0) "$h jam" else "$h jam $m menit"
        }
    }
	
	fun updateNotifState(prayers: List<PrayerTime>) {
        val info = lastPrayerInfo ?: return
        lastPrayerInfo = info.copy(prayers = prayers)
        _uiState.value = UiState.Success(lastPrayerInfo!!)
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}