package com.igoy86.digitaltasbih.data.model

data class PrayerTime(
    val key: String,            // "fajr", "dhuhr", dst — untuk notif
    val name: String,           // "Subuh", "Zuhur", dst
    val time: String,           // "04:40"
    val timeInMinutes: Int,     // untuk kalkulasi
    val isNext: Boolean = false,
    val isPast: Boolean = false,
    val notifEnabled: Boolean = true
)