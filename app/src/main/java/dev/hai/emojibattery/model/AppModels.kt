package dev.hai.emojibattery.model

enum class MainSection(val title: String) {
    Home("Home"),
    Customize("Customize"),
    Gesture("Gesture"),
    Achievement("Achievement"),
}

enum class StatusBarTab(val title: String) {
    Battery("Battery"),
    Emoji("Emoji"),
    Theme("Theme"),
    Settings("Settings"),
}

enum class CustomizeEntry(val title: String, val subtitle: String) {
    Wifi("Wi-Fi", "Status bar indicator styling"),
    Data("Data", "Network badge and meter"),
    Signal("Signal", "Cellular icon styling"),
    Airplane("Airplane", "Quick symbol override"),
    Hotspot("Hotspot", "Hotspot badge look"),
    Ringer("Ringer", "Silent and vibrate states"),
    Charge("Battery", "Battery body, color, percentage"),
    Emotion("Emoji", "Emoji battery face and size"),
    DateTime("Date & Time", "Clock typography and layout"),
    Theme("Theme", "Preset templates for the whole bar"),
    Settings("Settings", "Animation and visibility"),
}

enum class GestureTrigger(val title: String, val subtitle: String) {
    SingleTap("Single tap action", "Quick action when the status bar is tapped once"),
    SwipeLeftToRight("Swipe left to right action", "Horizontal swipe from left to right"),
    SwipeRightToLeft("Swipe right to left action", "Horizontal swipe from right to left"),
    SwipeTopToBottom("Swipe up to down", "Vertical swipe from the top edge downward"),
    LongPress("Long press action", "Hold on the status bar to trigger an action"),
}

enum class GestureAction(val title: String, val subtitle: String) {
    None("None", "No action"),
    OpenCustomize("Open customize", "Jump into the battery and emoji editor"),
    OpenEmojiSticker("Open emoji sticker", "Open sticker overlay configuration"),
    OpenBatteryTroll("Open battery troll", "Open prank battery templates"),
    OpenSearch("Open search", "Open the search landing flow"),
    ToggleOverlay("Toggle overlay", "Enable or disable the active overlay quickly"),
}

data class BatteryPreset(
    val id: String,
    val name: String,
    val body: String,
)

data class EmojiPreset(
    val id: String,
    val name: String,
    val glyph: String,
)

data class ThemePreset(
    val id: String,
    val name: String,
    val accent: Long,
    val background: Long,
)

data class StickerPreset(
    val id: String,
    val name: String,
    val glyph: String,
    val premium: Boolean = false,
    val animated: Boolean = false,
)

data class StickerPlacement(
    val stickerId: String,
    val size: Float = 0.5f,
    val speed: Float = 0.5f,
)

data class FeatureConfig(
    val enabled: Boolean = true,
    val intensity: Float = 0.55f,
    val variant: String,
)

data class ContentTemplate(
    val id: String,
    val title: String,
    val summary: String,
    val tag: String,
    val accentGlyph: String,
    val premium: Boolean = false,
)

data class BatteryTrollTemplate(
    val id: String,
    val title: String,
    val summary: String,
    val prankMessage: String,
    val accentGlyph: String,
)

data class AchievementTask(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val progress: Int = 0,
    val claimed: Boolean = false,
    val reward: String,
)

data class SearchTemplate(
    val id: String,
    val title: String,
    val summary: String,
    val route: String,
    val category: String,
    val glyph: String,
    val tags: List<String>,
    val premium: Boolean = false,
    val animated: Boolean = false,
)

data class PaywallState(
    val featureKey: String,
    val title: String,
    val message: String,
)

data class BatteryIconConfig(
    val batteryPresetId: String,
    val emojiPresetId: String,
    val themePresetId: String,
    val batteryPercentScale: Float = 0.56f,
    val emojiScale: Float = 0.64f,
    val showPercentage: Boolean = true,
    val animateCharge: Boolean = true,
    val showStroke: Boolean = true,
    val accentColor: Long,
    val backgroundColor: Long,
)

