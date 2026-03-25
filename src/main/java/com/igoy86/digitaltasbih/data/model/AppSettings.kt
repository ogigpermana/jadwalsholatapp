package com.igoy86.digitaltasbih.data.model

data class AppSettings(
    val isDarkMode: Boolean = false,
    val fontSizeOption: FontSizeOption = FontSizeOption.MEDIUM,
    val isBeepEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.INDONESIA
)

enum class FontSizeOption(val sp: Float) {
    SMALL(20f),
    MEDIUM(45f),
    LARGE(70f)
}

enum class AppLanguage(val code: String) {
    INDONESIA("id"),
    ENGLISH("en"),
    ARABIC("ar")
}