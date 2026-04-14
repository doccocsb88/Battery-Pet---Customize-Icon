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
    enum class Target(val flags: Int) {
        HOME(WallpaperManager.FLAG_SYSTEM),
        LOCK(WallpaperManager.FLAG_LOCK),
        BOTH(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK),
    }

    suspend fun setWallpaper(
        context: Context,
        imageUrl: String?,
        @DrawableRes fallbackResId: Int,
        target: Target = Target.BOTH,
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
            WallpaperManager.getInstance(context).setBitmap(
                drawable.toBitmap(),
                null,
                true,
                target.flags,
            )
            Unit
        }
    }
}
