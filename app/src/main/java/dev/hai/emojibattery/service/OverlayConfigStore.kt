package dev.hai.emojibattery.service

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.BatteryIconConfig
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.FeatureConfig
import dev.hai.emojibattery.model.HomeBatteryItem
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.model.batteryTrollTemplateForId
import dev.hai.emojibattery.model.stickerPresetForId

data class OverlaySnapshot(
    val statusBarEnabled: Boolean,
    val batteryEmojiSource: String,
    val batteryText: String,
    val batteryBody: String,
    val emojiGlyph: String,
    val batteryArtUrl: String?,
    val batteryArtDrawableRes: Int?,
    val emojiArtUrl: String?,
    val emojiArtDrawableRes: Int?,
    val accentColor: Long,
    val backgroundColor: Long,
    /** Full-bleed status strip background when set (Theme background template). */
    val backgroundTemplatePhotoUrl: String?,
    /** Local drawable for theme template ([R.drawable.theme_bg_template_XX]); takes precedence over URL. */
    val backgroundTemplateDrawableRes: Int?,
    val stickerEnabled: Boolean,
    val stickerGlyph: String,
    /** When set, overlay draws the Volio thumbnail instead of [stickerGlyph]. */
    val stickerThumbnailUrl: String?,
    /** Optional Lottie JSON source for sticker overlay (preserved on apply). */
    val stickerLottieUrl: String?,
    val stickerSize: Float,
    val stickerRotation: Float,
    val stickerOffsetX: Float,
    val stickerOffsetY: Float,
    val trollEnabled: Boolean,
    val trollMessage: String,
    val trollBatteryArtUrl: String?,
    val trollEmojiArtUrl: String?,
    val trollBatteryOptionsUrls: List<String>,
    val trollEmojiOptionsUrls: List<String>,
    val trollShowEmoji: Boolean,
    val trollUseRealBattery: Boolean,
    val trollRandomizedMode: Boolean,
    val trollShowPercentage: Boolean,
    val trollPercentageSizeDp: Int,
    val trollEmojiSizeDp: Int,
    val realTimeEnabled: Boolean,
    val realTimeGlyph: String,
    val realTimeTitle: String,
    /** Original notch selector ID (-1 hide, 1..13 visible variants). */
    val notchTemplateId: Int,
    val notchColorVariant: String,
    val notchScale: Float,
    val notchOffsetX: Float,
    val notchOffsetY: Float,
    val statusBarHeight: Float,
    val leftMargin: Float,
    val rightMargin: Float,
    val batteryPercentScale: Float,
    val emojiScale: Float,
    val emojiAdjustmentScale: Float,
    val emojiOffsetX: Float,
    val emojiOffsetY: Float,
    val showPercentage: Boolean,
    val animateCharge: Boolean,
    val showStroke: Boolean,
    val animationEnabled: Boolean,
    val animationSizePercent: Int,
    /** Horizontal anchor for animation overlay (0f..1f). */
    val animationOffsetX: Float,
    val animationAssetPath: String?,
    val animationIsLottie: Boolean,
    val featureConfigs: Map<CustomizeEntry, FeatureConfig>,
)

data class AnimationOverlayPrefs(
    val enabled: Boolean,
    /** 0..100 scale from original seekbar style. */
    val sizePercent: Int,
    /** Horizontal anchor for animation overlay (0f..1f). */
    val offsetX: Float,
    val templateId: Int,
)

object OverlayConfigStore {
    const val BATTERY_EMOJI_SOURCE_STATUS_BAR_CUSTOM = "status_bar_custom"
    const val BATTERY_EMOJI_SOURCE_BATTERY_TROLL = "battery_troll"

