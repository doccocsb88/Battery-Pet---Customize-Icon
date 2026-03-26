package dev.hai.emojibattery.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.hai.emojibattery.data.assets.StoreOnDemandAssetPack
import dev.hai.emojibattery.data.volio.VolioCategoryDto
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.data.volio.VolioListResponse
import dev.hai.emojibattery.model.StickerPreset
import java.io.File

/**
 * Offline sticker feed from PAD (`store/sticker/...`), with optional bundled assets fallback.
 */
object VolioStickerRepository {

    private val gson = Gson()
    private val categoryListType = object : TypeToken<VolioListResponse<VolioCategoryDto>>() {}.type
    private val itemListType = object : TypeToken<VolioListResponse<VolioEmojiBatteryItemDto>>() {}.type

    fun fetchStickerPresets(context: Context): List<StickerPreset> {
        val root = StoreOnDemandAssetPack.assetsRootOrNull(context)
        if (root != null) {
            val categoriesFile = File(root, "store/sticker/categories_all.json")
            if (categoriesFile.isFile) {
                val categoriesJson = categoriesFile.readText(Charsets.UTF_8)
                val categoriesResponse: VolioListResponse<VolioCategoryDto> = gson.fromJson(categoriesJson, categoryListType)
                val allCategory = categoriesResponse.data.orEmpty()
                    .filter { it.status != false }
                    .firstOrNull { it.name?.contains("All", ignoreCase = true) == true }
                    ?: categoriesResponse.data.orEmpty().firstOrNull { it.status != false }
                if (allCategory != null) {
                    val dir = File(root, "store/sticker/items/${allCategory.id}")
                    if (dir.isDirectory) {
                        val pages = dir.listFiles()
                            ?.filter { it.isFile && it.name.startsWith("page_") && it.name.endsWith(".json") }
                            ?.sortedBy { it.name }
                            .orEmpty()
                        if (pages.isNotEmpty()) {
                            val merged = buildList {
                                pages.forEach { page ->
                                    val json = page.readText(Charsets.UTF_8)
                                    val response: VolioListResponse<VolioEmojiBatteryItemDto> = gson.fromJson(json, itemListType)
                                    addAll(response.data.orEmpty())
                                }
                            }
                            return merged.map { it.toStickerPreset(root) }
                        }
                    }
                }
            }
        }

        val names = try {
            context.assets.list("bundled_volio/downloaded_assets/sticker")?.toSet().orEmpty()
        } catch (_: Exception) {
            emptySet()
        }
        if (names.isEmpty()) return emptyList()
        return names.asSequence()
            .filter { it.contains("_thumbnail__") }
            .map { name ->
                val itemId = name.substringBefore("_thumbnail__")
                StickerPreset(
                    id = itemId,
                    name = itemId,
                    glyph = "✨",
                    thumbnailUrl = "file:///android_asset/bundled_volio/downloaded_assets/sticker/$name",
                    remotePhotoUrl = null,
                )
            }
            .toList()
    }
}

private fun VolioEmojiBatteryItemDto.toStickerPreset(padRoot: File): StickerPreset {
    val contentUrl = customFields?.content?.takeIf { it.isNotBlank() }
    val lottieUrl = contentUrl?.takeIf { it.endsWith(".json", ignoreCase = true) }
    val thumb = thumbnail?.takeIf { it.isNotBlank() }
    val photoUrl = photo?.takeIf { it.isNotBlank() }
    val animated = lottieUrl != null || thumb?.endsWith(".gif", ignoreCase = true) == true
    val localThumb = resolvePadStickerAssetUri(
        padRoot = padRoot,
        itemId = id,
        role = "thumbnail",
        remoteUrlOrName = thumb,
    )
    val localPhoto = resolvePadStickerAssetUri(
        padRoot = padRoot,
        itemId = id,
        role = "photo",
        remoteUrlOrName = photoUrl,
    )
    return StickerPreset(
        id = id,
        name = name?.takeIf { it.isNotBlank() } ?: "Sticker",
        glyph = "✨",
        premium = isPro == true,
        animated = animated,
        thumbnailUrl = localThumb ?: thumb,
        lottieUrl = lottieUrl,
        remotePhotoUrl = localPhoto ?: photoUrl,
    )
}

private fun resolvePadStickerAssetUri(
    padRoot: File,
    itemId: String,
    role: String,
    remoteUrlOrName: String?,
): String? {
    val hint = remoteUrlOrName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.substringBefore('?')
        ?.substringAfterLast('/')
        ?: return null
    val dir = File(padRoot, "store/downloaded_assets/sticker")
    if (!dir.isDirectory) return null
    val prefix = "${itemId}_${role}__"
    val file = dir.listFiles()
        ?.firstOrNull { it.isFile && it.name.startsWith(prefix) && it.name.contains(hint) }
        ?: return null
    return Uri.fromFile(file).toString()
}
