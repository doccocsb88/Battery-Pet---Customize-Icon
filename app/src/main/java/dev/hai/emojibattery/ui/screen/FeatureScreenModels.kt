package dev.hai.emojibattery.ui.screen

import androidx.compose.ui.graphics.Color

internal data class WifiColorOption(
    val id: String,
    val color: Color,
)

internal val WifiColorOptions = listOf(
    WifiColorOption("picker", Color.Transparent),
    WifiColorOption("blue", Color(0xFF2952F4)),
    WifiColorOption("green", Color(0xFF2BDF52)),
    WifiColorOption("orange", Color(0xFFF18410)),
    WifiColorOption("black", Color(0xFF11111A)),
    WifiColorOption("yellow", Color(0xFFF1DF1E)),
)

private const val PickerColorPrefix = "picker#"

internal fun isPickerColorVariant(raw: String): Boolean =
    raw == "picker" || raw.startsWith(PickerColorPrefix, ignoreCase = true)

internal fun encodePickerColorVariant(argb: Long): String {
    val normalized = argb and 0xFFFFFFFFL
    return PickerColorPrefix + normalized.toString(16).padStart(8, '0')
}

internal fun parsePickerColorVariant(raw: String?): Long? {
    if (raw == null || !raw.startsWith(PickerColorPrefix, ignoreCase = true)) return null
    val hex = raw.removePrefix(PickerColorPrefix)
    return hex.toLongOrNull(16)?.and(0xFFFFFFFFL)
}

internal data class ChargeOption(
    val id: String,
    val glyph: String,
)

internal val ChargeOptions = listOf(
    ChargeOption("chg_1", "⚡"),
    ChargeOption("chg_2", "↯"),
    ChargeOption("chg_3", "⌁"),
    ChargeOption("chg_4", "⏻"),
    ChargeOption("chg_5", "🔌"),
    ChargeOption("chg_6", "⏚"),
    ChargeOption("chg_7", "ϟ"),
    ChargeOption("chg_8", "⌬"),
    ChargeOption("chg_9", "⎓"),
    ChargeOption("chg_10", "⟡"),
    ChargeOption("chg_11", "⌇"),
    ChargeOption("chg_12", "⋇"),
)

internal data class DateTimeStyleOption(
    val id: String,
    val line1: String,
    val line2: String? = null,
    val line2Bold: Boolean = false,
)

internal val DateTimeStyles = listOf(
    DateTimeStyleOption("style_1", "Tue, Mar 24"),
    DateTimeStyleOption("style_2", "Tue, Mar", "24", line2Bold = true),
    DateTimeStyleOption("style_3", "Tue", "24", line2Bold = true),
    DateTimeStyleOption("style_4", "Mar 24"),
    DateTimeStyleOption("style_5", "Tuesday"),
    DateTimeStyleOption("style_6", "Tuesday", "24", line2Bold = true),
)

internal data class DateTimeVariantState(
    val styleId: String,
    val colorId: String,
    val showDate: Boolean,
)

internal fun parseDateTimeVariant(raw: String?): DateTimeVariantState {
    val fallback = DateTimeVariantState(styleId = "style_4", colorId = "blue", showDate = true)
    if (raw.isNullOrBlank()) return fallback
    val pieces = raw.split(";").mapNotNull {
        val p = it.split("=", limit = 2)
        if (p.size == 2) p[0] to p[1] else null
    }.toMap()
    return DateTimeVariantState(
        styleId = pieces["style"] ?: fallback.styleId,
        colorId = pieces["color"] ?: fallback.colorId,
        showDate = (pieces["show"] ?: "1") == "1",
    )
}

internal fun encodeDateTimeVariant(state: DateTimeVariantState): String =
    "style=${state.styleId};color=${state.colorId};show=${if (state.showDate) "1" else "0"}"

internal data class EmotionOption(
    val id: String,
    val glyph: String,
)

internal val EmotionOptions = listOf(
    EmotionOption("emo_cute", "🥺"),
    EmotionOption("emo_laugh", "😆"),
    EmotionOption("emo_dizzy", "😵"),
    EmotionOption("emo_shock", "😮"),
    EmotionOption("emo_heart", "😍"),
    EmotionOption("emo_kiss", "😙"),
    EmotionOption("emo_plead", "😟"),
    EmotionOption("emo_smile", "😊"),
    EmotionOption("emo_winkkiss", "😘"),
    EmotionOption("emo_sleepy", "🥱"),
    EmotionOption("emo_cool", "😎"),
    EmotionOption("emo_sleep", "😪"),
    EmotionOption("emo_relief", "🙂"),
    EmotionOption("emo_crylaugh", "😅"),
    EmotionOption("emo_scared", "😨"),
)