    private const val PREFS = "overlay_config"
    private const val KEY_STATUS_ENABLED = "status_enabled"
    private const val KEY_BATTERY_EMOJI_SOURCE = "battery_emoji_source"
    private const val KEY_BATTERY_TEXT = "battery_text"
    private const val KEY_BATTERY_BODY = "battery_body"
    private const val KEY_EMOJI_GLYPH = "emoji_glyph"
    private const val KEY_BATTERY_ART_URL = "battery_art_url"
    private const val KEY_BATTERY_ART_DRAWABLE = "battery_art_drawable"
    private const val KEY_EMOJI_ART_URL = "emoji_art_url"
    private const val KEY_EMOJI_ART_DRAWABLE = "emoji_art_drawable"
    private const val KEY_ACCENT = "accent"
    private const val KEY_BACKGROUND = "background"
    private const val KEY_BACKGROUND_TEMPLATE_PHOTO = "background_template_photo"
    private const val KEY_BACKGROUND_TEMPLATE_DRAWABLE = "background_template_drawable"
    private const val KEY_STICKER_ENABLED = "sticker_enabled"
    private const val KEY_STICKER_GLYPH = "sticker_glyph"
    private const val KEY_STICKER_THUMB_URL = "sticker_thumb_url"
    private const val KEY_STICKER_LOTTIE_URL = "sticker_lottie_url"
    private const val KEY_STICKER_SIZE = "sticker_size"
    private const val KEY_STICKER_ROTATION = "sticker_rotation"
    private const val KEY_STICKER_OFFSET_X = "sticker_offset_x"
    private const val KEY_STICKER_OFFSET_Y = "sticker_offset_y"
    private const val KEY_TROLL_ENABLED = "troll_enabled"
    private const val KEY_TROLL_MESSAGE = "troll_message"
    private const val KEY_TROLL_BATTERY_ART_URL = "troll_battery_art_url"
    private const val KEY_TROLL_EMOJI_ART_URL = "troll_emoji_art_url"
    private const val KEY_TROLL_BATTERY_OPTIONS_URLS = "troll_battery_options_urls"
    private const val KEY_TROLL_EMOJI_OPTIONS_URLS = "troll_emoji_options_urls"
    private const val KEY_TROLL_SHOW_EMOJI = "troll_show_emoji"
    private const val KEY_TROLL_USE_REAL_BATTERY = "troll_use_real_battery"
    private const val KEY_TROLL_RANDOMIZED_MODE = "troll_randomized_mode"
    private const val KEY_TROLL_SHOW_PERCENTAGE = "troll_show_percentage"
    private const val KEY_TROLL_PERCENTAGE_SIZE_DP = "troll_percentage_size_dp"
    private const val KEY_TROLL_EMOJI_SIZE_DP = "troll_emoji_size_dp"
    private const val KEY_REALTIME_ENABLED = "realtime_enabled"
    private const val KEY_REALTIME_GLYPH = "realtime_glyph"
    private const val KEY_REALTIME_TITLE = "realtime_title"
    private const val KEY_NOTCH_TEMPLATE_ID = "notch_template_id"
    private const val KEY_NOTCH_COLOR_VARIANT = "notch_color_variant"
    private const val KEY_NOTCH_SCALE = "notch_scale"
    private const val KEY_NOTCH_OFFSET_X = "notch_offset_x"
    private const val KEY_NOTCH_OFFSET_Y = "notch_offset_y"
    private const val KEY_STATUS_BAR_HEIGHT = "status_bar_height"
    private const val KEY_STATUS_LEFT_MARGIN = "status_left_margin"
    private const val KEY_STATUS_RIGHT_MARGIN = "status_right_margin"
    private const val KEY_BATTERY_PERCENT_SCALE = "battery_percent_scale"
    private const val KEY_EMOJI_SCALE = "emoji_scale"
    private const val KEY_EMOJI_ADJUSTMENT_SCALE = "emoji_adjustment_scale"
    private const val KEY_EMOJI_OFFSET_X = "emoji_offset_x"
    private const val KEY_EMOJI_OFFSET_Y = "emoji_offset_y"
    private const val KEY_SHOW_PERCENTAGE = "show_percentage"
    private const val KEY_ANIMATE_CHARGE = "animate_charge"
    private const val KEY_SHOW_STROKE = "show_stroke"
    private const val KEY_ANIMATION_ENABLED = "animation_enabled"
    private const val KEY_ANIMATION_SIZE_PERCENT = "animation_size_percent"
    private const val KEY_ANIMATION_OFFSET_X = "animation_offset_x"
    private const val KEY_ANIMATION_TEMPLATE_ID = "animation_template_id"
    private const val KEY_FEATURE_CONFIGS = "feature_configs"

