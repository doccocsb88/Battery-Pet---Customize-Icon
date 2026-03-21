package dev.hai.emojibattery.ui.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Language : AppRoute("language")
    data object Onboarding : AppRoute("onboarding")
    data object Tutorial : AppRoute("tutorial")
    data object Home : AppRoute("home")
    data object Customize : AppRoute("customize")
    data object Gesture : AppRoute("gesture")
    data object Achievement : AppRoute("achievement")
    data object StatusBarCustom : AppRoute("statusbar_custom")
    data object LegacyBattery : AppRoute("legacy_battery")
    data object Search : AppRoute("search")
    data object Settings : AppRoute("settings")
    data object Feedback : AppRoute("feedback")
    data object Paywall : AppRoute("paywall")
    data object Legal : AppRoute("legal/{document}") {
        fun create(document: String): String = "legal/$document"
    }
    data object RealTime : AppRoute("real_time")
    data object BatteryTroll : AppRoute("battery_troll")
    data object EmojiSticker : AppRoute("emoji_sticker")
    data object FeatureDetail : AppRoute("feature/{feature}") {
        fun create(feature: String): String = "feature/$feature"
    }
}
