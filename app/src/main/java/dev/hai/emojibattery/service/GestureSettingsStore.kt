package dev.hai.emojibattery.service

import android.content.Context
import dev.hai.emojibattery.model.GestureAction
import dev.hai.emojibattery.model.GestureTrigger
import dev.hai.emojibattery.model.SampleCatalog

/**
 * Persists gesture toggle, haptics, and per-trigger action mappings so they survive process death.
 * (Runtime gesture recognition on the status bar is not implemented in [OverlayAccessibilityService] yet.)
 */
object GestureSettingsStore {
    private const val PREFS = "gesture_settings"
    private const val KEY_ENABLED = "gesture_enabled"
    private const val KEY_VIBRATE = "vibrate_feedback"
    private const val KEY_ACTION_PREFIX = "action_"

    fun read(context: Context): Snapshot {
        val prefs = prefs(context)
        val actions = GestureTrigger.entries.associateWith { trigger ->
            val raw = prefs.getString(KEY_ACTION_PREFIX + trigger.name, null)
            parseStoredAction(raw, trigger)
        }
        return Snapshot(
            gestureEnabled = prefs.getBoolean(KEY_ENABLED, false),
            vibrateFeedback = prefs.getBoolean(KEY_VIBRATE, true),
            gestureActions = actions,
        )
    }

    fun write(context: Context, gestureEnabled: Boolean, vibrateFeedback: Boolean, gestureActions: Map<GestureTrigger, GestureAction>) {
        val editor = prefs(context).edit()
            .putBoolean(KEY_ENABLED, gestureEnabled)
            .putBoolean(KEY_VIBRATE, vibrateFeedback)
        gestureActions.forEach { (trigger, action) ->
            editor.putString(KEY_ACTION_PREFIX + trigger.name, action.name)
        }
        editor.apply()
    }

    data class Snapshot(
        val gestureEnabled: Boolean,
        val vibrateFeedback: Boolean,
        val gestureActions: Map<GestureTrigger, GestureAction>,
    )

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Maps legacy enum names from older builds to the current [GestureAction] set. */
    private fun parseStoredAction(raw: String?, trigger: GestureTrigger): GestureAction {
        if (raw.isNullOrBlank()) {
            return SampleCatalog.defaultGestureActions[trigger] ?: GestureAction.DoNothing
        }
        return when (raw) {
            "None" -> GestureAction.DoNothing
            "OpenCustomize", "OpenEmojiSticker", "OpenSearch", "OpenBatteryTroll", "ToggleOverlay" ->
                SampleCatalog.defaultGestureActions[trigger] ?: GestureAction.DoNothing
            else -> runCatching { GestureAction.valueOf(raw) }.getOrElse {
                SampleCatalog.defaultGestureActions[trigger] ?: GestureAction.DoNothing
            }
        }
    }
}
