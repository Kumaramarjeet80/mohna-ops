package com.mohna.ops.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RiderColorScheme = darkColorScheme(
    primary = RiderAccent,
    onPrimary = RiderBg,
    primaryContainer = RiderAccentDark,
    onPrimaryContainer = RiderText,
    secondary = RiderAccentDark,
    onSecondary = RiderText,
    background = RiderBg,
    onBackground = RiderText,
    surface = RiderSurface,
    onSurface = RiderText,
    surfaceVariant = RiderSurfaceVariant,
    onSurfaceVariant = RiderTextMuted,
    outline = RiderBorder,
    error = RiderLate,
    onError = RiderText
)

private val AdminColorScheme = lightColorScheme(
    primary = AdminAccent,
    onPrimary = AdminSurface,
    primaryContainer = AdminAccent,
    onPrimaryContainer = AdminSurface,
    secondary = AdminAccentDark,
    onSecondary = AdminSurface,
    background = AdminBg,
    onBackground = AdminText,
    surface = AdminSurface,
    onSurface = AdminText,
    surfaceVariant = AdminSurfaceVariant,
    onSurfaceVariant = AdminTextMuted,
    outline = AdminBorder,
    error = AdminLate,
    onError = AdminSurface
)

@Composable
fun OpsTheme(
    isAdminMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAdminMode) AdminColorScheme else RiderColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            // In Admin mode (light), use dark status bar icons; in Rider mode (dark), use light status bar icons
            windowInsetsController.isAppearanceLightStatusBars = isAdminMode
            windowInsetsController.isAppearanceLightNavigationBars = isAdminMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
