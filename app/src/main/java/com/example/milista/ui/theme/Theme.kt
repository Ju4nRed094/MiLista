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
fun NoctraTheme(
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
        getTypography(selectedFontFamily, selectedFontSize)
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
