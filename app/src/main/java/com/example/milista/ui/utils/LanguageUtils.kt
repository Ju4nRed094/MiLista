package com.example.milista.ui.utils

import com.example.milista.R

data class AppLanguage(val name: String, val code: String, val flag: String? = null, val imageRes: Int? = null)

val AppLanguages = listOf(
    AppLanguage("Español", "es", "🇨🇱"),
    AppLanguage("Mapudungun • Mapuche 🌿", "arn", null, R.drawable.mapuche),
    AppLanguage("English", "en", "🇺🇸"),
    AppLanguage("Português", "pt", "🇵🇹"),
    AppLanguage("日本語", "ja", "🇯🇵"),
    AppLanguage("한국어", "ko", "🇰🇷"),
    AppLanguage("Русский", "ru", "🇷🇺"),
    AppLanguage("العربية", "ar", "🇸🇦")
)

fun getLocaleCode(language: String): String {
    return AppLanguages.find { it.name == language || it.code == language }?.code ?: "es"
}
