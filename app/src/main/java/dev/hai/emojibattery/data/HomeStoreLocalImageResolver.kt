package dev.hai.emojibattery.data

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import dev.hai.emojibattery.data.assets.StoreOnDemandAssetPack
import dev.hai.emojibattery.model.HomeBatteryItem
import java.io.File

/**
 * Resolves grid thumbnails to flat files under:
 * 1) PAD: `[pack]/store/downloaded_assets/home/` (preferred)
 * 2) APK assets: `bundled_volio/downloaded_assets/home/`
 *
 * If no local match, returns the original URL (e.g. https CDN).
 *
 * File names follow the crawl layout: `{categoryId}_{itemId}_{role}__...` (see [volio_download_assets.py]).
 * Matching uses the last path segment of a CDN URL (or a plain filename) as a hint.
 */
object HomeStoreLocalImageResolver {

    private const val ASSET_HOME_FLAT = "bundled_volio/downloaded_assets/home"

    @Volatile
    private var assetFlatNamesCache: List<String>? = null

    private fun assetFlatNames(assets: AssetManager): List<String> {
        assetFlatNamesCache?.let { return it }
        synchronized(this) {
            assetFlatNamesCache?.let { return it }
            val names = try {
                assets.list(ASSET_HOME_FLAT)?.toList().orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
            assetFlatNamesCache = names
            return names
        }
    }

    fun clearAssetNameCache() {
        synchronized(this) {
            assetFlatNamesCache = null
        }
    }

    private fun sanitizeLikeCrawler(input: String): String =
        input.replace(Regex("[^a-zA-Z0-9._-]+"), "_")

    /**
     * Prefer local/PAD file URI; otherwise returns [remoteUrlOrName] (typically https).
     *
     * [roleHints] match crawl filename prefixes:
     * - `thumbnail`
     * - `photo`
     * - `custom_fields_battery`
     * - `custom_fields_emoji`
     */
    fun resolveModel(
        context: Context,
        categoryId: String,
        itemId: String,
        remoteUrlOrName: String?,
        roleHints: List<String>,
    ): String? {
        val raw = remoteUrlOrName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val hint = hintFromUrlOrName(raw)
        val sanitizedHint = sanitizeLikeCrawler(hint)
        val prefix = "${categoryId}_${itemId}_"
        val roles = roleHints.map(::sanitizeLikeCrawler)

        val padRoot = StoreOnDemandAssetPack.assetsRootOrNull(context.applicationContext)
        if (padRoot != null) {
            val dir = File(padRoot, "store/downloaded_assets/home")
            if (dir.isDirectory) {
                val fromPad = dir.listFiles()
                    ?.asSequence()
                    ?.filter { it.isFile && it.name.startsWith(prefix) }
                    ?.filter { file -> roles.any { role -> file.name.contains("${role}__") } }
                    ?.firstOrNull { file ->
                        file.name.contains(hint) || file.name.contains(sanitizedHint)
                    }
                if (fromPad != null) return Uri.fromFile(fromPad).toString()
            }
        }

        val fromAssets = assetFlatNames(context.assets).firstOrNull { name ->
            name.startsWith(prefix) &&
                roles.any { role -> name.contains("${role}__") } &&
                (name.contains(hint) || name.contains(sanitizedHint))
        }
        if (fromAssets != null) {
            return "file:///android_asset/$ASSET_HOME_FLAT/$fromAssets"
        }

        return raw
    }

    fun enrichItems(context: Context, items: List<HomeBatteryItem>): List<HomeBatteryItem> =
        items.map { item ->
            val thumbnailSource = item.thumbnailUrl
            val batterySource = item.batteryArtUrl ?: item.thumbnailUrl
            val emojiSource = item.emojiArtUrl ?: item.thumbnailUrl
            val backgroundSource = item.backgroundPhotoUrl ?: item.thumbnailUrl

            val resolvedThumb = resolveModel(
                context = context,
                categoryId = item.categoryId,
                itemId = item.id,
                remoteUrlOrName = thumbnailSource,
                roleHints = listOf("thumbnail", "photo"),
            ) ?: thumbnailSource

            val resolvedBattery = resolveModel(
                context = context,
                categoryId = item.categoryId,
                itemId = item.id,
                remoteUrlOrName = batterySource,
                roleHints = listOf("custom_fields_battery", "battery", "thumbnail"),
            ) ?: batterySource

            val resolvedEmoji = resolveModel(
                context = context,
                categoryId = item.categoryId,
                itemId = item.id,
                remoteUrlOrName = emojiSource,
                roleHints = listOf("custom_fields_emoji", "emoji", "thumbnail"),
            ) ?: emojiSource

            val resolvedBackground = resolveModel(
                context = context,
                categoryId = item.categoryId,
                itemId = item.id,
                remoteUrlOrName = backgroundSource,
                roleHints = listOf("photo", "thumbnail"),
            ) ?: backgroundSource

            item.copy(
                thumbnailUrl = resolvedThumb,
                batteryArtUrl = resolvedBattery,
                emojiArtUrl = resolvedEmoji,
                backgroundPhotoUrl = resolvedBackground,
            )
        }

    private fun hintFromUrlOrName(raw: String): String {
        val path = raw.substringBefore('?').trim()
        return if (path.contains("://", ignoreCase = true)) {
            path.substringAfterLast('/')
        } else {
            path.substringAfterLast('/')
        }
    }
}
