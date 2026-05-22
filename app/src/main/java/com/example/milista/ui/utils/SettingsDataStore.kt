package com.example.milista.ui.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val SELECTED_FONT = stringPreferencesKey("selected_font")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val SELECTED_FONT_SIZE = stringPreferencesKey("selected_font_size")
        val ONBOARDING_COMPLETED = stringPreferencesKey("onboarding_completed")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED]?.toBoolean() ?: false
        }

    val selectedLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_LANGUAGE] ?: java.util.Locale.getDefault().language
        }

    val selectedFont: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_FONT] ?: "Roboto"
        }

    val selectedTheme: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_THEME] ?: "Oscuro"
        }

    val selectedFontSize: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_FONT_SIZE]?.toFloat() ?: 16f
        }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = language
        }
    }

    suspend fun saveFont(font: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_FONT] = font
        }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_THEME] = theme
        }
    }

    suspend fun saveFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_FONT_SIZE] = size.toString()
        }
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed.toString()
        }
    }
}
