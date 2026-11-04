package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MyatoDarkColorScheme = darkColorScheme(
    primary = MyatoGreen,
    onPrimary = MyatoBackground,
    primaryContainer = MyatoGreenDark,
    onPrimaryContainer = MyatoWhite,
    secondary = MyatoGreenLight,
    onSecondary = MyatoBackground,
    secondaryContainer = MyatoSurfaceVariant,
    onSecondaryContainer = MyatoWhite,
    tertiary = MyatoGreenGlow,
    onTertiary = MyatoBackground,
    background = MyatoBackground,
    onBackground = MyatoWhite,
    surface = MyatoSurface,
    onSurface = MyatoWhite,
    surfaceVariant = MyatoSurfaceVariant,
    onSurfaceVariant = MyatoGrayLight,
    surfaceContainer = MyatoCard,
    surfaceContainerHigh = MyatoSurfaceVariant,
    outline = MyatoBorder,
    outlineVariant = MyatoBorder,
    error = MyatoError,
    onError = MyatoWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Myato dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = MyatoDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = MyatoBackground.toArgb()
                window.navigationBarColor = MyatoBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
