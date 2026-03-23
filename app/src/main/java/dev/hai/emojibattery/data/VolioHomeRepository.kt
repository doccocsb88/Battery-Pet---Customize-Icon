package dev.hai.emojibattery.data

import android.util.Log
import dev.hai.emojibattery.data.volio.VolioConstants
import dev.hai.emojibattery.data.volio.VolioNetwork
import dev.hai.emojibattery.model.HomeBatteryItem
import dev.hai.emojibattery.model.HomeCategoryTab

/**
 * Fetches the same public Volio store feed as the original app ([InterfaceC3491Va] + [hungvv.OS]).
 */
object VolioHomeRepository {

    private const val TAG = "HomeFeed"

    suspend fun fetchCategoryTabs(): List<HomeCategoryTab> {
        val response = VolioNetwork.api.categoriesAll(VolioConstants.PARENT_APP_ID)
        Log.d(
            TAG,
            "categoriesAll status=${response.status} message=${response.message} rawCount=${response.data?.size}",
        )
        val rows = response.data.orEmpty().filter { it.status != false }
        Log.d(TAG, "categoriesAll activeCount=${rows.size} first=${rows.firstOrNull()?.id} ${rows.firstOrNull()?.name}")
        return rows.map { HomeCategoryTab(id = it.id, title = it.name.orEmpty()) }
    }

    suspend fun fetchItemsForCategory(categoryId: String): List<HomeBatteryItem> {
        val response = VolioNetwork.api.items(
            categoryId = categoryId,
            offset = 0,
            limit = VolioConstants.ITEM_PAGE_SIZE,
        )
        val rows = response.data.orEmpty()
        Log.d(
            TAG,
            "items categoryId=$categoryId count=${rows.size} status=${response.status} " +
                "firstThumb=${rows.firstOrNull()?.thumbnail?.take(96)}",
        )
        return rows.map { dto -> dto.toHomeBatteryItem(categoryId) }.shuffled()
    }
}
