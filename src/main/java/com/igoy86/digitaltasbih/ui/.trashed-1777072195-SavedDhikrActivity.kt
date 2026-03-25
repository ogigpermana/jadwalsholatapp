package com.igoy86.digitaltasbih.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.igoy86.digitaltasbih.R
import com.igoy86.digitaltasbih.data.local.SettingsDataStore
import com.igoy86.digitaltasbih.data.local.dataStore
import com.igoy86.digitaltasbih.data.model.AppLanguage
import com.igoy86.digitaltasbih.data.model.DhikrEntity
import com.igoy86.digitaltasbih.ui.viewmodel.SavedDhikrViewModel
import com.igoy86.digitaltasbih.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class SavedDhikrActivity : AppCompatActivity() {

    private val viewModel: SavedDhikrViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private lateinit var adapter: DhikrAdapter

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
        super.onCreate(savedInstanceState)

        val cachedColor = Color.parseColor(ThemeCache.load(this))
        window.statusBarColor = cachedColor
        window.navigationBarColor = cachedColor

        setContentView(R.layout.activity_saved_dhikr)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
        onBackPressedDispatcher.addCallback(this) { finish() }

        val llEmptyState = findViewById<LinearLayout>(R.id.ll_empty_state)
        val rvDhikrList = findViewById<RecyclerView>(R.id.rv_dhikr_list)

        findViewById<Button>(R.id.btn_go_to_counter).setOnClickListener { finish() }

        val showFavorites = intent.getBooleanExtra("show_favorites", false)
        findViewById<TextView>(R.id.tv_title).text =
            if (showFavorites) getString(R.string.saved_favorite_title)
            else getString(R.string.saved_dhikr_title)

        adapter = DhikrAdapter(
            onDelete = { showDeleteConfirm(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onEditName = { showEditNameDialog(it) },
            onLoad = { loadToCounter(it) }
        )

        rvDhikrList.layoutManager = LinearLayoutManager(this)
        rvDhikrList.adapter = adapter

        themeViewModel.themeState.value.let { state ->
            applyTheme(state.primaryColor, state.darkColor)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dhikrList.collect { list ->
                        val filtered = if (showFavorites) list.filter { it.isFavorite } else list
                        adapter.submitList(filtered)
                        llEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                        rvDhikrList.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
                launch {
                    themeViewModel.themeState.collect { state ->
                        applyTheme(state.primaryColor, state.darkColor)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        themeViewModel.themeState.value.let { state ->
            applyTheme(state.primaryColor, state.darkColor)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun applyTheme(primaryHex: String, darkHex: String) {
        val color = Color.parseColor(primaryHex)
        window.statusBarColor = color
        window.navigationBarColor = color
        findViewById<View>(R.id.root_layout).setBackgroundColor(color)
        adapter.updateThemeColor(darkHex)
        ThemeApplier.applyButtonTint(findViewById(R.id.btn_go_to_counter), darkHex)
    }

    private fun showDeleteConfirm(dhikr: DhikrEntity) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Dzikir")
            .setMessage("Hapus \"${dhikr.name}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.delete(dhikr)
                Toast.makeText(this, "\"${dhikr.name}\" dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditNameDialog(dhikr: DhikrEntity) {
        val editText = EditText(this).apply {
            setText(dhikr.name)
            selectAll()
        }
        AlertDialog.Builder(this)
            .setTitle("Edit Nama")
            .setView(editText)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotBlank()) {
                    viewModel.updateName(dhikr, newName)
                    Toast.makeText(this, "Nama diperbarui ✓", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadToCounter(dhikr: DhikrEntity) {
        val result = Intent().apply {
            putExtra("load_id", dhikr.id)
            putExtra("load_name", dhikr.name)
            putExtra("load_count", dhikr.count)
            putExtra("load_target", dhikr.target)
        }
        setResult(RESULT_OK, result)
        finish()
    }
}