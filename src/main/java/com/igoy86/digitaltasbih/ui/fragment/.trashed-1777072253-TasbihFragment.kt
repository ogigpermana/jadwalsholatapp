package com.igoy86.digitaltasbih.ui.fragment

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.app.AppCompatDelegate
import android.content.res.Configuration
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.igoy86.digitaltasbih.R
import com.igoy86.digitaltasbih.data.model.FontSizeOption
import com.igoy86.digitaltasbih.databinding.FragmentTasbihBinding
import com.igoy86.digitaltasbih.ui.MainActivity
import com.igoy86.digitaltasbih.ui.SavedDhikrActivity
import com.igoy86.digitaltasbih.ui.SettingsActivity
import com.igoy86.digitaltasbih.ui.TargetBottomSheet
import com.igoy86.digitaltasbih.ui.ThemeApplier
import com.igoy86.digitaltasbih.ui.ThemeBottomSheet
import com.igoy86.digitaltasbih.ui.ThemeCache
import com.igoy86.digitaltasbih.ui.viewmodel.SaveEvent
import com.igoy86.digitaltasbih.ui.viewmodel.TasbihViewModel
import com.igoy86.digitaltasbih.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class TasbihFragment : Fragment() {

    private var _binding: FragmentTasbihBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TasbihViewModel by activityViewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private var toneGenerator: ToneGenerator? = null

    companion object {
        const val REQUEST_LOAD_DHIKR = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasbihBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        requireActivity().window.decorView.isHapticFeedbackEnabled = true

        themeViewModel.themeState.value.let { state ->
            applyTheme(state.primaryColor, state.darkColor)
        }

        setupFont()
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.count.collect { count ->
                        updateDisplayColor(count)
                    }
                }

                launch {
                    viewModel.saveStatus.collect { success ->
                        if (success == false) {
                            binding.tvDisplayCount.setTextColor(
                                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                            )
                        }
                    }
                }

                launch {
                    viewModel.targetReached.collect {
                        playBeep()
                        if (viewModel.isHapticEnabled) {
                            binding.root.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        }
                        Toast.makeText(requireContext(), getString(R.string.toast_target_reached), Toast.LENGTH_SHORT).show()
                    }
                }

                launch {
                    viewModel.saveEvent.collect { event ->
                        val msg = when (event) {
                            is SaveEvent.Saved   -> getString(R.string.toast_save_success)
                            is SaveEvent.Updated -> getString(R.string.toast_save_updated)
                            is SaveEvent.Failed  -> getString(R.string.toast_save_failed)
                        }
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }

                launch {
                    themeViewModel.themeState.collect { state ->
                        applyTheme(state.primaryColor, state.darkColor)
                    }
                }

                launch {
                    viewModel.settingsState.collect { s ->
                        val (widthRes, heightRes) = when (s.fontSizeOption) {
                            FontSizeOption.SMALL  -> Pair(R.dimen.display_width_small, R.dimen.display_height_small)
                            FontSizeOption.MEDIUM -> Pair(R.dimen.display_width_medium, R.dimen.display_height_medium)
                            FontSizeOption.LARGE  -> Pair(R.dimen.display_width_large, R.dimen.display_height_large)
                        }
                        val params = binding.flDisplay.layoutParams
                        params.width  = resources.getDimensionPixelSize(widthRes)
                        params.height = resources.getDimensionPixelSize(heightRes)
                        binding.flDisplay.layoutParams = params
                    }
                }
            }
        }
    }

    private fun applyTheme(primaryHex: String, darkHex: String) {
        ThemeCache.save(requireContext(), primaryHex)
        val color = android.graphics.Color.parseColor(primaryHex)
        binding.root.setBackgroundColor(color)
        binding.vTasbihOuter.setBackgroundColor(color)
        (requireActivity() as? MainActivity)?.applyWindowTheme(primaryHex)
    }

    private fun updateDisplayColor(count: Int) {
        val text = String.format(Locale.US, "%05d", count)
        val spannable = SpannableString(text)
        val colorDim    = ContextCompat.getColor(requireContext(), R.color.display_text_dim)
        val colorBright = ContextCompat.getColor(requireContext(), R.color.display_text)
        val firstNonZero = text.indexOfFirst { it != '0' }

        if (firstNonZero == -1) {
            spannable.setSpan(ForegroundColorSpan(colorDim), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            if (firstNonZero > 0) {
                spannable.setSpan(ForegroundColorSpan(colorDim), 0, firstNonZero, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            spannable.setSpan(ForegroundColorSpan(colorBright), firstNonZero, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvDisplayCount.text = spannable
    }

    private fun playBeep() {
        if (viewModel.isBeepEnabled) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
        }
    }

    private fun performHapticFeedback() {
        requireActivity().window.decorView.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    private fun setupListeners() {
        binding.btnMainCount.setOnClickListener {
            if (viewModel.isHapticEnabled) performHapticFeedback()
            viewModel.increment()
        }

        binding.btnSmallAction.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            viewModel.reset()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tasbih, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
	
	@Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        try {
            val method = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.java)
            method.isAccessible = true
            method.invoke(menu, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Force icon tint sesuai mode
        val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        || (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        && resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK 
        == android.content.res.Configuration.UI_MODE_NIGHT_YES)
        val tintColor = if (isDark) android.graphics.Color.WHITE 
                else android.graphics.Color.BLACK
    
        for (i in 0 until menu.size()) {
            menu.getItem(i).icon?.setTint(tintColor)
        }

        super.onPrepareOptionsMenu(menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_star -> {
                val intent = Intent(requireContext(), SavedDhikrActivity::class.java)
                intent.putExtra("show_favorites", true)
                startActivity(intent)
                true
            }
            R.id.menu_target -> {
                TargetBottomSheet().show(parentFragmentManager, TargetBottomSheet.TAG)
                true
            }
            R.id.menu_settings -> {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
                true
            }
            R.id.menu_puzzle -> {
                ThemeBottomSheet().show(parentFragmentManager, ThemeBottomSheet.TAG)
                true
            }
            R.id.menu_save -> {
                showSaveDialog()
                true
            }
            R.id.menu_saved_dhikr -> {
                @Suppress("DEPRECATION")
                startActivityForResult(
                    Intent(requireContext(), SavedDhikrActivity::class.java),
                    REQUEST_LOAD_DHIKR
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSaveDialog() {
        val activeId   = viewModel.activeDhikrId.value
        val activeName = viewModel.activeDhikrName.value
        val count      = viewModel.count.value
        val target     = viewModel.target.value
        val targetStr  = if (target > 0) "$target" else getString(R.string.dialog_save_no_target)

        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.dialog_save_hint)
            setText(activeName ?: "")
            selectAll()
        }

        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val px = resources.getDimensionPixelSize(R.dimen.spacing_md)
            setPadding(px, 0, px, 0)
            addView(editText)
        }

        val title   = if (activeId != null) getString(R.string.dialog_save_title_update) else getString(R.string.dialog_save_title_new)
        val btnText = if (activeId != null) getString(R.string.dialog_save_btn_update) else getString(R.string.dialog_save_btn_new)

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(getString(R.string.dialog_save_message, "$count", targetStr))
            .setView(container)
            .setPositiveButton(btnText) { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotBlank()) {
                    viewModel.saveDhikr(name)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.dialog_name_empty), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ -> viewModel.clearActiveDhikr() }
            .show()
    }

    private fun setupFont() {
        ResourcesCompat.getFont(requireContext(), R.font.dseg7classic_bold)?.let { font ->
            binding.tvDisplayCount.typeface = font
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOAD_DHIKR && resultCode == android.app.Activity.RESULT_OK) {
            val id     = data?.getIntExtra("load_id", -1) ?: -1
            val name   = data?.getStringExtra("load_name") ?: ""
            val count  = data?.getIntExtra("load_count", 0) ?: 0
            val target = data?.getIntExtra("load_target", 0) ?: 0
            if (id != -1) {
                viewModel.loadDhikr(id, name, count, target)
                Toast.makeText(requireContext(), getString(R.string.toast_dhikr_resumed, name), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveIfNeeded()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toneGenerator?.release()
        toneGenerator = null
        _binding = null
    }
}