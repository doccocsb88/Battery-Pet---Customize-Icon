package dev.hai.emojibattery.service

import android.content.Context
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.BatteryIconConfig
import dev.hai.emojibattery.model.SampleCatalog

data class OverlaySnapshot(
    val statusBarEnabled: Boolean,
    val batteryText: String,
    val accentColor: Long,
    val backgroundColor: Long,
    val stickerEnabled: Boolean,
    val stickerGlyph: String,
    val trollEnabled: Boolean,
    val trollMessage: String,
    val realTimeEnabled: Boolean,
    val realTimeGlyph: String,
    val realTimeTitle: String,
)

object OverlayConfigStore {
    private const val PREFS = "overlay_config"
    private const val KEY_STATUS_ENABLED = "status_enabled"
    private const val KEY_BATTERY_TEXT = "battery_text"
    private const val KEY_ACCENT = "accent"
    private const val KEY_BACKGROUND = "background"
    private const val KEY_STICKER_ENABLED = "sticker_enabled"
    private const val KEY_STICKER_GLYPH = "sticker_glyph"
    private const val KEY_TROLL_ENABLED = "troll_enabled"
    private const val KEY_TROLL_MESSAGE = "troll_message"
    private const val KEY_REALTIME_ENABLED = "realtime_enabled"
    private const val KEY_REALTIME_GLYPH = "realtime_glyph"
    private const val KEY_REALTIME_TITLE = "realtime_title"

    fun saveStatusBarConfig(context: Context, config: BatteryIconConfig) {
        val battery = SampleCatalog.batteryPresets.firstOrNull { it.id == config.batteryPresetId } ?: SampleCatalog.batteryPresets.first()
        val emoji = SampleCatalog.emojiPresets.firstOrNull { it.id == config.emojiPresetId } ?: SampleCatalog.emojiPresets.first()
        prefs(context).edit()
            .putBoolean(KEY_STATUS_ENABLED, true)
            .putString(KEY_BATTERY_TEXT, "${battery.body} ${emoji.glyph}")
            .putLong(KEY_ACCENT, config.accentColor)
            .putLong(KEY_BACKGROUND, config.backgroundColor)
            .apply()
    }

    fun saveStickerOverlay(context: Context, uiState: AppUiState) {
        val stickerId = uiState.selectedStickerId ?: uiState.stickerPlacements.lastOrNull()?.stickerId ?: return
        val sticker = SampleCatalog.stickerPresets.firstOrNull { it.id == stickerId } ?: return
        prefs(context).edit()
            .putBoolean(KEY_STICKER_ENABLED, true)
            .putString(KEY_STICKER_GLYPH, sticker.glyph)
            .apply()
    }

    fun clearStickerOverlay(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_STICKER_ENABLED, false)
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

    fun read(context: Context): OverlaySnapshot {
        val prefs = prefs(context)
        return OverlaySnapshot(
            statusBarEnabled = prefs.getBoolean(KEY_STATUS_ENABLED, false),
            batteryText = prefs.getString(KEY_BATTERY_TEXT, "").orEmpty(),
            accentColor = prefs.getLong(KEY_ACCENT, SampleCatalog.defaultConfig.accentColor),
            backgroundColor = prefs.getLong(KEY_BACKGROUND, SampleCatalog.defaultConfig.backgroundColor),
            stickerEnabled = prefs.getBoolean(KEY_STICKER_ENABLED, false),
            stickerGlyph = prefs.getString(KEY_STICKER_GLYPH, "✨").orEmpty(),
            trollEnabled = prefs.getBoolean(KEY_TROLL_ENABLED, false),
            trollMessage = prefs.getString(KEY_TROLL_MESSAGE, "999").orEmpty(),
            realTimeEnabled = prefs.getBoolean(KEY_REALTIME_ENABLED, false),
            realTimeGlyph = prefs.getString(KEY_REALTIME_GLYPH, "⚡").orEmpty(),
            realTimeTitle = prefs.getString(KEY_REALTIME_TITLE, "Real Time").orEmpty(),
        )
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
