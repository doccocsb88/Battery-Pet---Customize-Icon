package dev.hai.emojibattery.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dev.hai.emojibattery.data.assets.StoreOnDemandAssetPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class PadWallpaperCategory(
    val id: String,
    @SerializedName("pack_name") val packName: String,
    @SerializedName("delivery_pack_name") val deliveryPackName: String,
    val title: String?,
    @SerializedName("thumbnail_asset_path") val thumbnailAssetPath: String,
    val items: List<PadWallpaperItemMeta>,
)

data class PadWallpaperItemMeta(
    val id: String,
    val name: String?,
    val file: String,
    val path: String,
)

data class PadWallpaperItem(
    val id: String,
    val name: String,
    val assetUrl: String,
)

object PadWallpaperRepository {
    private const val MANIFEST_ASSET_PATH = "wallpapers/wallpaper_pack_manifest.json"
    private val gson = Gson()
    private val categoriesType = object : TypeToken<List<PadWallpaperCategory>>() {}.type

    suspend fun loadCategories(context: Context): List<PadWallpaperCategory> = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open(MANIFEST_ASSET_PATH).bufferedReader().use { reader ->
                gson.fromJson<List<PadWallpaperCategory>>(reader, categoriesType).orEmpty()
            }
        }.getOrElse { emptyList() }
    }

    fun thumbnailAssetUrl(category: PadWallpaperCategory): String =
        "file:///android_asset/${category.thumbnailAssetPath.trimStart('/')}"

    suspend fun loadItemsForCategory(
        context: Context,
        category: PadWallpaperCategory,
    ): List<PadWallpaperItem> = withContext(Dispatchers.IO) {
        val ready = runCatching {
            StoreOnDemandAssetPack.waitUntilCompleted(
                context = context.applicationContext,
                packName = category.deliveryPackName,
            )
        }.getOrDefault(false)
        if (!ready) return@withContext emptyList()

        val root = StoreOnDemandAssetPack.assetsRootOrNull(
            context = context.applicationContext,
            packName = category.deliveryPackName,
        ) ?: return@withContext emptyList()

        category.items.mapNotNull { item ->
            val file = File(root, item.path.trimStart('/'))
            if (!file.isFile) return@mapNotNull null
            PadWallpaperItem(
                id = item.id,
                name = item.name?.takeIf { it.isNotBlank() } ?: item.file,
                assetUrl = Uri.fromFile(file).toString(),
            )
        }
    }
}
