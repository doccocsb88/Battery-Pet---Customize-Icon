package dev.hai.emojibattery.service

import android.content.Context
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.BatteryIconConfig
import dev.hai.emojibattery.model.HomeBatteryItem
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.model.stickerPresetForId

data class OverlaySnapshot(
    val statusBarEnabled: Boolean,
    val batteryText: String,
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
    val stickerSize: Float,
    val stickerRotation: Float,
    val stickerOffsetX: Float,
    val stickerOffsetY: Float,
    val trollEnabled: Boolean,
    val trollMessage: String,
    val realTimeEnabled: Boolean,
    val realTimeGlyph: String,
    val realTimeTitle: String,
    /** Original notch selector ID (-1 hide, 1..13 visible variants). */
    val notchTemplateId: Int,
)

data class AnimationOverlayPrefs(
    val enabled: Boolean,
    /** 0..100 scale from original seekbar style. */
    val sizePercent: Int,
    val templateId: Int,
)

object OverlayConfigStore {
    private const val PREFS = "overlay_config"
    private const val KEY_STATUS_ENABLED = "status_enabled"
    private const val KEY_BATTERY_TEXT = "battery_text"
    private const val KEY_ACCENT = "accent"
    private const val KEY_BACKGROUND = "background"
    private const val KEY_BACKGROUND_TEMPLATE_PHOTO = "background_template_photo"
    private const val KEY_BACKGROUND_TEMPLATE_DRAWABLE = "background_template_drawable"
    private const val KEY_STICKER_ENABLED = "sticker_enabled"
    private const val KEY_STICKER_GLYPH = "sticker_glyph"
    private const val KEY_STICKER_THUMB_URL = "sticker_thumb_url"
    private const val KEY_STICKER_SIZE = "sticker_size"
    private const val KEY_STICKER_ROTATION = "sticker_rotation"
    private const val KEY_STICKER_OFFSET_X = "sticker_offset_x"
    private const val KEY_STICKER_OFFSET_Y = "sticker_offset_y"
    private const val KEY_TROLL_ENABLED = "troll_enabled"
    private const val KEY_TROLL_MESSAGE = "troll_message"
    private const val KEY_REALTIME_ENABLED = "realtime_enabled"
    private const val KEY_REALTIME_GLYPH = "realtime_glyph"
    private const val KEY_REALTIME_TITLE = "realtime_title"
    private const val KEY_NOTCH_TEMPLATE_ID = "notch_template_id"
    private const val KEY_ANIMATION_ENABLED = "animation_enabled"
    private const val KEY_ANIMATION_SIZE_PERCENT = "animation_size_percent"
    private const val KEY_ANIMATION_TEMPLATE_ID = "animation_template_id"

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
        prefs(context).edit()
            .putBoolean(KEY_STATUS_ENABLED, true)
            .putString(KEY_BATTERY_TEXT, "$batteryBody $emojiGlyph")
            .putLong(KEY_ACCENT, config.accentColor)
            .putLong(KEY_BACKGROUND, config.backgroundColor)
            .putString(KEY_BACKGROUND_TEMPLATE_PHOTO, config.backgroundTemplatePhotoUrl.orEmpty())
            .putInt(KEY_BACKGROUND_TEMPLATE_DRAWABLE, config.backgroundTemplateDrawableRes ?: 0)
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
            .remove(KEY_STICKER_SIZE)
            .remove(KEY_STICKER_ROTATION)
            .remove(KEY_STICKER_OFFSET_X)
            .remove(KEY_STICKER_OFFSET_Y)
            .apply()
    }

    fun saveBatteryTroll(context: Context, uiState: AppUiState) {
        prefs(context).edit()
            .putBoolean(KEY_TROLL_ENABLED, true)
            .putString(KEY_TROLL_MESSAGE, uiState.trollMessage)
            .apply()
    }

    fun clearBatteryTroll(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_TROLL_ENABLED, false)
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

    fun saveAnimationPrefs(
        context: Context,
        enabled: Boolean,
        sizePercent: Int,
        templateId: Int,
    ) {
        prefs(context).edit()
            .putBoolean(KEY_ANIMATION_ENABLED, enabled)
            .putInt(KEY_ANIMATION_SIZE_PERCENT, sizePercent.coerceIn(0, 100))
            .putInt(KEY_ANIMATION_TEMPLATE_ID, templateId)
            .apply()
    }

    fun readAnimationPrefs(context: Context): AnimationOverlayPrefs {
        val prefs = prefs(context)
        return AnimationOverlayPrefs(
            enabled = prefs.getBoolean(KEY_ANIMATION_ENABLED, false),
            sizePercent = prefs.getInt(KEY_ANIMATION_SIZE_PERCENT, 50).coerceIn(0, 100),
            templateId = prefs.getInt(KEY_ANIMATION_TEMPLATE_ID, 0),
        )
    }

    fun read(context: Context): OverlaySnapshot {
        val prefs = prefs(context)
        return OverlaySnapshot(
            statusBarEnabled = prefs.getBoolean(KEY_STATUS_ENABLED, false),
            batteryText = prefs.getString(KEY_BATTERY_TEXT, "").orEmpty(),
            accentColor = prefs.getLong(KEY_ACCENT, SampleCatalog.defaultConfig.accentColor),
            backgroundColor = prefs.getLong(KEY_BACKGROUND, SampleCatalog.defaultConfig.backgroundColor),
            backgroundTemplatePhotoUrl = prefs.getString(KEY_BACKGROUND_TEMPLATE_PHOTO, null)?.takeIf { it.isNotBlank() },
            backgroundTemplateDrawableRes = prefs.getInt(KEY_BACKGROUND_TEMPLATE_DRAWABLE, 0).takeIf { it != 0 },
            stickerEnabled = prefs.getBoolean(KEY_STICKER_ENABLED, false),
            stickerGlyph = prefs.getString(KEY_STICKER_GLYPH, "✨").orEmpty(),
            stickerThumbnailUrl = prefs.getString(KEY_STICKER_THUMB_URL, null)?.takeIf { it.isNotBlank() },
            stickerSize = prefs.getFloat(KEY_STICKER_SIZE, 0.5f).coerceIn(0.2f, 1f),
            stickerRotation = prefs.getFloat(KEY_STICKER_ROTATION, 0f).coerceIn(-180f, 180f),
            stickerOffsetX = prefs.getFloat(KEY_STICKER_OFFSET_X, 0.5f).coerceIn(0f, 1f),
            stickerOffsetY = prefs.getFloat(KEY_STICKER_OFFSET_Y, 0.5f).coerceIn(0f, 1f),
            trollEnabled = prefs.getBoolean(KEY_TROLL_ENABLED, false),
            trollMessage = prefs.getString(KEY_TROLL_MESSAGE, "999").orEmpty(),
            realTimeEnabled = prefs.getBoolean(KEY_REALTIME_ENABLED, false),
            realTimeGlyph = prefs.getString(KEY_REALTIME_GLYPH, "⚡").orEmpty(),
            realTimeTitle = prefs.getString(KEY_REALTIME_TITLE, "Real Time").orEmpty(),
            // Match original "dynamic notch" preview behavior by default.
            notchTemplateId = prefs.getInt(KEY_NOTCH_TEMPLATE_ID, 8),
        )
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
