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
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

private const val BUNDLED_ASSET_ROOT = "bundled_volio/battery_troll"
private const val PAD_BATTERY_TROLL_ASSET_DIR = "store/downloaded_assets/battery_troll"

/**
 * Offline Battery Troll templates from PAD (`store/battery_troll/...`).
 * Scope is optional: returns empty when the pack does not include this folder.
 */
object PadVolioBatteryTrollRepository {

    private const val TAG = "PadVolioBatteryTroll"
    private val gson = Gson()
    private val categoryListType =
        TypeToken.getParameterized(VolioListResponse::class.java, VolioCategoryDto::class.java).type
    private val itemListType =
        TypeToken.getParameterized(VolioListResponse::class.java, VolioEmojiBatteryItemDto::class.java).type

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
        Log.d(TAG, "fetchTemplates: assetsRoot=${root?.absolutePath ?: "null"}")
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
        Log.d(TAG, "fetchTemplates: categoryId=$categoryId pages=${pages.size}")
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
        val extractedRoot = File(context.cacheDir, "battery_troll_zip")
        val downloadedAssetCache = File(context.cacheDir, "pad_battery_troll_assets")
        val mapped = merged.map { dto ->
            dto.toPadBatteryTrollTemplate(
                padRoot = root,
                assets = context.assets,
                categoryId = categoryId,
                extractedRoot = extractedRoot,
                downloadedAssetCache = downloadedAssetCache,
            )
        }
        Log.d(
            TAG,
            "fetchTemplates: loaded templates=${mapped.size}/${merged.size} category=$categoryId root=${root?.absolutePath}",
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
    assets: AssetManager,
    categoryId: String,
    extractedRoot: File,
    downloadedAssetCache: File,
): BatteryTrollTemplate {
    val contentUrl = customFields?.content?.takeIf { it.isNotBlank() }
    val contentZipUrl = customFields?.contentZip?.takeIf { it.isNotBlank() }
    val lottieUrl = contentUrl?.takeIf { it.endsWith(".json", ignoreCase = true) }
    val thumb = thumbnail?.takeIf { it.isNotBlank() }
    val title = name?.takeIf { it.isNotBlank() } ?: "Battery Troll"
    val animated = lottieUrl != null || thumb?.endsWith(".gif", ignoreCase = true) == true
    val prank = title.replace("\n", " ").trim().let { if (it.length > 16) it.take(16) + "…" else it }

    val localThumb = resolvePadBatteryTrollAssetUri(
        padRoot = padRoot,
        assets = assets,
        categoryId = categoryId,
        itemId = id,
        role = "thumbnail",
        remoteUrlOrName = thumb,
        cacheDir = downloadedAssetCache,
    )
    val localPhoto = resolvePadBatteryTrollAssetUri(
        padRoot = padRoot,
        assets = assets,
        categoryId = categoryId,
        itemId = id,
        role = "photo",
        remoteUrlOrName = photo,
        cacheDir = downloadedAssetCache,
    )
    val localContent = resolvePadBatteryTrollAssetUri(
        padRoot = padRoot,
        assets = assets,
        categoryId = categoryId,
        itemId = id,
        role = "custom_fields_content",
        remoteUrlOrName = contentUrl,
        cacheDir = downloadedAssetCache,
    )
    val localContentZip = resolvePadBatteryTrollAssetUri(
        padRoot = padRoot,
        assets = assets,
        categoryId = categoryId,
        itemId = id,
        role = "custom_fields_contentZip",
        remoteUrlOrName = contentZipUrl,
        cacheDir = downloadedAssetCache,
    ) ?: resolvePadBatteryTrollAssetUri(
        padRoot = padRoot,
        assets = assets,
        categoryId = categoryId,
        itemId = id,
        role = "custom_fields_contentzip",
        remoteUrlOrName = contentZipUrl,
        cacheDir = downloadedAssetCache,
    )
    val resolvedZip = localContentZip
        ?: findSiblingZipForItem(id, localThumb, localPhoto, padRoot)
    val resolvedLottie = localContent?.takeIf { it.endsWith(".json", ignoreCase = true) } ?: lottieUrl
    Log.d(
        "PadVolioBatteryTroll",
        "template=$id resolvePaths thumb=${localThumb ?: "-"} photo=${localPhoto ?: "-"} content=${localContent ?: "-"} contentZip=${localContentZip ?: "-"} siblingZip=${resolvedZip ?: "-"}",
    )
    val (emojiFrames, batteryFrames) = extractZipFrames(
        zipUri = resolvedZip,
        templateId = id,
        outRoot = extractedRoot,
    )
    Log.d(
        "PadVolioBatteryTroll",
        "template=$id localZip=${resolvedZip ?: "-"} emoji=${emojiFrames.size} battery=${batteryFrames.size}",
    )

    return BatteryTrollTemplate(
        id = id,
        title = title,
        summary = if (animated) "Animated prank template" else "Prank battery label",
        prankMessage = prank,
        accentGlyph = "🔋",
        premium = isPro == true,
        thumbnailUrl = localThumb ?: localPhoto ?: thumb,
        lottieUrl = resolvedLottie,
        emojiThumbnailUrl = emojiFrames.firstOrNull() ?: localThumb ?: localPhoto ?: thumb,
        batteryThumbnailUrl = batteryFrames.firstOrNull() ?: localPhoto ?: localThumb ?: thumb,
        emojiOptionsUrls = emojiFrames,
        batteryOptionsUrls = batteryFrames,
    )
}

private fun extractZipFrames(
    zipUri: String?,
    templateId: String,
    outRoot: File,
): Pair<List<String>, List<String>> {
    if (zipUri.isNullOrBlank() || !zipUri.startsWith("file://")) {
        Log.d("PadVolioBatteryTroll", "extractZipFrames: skip template=$templateId zipUri=${zipUri ?: "null"}")
        return emptyList<String>() to emptyList()
    }
    val zipFile = File(Uri.parse(zipUri).path ?: return emptyList<String>() to emptyList())
    if (!zipFile.isFile) {
        Log.d("PadVolioBatteryTroll", "extractZipFrames: zip missing template=$templateId path=${zipFile.absolutePath}")
        return emptyList<String>() to emptyList()
    }
    Log.d(
        "PadVolioBatteryTroll",
        "extractZipFrames: start template=$templateId zip=${zipFile.absolutePath} size=${zipFile.length()}",
    )

    val templateDir = File(outRoot, templateId)
    if (!templateDir.exists()) templateDir.mkdirs()
    val marker = File(templateDir, ".done_${zipFile.length()}_${zipFile.lastModified()}")
    if (!marker.isFile) {
        templateDir.listFiles()?.forEach { if (it.isFile) it.delete() }
        runCatching {
            ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
                var entry = zis.nextEntry
                var extractedCount = 0
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val name = entry.name.substringAfterLast('/').trim()
                        val lower = name.lowercase()
                        if (lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                            val out = File(templateDir, name)
                            FileOutputStream(out).use { fos -> zis.copyTo(fos) }
                            extractedCount += 1
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
                Log.d("PadVolioBatteryTroll", "extractZipFrames: extracted template=$templateId count=$extractedCount")
            }
            marker.writeText("ok")
        }.onFailure {
            Log.w("PadVolioBatteryTroll", "extractZipFrames: failed template=$templateId zip=${zipFile.name}", it)
        }
    }

    val images = templateDir.listFiles()
        ?.filter { it.isFile && !it.name.startsWith(".done_") }
        ?.sortedBy { it.name.lowercase() }
        .orEmpty()
    Log.d(
        "PadVolioBatteryTroll",
        "extractZipFrames: imageFiles template=$templateId count=${images.size} names=${images.take(12).joinToString { it.name }}",
    )
    val emoji = images.filter {
        val n = it.name.lowercase()
        n.startsWith("emoji") || n.contains("emoji") || n.contains("emotion")
    }
        .map { Uri.fromFile(it).toString() }
    val battery = images.filter {
        val n = it.name.lowercase()
        n.startsWith("battery") || n.contains("battery")
    }
        .map { Uri.fromFile(it).toString() }

    if (emoji.isNotEmpty() || battery.isNotEmpty()) {
        Log.d(
            "PadVolioBatteryTroll",
            "extractZipFrames: classified template=$templateId emoji=${emoji.size} battery=${battery.size}",
        )
        return emoji.take(5) to battery.take(5)
    }
    val fallback = images.map { Uri.fromFile(it).toString() }.take(10)
    Log.d(
        "PadVolioBatteryTroll",
        "extractZipFrames: fallbackSplit template=$templateId fallback=${fallback.size}",
    )
    return fallback.take(5) to fallback.drop(5).take(5)
}

private fun resolvePadBatteryTrollAssetUri(
    padRoot: File?,
    assets: AssetManager,
    categoryId: String,
    itemId: String,
    role: String,
    remoteUrlOrName: String?,
    cacheDir: File,
): String? {
    val hint = remoteUrlOrName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.substringBefore('?')
        ?.substringAfterLast('/')
        ?: return null
    val prefix = "${categoryId}_${itemId}_${role}__"
    val rolePrefix = "${role}__"
    val root = padRoot
    if (root != null) {
        val dir = File(root, PAD_BATTERY_TROLL_ASSET_DIR)
        if (dir.isDirectory) {
            val flat = dir.listFiles()
                ?.firstOrNull { it.isFile && it.name.startsWith(prefix) && it.name.contains(hint) }
            if (flat != null) return Uri.fromFile(flat).toString()
            val flatByPrefix = dir.listFiles()
                ?.firstOrNull { it.isFile && it.name.startsWith(prefix) }
            if (flatByPrefix != null) return Uri.fromFile(flatByPrefix).toString()

            val nested = File(dir, "$categoryId/$itemId/${role}__${hint}")
            if (nested.isFile) return Uri.fromFile(nested).toString()

            val nestedByPrefix = File(dir, "$categoryId/$itemId").listFiles()
                ?.firstOrNull { it.isFile && it.name.startsWith("${role}__") && it.name.contains(hint) }
            if (nestedByPrefix != null) return Uri.fromFile(nestedByPrefix).toString()
            val nestedByRole = File(dir, "$categoryId/$itemId").listFiles()
                ?.firstOrNull { it.isFile && it.name.startsWith("${role}__") }
            if (nestedByRole != null) return Uri.fromFile(nestedByRole).toString()
        }
    }

    val flatAssetName = runCatching {
        assets.list(PAD_BATTERY_TROLL_ASSET_DIR)
            ?.firstOrNull { it.startsWith(prefix) && it.contains(hint) }
            ?: assets.list(PAD_BATTERY_TROLL_ASSET_DIR)?.firstOrNull { it.startsWith(prefix) }
    }.getOrNull()
    if (flatAssetName != null) {
        return copyAssetToCache(
            assets = assets,
            assetPath = "$PAD_BATTERY_TROLL_ASSET_DIR/$flatAssetName",
            cacheDir = cacheDir,
        )
    }

    val nestedDir = "$PAD_BATTERY_TROLL_ASSET_DIR/$categoryId/$itemId"
    val nestedAssetName = runCatching {
        assets.list(nestedDir)
            ?.firstOrNull { it.startsWith(rolePrefix) && it.contains(hint) }
            ?: assets.list(nestedDir)?.firstOrNull { it.startsWith(rolePrefix) }
    }.getOrNull()
    if (nestedAssetName != null) {
        return copyAssetToCache(
            assets = assets,
            assetPath = "$nestedDir/$nestedAssetName",
            cacheDir = cacheDir,
        )
    }

    // Last fallback: match by itemId + role only (no hint) to tolerate name drift.
    val byItemRoleFlat = runCatching {
        assets.list(PAD_BATTERY_TROLL_ASSET_DIR)
            ?.firstOrNull { it.contains("_${itemId}_${role}__", ignoreCase = true) }
    }.getOrNull()
    if (byItemRoleFlat != null) {
        return copyAssetToCache(
            assets = assets,
            assetPath = "$PAD_BATTERY_TROLL_ASSET_DIR/$byItemRoleFlat",
            cacheDir = cacheDir,
        )
    }
    val byItemRoleNested = runCatching {
        assets.list(nestedDir)
            ?.firstOrNull { it.startsWith(rolePrefix, ignoreCase = true) }
    }.getOrNull()
    if (byItemRoleNested != null) {
        return copyAssetToCache(
            assets = assets,
            assetPath = "$nestedDir/$byItemRoleNested",
            cacheDir = cacheDir,
        )
    }

    Log.d(
        "PadVolioBatteryTroll",
        "resolve asset miss: item=$itemId role=$role hint=$hint flatDir=$PAD_BATTERY_TROLL_ASSET_DIR nestedDir=$nestedDir",
    )
    return null
}

private fun copyAssetToCache(
    assets: AssetManager,
    assetPath: String,
    cacheDir: File,
): String? {
    if (!cacheDir.exists()) cacheDir.mkdirs()
    val safeName = assetPath.substringAfterLast('/')
    val out = File(cacheDir, safeName)
    runCatching {
        if (!out.isFile || out.length() == 0L) {
            assets.open(assetPath).use { input ->
                FileOutputStream(out).use { output -> input.copyTo(output) }
            }
        }
        Uri.fromFile(out).toString()
    }.onFailure {
        Log.w("PadVolioBatteryTroll", "copyAssetToCache failed for $assetPath", it)
    }
    return if (out.isFile) Uri.fromFile(out).toString() else null
}

private fun findSiblingZipForItem(
    itemId: String,
    localThumb: String?,
    localPhoto: String?,
    padRoot: File?,
): String? {
    val roleToken = "_custom_fields_contentzip__"
    val fileUris = listOfNotNull(localThumb, localPhoto)
        .filter { it.startsWith("file://") }
        .mapNotNull { Uri.parse(it).path }
        .map(::File)

    fileUris.forEach { ref ->
        val dir = ref.parentFile ?: return@forEach
        val match = dir.listFiles()?.firstOrNull { file ->
            val name = file.name.lowercase()
            file.isFile &&
                name.endsWith(".zip") &&
                name.contains("_${itemId.lowercase()}_") &&
                name.contains(roleToken)
        }
        if (match != null) return Uri.fromFile(match).toString()
    }

    val padDir = padRoot?.let { File(it, PAD_BATTERY_TROLL_ASSET_DIR) }
    val padMatch = padDir?.listFiles()?.firstOrNull { file ->
        val name = file.name.lowercase()
        file.isFile &&
            name.endsWith(".zip") &&
            name.contains("_${itemId.lowercase()}_") &&
            name.contains(roleToken)
    }
    if (padMatch != null) return Uri.fromFile(padMatch).toString()
    return null
}
