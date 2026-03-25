package com.igoy86.digitaltasbih.ui                         
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.igoy86.digitaltasbih.R
import com.igoy86.digitaltasbih.data.local.SettingsDataStore
import com.igoy86.digitaltasbih.data.local.dataStore        import com.igoy86.digitaltasbih.data.model.AppLanguage
import com.igoy86.digitaltasbih.data.model.FontSizeOption
import com.igoy86.digitaltasbih.databinding.ActivitySettingsBinding
import com.igoy86.digitaltasbih.ui.viewmodel.SettingsViewModel
import com.igoy86.digitaltasbih.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import android.content.Intent

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    @Volatile
    private var isInitialized = false

    override fun attachBaseContext(newBase: Context) {
        val lang = runBlocking {
            try {
                newBase.dataStore.data.first()[SettingsDataStore.KEY_LANGUAGE]
                    ?: AppLanguage.INDONESIA.name
            } catch (e: Exception) {
                AppLanguage.INDONESIA.name
            }
        }
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, AppLanguage.valueOf(lang)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.setDefaultUncaughtExceptionHandler { _, _ -> }
        super.onCreate(savedInstanceState)

        // Set warna window dari cache SEBELUM layout inflate — cegah flash
        val cachedColor = Color.parseColor(ThemeCache.load(this))
        window.statusBarColor     = cachedColor
        window.navigationBarColor = cachedColor

        try {
            binding = ActivitySettingsBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e: Exception) {
            finish()
            return
        }

        // Terapkan tema dari StateFlow langsung
        themeViewModel.themeState.value.let { state ->
            applyTheme(state.primaryColor, state.darkColor)
        }

        setupToolbar()
        setupListeners()
        observeSettings()
    }

    override fun onResume() {
        super.onResume()
        themeViewModel.themeState.value.let { state ->
            applyTheme(state.primaryColor, state.darkColor)
        }
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = getString(R.string.settings_title)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    themeViewModel.themeState.collect { state ->
                        applyTheme(state.primaryColor, state.darkColor)
                    }
                }

                launch {
                    viewModel.settings.collect { s ->
                        isInitialized = false

                        binding.switchDarkMode.isChecked = s.isDarkMode
                        AppCompatDelegate.setDefaultNightMode(
                            if (s.isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO
                        )

                        binding.radioGroupFont.check(
                            when (s.fontSizeOption) {
                                FontSizeOption.SMALL  -> R.id.radioSmall
                                FontSizeOption.MEDIUM -> R.id.radioMedium
                                FontSizeOption.LARGE  -> R.id.radioLarge
                            }
                        )

                        binding.switchBeep.isChecked   = s.isBeepEnabled
                        binding.switchHaptic.isChecked = s.isHapticEnabled

                        binding.radioGroupLang.check(
                            when (s.language) {
                                AppLanguage.INDONESIA -> R.id.radioId
                                AppLanguage.ENGLISH   -> R.id.radioEn
                                AppLanguage.ARABIC    -> R.id.radioAr
                            }
                        )

                        isInitialized = true
                    }
                }
            }
        }
    }

    private fun applyTheme(primaryHex: String, darkHex: String) {
        ThemeCache.save(this, primaryHex)
        val color = Color.parseColor(primaryHex)
        window.statusBarColor     = color
        window.navigationBarColor = color
        binding.root.setBackgroundColor(color)
        binding.tvSectionDisplay.setTextColor(Color.WHITE)
        binding.tvSectionFeedback.setTextColor(Color.WHITE)
        binding.tvSectionLanguage.setTextColor(Color.WHITE)
    }

    private fun setupListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            if (!isInitialized) return@setOnCheckedChangeListener
            viewModel.toggleDarkMode(checked)
            // Terapkan tema sebelum recreate agar tidak flash
            themeViewModel.themeState.value.let { state ->
                applyTheme(state.primaryColor, state.darkColor)
            }
            lifecycleScope.launch {
                kotlinx.coroutines.delay(300)
                recreate()
            }
        }

        binding.radioGroupFont.setOnCheckedChangeListener { _, id ->
            if (!isInitialized) return@setOnCheckedChangeListener
            val option = when (id) {
                R.id.radioSmall  -> FontSizeOption.SMALL
                R.id.radioMedium -> FontSizeOption.MEDIUM
                else             -> FontSizeOption.LARGE
            }
            viewModel.setFontSize(option)
        }

        binding.switchBeep.setOnCheckedChangeListener { _, checked ->
            if (!isInitialized) return@setOnCheckedChangeListener
            viewModel.toggleBeep(checked)
        }

        binding.switchHaptic.setOnCheckedChangeListener { _, checked ->
            if (!isInitialized) return@setOnCheckedChangeListener
            viewModel.toggleHaptic(checked)
        }

        binding.radioGroupLang.setOnCheckedChangeListener { _, id ->
            if (!isInitialized) return@setOnCheckedChangeListener
            val lang = when (id) {
                R.id.radioId -> AppLanguage.INDONESIA
                R.id.radioEn -> AppLanguage.ENGLISH
                else         -> AppLanguage.ARABIC
            }
            viewModel.setLanguage(lang)
            lifecycleScope.launch {
                kotlinx.coroutines.delay(100)
                // Restart MainActivity dari awal agar bahasa berubah di semua activity
                val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}