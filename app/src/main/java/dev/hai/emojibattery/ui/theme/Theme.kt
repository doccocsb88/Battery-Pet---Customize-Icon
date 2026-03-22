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
    primary = StrawberryMilk.Primary,
    onPrimary = StrawberryMilk.OnPrimary,
    primaryContainer = StrawberryMilk.PrimaryContainer,
    onPrimaryContainer = StrawberryMilk.OnPrimaryContainer,
    secondary = StrawberryMilk.Secondary,
    onSecondary = StrawberryMilk.OnSecondary,
    secondaryContainer = StrawberryMilk.SecondaryContainer,
    onSecondaryContainer = StrawberryMilk.OnSurface,
    tertiary = StrawberryMilk.Tertiary,
    onTertiary = StrawberryMilk.OnSurface,
    background = StrawberryMilk.Background,
    onBackground = StrawberryMilk.OnSurface,
    surface = StrawberryMilk.Surface,
    onSurface = StrawberryMilk.OnSurface,
    surfaceVariant = StrawberryMilk.SurfaceVariant,
    onSurfaceVariant = StrawberryMilk.OnSurfaceVariant,
    outline = StrawberryMilk.Outline,
)

/** Dark theme: same Strawberry Milk brand (rose surfaces, violet secondaries), not generic purple-gray. */
private val DarkScheme = darkColorScheme(
    primary = Color(0xFFF472B6),
    onPrimary = Color(0xFF2D0818),
    primaryContainer = Color(0xFF831843),
    onPrimaryContainer = Color(0xFFFCE7F3),
    secondary = Color(0xFFD8B4FE),
    onSecondary = Color(0xFF1E0B2E),
    secondaryContainer = Color(0xFF581C87),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = Color(0xFFF9A8D4),
    onTertiary = Color(0xFF3D0618),
    background = Color(0xFF1A0F14),
    onBackground = Color(0xFFFCE7F3),
    surface = Color(0xFF261820),
    onSurface = Color(0xFFFCE7F3),
    surfaceVariant = Color(0xFF3D2A32),
    onSurfaceVariant = Color(0xFFE8C4D4),
    outline = Color(0xFF8B6578),
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
