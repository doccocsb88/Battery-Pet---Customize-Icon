package dev.hai.emojibattery.model

import co.q7labs.co.emoji.R

enum class MainSection(val title: String) {
    Home("Home"),
    Customize("Customize"),
    Gesture("Gesture"),
    Settings("Settings"),
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
    SingleTap("Single-tap action", "Tap once on the status bar"),
    SwipeLeftToRight("Swipe left to right action", "Horizontal swipe along the status bar"),
    SwipeRightToLeft("Swipe right to left action", "Horizontal swipe along the status bar"),
    SwipeTopToBottom("Swipe up to down", "Vertical swipe from the top edge downward"),
    LongPress("Long press action", "Press and hold on the status bar"),
}

/** System-style actions aligned with the original app gesture picker (see design screenshots). */
enum class GestureAction(val title: String, val subtitle: String) {
    OpenApp("Open App", "Launch a chosen application"),
    DoNothing("Do nothing", "No action"),
    BackAction("Back Action", "Navigate back"),
    HomeAction("Home Action", "Open the home screen"),
    RecentAction("Recent Action", "Open recent apps"),
    NotificationCenter("Notification center", "Open notifications"),
    ControlCenter("Control center", "Open quick settings / control center"),
    PowerSourceOptions("Power source options", "Battery and power settings"),
    LockScreen("Lock Screen", "Lock the device"),
    TakeScreenshot("Take Screenshot", "Capture the current screen"),
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
    /** Volio CDN thumbnail (PNG/GIF); shown in grid and status overlay when present. */
    val thumbnailUrl: String? = null,
    /** Lottie JSON URL from [custom_fields.content] when applicable. */
    val lottieUrl: String? = null,
    val remotePhotoUrl: String? = null,
)

