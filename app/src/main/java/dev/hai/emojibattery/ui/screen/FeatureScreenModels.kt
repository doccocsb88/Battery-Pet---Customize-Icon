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

internal data class ChargePackItem(
    val id: String,
    val label: String,
    val glyph: String? = null,
    val drawableName: String? = null,
)

internal data class ChargePackData(
    val id: String,
    val title: String,
    val columns: Int,
    val rows: Int,
    val items: List<ChargePackItem>,
)

private fun buildChargeSheetPack(
    id: String,
    title: String,
    sourceStem: String,
    columns: Int,
    rows: Int,
): ChargePackData {
    val items = buildList {
        for (row in 1..rows) {
            for (col in 1..columns) {
                add(
                    ChargePackItem(
                        id = "${sourceStem}_r%02d_c%02d".format(row, col),
                        label = "${row}:${col}",
                        drawableName = "${sourceStem}_r%02d_c%02d".format(row, col),
                    ),
                )
            }
        }
    }.take(ChargeItemsPerPage)
    val effectiveRows = ((items.size + columns - 1) / columns).coerceAtLeast(1)
    return ChargePackData(id = id, title = title, columns = columns, rows = effectiveRows, items = items)
}

internal val ChargePackCatalog = listOf(
    ChargePackData(
        id = "built_in",
        title = "Built-in",
        columns = 3,
        rows = (ChargeOptions.size + 2) / 3,
        items = ChargeOptions.map { option ->
            ChargePackItem(
                id = option.id,
                label = option.id,
                glyph = option.glyph,
            )
        },
    ),
    buildChargeSheetPack(
        id = "sheet_01",
        title = "Set 01",
        sourceStem = "hai_charge_01",
        columns = 5,
        rows = 3,
    ),
    buildChargeSheetPack(
        id = "sheet_02",
        title = "Set 02",
        sourceStem = "hai_charge_02",
        columns = 3,
        rows = 4,
    ),
    buildChargeSheetPack(
        id = "sheet_03",
        title = "Set 03",
        sourceStem = "hai_charge_03",
        columns = 5,
        rows = 3,
    ),
    buildChargeSheetPack(
        id = "sheet_04",
        title = "Set 04",
        sourceStem = "hai_charge_04",
        columns = 3,
        rows = 4,
    ),
)

internal const val ChargeItemsPerPage = 12

internal data class ChargePageData(
    val packId: String,
    val title: String,
    val pageIndexInPack: Int,
    val pageCountInPack: Int,
    val items: List<ChargePackItem>,
)

internal val ChargePageCatalog: List<ChargePageData> = ChargePackCatalog.flatMap { pack ->
    val pages = pack.items.chunked(ChargeItemsPerPage).ifEmpty { listOf(emptyList()) }
    pages.mapIndexed { index, items ->
        ChargePageData(
            packId = pack.id,
            title = if (pages.size > 1) "${pack.title} ${index + 1}" else pack.title,
            pageIndexInPack = index,
            pageCountInPack = pages.size,
            items = items,
        )
    }
}

internal fun chargePageIndexForVariant(
    pages: List<ChargePageData>,
    state: ChargeVariantState,
): Int {
    if (pages.isEmpty()) return 0
    val exactMatch = pages.indexOfFirst { page ->
        page.packId == state.packId && page.items.any { it.id == state.itemId }
    }
    if (exactMatch >= 0) return exactMatch
    val packMatch = pages.indexOfFirst { it.packId == state.packId }
    return if (packMatch >= 0) packMatch else 0
}

internal data class ChargeVariantState(
    val packId: String,
    val itemId: String,
)

internal fun parseChargeVariant(raw: String?): ChargeVariantState {
    val fallback = ChargeVariantState(packId = "built_in", itemId = ChargeOptions.first().id)
    if (raw.isNullOrBlank()) return fallback
    if (raw in ChargeOptions.map { it.id }) {
        return ChargeVariantState(packId = "built_in", itemId = raw)
    }
    if (";" !in raw && ":" !in raw && "=" !in raw) {
        return fallback
    }
    val pieces = raw.split(";").mapNotNull {
        val pair = it.split("=", limit = 2)
        if (pair.size == 2) pair[0] to pair[1] else null
    }.toMap()
    val legacyPack = raw.substringBefore(":", missingDelimiterValue = "")
    val legacyItem = raw.substringAfter(":", missingDelimiterValue = "")
    val packId = pieces["pack"] ?: pieces["sheet"] ?: if (legacyPack.isNotBlank() && legacyItem.isNotBlank()) legacyPack else fallback.packId
    val itemId = pieces["item"] ?: pieces["drawable"] ?: if (legacyPack.isNotBlank() && legacyItem.isNotBlank()) legacyItem else fallback.itemId
    return when {
        packId == "built_in" && itemId in ChargeOptions.map { it.id } -> ChargeVariantState(packId = packId, itemId = itemId)
        ChargePackCatalog.any { it.id == packId } && itemId.isNotBlank() -> ChargeVariantState(packId = packId, itemId = itemId)
        else -> fallback
    }
}

