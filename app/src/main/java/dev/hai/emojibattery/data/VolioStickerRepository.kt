package dev.hai.emojibattery.data

import android.content.Context
import android.net.Uri
import android.util.Log
import co.q7labs.co.emoji.BuildConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.hai.emojibattery.data.assets.StoreOnDemandAssetPack
import dev.hai.emojibattery.data.volio.VolioCategoryDto
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.data.volio.VolioListResponse
import dev.hai.emojibattery.model.StickerPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Offline sticker feed from PAD (`store/sticker/...`), with optional bundled assets fallback.
 */
object VolioStickerRepository {
    private const val TAG = "StickerCatalog"
    private const val STICKERS_PER_CATALOG_PAGE = 16

    private val gson = Gson()
    private val categoryListType = object : TypeToken<VolioListResponse<VolioCategoryDto>>() {}.type
    private val itemListType = object : TypeToken<VolioListResponse<VolioEmojiBatteryItemDto>>() {}.type

    data class StickerCatalogPageInfo(
        val pageFile: File,
        val mediaRoot: File,
    )

    suspend fun stickerCatalogPageCount(context: Context): Int = withContext(Dispatchers.IO) {
        val padPages = loadPadLogicalPages(context)
        if (!padPages.isNullOrEmpty()) return@withContext padPages.size
        bundledFallback(context)
            .chunked(STICKERS_PER_CATALOG_PAGE)
            .let { if (it.isEmpty()) 0 else it.size }
    }

    suspend fun fetchStickerPresetsPage(context: Context, pageIndex: Int): List<StickerPreset> = withContext(Dispatchers.IO) {
        val padPages = loadPadLogicalPages(context)
        if (!padPages.isNullOrEmpty()) {
            val page = padPages.getOrNull(pageIndex).orEmpty()
            logMappedSummary("pad_page_$pageIndex", page)
            return@withContext page
        }
        val fallbackPages = bundledFallback(context).chunked(STICKERS_PER_CATALOG_PAGE)
        val fallback = fallbackPages.getOrNull(pageIndex).orEmpty()
        if (fallback.isEmpty()) return@withContext emptyList()
        logMappedSummary("bundled_fallback_page_0", fallback)
        fallback
    }

    suspend fun fetchStickerPresets(context: Context): List<StickerPreset> {
        val pageCount = stickerCatalogPageCount(context)
        if (pageCount <= 0) return emptyList()
        return buildList {
            repeat(pageCount) { pageIndex ->
                addAll(fetchStickerPresetsPage(context, pageIndex))
            }
        }
    }

    private suspend fun resolvePadCatalog(context: Context): List<StickerCatalogPageInfo>? {
        val appContext = context.applicationContext
        val stickerPackNames = BuildConfig.STICKER_ASSET_PACKS.toList()
        if (stickerPackNames.isEmpty()) {
            Log.d(TAG, "fetchStickerPresets: no sticker asset packs configured")
            return null
        }
        val packRoots = buildList<Pair<String, File>> {
            stickerPackNames.forEach { packName ->
                val ready = StoreOnDemandAssetPack.waitUntilCompleted(appContext, packName)
                val root = StoreOnDemandAssetPack.assetsRootOrNull(appContext, packName)
                Log.d(
                    TAG,
                    "fetchStickerPresets: pack=$packName ready=$ready root=${root?.absolutePath ?: "null"}",
                )
                if (ready && root != null) add(packName to root)
            }
        }
        if (packRoots.isEmpty()) return null
        val categoriesFile = packRoots
            .asSequence()
            .map { (_, root) -> File(root, "store/sticker/categories_all.json") }
            .firstOrNull { it.isFile }
            ?: return null
        Log.d(TAG, "fetchStickerPresets: categoriesFile=${categoriesFile.absolutePath} exists=${categoriesFile.isFile}")
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
            ?: return null
        Log.d(
            TAG,
            "fetchStickerPresets: selectedCategory id=${allCategory.id} name=${allCategory.name} status=${allCategory.status}",
        )
        val mediaPackName = BuildConfig.STICKER_MEDIA_ASSET_PACK
        val mediaRoot = run {
            val ready = StoreOnDemandAssetPack.waitUntilCompleted(appContext, mediaPackName)
            val root = StoreOnDemandAssetPack.assetsRootOrNull(appContext, mediaPackName)
            Log.d(
                TAG,
                "fetchStickerPresets: mediaPack=$mediaPackName ready=$ready root=${root?.absolutePath ?: "null"}",
            )
            root
        }
        val pages = packRoots.flatMap { (packName, root) ->
            val dir = File(root, "store/sticker/items/${allCategory.id}")
            Log.d(
                TAG,
                "fetchStickerPresets: itemsDir=${dir.absolutePath} isDirectory=${dir.isDirectory} pack=$packName",
            )
            dir.listFiles()
                ?.filter { it.isFile && it.name.startsWith("page_") && it.name.endsWith(".json") }
                ?.sortedBy { it.name }
                ?.map { pageFile ->
                    StickerCatalogPageInfo(
                        pageFile = pageFile,
                        mediaRoot = mediaRoot ?: root,
                    )
                }
                .orEmpty()
        }.sortedBy { it.pageFile.name }
        Log.d(
            TAG,
            "fetchStickerPresets: pageFiles count=${pages.size} names=${pages.take(6).joinToString { it.pageFile.name }}",
        )
        if (pages.isEmpty()) return null
        return pages
    }

    private fun bundledFallback(context: Context): List<StickerPreset> {
        val names = try {
            context.assets.list("bundled_volio/downloaded_assets/sticker")?.toSet().orEmpty()
        } catch (_: Exception) {
            emptySet()
        }
        Log.d(TAG, "fetchStickerPresets: bundledAssetNames count=${names.size}")
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

    private suspend fun loadPadLogicalPages(context: Context): List<List<StickerPreset>>? {
        val catalogPages = resolvePadCatalog(context) ?: return null
        val merged = buildList {
            catalogPages.forEach { pageInfo ->
                val json = pageInfo.pageFile.readText(Charsets.UTF_8)
                val response: VolioListResponse<VolioEmojiBatteryItemDto> = gson.fromJson(json, itemListType)
                Log.d(
                    TAG,
                    "loadPadLogicalPages: page=${pageInfo.pageFile.name} itemCount=${response.data.orEmpty().size}",
                )
                addAll(response.data.orEmpty().map { it.toStickerPreset(pageInfo.mediaRoot) })
            }
        }
        return merged.chunked(STICKERS_PER_CATALOG_PAGE)
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
