package dev.hai.emojibattery.data.assets

import android.content.Context
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * On-demand Play Asset pack that mirrors [packName] in [store_pack] Gradle module.
 * Copy `volio_api_crawl` content under `store_pack/src/main/assets/store/` (home, sticker, downloaded_assets).
 *
 * PAD only applies when the app is installed from Play; sideloaded builds may not receive the pack.
 */
object StoreOnDemandAssetPack {

    const val PACK_NAME = "store_pack"

    /** Path segment under the pack assets root (see `store_pack/src/main/assets/store/`). */
    const val STORE_ASSETS_PREFIX = "store"

    fun manager(context: Context) =
        AssetPackManagerFactory.getInstance(context.applicationContext)

    /**
     * Triggers fetch if needed and waits until the pack reports [AssetPackStatus.COMPLETED].
     * @return false if state is missing or not completed (e.g. Play services unavailable).
     */
    suspend fun waitUntilCompleted(context: Context): Boolean {
        return waitUntilCompleted(context, PACK_NAME)
    }

    suspend fun waitUntilCompleted(context: Context, packName: String): Boolean {
        val assetPackManager = manager(context)
        assetPackManager.fetch(listOf(packName)).await()
        val states = assetPackManager.getPackStates(listOf(packName)).await()
        val state = states.packStates()[packName] ?: return false
        return state.status() == AssetPackStatus.COMPLETED
    }

    /**
     * Root directory of asset files inside the pack (contains the `store/` folder).
     * Null if the pack is not on device or not completed yet.
     */
    fun assetsRootOrNull(context: Context): File? {
        return assetsRootOrNull(context, PACK_NAME)
    }

    fun assetsRootOrNull(context: Context, packName: String): File? {
        val location = manager(context).getPackLocation(packName) ?: return null
        val path = location.assetsPath() ?: return null
        return File(path)
    }

    /**
     * Resolved `File` for paths like `store/home/categories_all.json` relative to [assetsRootOrNull].
     */
    fun storeFileOrNull(context: Context, relativePath: String): File? {
        val root = assetsRootOrNull(context) ?: return null
        val child = File(root, relativePath.trimStart('/'))
        return child.takeIf { it.exists() }
    }
}
