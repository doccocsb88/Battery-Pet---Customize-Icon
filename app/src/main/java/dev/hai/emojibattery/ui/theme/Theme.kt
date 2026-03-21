package dev.hai.emojibattery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.q7labs.co.emoji.R

private val LightScheme = lightColorScheme(
    primary = Color(0xFFD47DFE),
    onPrimary = Color.White,
    secondary = Color(0xFF5C4B51),
    onSecondary = Color.White,
    tertiary = Color(0xFFFFABE5),
    background = Color(0xFFFEF5FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFFFECF3),
    onSurface = Color(0xFF08162D),
    onSurfaceVariant = Color(0xFF5C4B51),
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

private val AlbertSans = FontFamily(
    Font(R.font.albert_sans_regular_400, FontWeight.Normal),
    Font(R.font.albert_sans_medium_500, FontWeight.Medium),
    Font(R.font.albert_sans_semibold_600, FontWeight.SemiBold),
    Font(R.font.albert_sans_bold_700, FontWeight.Bold),
    Font(R.font.albert_sans_extrabold_800, FontWeight.ExtraBold),
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = AlbertSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

@Composable
fun EmojiBatteryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AppTypography,
        content = content,
    )
}
