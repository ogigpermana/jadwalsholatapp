package com.igoy86.digitaltasbih.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.igoy86.digitaltasbih.data.model.PrayerTime
import java.util.Calendar

object PrayerNotificationScheduler {

    fun scheduleAll(context: Context, prayers: List<PrayerTime>) {
        prayers.forEachIndexed { index, prayer ->
            if (prayer.notifEnabled) {
                schedule(context, prayer, index)
            } else {
                cancel(context, index)
            }
        }
    }

    fun schedule(context: Context, prayer: PrayerTime, notifId: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        // Parse jam:menit dari prayer.time "04:40"
        val parts = prayer.time.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Kalau waktu sudah lewat hari ini, set besok
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            putExtra("prayer_name", prayer.name)
            putExtra("prayer_time", prayer.time)
            putExtra("notif_id", notifId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set exact alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, notifId: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, PrayerNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAll(context: Context, size: Int) {
        for (i in 0 until size) cancel(context, i)
    }
}