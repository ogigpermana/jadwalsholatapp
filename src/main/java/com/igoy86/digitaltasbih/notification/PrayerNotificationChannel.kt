package com.igoy86.digitaltasbih.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object PrayerNotificationChannel {

    const val CHANNEL_ID = "prayer_time_channel"
    const val CHANNEL_NAME = "Waktu Sholat"
    const val CHANNEL_DESC = "Notifikasi pengingat waktu sholat"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}