data class AppUiState(
    val splashDone: Boolean = false,
    val languageChosen: Boolean = false,
    val selectedLanguage: String = "English",
    val accessibilityGranted: Boolean = false,
    val activeMainSection: MainSection = MainSection.Home,
    val activeStatusBarTab: StatusBarTab = StatusBarTab.Battery,
    val editingConfig: BatteryIconConfig,
    val appliedConfig: BatteryIconConfig,
    val featureConfigs: Map<CustomizeEntry, FeatureConfig> = SampleCatalog.defaultFeatureConfigs,
    val stickerPlacements: List<StickerPlacement> = emptyList(),
    val selectedStickerId: String? = null,
    val stickerOverlayEnabled: Boolean = false,
    val gestureEnabled: Boolean = false,
    val vibrateFeedback: Boolean = true,
    val gestureActions: Map<GestureTrigger, GestureAction> = SampleCatalog.defaultGestureActions,
    val searchQuery: String = "",
    val selectedRealTimeTemplateId: String = SampleCatalog.realTimeTemplates.first().id,
    val selectedBatteryTrollTemplateId: String = SampleCatalog.batteryTrollTemplates.first().id,
    val trollMessage: String = SampleCatalog.batteryTrollTemplates.first().prankMessage,
    val trollAutoDrop: Boolean = true,
    val trollOverlayEnabled: Boolean = false,
    val tutorialCompleted: Boolean = false,
    val protectFromRecentApps: Boolean = false,
    val premiumUnlocked: Boolean = false,
    val unlockedFeatureKeys: Set<String> = emptySet(),
    val paywallState: PaywallState? = null,
    val achievements: List<AchievementTask> = SampleCatalog.defaultAchievements,
    val infoMessage: String? = null,
)

object SampleCatalog {
    val batteryPresets = listOf(
        BatteryPreset("pill", "Pill", "▰▰▰▱"),
        BatteryPreset("rounded", "Rounded", "▣▣▣▢"),
        BatteryPreset("block", "Block", "███░"),
        BatteryPreset("thin", "Thin", "▭▭▭▱"),
        BatteryPreset("capsule", "Capsule", "◼◼◼◻"),
    )

    val emojiPresets = listOf(
        EmojiPreset("spark", "Spark", "✨"),
        EmojiPreset("smile", "Smile", "😊"),
        EmojiPreset("cool", "Cool", "😎"),
        EmojiPreset("fire", "Fire", "🔥"),
        EmojiPreset("robot", "Robot", "🤖"),
        EmojiPreset("cat", "Cat", "🐱"),
    )

    val themePresets = listOf(
        ThemePreset("blush", "Blush", accent = 0xFFEA6A9AL, background = 0xFFFFF3F8L),
        ThemePreset("mint", "Mint", accent = 0xFF17A398L, background = 0xFFF0FFFBL),
        ThemePreset("sun", "Sun", accent = 0xFFF59E0BL, background = 0xFFFFF8E7L),
        ThemePreset("sky", "Sky", accent = 0xFF4C7DFFL, background = 0xFFF3F7FFL),
    )

    val stickerPresets = listOf(
        StickerPreset("sparkle_cat", "Sparkle Cat", "🐱", animated = true),
        StickerPreset("cool_ghost", "Cool Ghost", "👻", animated = true),
        StickerPreset("sun_blob", "Sun Blob", "🌞"),
        StickerPreset("rocket_face", "Rocket", "🚀", animated = true),
        StickerPreset("love_frog", "Love Frog", "🐸"),
        StickerPreset("robot_wave", "Robot", "🤖", premium = true),
        StickerPreset("panda_pop", "Panda", "🐼"),
        StickerPreset("star_orbit", "Star", "⭐", premium = true, animated = true),
    )

    val defaultTheme = themePresets.first()

    val defaultConfig = BatteryIconConfig(
        batteryPresetId = batteryPresets.first().id,
        emojiPresetId = emojiPresets.first().id,
        themePresetId = defaultTheme.id,
        accentColor = defaultTheme.accent,
        backgroundColor = defaultTheme.background,
    )

