package dev.hai.emojibattery.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dev.hai.emojibattery.data.assets.StoreOnDemandAssetPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

data class PadBackgroundTemplateCategory(
    @SerializedName("pack_name") val packName: String,
    @SerializedName("delivery_pack_name") val deliveryPackName: String,
    val title: String?,
    val items: List<PadBackgroundTemplateItemMeta>,
)

data class PadBackgroundTemplateItemMeta(
    val id: String,
    val name: String?,
    val file: String,
    val path: String,
)

data class PadBackgroundTemplateItem(
    val id: String,
    val name: String,
    val assetUrl: String,
)

object PadBackgroundTemplateRepository {
    private const val TAG = "BgTemplatePAD"
    private const val MANIFEST_ASSET_PATH = "background_templates/themes_pack_manifest.json"
    private val FALLBACK_PACKS = setOf("chinese_spring_landscape", "countryside")
    private val gson = Gson()
    private val categoriesType = object : TypeToken<List<PadBackgroundTemplateCategory>>() {}.type

    suspend fun loadCategories(context: Context): List<PadBackgroundTemplateCategory> = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open(MANIFEST_ASSET_PATH).bufferedReader().use { reader ->
                gson.fromJson<List<PadBackgroundTemplateCategory>>(reader, categoriesType).orEmpty()
            }
        }.onSuccess {
            Log.d(TAG, "loadCategories: count=${it.size} names=${it.joinToString { c -> c.deliveryPackName }}")
        }.onFailure {
            Log.e(TAG, "loadCategories: failed path=$MANIFEST_ASSET_PATH", it)
        }.getOrElse { emptyList() }
    }

    suspend fun loadItemsForCategory(
        context: Context,
        category: PadBackgroundTemplateCategory,
    ): List<PadBackgroundTemplateItem> = withContext(Dispatchers.IO) {
        Log.d(TAG, "loadItemsForCategory: pack=${category.deliveryPackName} requestedItems=${category.items.size}")
        val ready = withTimeoutOrNull(12_000L) {
            runCatching {
                StoreOnDemandAssetPack.waitUntilCompleted(
                    context = context.applicationContext,
                    packName = category.deliveryPackName,
                )
            }.getOrDefault(false)
        } ?: false
        if (!ready) {
            Log.w(TAG, "loadItemsForCategory: waitUntilCompleted=false pack=${category.deliveryPackName}")
            return@withContext loadFallbackItemsFromBundledAssets(context, category)
        }

        val root = StoreOnDemandAssetPack.assetsRootOrNull(
            context = context.applicationContext,
            packName = category.deliveryPackName,
        )
        if (root == null) {
            Log.w(TAG, "loadItemsForCategory: assetsRootOrNull=null pack=${category.deliveryPackName}")
            return@withContext loadFallbackItemsFromBundledAssets(context, category)
        }
        Log.d(TAG, "loadItemsForCategory: root=${root.absolutePath}")

        var missing = 0
        val resolved = category.items.mapNotNull { item ->
            val file = File(root, item.path.trimStart('/'))
            if (!file.isFile) {
                missing += 1
                return@mapNotNull null
            }
            PadBackgroundTemplateItem(
                id = item.id,
                name = item.name?.takeIf { it.isNotBlank() } ?: item.id,
                assetUrl = Uri.fromFile(file).toString(),
            )
        }
        Log.d(
            TAG,
            "loadItemsForCategory: pack=${category.deliveryPackName} loaded=${resolved.size} missing=$missing",
        )
        resolved
    }

    private fun loadFallbackItemsFromBundledAssets(
        context: Context,
        category: PadBackgroundTemplateCategory,
    ): List<PadBackgroundTemplateItem> {
        if (category.deliveryPackName !in FALLBACK_PACKS) return emptyList()
        val assets = context.assets
        val fallback = category.items.mapNotNull { item ->
            val assetPath = item.path.trimStart('/')
            val exists = runCatching {
                assets.open(assetPath).use { input -> input.read() >= 0 }
            }.getOrDefault(false)
            if (!exists) return@mapNotNull null
            PadBackgroundTemplateItem(
                id = item.id,
                name = item.name?.takeIf { it.isNotBlank() } ?: item.id,
                assetUrl = "file:///android_asset/$assetPath",
            )
        }
        Log.d(
            TAG,
            "loadItemsForCategory: bundled fallback pack=${category.deliveryPackName} loaded=${fallback.size}",
        )
        return fallback
    }
}
