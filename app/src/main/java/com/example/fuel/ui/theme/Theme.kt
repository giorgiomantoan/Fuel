package com.example.fuel.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Colori seed verde carburante
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B6B4A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA1F6C8),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4C6356),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEE9D8),
    onSecondaryContainer = Color(0xFF092016),
    tertiary = Color(0xFF3D6472),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC1E8F9),
    onTertiaryContainer = Color(0xFF001F27),
    error = Color(0xFFBA1A1A),
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5DC),
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF717972),
    outlineVariant = Color(0xFFC0C9C0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF85D9AD),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF005236),
    onPrimaryContainer = Color(0xFFA1F6C8),
    secondary = Color(0xFFB3CCBD),
    onSecondary = Color(0xFF1E352A),
    secondaryContainer = Color(0xFF344C3F),
    onSecondaryContainer = Color(0xFFCEE9D8),
    tertiary = Color(0xFFA5CCDD),
    onTertiary = Color(0xFF073542),
    tertiaryContainer = Color(0xFF244C59),
    onTertiaryContainer = Color(0xFFC1E8F9),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DE),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE1E3DE),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC0C9C0),
    outline = Color(0xFF8A938B),
    outlineVariant = Color(0xFF414942)
)

@Composable
fun FuelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
