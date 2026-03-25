package com.igoy86.digitaltasbih.ui

import android.content.Context

object ThemeCache {

    private const val PREF = "theme_cache"
    private const val KEY_PRIMARY = "primary"

    fun save(context: Context, primaryHex: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PRIMARY, primaryHex)
            .apply()
    }

    fun load(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_PRIMARY, "#2D763F") ?: "#2D763F"
    }
}