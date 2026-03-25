package com.igoy86.digitaltasbih.domain.repository

import com.igoy86.digitaltasbih.data.model.PrayerScheduleInfo

interface PrayerRepository {
    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<PrayerScheduleInfo>
}