    val featureVariants = mapOf(
        CustomizeEntry.Wifi to listOf("Rounded", "Mono", "Bold"),
        CustomizeEntry.Data to listOf("4G", "LTE", "Bars"),
        CustomizeEntry.Signal to listOf("Thin", "Filled", "Minimal"),
        CustomizeEntry.Airplane to listOf("Outline", "Solid", "Tiny"),
        CustomizeEntry.Hotspot to listOf("Ripple", "Dot", "Ring"),
        CustomizeEntry.Ringer to listOf("Bell", "Mute", "Wave"),
        CustomizeEntry.Charge to listOf("Pulse", "Percent", "Capsule"),
        CustomizeEntry.Emotion to listOf("Cute", "Calm", "Hype"),
        CustomizeEntry.DateTime to listOf("Compact", "Stacked", "Wide"),
        CustomizeEntry.Theme to listOf("Blush", "Mint", "Sky"),
        CustomizeEntry.Settings to listOf("Balanced", "Quiet", "Sharp"),
    )

    val defaultFeatureConfigs = CustomizeEntry.entries.associateWith { entry ->
        FeatureConfig(variant = featureVariants[entry]?.first().orEmpty())
    }

    val gestureActionOptions = GestureAction.entries

    val defaultGestureActions = linkedMapOf(
        GestureTrigger.SingleTap to GestureAction.OpenCustomize,
        GestureTrigger.SwipeLeftToRight to GestureAction.OpenEmojiSticker,
        GestureTrigger.SwipeRightToLeft to GestureAction.OpenSearch,
        GestureTrigger.SwipeTopToBottom to GestureAction.ToggleOverlay,
        GestureTrigger.LongPress to GestureAction.OpenBatteryTroll,
    )

    val realTimeTemplates = listOf(
        ContentTemplate("mood_flip", "Mood Flip", "Reactive emoji pack that changes by charge level.", "Real Time", "⚡"),
        ContentTemplate("tiny_forecast", "Tiny Forecast", "Compact weather mood on the battery cluster.", "Utility", "⛅"),
        ContentTemplate("cat_diary", "Cat Diary", "Battery face swaps between sleepy and playful cats.", "Cute", "🐱", premium = true),
        ContentTemplate("office_burn", "Office Burn", "Turns low battery into a chaotic workday meter.", "Funny", "🔥"),
    )

    val batteryTrollTemplates = listOf(
        BatteryTrollTemplate("fake_1", "1% Panic", "Pretend the battery is almost dead to prank a friend.", "1%", "😵"),
        BatteryTrollTemplate("fake_100", "Forever Full", "Lock the battery into an unreal always-full state.", "100%", "😎"),
        BatteryTrollTemplate("heat_warning", "Overheat Warning", "Show a dramatic overheating battery warning.", "HOT", "🥵"),
        BatteryTrollTemplate("charging_loop", "Infinite Charging", "Fake a charging loop that never finishes.", "CHG", "🔁"),
    )

    val trollMessageOptions = batteryTrollTemplates.map { it.prankMessage }.distinct()

    val defaultAchievements = listOf(
        AchievementTask(
            id = "apply_status_bar",
            title = "Apply First Theme",
            description = "Apply any status bar customization once.",
            target = 1,
            reward = "Unlock a premium-looking accent preset",
        ),
        AchievementTask(
            id = "save_sticker",
            title = "Sticker Starter",
            description = "Save the sticker overlay for the first time.",
            target = 1,
            reward = "Unlock one extra sticker slot",
        ),
        AchievementTask(
            id = "gesture_mapper",
            title = "Gesture Mapper",
            description = "Assign three different gesture actions.",
            target = 3,
            reward = "Unlock gesture-only quick actions",
        ),
        AchievementTask(
            id = "template_explorer",
            title = "Template Explorer",
            description = "Try two content templates from Real Time or Battery Troll.",
            target = 2,
            reward = "Unlock one prank template",
        ),
    )

