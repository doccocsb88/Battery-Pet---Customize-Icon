package dev.hai.emojibattery.ui.theme

import androidx.compose.ui.graphics.Brush

/**
 * Backward-compatible alias for legacy direct token references.
 * Values now resolve to the global Ocean Serenity theme palette.
 */
object StrawberryMilk {
    val Background = OceanSerenity.Background
    val Surface = OceanSerenity.Surface
    val Primary = OceanSerenity.Primary
    val OnPrimary = OceanSerenity.OnPrimary
    val Secondary = OceanSerenity.Secondary
    val OnSecondary = OceanSerenity.OnSecondary
    val Tertiary = OceanSerenity.Tertiary
    val OnSurface = OceanSerenity.OnSurface
    val OnSurfaceVariant = OceanSerenity.OnSurfaceVariant
    val SurfaceVariant = OceanSerenity.SurfaceVariant
    val PrimaryContainer = OceanSerenity.PrimaryContainer
    val OnPrimaryContainer = OceanSerenity.OnPrimaryContainer
    val SecondaryContainer = OceanSerenity.SecondaryContainer
    val Outline = OceanSerenity.Outline
    /** Badge “Popular” on paywall / highlights */
    val PopularBadge = OceanSerenity.PopularBadge
}

/** Primary horizontal gradient for CTAs (Language Next, Apply, paywall weekly, etc.). */
val StrawberryCtaGradientBrush: Brush
    get() = Brush.horizontalGradient(
        listOf(OceanSerenity.Primary, OceanSerenity.Secondary),
    )
