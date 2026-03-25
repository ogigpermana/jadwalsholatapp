package com.igoy86.digitaltasbih

import android.app.Application
import com.igoy86.digitaltasbih.notification.PrayerNotificationChannel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PrayerNotificationChannel.create(this)
    }
}