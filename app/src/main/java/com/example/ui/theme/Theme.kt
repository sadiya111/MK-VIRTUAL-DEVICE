package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryLight,
    onPrimary = DarkBackground,
    primaryContainer = BluePrimary,
    onPrimaryContainer = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainer,
    surfaceContainerHighest = DarkSurfaceHighest,
    outline = DarkOnSurfaceVariant.copy(alpha = 0.5f),
    outlineVariant = DarkSurfaceHighest
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = LightSurface,
    primaryContainer = LightSurfaceContainer,
    onPrimaryContainer = BluePrimary,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainer,
    surfaceContainerHighest = LightSurfaceHighest,
    outline = LightOnSurfaceVariant.copy(alpha = 0.5f),
    outlineVariant = LightSurfaceHighest
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted rich palette for consistency
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
