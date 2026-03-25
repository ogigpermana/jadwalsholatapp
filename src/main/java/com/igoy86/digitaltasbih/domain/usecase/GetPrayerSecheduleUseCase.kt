package com.igoy86.digitaltasbih.domain.usecase

import com.igoy86.digitaltasbih.data.model.PrayerScheduleInfo
import com.igoy86.digitaltasbih.domain.repository.PrayerRepository
import javax.inject.Inject

class GetPrayerScheduleUseCase @Inject constructor(
    private val repository: PrayerRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Result<PrayerScheduleInfo> {
        return repository.getPrayerTimes(latitude, longitude)
    }
}