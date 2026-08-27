package dev.hai.emojibattery.data

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import dev.hai.emojibattery.data.volio.VolioCategoryDto
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.data.volio.parseVolioCategories
import dev.hai.emojibattery.data.volio.parseVolioItems
import dev.hai.emojibattery.model.HomeBatteryItem
import dev.hai.emojibattery.model.HomeCategoryTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Offline fallback: same JSON shape as the public Volio API, shipped under assets
 * (`bundled_volio/home/...`). Thumbnails remain HTTPS URLs from the crawl so Coil can load them when
 * the device has network, even if the Volio API request fails.
 */
object BundledVolioHomeRepository {
    private const val TAG = "BundledVolioHome"

    private const val ASSET_ROOT = "bundled_volio/home"
    private const val CATEGORIES_ASSET_PATH = "$ASSET_ROOT/categories_all.json"

    fun hasBundledCatalog(assets: AssetManager): Boolean = runCatching {
        // Some Play-installed devices may not reliably report nested entries via assets.list().
        // Opening the file directly is a more robust existence check.
        assets.open(CATEGORIES_ASSET_PATH).use { true }
    }.getOrElse {
        false
    }

    suspend fun fetchCategoryTabs(context: Context): List<HomeCategoryTab> = withContext(Dispatchers.IO) {
        val am = context.assets
        if (!hasBundledCatalog(am)) return@withContext emptyList()
        val json = am.open(CATEGORIES_ASSET_PATH).bufferedReader().use { it.readText() }
        val rows = parseVolioCategories(json).filter { it.status != false }
        Log.d(TAG, "fetchCategoryTabs: loaded bundled rows=${rows.size}")
        rows.map { HomeCategoryTab(id = it.id, title = it.name.orEmpty()) }
    }

    suspend fun fetchItemsForCategory(context: Context, categoryId: String): List<HomeBatteryItem> =
        withContext(Dispatchers.IO) {
            val am = context.assets
            val itemDir = "$ASSET_ROOT/items/$categoryId"
            val files = try {
                am.list(itemDir)?.filter { it.endsWith(".json") && it.startsWith("page_") }?.sorted().orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
            if (files.isEmpty()) return@withContext emptyList()
            val merged = mutableListOf<VolioEmojiBatteryItemDto>()
            for (name in files) {
                val json = am.open("$itemDir/$name").bufferedReader().use { it.readText() }
                merged.addAll(parseVolioItems(json))
            }
            merged.map { dto -> dto.toHomeBatteryItem(categoryId) }.shuffled()
        }
}
