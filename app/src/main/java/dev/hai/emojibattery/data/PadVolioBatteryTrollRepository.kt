package dev.hai.emojibattery.data

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.hai.emojibattery.data.assets.StoreOnDemandAssetPack
import dev.hai.emojibattery.data.volio.VolioCategoryDto
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.data.volio.VolioListResponse
import dev.hai.emojibattery.model.BatteryTrollTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

private const val BUNDLED_ASSET_ROOT = "bundled_volio/battery_troll"

/**
 * Offline Battery Troll templates from PAD (`store/battery_troll/...`).
 * Scope is optional: returns empty when the pack does not include this folder.
 */
object PadVolioBatteryTrollRepository {

    private const val TAG = "PadVolioBatteryTroll"
    private val gson = Gson()
    private val categoryListType = object : TypeToken<VolioListResponse<VolioCategoryDto>>() {}.type
    private val itemListType = object : TypeToken<VolioListResponse<VolioEmojiBatteryItemDto>>() {}.type

    suspend fun fetchTemplates(context: Context): List<BatteryTrollTemplate> = withContext(Dispatchers.IO) {
        val ready = withTimeoutOrNull(12_000L) {
            runCatching {
                StoreOnDemandAssetPack.waitUntilCompleted(context.applicationContext)
            }.getOrDefault(false)
        } ?: false
        if (!ready) {
            Log.w(TAG, "fetchTemplates: waitUntilCompleted=false pack=${StoreOnDemandAssetPack.PACK_NAME}")
        }

        val root = StoreOnDemandAssetPack.assetsRootOrNull(context.applicationContext)
        val categoriesJson = readCategoriesJson(context.assets, root) ?: run {
            Log.d(TAG, "fetchTemplates: no categories in PAD or bundled assets")
            return@withContext emptyList()
        }
        val categoriesResponse: VolioListResponse<VolioCategoryDto> = gson.fromJson(categoriesJson, categoryListType)
        val allCategory = categoriesResponse.data.orEmpty()
            .filter { it.status != false }
            .firstOrNull { it.name?.contains("All", ignoreCase = true) == true }
            ?: categoriesResponse.data.orEmpty().firstOrNull { it.status != false }
        val categoryId = allCategory?.id ?: firstBundledCategoryId(context.assets)
            ?: return@withContext emptyList()
        val pages = readPageJsons(context.assets, root, categoryId)
        if (pages.isEmpty()) {
            Log.d(TAG, "fetchTemplates: no item pages for category=$categoryId root=${root?.absolutePath}")
            return@withContext emptyList()
        }

        val merged = buildList {
            pages.forEach { json ->
                val response: VolioListResponse<VolioEmojiBatteryItemDto> = gson.fromJson(json, itemListType)
                addAll(response.data.orEmpty())
            }
        }
        val mapped = merged.map { dto -> dto.toPadBatteryTrollTemplate(root, categoryId) }
        Log.d(
            TAG,
            "fetchTemplates: loaded templates=${mapped.size} category=$categoryId root=${root?.absolutePath}",
        )
        mapped
    }
}

private fun readCategoriesJson(assets: AssetManager, padRoot: File?): String? {
    val fromPad = padRoot?.let { File(it, "store/battery_troll/categories_all.json") }
        ?.takeIf { it.isFile }
        ?.readText(Charsets.UTF_8)
    if (fromPad != null) return fromPad
    return runCatching {
        assets.open("$BUNDLED_ASSET_ROOT/categories_all.json").bufferedReader().use { it.readText() }
    }.getOrNull()
}

private fun firstBundledCategoryId(assets: AssetManager): String? =
    runCatching { assets.list("$BUNDLED_ASSET_ROOT/items")?.firstOrNull() }.getOrNull()

