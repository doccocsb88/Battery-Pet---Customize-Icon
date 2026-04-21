package dev.hai.emojibattery.service

import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperSetter {
    private const val TAG = "WallpaperSetter"

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
            val sourceBitmap = drawable.toBitmap()
            val dm = context.resources.displayMetrics
            val wallpaperManager = WallpaperManager.getInstance(context)
            Log.d(
                TAG,
                "setWallpaper target=${target.name} source=${sourceBitmap.width}x${sourceBitmap.height} " +
                    "display=${dm.widthPixels}x${dm.heightPixels}@${"%.2f".format(dm.density)} " +
                    "desired=${wallpaperManager.desiredMinimumWidth}x${wallpaperManager.desiredMinimumHeight} " +
                    "input=${imageUrl ?: "res:$fallbackResId"} cropHint=system_default",
            )
            wallpaperManager.setBitmap(
                sourceBitmap,
                null,
                true,
                target.flags,
            )
            Unit
        }
    }
}