    /**
     * Persists the status-bar editor config for the accessibility overlay (original app: one **BatteryConfig**
     * committed on Apply regardless of which sub-screen is open).
     *
     * **volioCatalogItems** — same list as Battery/Emoji grids; preset IDs are often Volio UUIDs, so we avoid
     * resolving them as unrelated sample catalog entries.
     */
    fun saveStatusBarConfig(
        context: Context,
        config: BatteryIconConfig,
        volioCatalogItems: List<HomeBatteryItem> = emptyList(),
    ) {
        val batteryBody = batteryBodyForOverlay(config.batteryPresetId, volioCatalogItems)
        val emojiGlyph = emojiGlyphForOverlay(config.emojiPresetId, volioCatalogItems)
        val batteryItem = volioCatalogItems.firstOrNull { it.id == config.batteryPresetId }
        val batteryArtUrl = batteryItem?.batteryArtUrl
            ?.takeIf { it.isNotBlank() }
            ?: batteryItem?.thumbnailUrl?.takeIf { it.isNotBlank() }
        val batteryArtDrawableRes = batteryItem?.previewRes?.takeIf { it != 0 }
        val emojiItem = volioCatalogItems.firstOrNull { it.id == config.emojiPresetId }
        val emojiArtUrl = emojiItem?.emojiArtUrl?.takeIf { it.isNotBlank() }
        val emojiArtDrawableRes = emojiItem?.previewRes?.takeIf { it != 0 }
        prefs(context).edit()
            .putString(KEY_BATTERY_TEXT, "$batteryBody $emojiGlyph")
            .putString(KEY_BATTERY_BODY, batteryBody)
            .putString(KEY_EMOJI_GLYPH, emojiGlyph)
            .putString(KEY_BATTERY_ART_URL, batteryArtUrl.orEmpty())
            .putInt(KEY_BATTERY_ART_DRAWABLE, batteryArtDrawableRes ?: 0)
            .putString(KEY_EMOJI_ART_URL, emojiArtUrl.orEmpty())
            .putInt(KEY_EMOJI_ART_DRAWABLE, emojiArtDrawableRes ?: 0)
            .putLong(KEY_ACCENT, config.accentColor)
            .putLong(KEY_BACKGROUND, config.backgroundColor)
            .putString(KEY_BACKGROUND_TEMPLATE_PHOTO, config.backgroundTemplatePhotoUrl.orEmpty())
            .putInt(KEY_BACKGROUND_TEMPLATE_DRAWABLE, config.backgroundTemplateDrawableRes ?: 0)
            .putFloat(KEY_STATUS_BAR_HEIGHT, config.statusBarHeight.coerceIn(0f, 1f))
            .putFloat(KEY_STATUS_LEFT_MARGIN, config.leftMargin.coerceIn(0f, 1f))
            .putFloat(KEY_STATUS_RIGHT_MARGIN, config.rightMargin.coerceIn(0f, 1f))
            .putFloat(KEY_BATTERY_PERCENT_SCALE, config.batteryPercentScale.coerceIn(0f, 1f))
            .putFloat(KEY_EMOJI_SCALE, config.emojiScale.coerceIn(0f, 1f))
            .putFloat(KEY_EMOJI_ADJUSTMENT_SCALE, config.emojiAdjustmentScale.coerceIn(0.35f, 2.2f))
            .putFloat(KEY_EMOJI_OFFSET_X, config.emojiOffsetX.coerceIn(0f, 1f))
            .putFloat(KEY_EMOJI_OFFSET_Y, config.emojiOffsetY.coerceIn(0f, 1f))
            .putBoolean(KEY_SHOW_PERCENTAGE, config.showPercentage)
            .putBoolean(KEY_ANIMATE_CHARGE, config.animateCharge)
            .putBoolean(KEY_SHOW_STROKE, config.showStroke)
            .apply()
    }