private fun readPageJsons(
    assets: AssetManager,
    padRoot: File?,
    categoryId: String,
): List<String> {
    val fromPad = padRoot?.let { File(it, "store/battery_troll/items/$categoryId") }
        ?.takeIf { it.isDirectory }
        ?.listFiles()
        ?.filter { it.isFile && it.name.startsWith("page_") && it.name.endsWith(".json") }
        ?.sortedBy { it.name }
        ?.map { it.readText(Charsets.UTF_8) }
        .orEmpty()
    if (fromPad.isNotEmpty()) return fromPad

    val itemDir = "$BUNDLED_ASSET_ROOT/items/$categoryId"
    val files = runCatching {
        assets.list(itemDir)
            ?.filter { it.startsWith("page_") && it.endsWith(".json") }
            ?.sorted()
            .orEmpty()
    }.getOrElse { emptyList() }
    return files.mapNotNull { name ->
        runCatching { assets.open("$itemDir/$name").bufferedReader().use { it.readText() } }.getOrNull()
    }
}

private fun VolioEmojiBatteryItemDto.toPadBatteryTrollTemplate(
    padRoot: File?,
    categoryId: String,
): BatteryTrollTemplate {
    val contentUrl = customFields?.content?.takeIf { it.isNotBlank() }
    val lottieUrl = contentUrl?.takeIf { it.endsWith(".json", ignoreCase = true) }
    val thumb = thumbnail?.takeIf { it.isNotBlank() }
    val title = name?.takeIf { it.isNotBlank() } ?: "Battery Troll"
    val animated = lottieUrl != null || thumb?.endsWith(".gif", ignoreCase = true) == true
    val prank = title.replace("\n", " ").trim().let { if (it.length > 16) it.take(16) + "…" else it }

    val localThumb = resolvePadBatteryTrollAssetUri(
        padRoot = padRoot,
        categoryId = categoryId,
        itemId = id,
        role = "thumbnail",
        remoteUrlOrName = thumb,
    )
    val localPhoto = resolvePadBatteryTrollAssetUri(
        padRoot = padRoot,
        categoryId = categoryId,
        itemId = id,
        role = "photo",
        remoteUrlOrName = photo,
    )
    val localContent = resolvePadBatteryTrollAssetUri(
        padRoot = padRoot,
        categoryId = categoryId,
        itemId = id,
        role = "custom_fields_content",
        remoteUrlOrName = contentUrl,
    )
    val resolvedLottie = localContent?.takeIf { it.endsWith(".json", ignoreCase = true) } ?: lottieUrl

    return BatteryTrollTemplate(
        id = id,
        title = title,
        summary = if (animated) "Animated prank template" else "Prank battery label",
        prankMessage = prank,
        accentGlyph = "🔋",
        premium = isPro == true,
        thumbnailUrl = localThumb ?: localPhoto ?: thumb,
        lottieUrl = resolvedLottie,
        emojiThumbnailUrl = localThumb ?: localPhoto ?: thumb,
        batteryThumbnailUrl = localPhoto ?: localThumb ?: thumb,
    )
}

private fun resolvePadBatteryTrollAssetUri(
    padRoot: File?,
    categoryId: String,
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
    val root = padRoot ?: return null
    val dir = File(root, "store/downloaded_assets/battery_troll")
    if (!dir.isDirectory) return null
    val prefix = "${categoryId}_${itemId}_${role}__"
    val flat = dir.listFiles()
        ?.firstOrNull { it.isFile && it.name.startsWith(prefix) && it.name.contains(hint) }
    if (flat != null) return Uri.fromFile(flat).toString()

    val nested = File(dir, "$categoryId/$itemId/${role}__${hint}")
    if (nested.isFile) return Uri.fromFile(nested).toString()

    val nestedByPrefix = File(dir, "$categoryId/$itemId").listFiles()
        ?.firstOrNull { it.isFile && it.name.startsWith("${role}__") && it.name.contains(hint) }
        ?: return null
    return Uri.fromFile(nestedByPrefix).toString()
}
