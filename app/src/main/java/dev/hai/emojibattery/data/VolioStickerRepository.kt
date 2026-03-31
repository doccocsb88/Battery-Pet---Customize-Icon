package dev.hai.emojibattery.data

import android.content.Context
import android.net.Uri
import android.util.Log
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
    private const val TAG = "StickerCatalog"

    private val gson = Gson()
    private val categoryListType = object : TypeToken<VolioListResponse<VolioCategoryDto>>() {}.type
    private val itemListType = object : TypeToken<VolioListResponse<VolioEmojiBatteryItemDto>>() {}.type

    fun fetchStickerPresets(context: Context): List<StickerPreset> {
        val root = StoreOnDemandAssetPack.assetsRootOrNull(context)
        Log.d(TAG, "fetchStickerPresets: assetsRoot=${root?.absolutePath ?: "null"}")
        if (root != null) {
            val categoriesFile = File(root, "store/sticker/categories_all.json")
            Log.d(
                TAG,
                "fetchStickerPresets: categoriesFile=${categoriesFile.absolutePath} exists=${categoriesFile.isFile}",
            )
            if (categoriesFile.isFile) {
                val categoriesJson = categoriesFile.readText(Charsets.UTF_8)
                val categoriesResponse: VolioListResponse<VolioCategoryDto> = gson.fromJson(categoriesJson, categoryListType)
                Log.d(
                    TAG,
                    "fetchStickerPresets: categoriesCount=${categoriesResponse.data.orEmpty().size}",
                )
                val allCategory = categoriesResponse.data.orEmpty()
                    .filter { it.status != false }
                    .firstOrNull { it.name?.contains("All", ignoreCase = true) == true }
                    ?: categoriesResponse.data.orEmpty().firstOrNull { it.status != false }
                if (allCategory != null) {
                    Log.d(
                        TAG,
                        "fetchStickerPresets: selectedCategory id=${allCategory.id} name=${allCategory.name} status=${allCategory.status}",
                    )
                    val dir = File(root, "store/sticker/items/${allCategory.id}")
                    Log.d(
                        TAG,
                        "fetchStickerPresets: itemsDir=${dir.absolutePath} isDirectory=${dir.isDirectory}",
                    )
                    if (dir.isDirectory) {
                        val pages = dir.listFiles()
                            ?.filter { it.isFile && it.name.startsWith("page_") && it.name.endsWith(".json") }
                            ?.sortedBy { it.name }
                            .orEmpty()
                        Log.d(
                            TAG,
                            "fetchStickerPresets: pageFiles count=${pages.size} names=${pages.take(6).joinToString { it.name }}",
                        )
                        if (pages.isNotEmpty()) {
                            val merged = buildList {
                                pages.forEach { page ->
                                    val json = page.readText(Charsets.UTF_8)
                                    val response: VolioListResponse<VolioEmojiBatteryItemDto> = gson.fromJson(json, itemListType)
                                    Log.d(
                                        TAG,
                                        "fetchStickerPresets: page=${page.name} itemCount=${response.data.orEmpty().size}",
                                    )
                                    addAll(response.data.orEmpty())
                                }
                            }
                            val mapped = merged.map { it.toStickerPreset(root) }
                            logMappedSummary("pad", mapped)
                            return mapped
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
        Log.d(TAG, "fetchStickerPresets: bundledAssetNames count=${names.size}")
        if (names.isEmpty()) return emptyList()
        val mapped = names.asSequence()
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
        logMappedSummary("bundled_fallback", mapped)
        return mapped
    }

    private fun logMappedSummary(source: String, mapped: List<StickerPreset>) {
        val withThumb = mapped.count { !it.thumbnailUrl.isNullOrBlank() }
        val withLottie = mapped.count { !it.lottieUrl.isNullOrBlank() }
        val emptyMedia = mapped.count { it.thumbnailUrl.isNullOrBlank() && it.lottieUrl.isNullOrBlank() }
        Log.d(
            TAG,
            "fetchStickerPresets: source=$source mappedCount=${mapped.size} withThumb=$withThumb withLottie=$withLottie emptyMedia=$emptyMedia",
        )
        mapped.take(5).forEachIndexed { index, item ->
            Log.d(
                TAG,
                "fetchStickerPresets: sample[$index] id=${item.id} name=${item.name} thumb=${item.thumbnailUrl} lottie=${item.lottieUrl} premium=${item.premium}",
            )
        }
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
