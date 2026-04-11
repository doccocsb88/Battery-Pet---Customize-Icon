package dev.hai.emojibattery.service

import android.app.WallpaperManager
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperSetter {
    suspend fun setWallpaper(
        context: Context,
        imageUrl: String?,
        @DrawableRes fallbackResId: Int,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val imageLoader = ImageLoader.Builder(context)
                .allowHardware(false)
                .build()
            val request = ImageRequest.Builder(context)
                .data(imageUrl ?: fallbackResId)
                .allowHardware(false)
                .build()
            val result = imageLoader.execute(request)
            val drawable = (result as? SuccessResult)?.drawable
                ?: error("Unable to load wallpaper source.")
            WallpaperManager.getInstance(context).setBitmap(drawable.toBitmap())
        }
    }
}
