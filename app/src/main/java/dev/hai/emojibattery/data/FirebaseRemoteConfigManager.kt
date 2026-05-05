package dev.hai.emojibattery.data

import android.content.Context
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseRemoteConfigManager {
    private const val TAG = "FirebaseRemoteConfig"
    private const val FALLBACK_ASSET_PATH = "wallpapers/firebase_wallpaper_categories.json"
    private const val REMOTE_CONFIG_KEY = "wallpaper_categories_json"
    private const val DEFAULT_WALLPAPER_CATEGORIES_JSON = """
        {
          "schemaVersion": 1,
          "categories": [
            {
              "id": "firebase_couple",
              "source": "firebase_storage",
              "title": "Couple",
              "subtitle": "Romantic pair wallpapers",
              "description": "Sweet couple moments and cozy romantic scenes for a warm, affectionate look.",
              "item_count": 28,
              "storage_folder_url": "gs://battery-emoji-266a5.firebasestorage.app/wallpaper/couple/",
              "thumbnail_file_name": "couple_10.webp",
              "keywords": ["couple", "love", "romantic", "pair"]
            },
            {
              "id": "firebase_matching",
              "source": "firebase_storage",
              "title": "Matching",
              "subtitle": "Matching wallpapers for two screens",
              "description": "Playful matching wallpaper sets designed for pairs who want a coordinated style.",
              "item_count": 20,
              "storage_folder_url": "gs://battery-emoji-266a5.firebasestorage.app/wallpaper/matching/",
              "thumbnail_file_name": "Gemini_Generated_Image_c7kidlc7kidlc7ki.png",
              "keywords": ["matching", "pair", "couple", "duo"]
            }
          ]
        }
    """
    private val gson = Gson()

    @Volatile
    private var cachedCategories: List<FirebaseStorageWallpaperCategoryConfig>? = null

    suspend fun loadWallpaperCategories(context: Context): List<FirebaseStorageWallpaperCategoryConfig> =
        withContext(Dispatchers.IO) {
            cachedCategories?.let { return@withContext it }
            WallpaperMetadataCache.readRemoteConfigCategories(context)?.let { cached ->
                cachedCategories = cached
                return@withContext cached
            }

            val fallbackJson = runCatching {
                context.assets.open(FALLBACK_ASSET_PATH).bufferedReader().use { it.readText() }
            }.getOrElse { error ->
                Log.w(TAG, "loadWallpaperCategories: failed to read fallback asset", error)
                DEFAULT_WALLPAPER_CATEGORIES_JSON.trimIndent()
            }

            val remoteConfig = Firebase.remoteConfig
            runCatching {
                remoteConfig.setConfigSettingsAsync(
                    remoteConfigSettings {
                        minimumFetchIntervalInSeconds = 3_600
                    },
                ).await()
                remoteConfig.setDefaultsAsync(mapOf(REMOTE_CONFIG_KEY to fallbackJson)).await()
                remoteConfig.fetchAndActivate().await()
            }.onFailure { error ->
                Log.w(TAG, "loadWallpaperCategories: fetchAndActivate failed, using fallback", error)
            }

            val rawJson = remoteConfig.getString(REMOTE_CONFIG_KEY).takeIf { it.isNotBlank() } ?: fallbackJson
            parseCategories(rawJson)
                .ifEmpty {
                    WallpaperMetadataCache.readRemoteConfigCategories(context, allowStale = true)
                        ?: parseCategories(fallbackJson)
                }
                .also { loaded ->
                    cachedCategories = loaded
                    if (loaded.isNotEmpty()) {
                        WallpaperMetadataCache.writeRemoteConfigCategories(context, loaded)
                    }
                }
        }

    private fun parseCategories(rawJson: String): List<FirebaseStorageWallpaperCategoryConfig> {
        return runCatching {
            gson.fromJson(rawJson, FirebaseStorageWallpaperCategoryConfigPayload::class.java)
        }.getOrElse { error ->
            Log.w(TAG, "parseCategories: invalid JSON payload", error)
            null
        }?.categories
            .orEmpty()
            .filter { config ->
                config.id.isNotBlank() &&
                    config.source.toWallpaperCategorySourceOrNull() == WallpaperCategorySource.FIREBASE_STORAGE &&
                    config.storageFolderUrl.isNotBlank()
            }
    }
}

data class FirebaseStorageWallpaperCategoryConfigPayload(
    @SerializedName("schemaVersion")
    val schemaVersion: Int? = null,
    @SerializedName("categories")
    val categories: List<FirebaseStorageWallpaperCategoryConfig>? = null,
)

data class FirebaseStorageWallpaperCategoryConfig(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("source")
    val source: String = "firebase_storage",
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("subtitle")
    val subtitle: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("item_count")
    val itemCount: Int? = null,
    @SerializedName("storage_folder_url")
    val storageFolderUrl: String = "",
    @SerializedName("thumbnail_file_name")
    val thumbnailFileName: String? = null,
    @SerializedName("keywords")
    val keywords: List<String>? = null,
)
