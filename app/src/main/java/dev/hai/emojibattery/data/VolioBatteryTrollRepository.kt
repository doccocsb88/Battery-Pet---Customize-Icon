package dev.hai.emojibattery.data

import dev.hai.emojibattery.data.volio.VolioConstants
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.data.volio.VolioNetwork
import dev.hai.emojibattery.model.BatteryTrollTemplate

/**
 * Optional Volio feed for Battery Troll templates (same [items] DTO as sticker/battery store).
 * Disabled when [VolioConstants.BATTERY_TROLL_PARENT_ID] is blank — fill UUID from jadx in the original app.
 */
object VolioBatteryTrollRepository {

    suspend fun fetchTemplates(): List<BatteryTrollTemplate> {
        val parentId = VolioConstants.BATTERY_TROLL_PARENT_ID.trim()
        if (parentId.isEmpty()) return emptyList()
        val categories = VolioNetwork.api.categoriesAll(parentId).data.orEmpty()
            .filter { it.status != false }
        val allCategory = categories.firstOrNull { it.name?.contains("All", ignoreCase = true) == true }
            ?: categories.firstOrNull()
            ?: return emptyList()
        val response = VolioNetwork.api.items(
            categoryId = allCategory.id,
            offset = 0,
            limit = VolioConstants.ITEM_PAGE_SIZE,
        )
        return response.data.orEmpty().map { it.toBatteryTrollTemplate() }
    }
}

private fun VolioEmojiBatteryItemDto.toBatteryTrollTemplate(): BatteryTrollTemplate {
    val contentUrl = customFields?.content?.takeIf { it.isNotBlank() }
    val lottieUrl = contentUrl?.takeIf { it.endsWith(".json", ignoreCase = true) }
    val thumb = thumbnail?.takeIf { it.isNotBlank() }
    val title = name?.takeIf { it.isNotBlank() } ?: "Battery Troll"
    val animated = lottieUrl != null || thumb?.endsWith(".gif", ignoreCase = true) == true
    val prank = title.replace("\n", " ").trim().let { if (it.length > 16) it.take(16) + "…" else it }
    return BatteryTrollTemplate(
        id = id,
        title = title,
        summary = if (animated) "Animated prank template" else "Prank battery label",
        prankMessage = prank,
        accentGlyph = "🔋",
        premium = isPro == true,
        thumbnailUrl = thumb,
        lottieUrl = lottieUrl,
    )
}
