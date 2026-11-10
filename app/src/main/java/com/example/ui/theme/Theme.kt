package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AuraPrimaryViolet,
    onPrimary = Color.White,
    primaryContainer = AuraDarkSurfaceHighlight,
    onPrimaryContainer = Color.White,
    secondary = AuraSecondaryCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0E3842),
    onSecondaryContainer = AuraSecondaryCyan,
    tertiary = AuraTertiaryPink,
    background = AuraDarkBackground,
    onBackground = AuraTextPrimary,
    surface = AuraDarkSurface,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraDarkSurfaceElevated,
    onSurfaceVariant = AuraTextSecondary,
    outline = AuraOutline
)

private val LightColorScheme = darkColorScheme( // For this theme app, an AMOLED/OLED night aesthetic is immersive
    primary = AuraPrimaryViolet,
    onPrimary = Color.White,
    primaryContainer = AuraDarkSurfaceHighlight,
    onPrimaryContainer = Color.White,
    secondary = AuraSecondaryCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0E3842),
    onSecondaryContainer = AuraSecondaryCyan,
    tertiary = AuraTertiaryPink,
    background = AuraDarkBackground,
    onBackground = AuraTextPrimary,
    surface = AuraDarkSurface,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraDarkSurfaceElevated,
    onSurfaceVariant = AuraTextSecondary,
    outline = AuraOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
