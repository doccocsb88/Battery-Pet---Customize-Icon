package dev.hai.emojibattery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFFEA6A9A),
    onPrimary = Color.White,
    secondary = Color(0xFF5C4B51),
    onSecondary = Color.White,
    tertiary = Color(0xFF17A398),
    background = Color(0xFFFFF7FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFFFECF3),
    onSurface = Color(0xFF24161B),
    onSurfaceVariant = Color(0xFF6F5B62),
    outline = Color(0xFFE5C7D2),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFFF8FB1),
    secondary = Color(0xFFE6B8C7),
    tertiary = Color(0xFF74E4D9),
    background = Color(0xFF181216),
    surface = Color(0xFF22191F),
    onSurface = Color(0xFFF8EEF2),
    onSurfaceVariant = Color(0xFFD7C0C9),
)

@Composable
fun EmojiBatteryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
