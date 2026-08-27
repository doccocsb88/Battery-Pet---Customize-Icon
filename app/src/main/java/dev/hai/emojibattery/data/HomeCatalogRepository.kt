package dev.hai.emojibattery.data

import dev.hai.emojibattery.model.HomeBatteryItem
import dev.hai.emojibattery.model.HomeCategoryTab
import dev.hai.emojibattery.model.SampleCatalog

/**
 * Local catalog backing the home feed. The original app loads categories and per-category
 * items asynchronously via a use case ([OS]); here the same data lives in [SampleCatalog].
 *
 * Sub-home in the original app shuffles the emoji-battery list on each successful load
 * (see decompiled SubHomeViewModel$getListEmojiBattery$1).
 */
object HomeCatalogRepository {

    fun categoryTabs(): List<HomeCategoryTab> =
        SampleCatalog.homeCategories.map { HomeCategoryTab(id = it.id, title = it.title) }

    /**
     * Returns a shuffled copy of items for [categoryId], preserving item ids derived from
     * [SampleCatalog.buildHomeItems] (stable per index before shuffle).
     */
    fun loadItemsForCategory(categoryId: String): List<HomeBatteryItem> {
        val category = SampleCatalog.homeCategories.firstOrNull { it.id == categoryId }
            ?: return emptyList()
        return category.items.shuffled()
    }
}