data class StickerPlacement(
    val stickerId: String,
    val size: Float = 0.5f,
    val speed: Float = 0.5f,
    val rotation: Float = 0f,
    /** Normalized X anchor inside preview/overlay region (0f..1f). */
    val offsetX: Float = 0.5f,
    /** Normalized Y anchor inside preview/overlay region (0f..1f). */
    val offsetY: Float = 0.5f,
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
    val premium: Boolean = false,
    val thumbnailUrl: String? = null,
    val lottieUrl: String? = null,
    /** Dedicated preview for Show Emoji row (Volio custom_fields.emoji). */
    val emojiThumbnailUrl: String? = null,
    /** Dedicated preview for Show Battery row (Volio custom_fields.battery). */
    val batteryThumbnailUrl: String? = null,
    /** Extracted from contentZip (expected 5 entries). */
    val emojiOptionsUrls: List<String> = emptyList(),
    /** Extracted from contentZip (expected 5 entries). */
    val batteryOptionsUrls: List<String> = emptyList(),
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

data class HomeBatteryItem(
    val id: String,
    val categoryId: String,
    val title: String,
    val previewRes: Int,
    /** Remote preview from Volio CDN (original app); when set, UI prefers this over [previewRes]. */
    val thumbnailUrl: String? = null,
    /** Art for Battery tab / battery column — from [VolioItemCustomFieldsDto.battery], else [thumbnailUrl]. */
    val batteryArtUrl: String? = null,
    /** Art for Emoji tab / emoji column — from [VolioItemCustomFieldsDto.emoji], else [thumbnailUrl]. */
    val emojiArtUrl: String? = null,
    /** Full background image for Theme → background template (Volio `photo`). */
    val backgroundPhotoUrl: String? = null,
    val premium: Boolean = false,
    val animated: Boolean = false,
)

data class HomeCategory(
    val id: String,
    val title: String,
    val remotePath: String = "items",
    val items: List<HomeBatteryItem>,
)

/** Tab metadata for the home horizontal category strip (maps to original CategoryEmojiBatteryModel id/title). */
data class HomeCategoryTab(
    val id: String,
    val title: String,
)

data class PaywallState(
    val featureKey: String,
    val title: String,
    val message: String,
)

data class OnboardingPage(
    val id: String,
    val title: String,
    val body: String,
    val accentGlyph: String,
)

data class FeedbackReason(
    val id: String,
    val label: String,
)

data class BatteryIconConfig(
    val batteryPresetId: String,
    val emojiPresetId: String,
    val themePresetId: String,
    val statusBarHeight: Float = 0.2f,
    val leftMargin: Float = 0.12f,
    val rightMargin: Float = 0.12f,
    val batteryPercentScale: Float = 0.56f,
    val emojiScale: Float = 0.64f,
    val showPercentage: Boolean = true,
    val animateCharge: Boolean = true,
    val showStroke: Boolean = true,
    val accentColor: Long,
    val backgroundColor: Long,
    /**
     * Optional full-width status-bar background image URL (legacy / remote).
     * Prefer [backgroundTemplateDrawableRes] for the Theme tab (matches original asset templates).
     */
    val backgroundTemplatePhotoUrl: String? = null,
    /**
     * Local background template preview (Theme tab) — `R.drawable.theme_bg_template_XX`,
     * mirroring original `ColorTemplateLocalModel` indices 0–19.
     */
    val backgroundTemplateDrawableRes: Int? = null,
)

data class AppUiState(
    val splashDone: Boolean = false,
    val languageChosen: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val onboardingPage: Int = 0,
    /** BCP 47 tag aligned with [dev.hai.emojibattery.locale.SUPPORTED_APP_LANGUAGES]. */
    val selectedLocaleTag: String = "en",
    val accessibilityGranted: Boolean = false,
    val activeMainSection: MainSection = MainSection.Home,
    val selectedHomeCategoryId: String = SampleCatalog.homeCategories.first().id,
    /** Category strip; filled at startup from [SampleCatalog] (mirrors loading all categories in the original HomeViewModel). */
    val homeTabs: List<HomeCategoryTab> = emptyList(),
    /** Lazy-loaded, shuffled items per category id (mirrors SubHome per-category load). */
    val homeItemsByCategoryId: Map<String, List<HomeBatteryItem>> = emptyMap(),
    /** True while reading offline home catalog metadata before the first category list render. */
    val padCatalogLoading: Boolean = false,
    val homeCategoryLoadingId: String? = null,
    /**
     * Volio store emoji-battery rows for the Status Bar editor (decompiled: [EmojiBatteryRepository] + [hungvv.OS]).
     * Shared by Battery / Emoji tabs like the original [EmojiBatteryModel] list.
     */
    val statusBarCatalogItems: List<HomeBatteryItem> = emptyList(),
    val activeStatusBarTab: StatusBarTab = StatusBarTab.Battery,
    val editingConfig: BatteryIconConfig,
    val appliedConfig: BatteryIconConfig,
    val featureConfigs: Map<CustomizeEntry, FeatureConfig> = SampleCatalog.defaultFeatureConfigs,
    val stickerPlacements: List<StickerPlacement> = emptyList(),
    val selectedStickerId: String? = null,
    val showStickerAdjustmentPanel: Boolean = false,
    val stickerOverlayEnabled: Boolean = false,
    val gestureEnabled: Boolean = false,
    val vibrateFeedback: Boolean = true,
    val gestureActions: Map<GestureTrigger, GestureAction> = SampleCatalog.defaultGestureActions,
    val searchQuery: String = "",
    val selectedRealTimeTemplateId: String = SampleCatalog.realTimeTemplates.first().id,
    val selectedBatteryTrollTemplateId: String = SampleCatalog.batteryTrollTemplates.first().id,
    val trollMessage: String = SampleCatalog.batteryTrollTemplates.first().prankMessage,
    val trollFeatureEnabled: Boolean = true,
    val trollUseRealBattery: Boolean = false,
    val trollShowPercentage: Boolean = true,
    /** Slider value displayed as dp in the original layout. */
    val trollPercentageSizeDp: Int = 5,
    /** Slider value displayed as dp in the original layout. */
    val trollEmojiSizeDp: Int = 40,
    val trollRandomizedMode: Boolean = false,
    val trollShowEmoji: Boolean = true,
    // Original app ties "more funny each time screen turned on/off" to randomized mode.
    val trollAutoDrop: Boolean = false,
    val trollSelectedEmojiUrl: String? = null,
    val trollSelectedBatteryUrl: String? = null,
    val trollOverlayEnabled: Boolean = false,
    val tutorialCompleted: Boolean = false,
    val tutorialPage: Int = 0,
    val protectFromRecentApps: Boolean = false,
    val premiumUnlocked: Boolean = false,
    val unlockedFeatureKeys: Set<String> = emptySet(),
    val paywallState: PaywallState? = null,
    val achievements: List<AchievementTask> = SampleCatalog.defaultAchievements,
    val feedbackReasons: Set<String> = emptySet(),
    val feedbackNote: String = "",
    val lastFeedbackSubmitted: Boolean = false,
    val ratingSelection: Int = 0,
    val infoMessage: String? = null,
    /** Remote Volio sticker library (Emoji sticker scope); merged at UI with [SampleCatalog.stickerPresets]. */
    val stickerCatalogRemote: List<StickerPreset> = emptyList(),
    val stickerCatalogLoading: Boolean = false,
    /** Remote Volio battery-troll templates when [VolioConstants.BATTERY_TROLL_PARENT_ID] is configured. */
    val batteryTrollCatalogRemote: List<BatteryTrollTemplate> = emptyList(),
    val batteryTrollCatalogLoading: Boolean = false,
)

/** Resolves a sticker from bundled samples or the remote Volio sticker catalog. */
fun AppUiState.stickerPresetForId(stickerId: String): StickerPreset? =
    SampleCatalog.stickerPresets.firstOrNull { it.id == stickerId }
        ?: stickerCatalogRemote.firstOrNull { it.id == stickerId }

/** Bundled samples or remote Volio battery troll row. */
fun AppUiState.batteryTrollTemplateForId(templateId: String): BatteryTrollTemplate? =
    SampleCatalog.batteryTrollTemplates.firstOrNull { it.id == templateId }
        ?: batteryTrollCatalogRemote.firstOrNull { it.id == templateId }

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

    val homeCategories = listOf(
        HomeCategory("hot", "HOT", items = buildHomeItems("hot", listOf(
            "Gudetama Toast" to R.drawable.ic_item_charge,
            "Sunset Vibes" to R.drawable.ic_item_data,
            "Jerry Power" to R.drawable.ic_item_emotion,
            "Shark Cruise" to R.drawable.ic_item_wifi,
            "Stitch Charge" to R.drawable.ic_item_charge,
            "Palm Beach" to R.drawable.ic_item_date_time,
            "My Melody" to R.drawable.ic_item_hotspot,
            "Fox Battery" to R.drawable.ic_item_charge,
            "Cosmo Boy" to R.drawable.ic_item_signal,
            "Pink Bear" to R.drawable.ic_item_emotion,
            "Croc Jet" to R.drawable.ic_item_signal,
            "Pompompurin" to R.drawable.ic_item_date_time,
        ))),
        HomeCategory("sanrio", "Sanrio", items = buildHomeItems("sanrio", listOf(
            "Hello Kitty" to R.drawable.ic_item_hotspot,
            "My Melody" to R.drawable.ic_item_emotion,
            "Kuromi" to R.drawable.ic_item_signal,
            "Cinnamoroll" to R.drawable.ic_item_wifi,
            "Pochacco" to R.drawable.ic_item_data,
            "Keroppi" to R.drawable.ic_item_airplane,
            "Badtz-Maru" to R.drawable.ic_item_ringer,
            "Pompompurin" to R.drawable.ic_item_charge,
            "Little Twin Stars" to R.drawable.ic_item_date_time,
        ))),
        HomeCategory("avatar", "Avatar", items = buildHomeItems("avatar", listOf(
            "Blue Spirit" to R.drawable.ic_item_emotion,
            "Water Tribe" to R.drawable.ic_item_wifi,
            "Fire Nation" to R.drawable.ic_item_data,
            "Aang Air" to R.drawable.ic_item_airplane,
            "Sokka Signal" to R.drawable.ic_item_signal,
            "Katara Flow" to R.drawable.ic_item_hotspot,
            "Toph Metal" to R.drawable.ic_item_charge,
            "Appa Ride" to R.drawable.ic_item_date_time,
            "Momo Vibe" to R.drawable.ic_item_ringer,
        ))),
        HomeCategory("zootopia", "Zootopia", items = buildHomeItems("zootopia", listOf(
            "Judy Hopps" to R.drawable.ic_item_emotion,
            "Nick Wilde" to R.drawable.ic_item_wifi,
            "Flash Speed" to R.drawable.ic_item_data,
            "Chief Bogo" to R.drawable.ic_item_signal,
            "Clawhauser" to R.drawable.ic_item_hotspot,
            "Bellwether" to R.drawable.ic_item_ringer,
            "Duke Weaselton" to R.drawable.ic_item_airplane,
            "Gazelle" to R.drawable.ic_item_date_time,
            "City Patrol" to R.drawable.ic_item_charge,
        ))),
        HomeCategory("actor", "Actor", items = buildHomeItems("actor", listOf(
            "Cinema Night" to R.drawable.ic_item_date_time,
            "Red Carpet" to R.drawable.ic_item_data,
            "Star Power" to R.drawable.ic_item_charge,
            "Spotlight" to R.drawable.ic_item_signal,
            "Action Hero" to R.drawable.ic_item_emotion,
            "Director Cut" to R.drawable.ic_item_ringer,
            "Award Season" to R.drawable.ic_item_hotspot,
            "Golden Frame" to R.drawable.ic_item_wifi,
        ))),
        HomeCategory("kpop", "Kpop", items = buildHomeItems("kpop", listOf(
            "Stage Glow" to R.drawable.ic_item_emotion,
            "Neon Lightstick" to R.drawable.ic_item_signal,
            "Encore Night" to R.drawable.ic_item_wifi,
            "Bubble Pop" to R.drawable.ic_item_data,
            "Candy Heart" to R.drawable.ic_item_hotspot,
            "Idol Wave" to R.drawable.ic_item_ringer,
            "Midnight Dance" to R.drawable.ic_item_charge,
            "Comback Day" to R.drawable.ic_item_date_time,
        ), premiumEveryThird = true)),
        HomeCategory("stitch", "Stitch", items = buildHomeItems("stitch", listOf(
            "Surf Stitch" to R.drawable.ic_item_wifi,
            "Blue Buddy" to R.drawable.ic_item_charge,
            "Aloha Mood" to R.drawable.ic_item_date_time,
            "Island Bloom" to R.drawable.ic_item_hotspot,
            "Space Stitch" to R.drawable.ic_item_signal,
            "Experiment 626" to R.drawable.ic_item_data,
            "Cuddle Chaos" to R.drawable.ic_item_emotion,
            "Ohana" to R.drawable.ic_item_ringer,
        ))),
        HomeCategory("shin", "Shin", items = buildHomeItems("shin", listOf(
            "Crayon Wink" to R.drawable.ic_item_emotion,
            "Kasukabe Ride" to R.drawable.ic_item_airplane,
            "Chocobi" to R.drawable.ic_item_charge,
            "Action Mask" to R.drawable.ic_item_signal,
            "Nene Chan" to R.drawable.ic_item_hotspot,
            "Bo Chan" to R.drawable.ic_item_wifi,
            "Shiro" to R.drawable.ic_item_data,
            "Misae Rage" to R.drawable.ic_item_ringer,
        ))),
        HomeCategory("xmas", "X-Mas", items = buildHomeItems("xmas", listOf(
            "Candy Cane" to R.drawable.ic_item_charge,
            "Snow Globe" to R.drawable.ic_item_date_time,
            "Santa Bell" to R.drawable.ic_item_ringer,
            "Gift Box" to R.drawable.ic_item_hotspot,
            "Reindeer Dash" to R.drawable.ic_item_signal,
            "Holiday Tree" to R.drawable.ic_item_wifi,
            "Warm Cocoa" to R.drawable.ic_item_emotion,
            "Spark Lights" to R.drawable.ic_item_data,
        ), animatedEverySecond = true)),
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
        GestureTrigger.SingleTap to GestureAction.DoNothing,
        GestureTrigger.SwipeTopToBottom to GestureAction.NotificationCenter,
        GestureTrigger.SwipeLeftToRight to GestureAction.ControlCenter,
        GestureTrigger.SwipeRightToLeft to GestureAction.BackAction,
        GestureTrigger.LongPress to GestureAction.DoNothing,
    )

    val onboardingPages = listOf(
        OnboardingPage(
            id = "customize",
            title = "Unique Emoji Battery",
            body = "Transform your phone's battery icon with playful emoji options to express your personality",
            accentGlyph = "",
        ),
        OnboardingPage(
            id = "gesture",
            title = "Easy Customization",
            body = "Choose styles, colors, and effects in just a few taps.",
            accentGlyph = "",
        ),
        OnboardingPage(
            id = "sticker",
            title = "Animated Stickers",
            body = "Place fun battery-themed stickers right on your screen.",
            accentGlyph = "",
        ),
    )

    val tutorialPages = listOf(
        OnboardingPage(
            id = "permission",
            title = "How To Request Permission",
            body = "Enable the accessibility bridge so the app can draw and refresh the custom status bar overlay.",
            accentGlyph = "🛡",
        ),
        OnboardingPage(
            id = "question",
            title = "Why Accessibility Is Needed",
            body = "The overlay sits above the system bar and can trigger your selected shortcuts without collecting personal content.",
            accentGlyph = "❔",
        ),
        OnboardingPage(
            id = "gesture_guide",
            title = "How To Use Gestures",
            body = "Bind tap, swipe, and long press actions, then test them from the gesture screen after the service is enabled.",
            accentGlyph = "👉",
        ),
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

    val feedbackReasons = listOf(
        FeedbackReason("ads", "I don't want to see ads"),
        FeedbackReason("how_to_use", "I don't know how to use it"),
        FeedbackReason("more_emoji", "I want more emojis"),
        FeedbackReason("other", "Other"),
    )

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
    const val PREMIUM_STICKER_SLOTS = 6
    const val REWARD_EXTRA_STICKER_SLOTS = 2

    const val FEATURE_EXTRA_STICKER_SLOT = "reward:extra_sticker_slot"
    const val FEATURE_PREMIUM_REALTIME_CAT_DIARY = "reward:cat_diary"

    private fun buildHomeItems(
        categoryId: String,
        entries: List<Pair<String, Int>>,
        premiumEveryThird: Boolean = false,
        animatedEverySecond: Boolean = false,
    ): List<HomeBatteryItem> = entries.mapIndexed { index, (title, previewRes) ->
        HomeBatteryItem(
            id = "$categoryId-${index + 1}",
            categoryId = categoryId,
            title = title,
            previewRes = previewRes,
            premium = premiumEveryThird && index % 3 == 2,
            animated = animatedEverySecond && index % 2 == 1,
        )
    }
}
