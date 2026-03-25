package dev.hai.emojibattery.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Design tokens for the app palette.
 * Primary CTA gradient: soft blue -> slate blue.
 */
object StrawberryMilk {
    val Background = Color(0xFFFBF9F8)
    val Surface = Color.White
    val Primary = Color(0xFF8FB6D4)
    val OnPrimary = Color.White
    val Secondary = Color(0xFF3C637E)
    val OnSecondary = Color.White
    val Tertiary = Color(0xFF76916B)
    val OnSurface = Color(0xFF5C4B51)
    val OnSurfaceVariant = Color(0xFF7B6E75)
    val SurfaceVariant = Color(0xFFF2F2F2)
    val PrimaryContainer = Color(0xFFEAF3FA)
    val OnPrimaryContainer = Color(0xFF3C637E)
    val SecondaryContainer = Color(0xFFDCE8F3)
    val Outline = Color(0xFFD8DDE2)
    /** Badge “Popular” on paywall / highlights */
    val PopularBadge = Color(0xFF3C637E)
}

/** Primary horizontal gradient for CTAs (Language Next, Apply, paywall weekly, etc.). */
val StrawberryCtaGradientBrush: Brush
    get() = Brush.horizontalGradient(
        listOf(StrawberryMilk.Primary, StrawberryMilk.Secondary),
    )
