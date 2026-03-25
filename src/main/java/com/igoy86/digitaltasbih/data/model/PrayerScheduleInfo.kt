package com.igoy86.digitaltasbih.data.model

data class PrayerScheduleInfo(
    val prayers: List<PrayerTime>,
    val hijriDate: String,
    val statusText: String
)