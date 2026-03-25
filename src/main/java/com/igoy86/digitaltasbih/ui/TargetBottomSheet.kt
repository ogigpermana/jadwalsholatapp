package com.igoy86.digitaltasbih.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.igoy86.digitaltasbih.R
import com.igoy86.digitaltasbih.ui.viewmodel.TasbihViewModel
import com.igoy86.digitaltasbih.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

class TargetBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: TasbihViewModel by activityViewModels()
    private val themeViewModel: ThemeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_target, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
		
		dialog?.window?.navigationBarColor = Color.parseColor(ThemeCache.load(requireContext()))

        val etCustomTarget = view.findViewById<EditText>(R.id.et_custom_target)
        val btnSet         = view.findViewById<Button>(R.id.btn_set_target)
        val btnClear       = view.findViewById<Button>(R.id.btn_clear_target)
        val tvCurrent      = view.findViewById<TextView>(R.id.tv_current_target)

        val currentTarget = viewModel.target.value
        tvCurrent.text = if (currentTarget > 0)
            getString(R.string.target_current_format, currentTarget)
        else
            getString(R.string.target_current_none)

        view.findViewById<Button>(R.id.btn_preset_33).setOnClickListener   { applyTarget(33) }
        view.findViewById<Button>(R.id.btn_preset_99).setOnClickListener   { applyTarget(99) }
        view.findViewById<Button>(R.id.btn_preset_1000).setOnClickListener { applyTarget(1000) }

        btnSet.setOnClickListener {
            val input = etCustomTarget.text.toString().toIntOrNull()
            if (input != null && input > 0) {
                applyTarget(input)
            } else {
                etCustomTarget.error = getString(R.string.target_invalid_input)
            }
        }

        btnClear.setOnClickListener {
            viewModel.clearTarget()
            dismiss()
        }

        btnSet.isEnabled = false
        etCustomTarget.doAfterTextChanged {
            btnSet.isEnabled = it.toString().isNotEmpty()
        }

        themeViewModel.themeState.value.let { state ->
            applyTheme(view, state.primaryColor, state.darkColor)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeViewModel.themeState.collect { state ->
                    applyTheme(view, state.primaryColor, state.darkColor)
					dialog?.window?.navigationBarColor = Color.parseColor(state.primaryColor)
                }
            }
        }
    }

    private fun applyTheme(root: View, primaryHex: String, darkHex: String) {
        val primaryColor = Color.parseColor(primaryHex)
        val darkColor    = Color.parseColor(darkHex)
        root.setBackgroundColor(primaryColor)
        listOf(
            root.findViewById<Button>(R.id.btn_preset_33),
            root.findViewById<Button>(R.id.btn_preset_99),
            root.findViewById<Button>(R.id.btn_preset_1000),
            root.findViewById<Button>(R.id.btn_set_target),
            root.findViewById<Button>(R.id.btn_clear_target)
        ).forEach { btn ->
            btn?.backgroundTintList =
                android.content.res.ColorStateList.valueOf(darkColor)
            btn?.setTextColor(Color.WHITE)
        }
    }

    private fun applyTarget(target: Int) {
        viewModel.setTarget(target)
        Toast.makeText(
            requireContext(),
            getString(R.string.target_selected_format, target),
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    companion object {
        const val TAG = "TargetBottomSheet"
    }
}