package com.igoy86.digitaltasbih.data.repository

import com.igoy86.digitaltasbih.data.model.PrayerScheduleInfo
import com.igoy86.digitaltasbih.data.model.PrayerTime
import com.igoy86.digitaltasbih.data.remote.AladhanApi
import com.igoy86.digitaltasbih.domain.repository.PrayerRepository
import java.util.Calendar
import javax.inject.Inject

class PrayerRepositoryImpl @Inject constructor(
    private val api: AladhanApi
) : PrayerRepository {

    override suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<PrayerScheduleInfo> {
        return try {
            val response = api.getPrayerTimings(
                latitude = latitude,
                longitude = longitude
            )

            val timings = response.data.timings
            val hijri = response.data.date.hijri

            val now = Calendar.getInstance()
            val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            val rawList = listOf(
                Triple("imsak",   "Imsak",   timings.imsak),
                Triple("fajr",    "Subuh",   timings.fajr),
                Triple("sunrise", "Terbit",  timings.sunrise),
                Triple("dhuhr",   "Zuhur",   timings.dhuhr),
                Triple("asr",     "Ashar",   timings.asr),
                Triple("maghrib", "Maghrib", timings.maghrib),
                Triple("isha",    "Isya",    timings.isha)
            )

            // Parse semua waktu ke menit
            val minutesList = rawList.map { parseToMinutes(it.third) }

            // Cek apakah semua sudah lewat
            val allPast = minutesList.all { it <= nowMinutes }

            var nextIndex = if (allPast) 1 else 0  // default ke Subuh jika semua lewat
            if (!allPast) {
                for (i in rawList.indices) {
                    if (minutesList[i] > nowMinutes &&
                    rawList[i].first != "imsak" &&
                    rawList[i].first != "sunrise") {
                        nextIndex = i
                        break
                    }
                }
            }

            // Status aktif
            val statusText = buildStatusText(rawList, minutesList, nowMinutes, nextIndex)

            val prayerList = rawList.mapIndexed { index, (key, name, time) ->
                PrayerTime(
                    key = key,
                    name = name,
                    time = formatTime(time),
                    timeInMinutes = minutesList[index],
                    isNext = index == nextIndex,
                    isPast = minutesList[index] <= nowMinutes,
					notifEnabled = key != "imsak" && key != "sunrise"
                )
            }

            // Format tanggal Hijriyah
            val hijriText = getHijriDate()

            Result.success(
                PrayerScheduleInfo(
                    prayers = prayerList,
                    hijriDate = hijriText,
                    statusText = statusText
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildStatusText(
        rawList: List<Triple<String, String, String>>,
        minutesList: List<Int>,
        nowMinutes: Int,
        nextIndex: Int
    ): String {
        // Cari waktu yang baru saja lewat (sebelum nextIndex)
        val pastIndex = if (nextIndex > 0) nextIndex - 1 else rawList.size - 1
        val pastMinutes = minutesList[pastIndex]
        val pastName = rawList[pastIndex].second

        return if (nextIndex == 0 && nowMinutes > minutesList.last()) {
            // Semua sudah lewat
            val diff = nowMinutes - minutesList.last()
            "Waktu ${rawList.last().second} Sudah Lewat · ${formatDiff(diff)} yang lalu"
        } else if (nextIndex == 0) {
            // Belum ada yang lewat, menuju Imsak
            val diff = minutesList[0] - nowMinutes
            "Menuju ${rawList[0].second} · ${formatDiff(diff)} lagi"
        } else {
            val diffPast = nowMinutes - pastMinutes
            val diffNext = minutesList[nextIndex] - nowMinutes
            if (diffPast < diffNext) {
                "Waktu $pastName Sudah Lewat · ${formatDiff(diffPast)} yang lalu"
            } else {
                "Menuju ${rawList[nextIndex].second} · ${formatDiff(diffNext)} lagi"
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

    private fun parseToMinutes(raw: String): Int {
        return try {
            val clean = raw.trim().split(" ")[0]
            val parts = clean.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) { 0 }
    }

    private fun formatTime(raw: String): String {
        return raw.trim().split(" ")[0]
    }
	
	private fun getHijriDate(): String {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val islamicCal = android.icu.util.IslamicCalendar()
                val day   = islamicCal.get(android.icu.util.Calendar.DAY_OF_MONTH)
                val month = islamicCal.get(android.icu.util.Calendar.MONTH) // 0-based
                val year  = islamicCal.get(android.icu.util.Calendar.YEAR)

                val monthNames = listOf(
                    "Muharram", "Safar", "Rabi'ul Awal", "Rabi'ul Akhir",
                    "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban",
                    "Ramadan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
                    )

                    "$day ${monthNames[month]} $year H"
                    } else {
                        "- H" // fallback untuk API < 24
                    }
                } catch (e: Exception) {
                "- H"
                }
    }
}