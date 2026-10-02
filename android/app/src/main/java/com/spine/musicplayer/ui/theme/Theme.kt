package com.spine.musicplayer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SpineDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF5F5F4),
    onPrimary = Color(0xFF141312),
    surface = Color(0xFF141312),
    onSurface = Color(0xFFE7E5E4),
    background = Color(0xFF0F0E0D),
    onBackground = Color(0xFFE7E5E4),
    surfaceVariant = Color(0xFF262422),
    onSurfaceVariant = Color(0xFFA8A29E)
)

@Composable
fun SpineTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            try {
                val window = activity.window
                window.statusBarColor = Color(0xFF141312).toArgb()
                window.navigationBarColor = Color(0xFF141210).toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            } catch (_: Exception) {
                // Ignore edge-to-edge / window access exceptions on newer Android runtimes
            }
        }
    }

    MaterialTheme(
        colorScheme = SpineDarkColorScheme,
        content = content
    )
}
