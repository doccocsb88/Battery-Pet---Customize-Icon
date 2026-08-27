package dev.hai.emojibattery.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File
import java.security.MessageDigest

object WallpaperMetadataCache {
    private const val TAG = "WallpaperMetadataCache"
    private const val CACHE_DIR = "wallpaper_metadata_cache"
    const val CACHE_TTL_MS: Long = 24L * 60L * 60L * 1_000L
    private val gson = Gson()

    fun readRemoteConfigCategories(
        context: Context,
        allowStale: Boolean = false,
    ): List<FirebaseStorageWallpaperCategoryConfig>? {
        val payload = readPayload<RemoteConfigCachePayload>(context, "remote_config_categories.json") ?: return null
        if (!allowStale && isExpired(payload.cachedAtMs)) return null
        return payload.categories.orEmpty()
    }

    fun writeRemoteConfigCategories(
        context: Context,
        categories: List<FirebaseStorageWallpaperCategoryConfig>,
    ) {
        writePayload(
            context = context,
            name = "remote_config_categories.json",
            payload = RemoteConfigCachePayload(
                cachedAtMs = System.currentTimeMillis(),
                categories = categories,
            ),
        )
    }

    fun readStorageItems(
        context: Context,
        folderPathOrUrl: String,
        allowStale: Boolean = false,
    ): List<StorageImageItem>? {
        val payload = readPayload<StorageItemsCachePayload>(context, keyToFileName("items_", folderPathOrUrl)) ?: return null
        if (!allowStale && isExpired(payload.cachedAtMs)) return null
        return payload.items.orEmpty()
    }

    fun writeStorageItems(
        context: Context,
        folderPathOrUrl: String,
        items: List<StorageImageItem>,
    ) {
        writePayload(
            context = context,
            name = keyToFileName("items_", folderPathOrUrl),
            payload = StorageItemsCachePayload(
                cachedAtMs = System.currentTimeMillis(),
                items = items,
            ),
        )
    }

    fun readStorageUrl(
        context: Context,
        pathOrUrl: String,
        allowStale: Boolean = false,
    ): String? {
        val payload = readPayload<StorageUrlCachePayload>(context, keyToFileName("url_", pathOrUrl)) ?: return null
        if (!allowStale && isExpired(payload.cachedAtMs)) return null
        return payload.url?.takeIf { it.isNotBlank() }
    }

    fun writeStorageUrl(
        context: Context,
        pathOrUrl: String,
        url: String,
    ) {
        writePayload(
            context = context,
            name = keyToFileName("url_", pathOrUrl),
            payload = StorageUrlCachePayload(
                cachedAtMs = System.currentTimeMillis(),
                url = url,
            ),
        )
    }

    private inline fun <reified T> readPayload(context: Context, name: String): T? {
        val file = cacheFile(context, name)
        if (!file.isFile) return null
        return runCatching {
            file.bufferedReader().use { reader -> gson.fromJson(reader, T::class.java) }
        }.getOrElse { error ->
            Log.w(TAG, "readPayload: failed file=${file.absolutePath}", error)
            null
        }
    }

    private fun writePayload(
        context: Context,
        name: String,
        payload: Any,
    ) {
        val file = cacheFile(context, name)
        runCatching {
            file.parentFile?.mkdirs()
            file.bufferedWriter().use { writer -> gson.toJson(payload, writer) }
        }.onFailure { error ->
            Log.w(TAG, "writePayload: failed file=${file.absolutePath}", error)
        }
    }

    private fun cacheFile(context: Context, name: String): File =
        File(File(context.cacheDir, CACHE_DIR), name)

    private fun isExpired(cachedAtMs: Long): Boolean =
        cachedAtMs <= 0L || (System.currentTimeMillis() - cachedAtMs) > CACHE_TTL_MS

    private fun keyToFileName(prefix: String, key: String): String =
        prefix + sha256(key) + ".json"

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return buildString(bytes.size * 2) {
            bytes.forEach { byte -> append("%02x".format(byte)) }
        }
    }
}

data class RemoteConfigCachePayload(
    @SerializedName("cached_at_ms")
    val cachedAtMs: Long,
    @SerializedName("categories")
    val categories: List<FirebaseStorageWallpaperCategoryConfig>? = null,
)

data class StorageItemsCachePayload(
    @SerializedName("cached_at_ms")
    val cachedAtMs: Long,
    @SerializedName("items")
    val items: List<StorageImageItem>? = null,
)

data class StorageUrlCachePayload(
    @SerializedName("cached_at_ms")
    val cachedAtMs: Long,
    @SerializedName("url")
    val url: String? = null,
)
