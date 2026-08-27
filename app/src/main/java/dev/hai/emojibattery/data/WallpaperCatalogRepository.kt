package dev.hai.emojibattery.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class WallpaperCategorySource {
    PAD,
    FIREBASE_STORAGE,
}

data class WallpaperCatalogCategory(
    val id: String,
    val title: String?,
    val subtitle: String?,
    val description: String?,
    val itemCount: Int,
    val source: WallpaperCategorySource,
    val thumbnailUrl: String,
    val keywords: List<String> = emptyList(),
    val padCategory: PadWallpaperCategory? = null,
    val firebaseCategory: FirebaseStorageWallpaperCategoryConfig? = null,
)

data class WallpaperCatalogItem(
    val id: String,
    val name: String,
    val assetUrl: String,
    val source: WallpaperCategorySource,
)

object WallpaperCatalogRepository {
    private const val TAG = "WallpaperCatalogRepo"
    private const val MAX_CACHED_CATEGORY_ITEMS = 4
    private val cacheLock = Any()
    private val storageManager = StorageManager()

    @Volatile
    private var categoriesCache: List<WallpaperCatalogCategory>? = null
    @Volatile
    private var keywordIndexCache: Map<String, List<String>>? = null

    private val itemsCache = object : LinkedHashMap<String, List<WallpaperCatalogItem>>(
        MAX_CACHED_CATEGORY_ITEMS,
        0.75f,
        true,
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<WallpaperCatalogItem>>): Boolean =
            size > MAX_CACHED_CATEGORY_ITEMS
    }

    suspend fun loadCategories(context: Context): List<WallpaperCatalogCategory> = withContext(Dispatchers.IO) {
        categoriesCache?.let { return@withContext it }
        val loaded = loadPadCategories(context) + loadFirebaseCategories(context)
        categoriesCache = loaded
        loaded
    }

    suspend fun loadPadCategories(context: Context): List<WallpaperCatalogCategory> = withContext(Dispatchers.IO) {
        PadWallpaperRepository.loadCategories(context).map { category ->
            WallpaperCatalogCategory(
                id = category.id,
                title = category.title ?: category.packName,
                subtitle = null,
                description = category.description,
                itemCount = category.itemCount,
                source = WallpaperCategorySource.PAD,
                thumbnailUrl = PadWallpaperRepository.thumbnailAssetUrl(category),
                padCategory = category,
            )
        }
    }

    suspend fun loadFirebaseCategories(context: Context): List<WallpaperCatalogCategory> = withContext(Dispatchers.IO) {
        FirebaseRemoteConfigManager.loadWallpaperCategories(context).mapNotNull { config ->
            val source = config.source.toWallpaperCategorySourceOrNull()
            if (source != WallpaperCategorySource.FIREBASE_STORAGE) return@mapNotNull null
            val thumbnailUrl = resolveFirebaseThumbnailUrl(context, config)
            WallpaperCatalogCategory(
                id = config.id,
                title = config.title,
                subtitle = config.subtitle,
                description = config.description,
                itemCount = config.itemCount ?: 0,
                source = source,
                thumbnailUrl = thumbnailUrl,
                keywords = config.keywords.orEmpty(),
                firebaseCategory = config,
            )
        }
    }

    suspend fun loadCategoryKeywordIndex(context: Context): Map<String, List<String>> = withContext(Dispatchers.IO) {
        keywordIndexCache?.let { return@withContext it }

        val padKeywords = PadWallpaperRepository.loadCategoryKeywordIndex(context)
        val firebaseKeywords = FirebaseRemoteConfigManager.loadWallpaperCategories(context).associate { config ->
            config.id to buildList {
                addAll(config.keywords.orEmpty())
                add(config.id)
                config.title?.let(::add)
                config.subtitle?.let(::add)
                config.description?.let(::add)
            }.flatMap(::keywordTokens)
                .distinct()
        }

        (padKeywords + firebaseKeywords).also { keywordIndexCache = it }
    }

    suspend fun loadItemsForCategory(
        context: Context,
        category: WallpaperCatalogCategory,
    ): List<WallpaperCatalogItem> = withContext(Dispatchers.IO) {
        synchronized(cacheLock) {
            itemsCache[itemCacheKey(category.source, category.id)]
        }?.let { return@withContext it }

        val loaded = when (category.source) {
            WallpaperCategorySource.PAD -> {
                val padCategory = category.padCategory ?: PadWallpaperRepository.loadCategories(context)
                    .firstOrNull { it.id == category.id }
                    ?: return@withContext emptyList()
                PadWallpaperRepository.loadItemsForCategory(context, padCategory).map { item ->
                    WallpaperCatalogItem(
                        id = item.id,
                        name = item.name,
                        assetUrl = item.assetUrl,
                        source = WallpaperCategorySource.PAD,
                    )
                }
            }

            WallpaperCategorySource.FIREBASE_STORAGE -> {
                val config = category.firebaseCategory ?: FirebaseRemoteConfigManager.loadWallpaperCategories(context)
                    .firstOrNull { it.id == category.id }
                    ?: return@withContext emptyList()
                runCatching { loadFirebaseItems(context, config) }
                    .onFailure { error ->
                        Log.w(TAG, "loadItemsForCategory: failed to load firebase category=${config.id}", error)
                    }
                    .getOrDefault(emptyList())
            }
        }

        synchronized(cacheLock) {
            if (loaded.isNotEmpty()) {
                itemsCache[itemCacheKey(category.source, category.id)] = loaded
            }
        }
        loaded
    }

    fun peekCachedCategories(): List<WallpaperCatalogCategory>? = categoriesCache

    fun peekCachedItems(
        categoryId: String,
        source: WallpaperCategorySource,
    ): List<WallpaperCatalogItem>? = synchronized(cacheLock) {
        itemsCache[itemCacheKey(source, categoryId)]
    }

    private suspend fun loadFirebaseItems(
        context: Context,
        config: FirebaseStorageWallpaperCategoryConfig,
    ): List<WallpaperCatalogItem> {
        return storageManager.listImageItems(context, config.storageFolderUrl).map { item ->
            WallpaperCatalogItem(
                id = item.id,
                name = item.name,
                assetUrl = item.downloadUrl,
                source = WallpaperCategorySource.FIREBASE_STORAGE,
            )
        }
    }

    private suspend fun resolveFirebaseThumbnailUrl(
        context: Context,
        config: FirebaseStorageWallpaperCategoryConfig,
    ): String {
        val fileName = config.thumbnailFileName?.trim().orEmpty()
        if (fileName.isBlank()) return ""

        val folder = config.storageFolderUrl.trimEnd('/')
        return storageManager.resolveDownloadUrlOrNull(context, "$folder/$fileName")
            .orEmpty()
    }

    private fun itemCacheKey(source: WallpaperCategorySource, categoryId: String): String =
        "${source.name}:$categoryId"

    private fun keywordTokens(value: String): List<String> {
        return value
            .trim()
            .lowercase()
            .split(Regex("[^a-z0-9]+"))
            .map { it.trim() }
            .filter { it.length >= 2 }
    }
}

internal fun String.toWallpaperCategorySourceOrNull(): WallpaperCategorySource? =
    when (trim().lowercase()) {
        "pad" -> WallpaperCategorySource.PAD
        "firebase_storage" -> WallpaperCategorySource.FIREBASE_STORAGE
        else -> null
    }