    val searchTemplates = listOf(
        SearchTemplate(
            id = "statusbar_editor",
            title = "Status Bar Custom",
            summary = "Unified editor for battery, emoji, theme, and settings.",
            route = "statusbar_custom",
            category = "recommend",
            glyph = "🔋",
            tags = listOf("battery", "emoji", "status bar", "theme", "custom"),
        ),
        SearchTemplate(
            id = "legacy_battery",
            title = "Legacy Battery Flow",
            summary = "Classic battery body and emoji picker.",
            route = "legacy_battery",
            category = "recommend",
            glyph = "🪫",
            tags = listOf("battery", "legacy", "icon", "percent"),
        ),
        SearchTemplate(
            id = "emoji_sticker",
            title = "Emoji Sticker",
            summary = "Floating sticker overlay with size and speed controls.",
            route = "emoji_sticker",
            category = "recommend",
            glyph = "✨",
            tags = listOf("sticker", "emoji", "overlay", "cute", "gif"),
            animated = true,
        ),
        SearchTemplate(
            id = "battery_troll",
            title = "Battery Troll",
            summary = "Prank battery overlays and fake percentages.",
            route = "battery_troll",
            category = "recommend",
            glyph = "😈",
            tags = listOf("troll", "prank", "fake battery", "warning"),
        ),
        SearchTemplate(
            id = "real_time",
            title = "Real Time",
            summary = "Reactive battery themes and content templates.",
            route = "real_time",
            category = "recommend",
            glyph = "⚡",
            tags = listOf("realtime", "mood", "template", "dynamic"),
            premium = true,
        ),
        SearchTemplate(
            id = "gesture",
            title = "Gesture",
            summary = "Map status-bar gestures to custom actions.",
            route = "gesture",
            category = "tools",
            glyph = "☝",
            tags = listOf("gesture", "swipe", "tap", "shortcut"),
        ),
        SearchTemplate(
            id = "achievement",
            title = "Achievement",
            summary = "Challenge list, reward claiming, and unlock tracking.",
            route = "achievement",
            category = "tools",
            glyph = "🏆",
            tags = listOf("achievement", "reward", "claim", "milestone"),
        ),
        SearchTemplate(
            id = "settings",
            title = "Settings",
            summary = "Language, tutorial, privacy, and update actions.",
            route = "settings",
            category = "tools",
            glyph = "⚙",
            tags = listOf("settings", "language", "privacy", "update"),
        ),
    ) + CustomizeEntry.entries.map { entry ->
        SearchTemplate(
            id = "feature_${entry.name.lowercase()}",
            title = entry.title,
            summary = entry.subtitle,
            route = "feature/${entry.title}",
            category = "feature",
            glyph = when (entry) {
                CustomizeEntry.Wifi -> "📶"
                CustomizeEntry.Data -> "📡"
                CustomizeEntry.Signal -> "📳"
                CustomizeEntry.Airplane -> "✈"
                CustomizeEntry.Hotspot -> "🛜"
                CustomizeEntry.Ringer -> "🔔"
                CustomizeEntry.Charge -> "🔋"
                CustomizeEntry.Emotion -> "😊"
                CustomizeEntry.DateTime -> "🕒"
                CustomizeEntry.Theme -> "🎨"
                CustomizeEntry.Settings -> "⚙"
            },
            tags = listOf(entry.title.lowercase(), entry.subtitle.lowercase(), "customize", "status bar"),
        )
    }

    val mostSearchedTags = listOf(
        "battery",
        "emoji",
        "sticker",
        "gesture",
        "theme",
        "troll",
        "realtime",
        "wifi",
        "signal",
    )

    val recommendedSearchTemplates = searchTemplates.filter { it.category == "recommend" }.take(6)

    const val FREE_STICKER_SLOTS = 1
    const val PREMIUM_STICKER_SLOTS = 4
    const val REWARD_EXTRA_STICKER_SLOTS = 2

    const val FEATURE_EXTRA_STICKER_SLOT = "slot:extra"
    const val FEATURE_PREMIUM_REALTIME_CAT_DIARY = "template:cat_diary"
}