    fun setStatusBarEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_STATUS_ENABLED, enabled)
            .apply()
    }

    fun setBatteryEmojiSource(context: Context, source: String) {
        val normalized = when (source) {
            BATTERY_EMOJI_SOURCE_BATTERY_TROLL -> BATTERY_EMOJI_SOURCE_BATTERY_TROLL
            else -> BATTERY_EMOJI_SOURCE_STATUS_BAR_CUSTOM
        }
        prefs(context).edit()
            .putString(KEY_BATTERY_EMOJI_SOURCE, normalized)
            .apply()
    }

    fun saveFeatureConfigs(context: Context, featureConfigs: Map<CustomizeEntry, FeatureConfig>) {
        prefs(context).edit()
            .putString(KEY_FEATURE_CONFIGS, encodeFeatureConfigs(featureConfigs))
            .apply()
    }

    private fun batteryBodyForOverlay(presetId: String, volioCatalog: List<HomeBatteryItem>): String {
        SampleCatalog.batteryPresets.firstOrNull { it.id == presetId }?.body?.let { return it }
        if (volioCatalog.any { it.id == presetId }) return "▰▰▰▱"
        return SampleCatalog.batteryPresets.first().body
    }

    private fun emojiGlyphForOverlay(presetId: String, volioCatalog: List<HomeBatteryItem>): String {
        SampleCatalog.emojiPresets.firstOrNull { it.id == presetId }?.glyph?.let { return it }
        if (volioCatalog.any { it.id == presetId }) return "●"
        return SampleCatalog.emojiPresets.first().glyph
    }

    fun saveStickerOverlay(context: Context, uiState: AppUiState) {
        val stickerId = uiState.selectedStickerId ?: uiState.stickerPlacements.lastOrNull()?.stickerId ?: return
        val sticker = uiState.stickerPresetForId(stickerId) ?: return
        val placement = uiState.stickerPlacements.firstOrNull { it.stickerId == stickerId }
        prefs(context).edit()
            .putBoolean(KEY_STICKER_ENABLED, true)
            .putString(KEY_STICKER_GLYPH, sticker.glyph)
            .putString(KEY_STICKER_THUMB_URL, sticker.thumbnailUrl?.takeIf { it.isNotBlank() }.orEmpty())
            .putString(KEY_STICKER_LOTTIE_URL, sticker.lottieUrl?.takeIf { it.isNotBlank() }.orEmpty())
            .putFloat(KEY_STICKER_SIZE, placement?.size?.coerceIn(0.2f, 1f) ?: 0.5f)
            .putFloat(KEY_STICKER_ROTATION, placement?.rotation?.coerceIn(-180f, 180f) ?: 0f)
            .putFloat(KEY_STICKER_OFFSET_X, placement?.offsetX?.coerceIn(0f, 1f) ?: 0.5f)
            .putFloat(KEY_STICKER_OFFSET_Y, placement?.offsetY?.coerceIn(0f, 1f) ?: 0.5f)
            .apply()
    }

    fun clearStickerOverlay(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_STICKER_ENABLED, false)
            .remove(KEY_STICKER_THUMB_URL)
            .remove(KEY_STICKER_LOTTIE_URL)
            .remove(KEY_STICKER_SIZE)
            .remove(KEY_STICKER_ROTATION)
            .remove(KEY_STICKER_OFFSET_X)
            .remove(KEY_STICKER_OFFSET_Y)
            .apply()
    }

    fun saveBatteryTroll(context: Context, uiState: AppUiState) {
        val selectedTemplate = uiState.batteryTrollTemplateForId(uiState.selectedBatteryTrollTemplateId)
        val batteryOptions = selectedTemplate?.batteryOptionsUrls
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val emojiOptions = selectedTemplate?.emojiOptionsUrls
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val batteryUrl = uiState.trollSelectedBatteryUrl
            ?: batteryOptions.firstOrNull()
            ?: selectedTemplate?.batteryThumbnailUrl
            ?: selectedTemplate?.thumbnailUrl
        val emojiUrl = uiState.trollSelectedEmojiUrl
            ?: emojiOptions.firstOrNull()
            ?: selectedTemplate?.emojiThumbnailUrl
            ?: selectedTemplate?.thumbnailUrl
        prefs(context).edit()
            .putBoolean(KEY_TROLL_ENABLED, true)
            .putString(KEY_TROLL_MESSAGE, uiState.trollMessage)
            .putString(KEY_TROLL_BATTERY_ART_URL, batteryUrl.orEmpty())
            .putString(KEY_TROLL_EMOJI_ART_URL, emojiUrl.orEmpty())
            .putString(KEY_TROLL_BATTERY_OPTIONS_URLS, encodeStringList(batteryOptions))
            .putString(KEY_TROLL_EMOJI_OPTIONS_URLS, encodeStringList(emojiOptions))
            .putBoolean(KEY_TROLL_SHOW_EMOJI, uiState.trollShowEmoji)
            .putBoolean(KEY_TROLL_USE_REAL_BATTERY, uiState.trollUseRealBattery)
            .putBoolean(KEY_TROLL_RANDOMIZED_MODE, uiState.trollRandomizedMode)
            .putBoolean(KEY_TROLL_SHOW_PERCENTAGE, uiState.trollShowPercentage)
            .putInt(KEY_TROLL_PERCENTAGE_SIZE_DP, uiState.trollPercentageSizeDp.coerceIn(5, 40))
            .putInt(KEY_TROLL_EMOJI_SIZE_DP, uiState.trollEmojiSizeDp.coerceIn(1, 50))
            .apply()
    }

    fun clearBatteryTroll(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_TROLL_ENABLED, false)
            .remove(KEY_TROLL_BATTERY_ART_URL)
            .remove(KEY_TROLL_EMOJI_ART_URL)
            .remove(KEY_TROLL_BATTERY_OPTIONS_URLS)
            .remove(KEY_TROLL_EMOJI_OPTIONS_URLS)
            .apply()
    }

    fun saveRealTime(context: Context, templateId: String) {
        val template = SampleCatalog.realTimeTemplates.firstOrNull { it.id == templateId } ?: return
        prefs(context).edit()
            .putBoolean(KEY_REALTIME_ENABLED, true)
            .putString(KEY_REALTIME_GLYPH, template.accentGlyph)
            .putString(KEY_REALTIME_TITLE, template.title)
            .apply()
    }

    fun saveNotchTemplateId(context: Context, templateId: Int) {
        prefs(context).edit()
            .putInt(KEY_NOTCH_TEMPLATE_ID, templateId)
            .apply()
    }

    fun saveNotchColorVariant(context: Context, variant: String) {
        prefs(context).edit()
            .putString(KEY_NOTCH_COLOR_VARIANT, variant.ifBlank { "black" })
            .apply()
    }

    fun saveNotchAdjustment(
        context: Context,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
    ) {
        prefs(context).edit()
            .putFloat(KEY_NOTCH_SCALE, scale.coerceIn(0.5f, 2.2f))
            .putFloat(KEY_NOTCH_OFFSET_X, offsetX.coerceIn(0f, 1f))
            .putFloat(KEY_NOTCH_OFFSET_Y, offsetY.coerceIn(0f, 1f))
            .apply()
    }

    fun saveAnimationPrefs(
        context: Context,
        enabled: Boolean,
        sizePercent: Int,
        offsetX: Float,
        templateId: Int,
    ) {
        prefs(context).edit()
            .putBoolean(KEY_ANIMATION_ENABLED, enabled)
            .putInt(KEY_ANIMATION_SIZE_PERCENT, sizePercent.coerceIn(0, 100))
            .putFloat(KEY_ANIMATION_OFFSET_X, offsetX.coerceIn(0f, 1f))
            .putInt(KEY_ANIMATION_TEMPLATE_ID, templateId)
            .apply()
    }

    fun readAnimationPrefs(context: Context): AnimationOverlayPrefs {
        val prefs = prefs(context)
        return AnimationOverlayPrefs(
            enabled = prefs.getBoolean(KEY_ANIMATION_ENABLED, false),
            sizePercent = prefs.getInt(KEY_ANIMATION_SIZE_PERCENT, 50).coerceIn(0, 100),
            offsetX = prefs.getFloat(KEY_ANIMATION_OFFSET_X, 0.5f).coerceIn(0f, 1f),
            templateId = prefs.getInt(KEY_ANIMATION_TEMPLATE_ID, 0),
        )
    }

    fun read(context: Context): OverlaySnapshot {
        val prefs = prefs(context)
        val animationEnabled = prefs.getBoolean(KEY_ANIMATION_ENABLED, false)
        val animationSizePercent = prefs.getInt(KEY_ANIMATION_SIZE_PERCENT, 50).coerceIn(0, 100)
        val animationTemplate = AnimationTemplateCatalog.resolve(prefs.getInt(KEY_ANIMATION_TEMPLATE_ID, 0))
        return OverlaySnapshot(
            statusBarEnabled = prefs.getBoolean(KEY_STATUS_ENABLED, true),
            batteryEmojiSource = prefs.getString(
                KEY_BATTERY_EMOJI_SOURCE,
                BATTERY_EMOJI_SOURCE_STATUS_BAR_CUSTOM,
            ).orEmpty().ifBlank { BATTERY_EMOJI_SOURCE_STATUS_BAR_CUSTOM },
            batteryText = prefs.getString(KEY_BATTERY_TEXT, "").orEmpty(),
            batteryBody = prefs.getString(KEY_BATTERY_BODY, "▰▰▰▱").orEmpty().ifBlank { "▰▰▰▱" },
            emojiGlyph = prefs.getString(KEY_EMOJI_GLYPH, "●").orEmpty().ifBlank { "●" },
            batteryArtUrl = prefs.getString(KEY_BATTERY_ART_URL, null)?.takeIf { it.isNotBlank() },
            batteryArtDrawableRes = prefs.getInt(KEY_BATTERY_ART_DRAWABLE, 0).takeIf { it != 0 },
            emojiArtUrl = prefs.getString(KEY_EMOJI_ART_URL, null)?.takeIf { it.isNotBlank() },
            emojiArtDrawableRes = prefs.getInt(KEY_EMOJI_ART_DRAWABLE, 0).takeIf { it != 0 },
            accentColor = prefs.getLong(KEY_ACCENT, SampleCatalog.defaultConfig.accentColor),
            backgroundColor = prefs.getLong(KEY_BACKGROUND, SampleCatalog.defaultConfig.backgroundColor),
            backgroundTemplatePhotoUrl = prefs.getString(KEY_BACKGROUND_TEMPLATE_PHOTO, null)?.takeIf { it.isNotBlank() },
            backgroundTemplateDrawableRes = prefs.getInt(KEY_BACKGROUND_TEMPLATE_DRAWABLE, 0).takeIf { it != 0 },
            stickerEnabled = prefs.getBoolean(KEY_STICKER_ENABLED, false),
            stickerGlyph = prefs.getString(KEY_STICKER_GLYPH, "✨").orEmpty(),
            stickerThumbnailUrl = prefs.getString(KEY_STICKER_THUMB_URL, null)?.takeIf { it.isNotBlank() },
            stickerLottieUrl = prefs.getString(KEY_STICKER_LOTTIE_URL, null)?.takeIf { it.isNotBlank() },
            stickerSize = prefs.getFloat(KEY_STICKER_SIZE, 0.5f).coerceIn(0.2f, 1f),
            stickerRotation = prefs.getFloat(KEY_STICKER_ROTATION, 0f).coerceIn(-180f, 180f),
            stickerOffsetX = prefs.getFloat(KEY_STICKER_OFFSET_X, 0.5f).coerceIn(0f, 1f),
            stickerOffsetY = prefs.getFloat(KEY_STICKER_OFFSET_Y, 0.5f).coerceIn(0f, 1f),
            trollEnabled = prefs.getBoolean(KEY_TROLL_ENABLED, false),
            trollMessage = prefs.getString(KEY_TROLL_MESSAGE, "999").orEmpty(),
            trollBatteryArtUrl = prefs.getString(KEY_TROLL_BATTERY_ART_URL, null)?.takeIf { it.isNotBlank() },
            trollEmojiArtUrl = prefs.getString(KEY_TROLL_EMOJI_ART_URL, null)?.takeIf { it.isNotBlank() },
            trollBatteryOptionsUrls = decodeStringList(prefs.getString(KEY_TROLL_BATTERY_OPTIONS_URLS, null)),
            trollEmojiOptionsUrls = decodeStringList(prefs.getString(KEY_TROLL_EMOJI_OPTIONS_URLS, null)),
            trollShowEmoji = prefs.getBoolean(KEY_TROLL_SHOW_EMOJI, true),
            trollUseRealBattery = prefs.getBoolean(KEY_TROLL_USE_REAL_BATTERY, false),
            trollRandomizedMode = prefs.getBoolean(KEY_TROLL_RANDOMIZED_MODE, false),
            trollShowPercentage = prefs.getBoolean(KEY_TROLL_SHOW_PERCENTAGE, true),
            trollPercentageSizeDp = prefs.getInt(KEY_TROLL_PERCENTAGE_SIZE_DP, 5).coerceIn(5, 40),
            trollEmojiSizeDp = prefs.getInt(KEY_TROLL_EMOJI_SIZE_DP, 40).coerceIn(1, 50),
            realTimeEnabled = prefs.getBoolean(KEY_REALTIME_ENABLED, false),
            realTimeGlyph = prefs.getString(KEY_REALTIME_GLYPH, "⚡").orEmpty(),
            realTimeTitle = prefs.getString(KEY_REALTIME_TITLE, "Real Time").orEmpty(),
            // Default to hidden notch until user explicitly selects a template.
            notchTemplateId = prefs.getInt(KEY_NOTCH_TEMPLATE_ID, -1),
            notchColorVariant = prefs.getString(KEY_NOTCH_COLOR_VARIANT, "black").orEmpty().ifBlank { "black" },
            notchScale = prefs.getFloat(KEY_NOTCH_SCALE, 1f).coerceIn(0.5f, 2.2f),
            notchOffsetX = prefs.getFloat(KEY_NOTCH_OFFSET_X, 0.5f).coerceIn(0f, 1f),
            notchOffsetY = prefs.getFloat(KEY_NOTCH_OFFSET_Y, 0.5f).coerceIn(0f, 1f),
            statusBarHeight = prefs.getFloat(KEY_STATUS_BAR_HEIGHT, SampleCatalog.defaultConfig.statusBarHeight).coerceIn(0f, 1f),
            leftMargin = prefs.getFloat(KEY_STATUS_LEFT_MARGIN, SampleCatalog.defaultConfig.leftMargin).coerceIn(0f, 1f),
            rightMargin = prefs.getFloat(KEY_STATUS_RIGHT_MARGIN, SampleCatalog.defaultConfig.rightMargin).coerceIn(0f, 1f),
            batteryPercentScale = prefs.getFloat(KEY_BATTERY_PERCENT_SCALE, SampleCatalog.defaultConfig.batteryPercentScale).coerceIn(0f, 1f),
            emojiScale = prefs.getFloat(KEY_EMOJI_SCALE, SampleCatalog.defaultConfig.emojiScale).coerceIn(0f, 1f),
            emojiAdjustmentScale = prefs.getFloat(KEY_EMOJI_ADJUSTMENT_SCALE, SampleCatalog.defaultConfig.emojiAdjustmentScale).coerceIn(0.35f, 2.2f),
            emojiOffsetX = prefs.getFloat(KEY_EMOJI_OFFSET_X, SampleCatalog.defaultConfig.emojiOffsetX).coerceIn(0f, 1f),
            emojiOffsetY = prefs.getFloat(KEY_EMOJI_OFFSET_Y, SampleCatalog.defaultConfig.emojiOffsetY).coerceIn(0f, 1f),
            showPercentage = prefs.getBoolean(KEY_SHOW_PERCENTAGE, SampleCatalog.defaultConfig.showPercentage),
            animateCharge = prefs.getBoolean(KEY_ANIMATE_CHARGE, SampleCatalog.defaultConfig.animateCharge),
            showStroke = prefs.getBoolean(KEY_SHOW_STROKE, SampleCatalog.defaultConfig.showStroke),
            animationEnabled = animationEnabled,
            animationSizePercent = animationSizePercent,
            animationOffsetX = prefs.getFloat(KEY_ANIMATION_OFFSET_X, 0.5f).coerceIn(0f, 1f),
            animationAssetPath = animationTemplate.assetPath,
            animationIsLottie = animationTemplate.isLottie,
            featureConfigs = decodeFeatureConfigs(prefs.getString(KEY_FEATURE_CONFIGS, null)),
        )
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun encodeStringList(values: List<String>): String {
        val array = JSONArray()
        values.forEach { array.put(it) }
        return array.toString()
    }

    private fun decodeStringList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val value = array.optString(index).trim()
                    if (value.isNotEmpty()) add(value)
                }
            }
        }.getOrElse { emptyList() }
    }

    private fun encodeFeatureConfigs(configs: Map<CustomizeEntry, FeatureConfig>): String {
        val root = JSONObject()
        configs.forEach { (entry, config) ->
            val node = JSONObject()
            node.put("enabled", config.enabled)
            node.put("intensity", config.intensity)
            node.put("variant", config.variant)
            root.put(entry.name, node)
        }
        return root.toString()
    }

    private fun decodeFeatureConfigs(raw: String?): Map<CustomizeEntry, FeatureConfig> {
        if (raw.isNullOrBlank()) return SampleCatalog.defaultFeatureConfigs
        return runCatching {
            val json = JSONObject(raw)
            val decoded = mutableMapOf<CustomizeEntry, FeatureConfig>()
            CustomizeEntry.entries.forEach { entry ->
                val default = SampleCatalog.defaultFeatureConfigs[entry] ?: return@forEach
                val node = json.optJSONObject(entry.name)
                if (node == null) {
                    decoded[entry] = default
                } else {
                    decoded[entry] = FeatureConfig(
                        enabled = node.optBoolean("enabled", default.enabled),
                        intensity = node.optDouble("intensity", default.intensity.toDouble()).toFloat().coerceIn(0f, 1f),
                        variant = node.optString("variant", default.variant),
                    )
                }
            }
            decoded
        }.getOrElse { SampleCatalog.defaultFeatureConfigs }
    }
}
