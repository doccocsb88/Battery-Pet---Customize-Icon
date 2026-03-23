package dev.hai.emojibattery.data

import co.q7labs.co.emoji.R
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.model.HomeBatteryItem

internal fun VolioEmojiBatteryItemDto.toHomeBatteryItem(categoryId: String): HomeBatteryItem {
    val thumb = thumbnail?.takeIf { it.isNotBlank() }
    val photo = photo?.takeIf { it.isNotBlank() }
    val batteryArt = customFields?.battery?.takeIf { it.isNotBlank() }
    val emojiArt = customFields?.emoji?.takeIf { it.isNotBlank() }
    return HomeBatteryItem(
        id = id,
        categoryId = categoryId,
        title = name.orEmpty(),
        previewRes = R.drawable.ic_item_charge,
        thumbnailUrl = thumb,
        batteryArtUrl = batteryArt ?: thumb,
        emojiArtUrl = emojiArt ?: thumb,
        backgroundPhotoUrl = photo ?: thumb,
        premium = isPro == true,
        animated = thumbnail?.endsWith(".gif", ignoreCase = true) == true,
    )
}
