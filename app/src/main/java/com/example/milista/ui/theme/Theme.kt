package com.example.milista.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MiListaColorScheme = darkColorScheme(
    primary = AcentoVerde,
    onPrimary = FondoPrincipal,
    surface = FondoCard,
    onSurface = TextoPrincipal,
    background = FondoPrincipal,
    onBackground = TextoPrincipal,
    secondaryContainer = FondoCard,
    onSecondaryContainer = TextoPrincipal,
    surfaceVariant = FondoCard,
    onSurfaceVariant = TextoSecundario
)

@Composable
fun MiListaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FondoPrincipal.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = MiListaColorScheme,
        typography = Typography,
        content = content
    )
}
