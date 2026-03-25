package com.igoy86.digitaltasbih.ui

import android.content.Context
import android.content.res.Configuration
import com.igoy86.digitaltasbih.data.model.AppLanguage
import java.util.Locale

object LocaleHelper {
    fun applyLocale(context: Context, language: AppLanguage): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