internal fun encodeChargeVariant(state: ChargeVariantState): String =
    if (state.packId == "built_in") {
        state.itemId
    } else {
        "pack=${state.packId};item=${state.itemId}"
    }

internal fun chargeVariantLabel(state: ChargeVariantState): String =
    if (state.packId == "built_in") {
        ChargeOptions.firstOrNull { it.id == state.itemId }?.glyph ?: ChargeOptions.first().glyph
    } else {
        state.itemId
    }

internal fun chargeVariantDrawableName(state: ChargeVariantState): String? =
    if (state.packId == "built_in") null else state.itemId

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
    val fallback = DateTimeVariantState(styleId = "style_4", colorId = "blue", showDate = false)
    if (raw.isNullOrBlank()) return fallback
    val pieces = raw.split(";").mapNotNull {
        val p = it.split("=", limit = 2)
        if (p.size == 2) p[0] to p[1] else null
    }.toMap()
    return DateTimeVariantState(
        styleId = pieces["style"] ?: fallback.styleId,
        colorId = pieces["color"] ?: fallback.colorId,
        showDate = (pieces["show"] ?: if (fallback.showDate) "1" else "0") == "1",
    )
}

internal fun encodeDateTimeVariant(state: DateTimeVariantState): String =
    "style=${state.styleId};color=${state.colorId};show=${if (state.showDate) "1" else "0"}"

internal data class RingerVariantState(
    val styleId: String,
    val colorId: String,
)

internal data class RingerPackOption(
    val id: String,
    val label: String,
    val muteDrawableName: String,
    val waveDrawableName: String,
)

private val RemovedRingerPackIndices = setOf(6, 8, 9, 12, 14)

internal val RingerPackOptions = (1..16)
    .filterNot { it in RemovedRingerPackIndices }
    .map { index ->
    val padded = index.toString().padStart(2, '0')
    RingerPackOption(
        id = "ringer_$padded",
        label = "Ringer $padded",
        muteDrawableName = "ringer_${padded}_mute",
        waveDrawableName = "ringer_${padded}_wave",
    )
}

private val RingerStyles = setOf("bell", "mute", "wave") + RingerPackOptions.map { it.id }

internal fun parseRingerVariant(raw: String?): RingerVariantState {
    val fallback = RingerVariantState(styleId = "bell", colorId = "blue")
    if (raw.isNullOrBlank()) return fallback
    if (";" !in raw && !raw.contains("=")) {
        val normalized = raw.lowercase()
        return when {
            normalized in RingerStyles -> fallback.copy(styleId = normalized)
            normalized in setOf("blue", "green", "orange", "black", "yellow") || isPickerColorVariant(raw) ->
                fallback.copy(colorId = raw)
            else -> fallback
        }
    }
    val pieces = raw.split(";").mapNotNull {
        val pair = it.split("=", limit = 2)
        if (pair.size == 2) pair[0] to pair[1] else null
    }.toMap()
    val style = (pieces["style"] ?: fallback.styleId).lowercase().takeIf { it in RingerStyles } ?: fallback.styleId
    val color = pieces["color"] ?: fallback.colorId
    return RingerVariantState(styleId = style, colorId = color)
}

internal fun encodeRingerVariant(state: RingerVariantState): String =
    "style=${state.styleId};color=${state.colorId}"

internal fun ringerDrawableName(styleId: String, ringerMode: Int): String? {
    val normalized = styleId.lowercase()
    return when {
        normalized in RingerPackOptions.map { it.id }.toSet() -> {
            "${normalized}_${if (ringerMode == 1) "wave" else "mute"}"
        }
        else -> null
    }
}

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

internal data class EmotionVariantState(
    val emotionId: String,
    val enabled: Boolean,
)

internal fun parseEmotionVariant(raw: String?): EmotionVariantState {
    val fallback = EmotionVariantState(
        emotionId = EmotionOptions.first().id,
        enabled = true,
    )
    if (raw.isNullOrBlank()) return fallback
    if (";" !in raw && !raw.contains("=")) {
        val id = EmotionOptions.firstOrNull { it.id == raw }?.id ?: fallback.emotionId
        return EmotionVariantState(emotionId = id, enabled = true)
    }
    val pieces = raw.split(";").mapNotNull {
        val pair = it.split("=", limit = 2)
        if (pair.size == 2) pair[0] to pair[1] else null
    }.toMap()
    val id = EmotionOptions.firstOrNull { it.id == pieces["emotion"] }?.id
        ?: EmotionOptions.firstOrNull { it.id == pieces["style"] }?.id
        ?: fallback.emotionId
    val enabled = (pieces["enabled"] ?: pieces["show"] ?: "1") == "1"
    return EmotionVariantState(emotionId = id, enabled = enabled)
}

internal fun encodeEmotionVariant(state: EmotionVariantState): String =
    "emotion=${state.emotionId};enabled=${if (state.enabled) "1" else "0"}"
