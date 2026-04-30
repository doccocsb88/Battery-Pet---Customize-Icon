package dev.hai.emojibattery.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import dev.hai.emojibattery.data.assets.StoreOnDemandAssetPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class PadWallpaperCategory(
    @SerializedName("id")
    val id: String,
    @SerializedName("pack_name") val packName: String,
    @SerializedName("delivery_pack_name") val deliveryPackName: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("thumbnail_asset_path") val thumbnailAssetPath: String,
    val itemCount: Int,
)

data class PadWallpaperItem(
    val id: String,
    val name: String,
    val assetUrl: String,
)

object PadWallpaperRepository {
    private const val TAG = "PadWallpaper"
    private const val MANIFEST_ASSET_PATH = "wallpapers/wallpaper_pack_manifest.json"
    private const val SLUG_INDEX_ASSET_PATH = "wallpapers/wallpaper_pack_slug_index.json"
    private const val MAX_CACHED_CATEGORY_ITEMS = 2
    private val gson = Gson()
    @Volatile
    private var categoriesCache: List<PadWallpaperCategory>? = null
    @Volatile
    private var categoryKeywordIndexCache: Map<String, List<String>>? = null
    private val cacheLock = Any()
    private val itemsCache = object : LinkedHashMap<String, List<PadWallpaperItem>>(
        MAX_CACHED_CATEGORY_ITEMS,
        0.75f,
        true,
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<PadWallpaperItem>>): Boolean =
            size > MAX_CACHED_CATEGORY_ITEMS
    }

    suspend fun loadCategories(context: Context): List<PadWallpaperCategory> = withContext(Dispatchers.IO) {
        categoriesCache?.let { return@withContext it }
        runCatching {
            val json = context.assets.open(MANIFEST_ASSET_PATH).bufferedReader().use { it.readText() }
            val arr = JsonParser.parseString(json).asJsonArray
            arr.mapNotNull { element ->
                runCatching { parseCategory(element.asJsonObject) }.getOrElse {
                    Log.w(TAG, "loadCategories: skip invalid category entry", it)
                    null
                }
            }
        }.getOrElse { error ->
            Log.w(TAG, "loadCategories: failed to read $MANIFEST_ASSET_PATH", error)
            emptyList()
        }
            .also { loaded ->
                if (loaded.isNotEmpty()) {
                    categoriesCache = loaded
                }
            }
    }

    fun peekCachedCategories(): List<PadWallpaperCategory>? = categoriesCache

    fun thumbnailAssetUrl(category: PadWallpaperCategory): String =
        "file:///android_asset/${category.thumbnailAssetPath.trimStart('/')}"

    suspend fun loadCategoryKeywordIndex(context: Context): Map<String, List<String>> = withContext(Dispatchers.IO) {
        categoryKeywordIndexCache?.let { return@withContext it }
        runCatching {
            context.assets.open(SLUG_INDEX_ASSET_PATH).bufferedReader().use { reader ->
                gson.fromJson(reader, PadWallpaperSlugIndex::class.java)
            }
        }.getOrNull()
            ?.packs
            .orEmpty()
            .mapNotNull { pack ->
                val categoryId = pack.category?.id?.trim().orEmpty()
                if (categoryId.isBlank()) return@mapNotNull null
                categoryId to pack.keywords.orEmpty().map { it.trim().lowercase() }.filter { it.isNotBlank() }
            }
            .toMap()
            .also { loaded ->
                if (loaded.isNotEmpty()) {
                    categoryKeywordIndexCache = loaded
                }
            }
    }

    suspend fun loadItemsForCategory(
        context: Context,
        category: PadWallpaperCategory,
    ): List<PadWallpaperItem> = withContext(Dispatchers.IO) {
        synchronized(cacheLock) {
            itemsCache[category.id]
        }?.let { return@withContext it }
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

        val categoryDir = File(root, "wallpapers/${category.id}")
        if (!categoryDir.isDirectory) {
            Log.w(TAG, "loadItemsForCategory: missing folder ${categoryDir.absolutePath}")
            return@withContext emptyList()
        }

        categoryDir.listFiles()
            ?.asSequence()
            ?.filter { it.isFile }
            ?.filter { file ->
                val name = file.name.lowercase()
                name.endsWith(".webp") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
            }
            ?.sortedBy { it.name }
            ?.map { file ->
                val fileName = file.name.substringBeforeLast('.')
                PadWallpaperItem(
                    id = fileName,
                    name = fileName.replace('_', ' '),
                    assetUrl = Uri.fromFile(file).toString(),
                )
            }
            ?.toList()
            .orEmpty()
            .also { loaded ->
            if (loaded.isNotEmpty()) {
                synchronized(cacheLock) {
                    itemsCache[category.id] = loaded
                }
            }
        }
    }

    fun peekCachedItems(categoryId: String): List<PadWallpaperItem>? = synchronized(cacheLock) {
        itemsCache[categoryId]
    }

    private fun parseCategory(json: JsonObject): PadWallpaperCategory {
        val id = json.stringOrThrow("id")
        val packName = json.stringOrThrow("pack_name")
        val deliveryPackName = json.stringOrThrow("delivery_pack_name")
        val title = json.stringOrNull("title")
        val description = json.stringOrNull("description")
        val thumbnailAssetPath = json.stringOrThrow("thumbnail_asset_path")
        val itemCount = json.getAsJsonArray("items")?.size() ?: 0
        return PadWallpaperCategory(
            id = id,
            packName = packName,
            deliveryPackName = deliveryPackName,
            title = title,
            description = description,
            thumbnailAssetPath = thumbnailAssetPath,
            itemCount = itemCount,
        )
    }
}

private fun JsonObject.stringOrNull(key: String): String? {
    val value = get(key) ?: return null
    if (value.isJsonNull) return null
    return value.asString
}

private fun JsonObject.stringOrThrow(key: String): String =
    stringOrNull(key)?.takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("Missing or blank required key: $key")

data class PadWallpaperSlugIndex(
    @SerializedName("schemaVersion")
    val schemaVersion: Int? = null,
    @SerializedName("packs")
    val packs: List<PadWallpaperSlugPack>? = null,
)

data class PadWallpaperSlugPack(
    @SerializedName("moduleName")
    val moduleName: String? = null,
    @SerializedName("moduleSlug")
    val moduleSlug: String? = null,
    @SerializedName("deliveryPackName")
    val deliveryPackName: String? = null,
    @SerializedName("category")
    val category: PadWallpaperSlugCategory? = null,
    @SerializedName("keywords")
    val keywords: List<String>? = null,
)

data class PadWallpaperSlugCategory(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("thumbnailAssetPath")
    val thumbnailAssetPath: String? = null,
)
