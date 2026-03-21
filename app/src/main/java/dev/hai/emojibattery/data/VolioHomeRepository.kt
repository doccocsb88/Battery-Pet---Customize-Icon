package dev.hai.emojibattery.data

import co.q7labs.co.emoji.R
import dev.hai.emojibattery.data.volio.VolioConstants
import dev.hai.emojibattery.data.volio.VolioNetwork
import dev.hai.emojibattery.model.HomeBatteryItem
import dev.hai.emojibattery.model.HomeCategoryTab

/**
 * Fetches the same public Volio store feed as the original app ([InterfaceC3491Va] + [hungvv.OS]).
 */
object VolioHomeRepository {

    suspend fun fetchCategoryTabs(): List<HomeCategoryTab> {
        val response = VolioNetwork.api.categoriesAll(VolioConstants.PARENT_APP_ID)
        val rows = response.data.orEmpty().filter { it.status != false }
        return rows.map { HomeCategoryTab(id = it.id, title = it.name.orEmpty()) }
    }

    suspend fun fetchItemsForCategory(categoryId: String): List<HomeBatteryItem> {
        val response = VolioNetwork.api.items(
            categoryId = categoryId,
            offset = 0,
            limit = VolioConstants.ITEM_PAGE_SIZE,
        )
        val rows = response.data.orEmpty()
        return rows.map { dto ->
            HomeBatteryItem(
                id = dto.id,
                categoryId = categoryId,
                title = dto.name.orEmpty(),
                previewRes = R.drawable.ic_item_charge,
                thumbnailUrl = dto.thumbnail?.takeIf { it.isNotBlank() },
                premium = dto.isPro == true,
                animated = dto.thumbnail?.endsWith(".gif", ignoreCase = true) == true,
            )
        }.shuffled()
    }
}
