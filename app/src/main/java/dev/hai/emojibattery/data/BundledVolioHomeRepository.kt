package dev.hai.emojibattery.data

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.hai.emojibattery.data.volio.VolioCategoryDto
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.data.volio.VolioListResponse
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

    private const val ASSET_ROOT = "bundled_volio/home"

    private val gson = Gson()
    private val categoryListType = object : TypeToken<VolioListResponse<VolioCategoryDto>>() {}.type
    private val itemListType = object : TypeToken<VolioListResponse<VolioEmojiBatteryItemDto>>() {}.type

    fun hasBundledCatalog(assets: AssetManager): Boolean =
        assets.list("bundled_volio/home")?.contains("categories_all.json") == true

    suspend fun fetchCategoryTabs(context: Context): List<HomeCategoryTab> = withContext(Dispatchers.IO) {
        val am = context.assets
        if (!hasBundledCatalog(am)) return@withContext emptyList()
        val json = am.open("$ASSET_ROOT/categories_all.json").bufferedReader().use { it.readText() }
        val response: VolioListResponse<VolioCategoryDto> = gson.fromJson(json, categoryListType)
        val rows = response.data.orEmpty().filter { it.status != false }
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
                val response: VolioListResponse<VolioEmojiBatteryItemDto> = gson.fromJson(json, itemListType)
                merged.addAll(response.data.orEmpty())
            }
            merged.map { dto -> dto.toHomeBatteryItem(categoryId) }.shuffled()
        }
}
