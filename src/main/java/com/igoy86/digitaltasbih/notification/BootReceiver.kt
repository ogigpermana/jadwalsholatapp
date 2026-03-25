package com.igoy86.digitaltasbih.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.igoy86.digitaltasbih.data.local.SettingsDataStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn                                
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BootReceiverEntryPoint {
    fun settingsDataStore(): SettingsDataStore
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                BootReceiverEntryPoint::class.java
            )
            val dataStore = entryPoint.settingsDataStore()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeKeys = dataStore.getActivePrayerKeys().first()
                    // activeKeys siap dipakai
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}