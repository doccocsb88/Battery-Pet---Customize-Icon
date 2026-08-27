package dev.hai.emojibattery.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Reusable design tokens for the Ocean Serenity app style.
 * Global palette is based on the Ocean Serenity board:
 * primary #0077BE, secondary #40E0D0, tertiary #98FFD9, neutral #F5F5DC.
 */
object OceanSerenity {
    val Background = Color(0xFFE9F1FF).copy(alpha = 0.5f)
    val Surface = Color(0xFFFFFCF2)
    val SurfaceVariant = Color(0xFFF3F0D9)
    val Outline = Color(0xFFC9D6D8)
    val OnSurface = Color(0xFF264447)
    val OnSurfaceVariant = Color(0xFF6F797A)

    val Primary = Color(0xFF0077BE)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFFD7ECFA)
    val OnPrimaryContainer = Color(0xFF003C61)

    val Secondary = Color(0xFF40E0D0)
    val OnSecondary = Color(0xFF073B38)
    val SecondaryContainer = Color(0xFFD9FBF8)
    val OnSecondaryContainer = Color(0xFF0B4C4F)

    val Tertiary = Color(0xFF98FFD9)
    val OnTertiary = Color(0xFF13463C)
    val PopularBadge = Primary

    val ModuleIconTint = Color(0xFF0097A7)
    val ModuleLabel = Color(0xFF006064)
    val ModuleIconHalo = ModuleIconTint.copy(alpha = 0.10f)
    val ModuleCard = Color(0xFFFDFEFE)
    val ModuleShadow = Color(0x1A006478)

    val BottomBarActive = Color(0xFF006874)
    val BottomBarInactive = Color(0xFF6F797A)
    val BottomBarHighlight = BottomBarActive.copy(alpha = 0.10f)
}

@Composable
fun oceanModuleLabelTextStyle(): TextStyle = MaterialTheme.typography.labelMedium.copy(
    fontWeight = FontWeight.Bold,
    fontSize = 9.sp,
    lineHeight = 11.sp,
    letterSpacing = 1.2.sp,
)
