package com.igoy86.digitaltasbih.ui

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils

object ThemeApplier {

    /**
     * Hitung warna dark otomatis dari warna primary (gelapkan 40%)
     * Konsisten dengan ThemeBottomSheet color picker
     */
    fun darken(colorHex: String, factor: Float = 0.4f): Int {
        val color = Color.parseColor(colorHex)
        return ColorUtils.blendARGB(color, Color.BLACK, factor)
    }

    /**
     * Terapkan tema penuh ke Activity:
     * - background root
     * - status bar
     * - navigation bar
     */
    fun applyToActivity(activity: Activity, primaryHex: String) {
        val color = Color.parseColor(primaryHex)
        activity.window.decorView
            .findViewById<View>(android.R.id.content)
            ?.setBackgroundColor(color)
        activity.window.statusBarColor     = color
        activity.window.navigationBarColor = color
    }

    /**
     * Terapkan warna primary ke background View
     */
    fun applyBackground(view: View, primaryHex: String) {
        view.setBackgroundColor(Color.parseColor(primaryHex))
    }

    /**
     * Terapkan warna dark ke Button backgroundTint
     */
    fun applyButtonTint(button: Button, darkHex: String) {
        button.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(darkHex))
    }

    /**
     * Terapkan warna dark ke CardView
     */
    fun applyCard(card: CardView, darkHex: String) {
        card.setCardBackgroundColor(Color.parseColor(darkHex))
    }

    /**
     * Terapkan warna ke bottom sheet container (root view)
     */
    fun applyBottomSheet(rootView: View, primaryHex: String) {
        rootView.setBackgroundColor(Color.parseColor(primaryHex))
    }
}

