package com.igoy86.digitaltasbih.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.igoy86.digitaltasbih.R
import java.util.Calendar

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Sholat"
        val prayerTime = intent.getStringExtra("prayer_time") ?: ""
        val notifId = intent.getIntExtra("notif_id", 0)

        // Beep saat notif muncul
        playBeep()

        // Tampilkan notifikasi
        val notification = NotificationCompat.Builder(context, PrayerNotificationChannel.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_on)
            .setContentTitle("🕌 Waktu $prayerName — $prayerTime")
            .setContentText("Wahai hamba Allah, waktu sholat telah tiba, ayo sholat berjamaah di mesjid-mesjid terdekat di kota anda")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Wahai hamba Allah, waktu sholat telah tiba, ayo sholat berjamaah di mesjid-mesjid terdekat di kota anda")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notifId, notification)

        // Reschedule untuk besok di waktu yang sama
        rescheduleForTomorrow(context, prayerName, prayerTime, notifId)
    }

    private fun rescheduleForTomorrow(
        context: Context,
        prayerName: String,
        prayerTime: String,
        notifId: Int
    ) {
        try {
            val parts = prayerTime.split(":")
            if (parts.size != 2) return
            val hour = parts[0].toIntOrNull() ?: return
            val minute = parts[1].toIntOrNull() ?: return

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }

            val newIntent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                putExtra("prayer_name", prayerName)
                putExtra("prayer_time", prayerTime)
                putExtra("notif_id", notifId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notifId,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(AlarmManager::class.java)

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playBeep() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
            Handler(Looper.getMainLooper()).postDelayed({
                toneGenerator.release()
            }, 600)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}