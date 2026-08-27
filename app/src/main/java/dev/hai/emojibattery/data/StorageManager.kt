package dev.hai.emojibattery.data

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class StorageManager(
    private val storage: FirebaseStorage = Firebase.storage,
) {
    private companion object {
        const val TAG = "StorageManager"
    }

    suspend fun listImageItems(
        context: android.content.Context,
        folderPathOrUrl: String,
    ): List<StorageImageItem> = withContext(Dispatchers.IO) {
        WallpaperMetadataCache.readStorageItems(context, folderPathOrUrl)?.let { return@withContext it }
        runCatching {
            val references = listImageReferences(referenceFor(folderPathOrUrl))
            references
                .sortedBy { it.name.lowercase(Locale.US) }
                .map { reference ->
                    StorageImageItem(
                        id = reference.name.substringBeforeLast('.'),
                        name = reference.name.substringBeforeLast('.').replace('_', ' '),
                        path = reference.path,
                        downloadUrl = reference.downloadUrl.await().toString(),
                    )
                }.also { loaded ->
                    if (loaded.isNotEmpty()) {
                        WallpaperMetadataCache.writeStorageItems(context, folderPathOrUrl, loaded)
                    }
                }
        }.getOrElse { error ->
            Log.w(TAG, "listImageItems: unable to read folder=$folderPathOrUrl", error)
            WallpaperMetadataCache.readStorageItems(context, folderPathOrUrl, allowStale = true).orEmpty()
        }
    }

    suspend fun resolveDownloadUrlOrNull(
        context: android.content.Context,
        pathOrUrl: String,
    ): String? = withContext(Dispatchers.IO) {
        WallpaperMetadataCache.readStorageUrl(context, pathOrUrl)?.let { return@withContext it }
        runCatching {
            referenceFor(pathOrUrl).downloadUrl.await().toString()
                .also { url ->
                    if (url.isNotBlank()) {
                        WallpaperMetadataCache.writeStorageUrl(context, pathOrUrl, url)
                    }
                }
        }.getOrElse { error ->
            Log.w(TAG, "resolveDownloadUrlOrNull: unable to resolve path=$pathOrUrl", error)
            WallpaperMetadataCache.readStorageUrl(context, pathOrUrl, allowStale = true)
        }
    }

    private suspend fun listImageReferences(reference: StorageReference): List<StorageReference> {
        val result = reference.listAll().await()
        val directItems = result.items.filter(::isSupportedImage)
        val nestedItems = result.prefixes.flatMap { child -> listImageReferences(child) }
        return directItems + nestedItems
    }

    private fun referenceFor(pathOrUrl: String): StorageReference {
        val trimmed = pathOrUrl.trim()
        return if (trimmed.startsWith("gs://", ignoreCase = true)) {
            storage.getReferenceFromUrl(trimmed)
        } else {
            storage.reference.child(trimmed.trimStart('/'))
        }
    }

    private fun isSupportedImage(reference: StorageReference): Boolean {
        val name = reference.name.lowercase(Locale.US)
        return name.endsWith(".png") ||
            name.endsWith(".jpg") ||
            name.endsWith(".jpeg") ||
            name.endsWith(".webp")
    }
}

data class StorageImageItem(
    val id: String,
    val name: String,
    val path: String,
    val downloadUrl: String,
)
