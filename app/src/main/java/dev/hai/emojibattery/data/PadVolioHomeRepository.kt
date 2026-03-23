package dev.hai.emojibattery.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.hai.emojibattery.data.assets.StoreOnDemandAssetPack
import dev.hai.emojibattery.data.volio.VolioCategoryDto
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.data.volio.VolioListResponse
import dev.hai.emojibattery.model.HomeBatteryItem
import dev.hai.emojibattery.model.HomeCategoryTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Home catalog JSON from the on-demand asset pack (`store/home/...` under [StoreOnDemandAssetPack]).
 * Same schema as bundled Volio-shaped JSON.
 */
object PadVolioHomeRepository {

    private val gson = Gson()
    private val categoryListType = object : TypeToken<VolioListResponse<VolioCategoryDto>>() {}.type
    private val itemListType = object : TypeToken<VolioListResponse<VolioEmojiBatteryItemDto>>() {}.type

    suspend fun fetchCategoryTabs(context: Context): List<HomeCategoryTab> = withContext(Dispatchers.IO) {
        val file = StoreOnDemandAssetPack.storeFileOrNull(context, "store/home/categories_all.json")
            ?: return@withContext emptyList()
        val json = file.readText(Charsets.UTF_8)
        val response: VolioListResponse<VolioCategoryDto> = gson.fromJson(json, categoryListType)
        val rows = response.data.orEmpty().filter { it.status != false }
        rows.map { HomeCategoryTab(id = it.id, title = it.name.orEmpty()) }
    }

    suspend fun fetchItemsForCategory(context: Context, categoryId: String): List<HomeBatteryItem> =
        withContext(Dispatchers.IO) {
            val root = StoreOnDemandAssetPack.assetsRootOrNull(context) ?: return@withContext emptyList()
            val dir = File(root, "store/home/items/$categoryId")
            if (!dir.isDirectory) return@withContext emptyList()
            val files = dir.listFiles()
                ?.map { it.name }
                ?.filter { it.endsWith(".json") && it.startsWith("page_") }
                ?.sorted()
                .orEmpty()
            if (files.isEmpty()) return@withContext emptyList()
            val merged = mutableListOf<VolioEmojiBatteryItemDto>()
            for (name in files) {
                val f = File(dir, name)
                if (!f.isFile) continue
                val json = f.readText(Charsets.UTF_8)
                val response: VolioListResponse<VolioEmojiBatteryItemDto> = gson.fromJson(json, itemListType)
                merged.addAll(response.data.orEmpty())
            }
            merged.map { dto -> dto.toHomeBatteryItem(categoryId) }.shuffled()
        }
}
