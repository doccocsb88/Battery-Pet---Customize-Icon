package dev.hai.emojibattery.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.hai.emojibattery.data.assets.StoreOnDemandAssetPack
import dev.hai.emojibattery.data.volio.VolioEmojiBatteryItemDto
import dev.hai.emojibattery.data.volio.VolioListResponse
import dev.hai.emojibattery.model.HomeBatteryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Home catalog JSON from the on-demand asset pack (`store/home/...` under [StoreOnDemandAssetPack]).
 * Same schema as bundled Volio-shaped JSON.
 */
object PadVolioHomeRepository {

    private const val TAG = "PadVolioHome"

    private val gson = Gson()
    private val itemListType = object : TypeToken<VolioListResponse<VolioEmojiBatteryItemDto>>() {}.type

    suspend fun fetchItemsForCategory(context: Context, categoryId: String): List<HomeBatteryItem> =
        withContext(Dispatchers.IO) {
            if (!HomePadCategoryRegistry.hasPadPack(categoryId)) {
                return@withContext emptyList()
            }
            val packName = HomeCategoryPackResolver.packNameFor(categoryId)
            val ready = StoreOnDemandAssetPack.waitUntilCompleted(
                context = context.applicationContext,
                packName = packName,
            )
            if (!ready) {
                Log.d(TAG, "fetchItemsForCategory: pack not ready category=$categoryId pack=$packName")
                return@withContext emptyList()
            }
            val root = StoreOnDemandAssetPack.assetsRootOrNull(
                context = context.applicationContext,
                packName = packName,
            ) ?: return@withContext emptyList()
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
            Log.d(TAG, "fetchItemsForCategory: category=$categoryId pack=$packName items=${merged.size}")
            merged.map { dto -> dto.toHomeBatteryItem(categoryId) }.shuffled()
        }
}
