package com.igoy86.digitaltasbih.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.igoy86.digitaltasbih.R
import com.igoy86.digitaltasbih.data.local.SettingsDataStore
import com.igoy86.digitaltasbih.data.local.dataStore
import com.igoy86.digitaltasbih.data.model.AppLanguage
import com.igoy86.digitaltasbih.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.igoy86.digitaltasbih.data.local.dataStore
import com.igoy86.digitaltasbih.notification.PrayerNotificationChannel
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        val lang = runBlocking {
            try {
                newBase.dataStore.data.first()[SettingsDataStore.KEY_LANGUAGE]
                    ?: AppLanguage.INDONESIA.name
            } catch (e: Exception) {
                AppLanguage.INDONESIA.name
            }
        }
        val appLang = AppLanguage.valueOf(lang)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, appLang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
		// Apply dark/light mode SEBELUM super.onCreate — krusial!
        val isDark = kotlinx.coroutines.runBlocking {
            dataStore.data.map { prefs ->
                prefs[com.igoy86.digitaltasbih.data.local.SettingsDataStore.KEY_DARK_MODE] ?: false
            }.first()
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (isDark) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )
	
        super.onCreate(savedInstanceState)

        // Apply cached theme color before inflate — prevent flash
        val cachedColor = Color.parseColor(ThemeCache.load(this))
        window.statusBarColor     = cachedColor
        window.navigationBarColor = cachedColor

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
		
		PrayerNotificationChannel.create(this)
		
		// Setup Toolbar sebagai ActionBar
        setSupportActionBar(binding.toolbar)

        // Setup Bottom Navigation + NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
		
		// Definisikan top-level destinations (tidak tampil back button)
        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.tasbihFragment,
                R.id.prayerScheduleFragment,
                R.id.qiblaCompassFragment
            )
        )
		
		// Judul toolbar otomatis ganti sesuai fragment aktif
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNav.setupWithNavController(navController)
    }
	
	fun applyWindowTheme(primaryHex: String) {
        val color = Color.parseColor(primaryHex)
        window.statusBarColor = color
        window.navigationBarColor = color
        binding.toolbar.setBackgroundColor(color)
        binding.bottomNav.setBackgroundColor(color)

        val states = android.content.res.ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.WHITE, Color.argb(180, 255, 255, 255))
        )
        binding.bottomNav.itemIconTintList = states
        binding.bottomNav.itemTextColor = states
    }

}