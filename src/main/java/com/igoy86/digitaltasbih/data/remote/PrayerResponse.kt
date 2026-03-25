package com.igoy86.digitaltasbih.data.remote

import com.google.gson.annotations.SerializedName

data class PrayerResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: PrayerTimings,
    val date: PrayerDate,
    val meta: PrayerMeta
)

data class PrayerTimings(
    @SerializedName("Imsak") val imsak: String,
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha") val isha: String
)

data class PrayerDate(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate
)

data class HijriDate(
    val date: String,
    val day: String,
    val month: HijriMonth,
    val year: String
)

data class HijriMonth(
    val number: Int,
    val en: String,
    val ar: String
)

data class PrayerMeta(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val method: PrayerMethod
)

data class PrayerMethod(
    val id: Int,
    val name: String
)