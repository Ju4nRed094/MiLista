package com.example.milista.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SamsungGreen,
    onPrimary = Color.Black,
    surface = CardDark,
    onSurface = SoftWhite,
    background = BackgroundDark,
    onBackground = SoftWhite,
    secondaryContainer = Color.White.copy(alpha = 0.06f),
    onSecondaryContainer = SoftWhite,
    surfaceVariant = Color.White.copy(alpha = 0.05f),
    onSurfaceVariant = GrayText
)

private val LightColorScheme = lightColorScheme(
    primary = SamsungGreen,
    onPrimary = Color.White,
    surface = LightSurface,
    onSurface = LightOnSurface,
    background = LightBackground,
    onBackground = LightOnSurface,
    secondaryContainer = Color(0xFFE5E5EA),
    onSecondaryContainer = LightOnSurface,
    surfaceVariant = Color.White,
    onSurfaceVariant = Color.Gray
)

@Composable
fun MiListaTheme(
    selectedFontFamily: FontFamily = FontFamily.Default,
    selectedTheme: String = "Oscuro",
    selectedFontSize: Float = 16f,
    content: @Composable () -> Unit
) {
    val darkTheme = when (selectedTheme) {
        "Oscuro" -> true
        "Claro" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    val dynamicTypography = remember(selectedFontFamily, selectedFontSize) {
        val baseTypography = getTypography(selectedFontFamily)
        val ratio = selectedFontSize / 16f
        
        baseTypography.copy(
            displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * ratio),
            displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * ratio),
            displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * ratio),
            headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * ratio),
            headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * ratio),
            headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * ratio),
            titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * ratio),
            titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * ratio),
            titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * ratio),
            bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * ratio),
            bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * ratio),
            bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * ratio),
            labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * ratio),
            labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * ratio),
            labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * ratio)
        )
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}
