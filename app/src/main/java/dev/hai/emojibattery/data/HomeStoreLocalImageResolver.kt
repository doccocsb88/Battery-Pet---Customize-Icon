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

    /**
     * Prefer local/PAD file URI; otherwise returns [remoteUrlOrName] (typically https).
     */
    fun resolveThumbnailModel(
        context: Context,
        categoryId: String,
        itemId: String,
        remoteUrlOrName: String?,
    ): String? {
        val raw = remoteUrlOrName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val hint = hintFromUrlOrName(raw)
        val prefix = "${categoryId}_${itemId}_"

        val padRoot = StoreOnDemandAssetPack.assetsRootOrNull(context.applicationContext)
        if (padRoot != null) {
            val dir = File(padRoot, "store/downloaded_assets/home")
            if (dir.isDirectory) {
                val fromPad = dir.listFiles()
                    ?.asSequence()
                    ?.filter { it.isFile && it.name.startsWith(prefix) && it.name.contains(hint) }
                    ?.firstOrNull()
                if (fromPad != null) return Uri.fromFile(fromPad).toString()
            }
        }

        val fromAssets = assetFlatNames(context.assets).firstOrNull { name ->
            name.startsWith(prefix) && name.contains(hint)
        }
        if (fromAssets != null) {
            return "file:///android_asset/$ASSET_HOME_FLAT/$fromAssets"
        }

        return raw
    }

    fun enrichItems(context: Context, items: List<HomeBatteryItem>): List<HomeBatteryItem> =
        items.map { item ->
            val resolved = resolveThumbnailModel(context, item.categoryId, item.id, item.thumbnailUrl)
                ?: item.thumbnailUrl
            if (resolved == item.thumbnailUrl) item else item.copy(thumbnailUrl = resolved)
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
