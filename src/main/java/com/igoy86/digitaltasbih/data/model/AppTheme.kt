package com.igoy86.digitaltasbih.data.model

enum class AppTheme(val primary: String, val dark: String) {
    GREEN ("#2D763F", "#1A531A"),
    RED   ("#8B2020", "#5C1010"),
    BLUE  ("#1A4F7A", "#0F3050"),
    PURPLE("#5B2D8B", "#3A1A5C"),
    ORANGE("#8B5A1A", "#5C3A0A"),
    CUSTOM("", "")  // warna disimpan terpisah di DataStore
}