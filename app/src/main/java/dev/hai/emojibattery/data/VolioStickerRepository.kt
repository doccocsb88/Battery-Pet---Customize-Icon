package dev.hai.emojibattery.data

import dev.hai.emojibattery.data.volio.VolioConstants
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.data.volio.VolioNetwork
import dev.hai.emojibattery.model.StickerPreset

/**
 * Public Volio sticker feed (same API as original [EmojiStickerFragment] / [hungvv.GT]).
 */
object VolioStickerRepository {

    suspend fun fetchStickerPresets(): List<StickerPreset> {
        val categories = VolioNetwork.api.categoriesAll(VolioConstants.STICKER_PARENT_ID).data.orEmpty()
            .filter { it.status != false }
        val allCategory = categories.firstOrNull { it.name?.contains("All", ignoreCase = true) == true }
            ?: categories.firstOrNull()
            ?: return emptyList()
        val response = VolioNetwork.api.items(
            categoryId = allCategory.id,
            offset = 0,
            limit = VolioConstants.ITEM_PAGE_SIZE,
        )
        return response.data.orEmpty().map { it.toStickerPreset() }
    }
}

private fun VolioEmojiBatteryItemDto.toStickerPreset(): StickerPreset {
    val contentUrl = customFields?.content?.takeIf { it.isNotBlank() }
    val lottieUrl = contentUrl?.takeIf { it.endsWith(".json", ignoreCase = true) }
    val thumb = thumbnail?.takeIf { it.isNotBlank() }
    val photoUrl = photo?.takeIf { it.isNotBlank() }
    val animated = lottieUrl != null || thumb?.endsWith(".gif", ignoreCase = true) == true
    return StickerPreset(
        id = id,
        name = name?.takeIf { it.isNotBlank() } ?: "Sticker",
        glyph = "✨",
        premium = isPro == true,
        animated = animated,
        thumbnailUrl = thumb,
        lottieUrl = lottieUrl,
        remotePhotoUrl = photoUrl,
    )
}
