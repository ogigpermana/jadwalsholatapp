package com.igoy86.digitaltasbih.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.igoy86.digitaltasbih.R
import com.igoy86.digitaltasbih.data.model.AppTheme
import com.igoy86.digitaltasbih.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

class ThemeBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ThemeBottomSheet"
    }

    private val themeViewModel: ThemeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_theme, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
		
		dialog?.window?.navigationBarColor = Color.parseColor(ThemeCache.load(requireContext()))

        // Tombol pilih tema preset
		themeViewModel.themeState.value.let { state ->
            view.setBackgroundColor(Color.parseColor(state.primaryColor))
        }
		
        view.findViewById<Button>(R.id.btn_theme_green).setOnClickListener {
            themeViewModel.saveTheme(AppTheme.GREEN); dismiss()
        }
        view.findViewById<Button>(R.id.btn_theme_red).setOnClickListener {
            themeViewModel.saveTheme(AppTheme.RED); dismiss()
        }
        view.findViewById<Button>(R.id.btn_theme_blue).setOnClickListener {
            themeViewModel.saveTheme(AppTheme.BLUE); dismiss()
        }
        view.findViewById<Button>(R.id.btn_theme_purple).setOnClickListener {
            themeViewModel.saveTheme(AppTheme.PURPLE); dismiss()
        }
        view.findViewById<Button>(R.id.btn_theme_orange).setOnClickListener {
            themeViewModel.saveTheme(AppTheme.ORANGE); dismiss()
        }
        view.findViewById<Button>(R.id.btn_theme_custom).setOnClickListener {
            showColorPickerDialog()
        }

        // Observer tema — terapkan ke background bottom sheet ini
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeViewModel.themeState.collect { state ->
                    view.setBackgroundColor(Color.parseColor(state.primaryColor))
					dialog?.window?.navigationBarColor = Color.parseColor(state.primaryColor)
                }
            }
        }
    }

    private fun showColorPickerDialog() {
        ColorPickerDialog
            .Builder(requireActivity())
            .setTitle("Pilih Warna")
            .setColorShape(ColorShape.SQAURE)
            .setDefaultColor(R.color.bg_primary_green)
            .setColorListener { color, _ ->
                // Konsisten: gelapkan 40% sama seperti TargetBottomSheet & ThemeApplier
                val darkColor  = ColorUtils.blendARGB(color, Color.BLACK, 0.4f)
                val primaryHex = "#%06X".format(color and 0xFFFFFF)
                val darkHex    = "#%06X".format(darkColor and 0xFFFFFF)
                themeViewModel.saveTheme(AppTheme.CUSTOM, primaryHex, darkHex)
                dismiss()
            }
            .show()
    }
}







