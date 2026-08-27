package dev.hai.emojibattery.ui.theme

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
    primary = OceanSerenity.Primary,
    onPrimary = OceanSerenity.OnPrimary,
    primaryContainer = OceanSerenity.PrimaryContainer,
    onPrimaryContainer = OceanSerenity.OnPrimaryContainer,
    secondary = OceanSerenity.Secondary,
    onSecondary = OceanSerenity.OnSecondary,
    secondaryContainer = OceanSerenity.SecondaryContainer,
    onSecondaryContainer = OceanSerenity.OnSecondaryContainer,
    tertiary = OceanSerenity.Tertiary,
    onTertiary = OceanSerenity.OnTertiary,
    background = OceanSerenity.Background,
    onBackground = OceanSerenity.OnSurface,
    surface = OceanSerenity.Surface,
    onSurface = OceanSerenity.OnSurface,
    surfaceVariant = OceanSerenity.SurfaceVariant,
    onSurfaceVariant = OceanSerenity.OnSurfaceVariant,
    outline = OceanSerenity.Outline,
)

/** Dark theme tuned to the Ocean Serenity palette rather than the legacy rose palette. */
private val DarkScheme = darkColorScheme(
    primary = Color(0xFF5EB6E7),
    onPrimary = Color(0xFF062C44),
    primaryContainer = Color(0xFF0E466A),
    onPrimaryContainer = Color(0xFFD7ECFA),
    secondary = Color(0xFF69E7DB),
    onSecondary = Color(0xFF083836),
    secondaryContainer = Color(0xFF0E5353),
    onSecondaryContainer = Color(0xFFD9FBF8),
    tertiary = Color(0xFF98FFD9),
    onTertiary = Color(0xFF103A32),
    background = Color(0xFF08191C),
    onBackground = Color(0xFFE5F3F1),
    surface = Color(0xFF102528),
    onSurface = Color(0xFFE5F3F1),
    surfaceVariant = Color(0xFF183236),
    onSurfaceVariant = Color(0xFFA5BBBD),
    outline = Color(0xFF557376),
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
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AppTypography,
        content = content,
    )
}
