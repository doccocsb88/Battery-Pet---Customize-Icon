package dev.hai.emojibattery.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Design tokens: **Strawberry Milk** (Soft Kawaii 2.0).
 * Primary CTA gradient: pink → magenta violet.
 */
object StrawberryMilk {
    val Background = Color(0xFFFBF9F8)
    val Surface = Color.White
    val Primary = Color(0xFFEC4899)
    val OnPrimary = Color.White
    val Secondary = Color(0xFFD47DFE)
    val OnSecondary = Color.White
    val Tertiary = Color(0xFFF9A8D4)
    val OnSurface = Color(0xFF5C4B51)
    val OnSurfaceVariant = Color(0xFF6D5A62)
    val SurfaceVariant = Color(0xFFFFE4EC)
    val PrimaryContainer = Color(0xFFFCE7F3)
    val OnPrimaryContainer = Color(0xFF5C4B51)
    val SecondaryContainer = Color(0xFFF3E8FF)
    val Outline = Color(0xFFFBCFE8)
    /** Badge “Popular” on paywall / highlights */
    val PopularBadge = Color(0xFFC026D3)
}

/** Primary horizontal gradient for CTAs (Language Next, Apply, paywall weekly, etc.). */
val StrawberryCtaGradientBrush: Brush
    get() = Brush.horizontalGradient(
        listOf(StrawberryMilk.Primary, StrawberryMilk.Secondary),
    )
