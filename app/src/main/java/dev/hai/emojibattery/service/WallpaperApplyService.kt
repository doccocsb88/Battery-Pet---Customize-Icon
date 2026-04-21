package dev.hai.emojibattery.service

import android.content.Context
import androidx.annotation.DrawableRes
import dev.hai.emojibattery.model.ThemeOptionCatalog
import dev.hai.emojibattery.model.ThemeOptionItem

object WallpaperApplyService {
    suspend fun applyWallpaper(
        context: Context,
        imageUrl: String?,
        target: WallpaperSetter.Target,
        @DrawableRes fallbackResId: Int,
    ): Result<Unit> {
        return WallpaperSetter.setWallpaper(
            context = context,
            imageUrl = imageUrl,
            fallbackResId = fallbackResId,
            target = target,
        )
    }

    suspend fun applyThemeWallpapers(
        context: Context,
        option: ThemeOptionItem,
        @DrawableRes fallbackResId: Int,
    ): String? {
        val homeAsset = firstExistingAsset(
            context = context,
            candidates = listOf(
                option.components.wallpaper.default,
                option.previewImage,
            ),
        )
        val lockAsset = firstExistingAsset(
            context = context,
            candidates = listOf(
                option.components.lockScreen,
                homeAsset.orEmpty(),
            ),
        )
        val homeUri = homeAsset?.let(ThemeOptionCatalog::assetUri)
        val lockUri = lockAsset?.let(ThemeOptionCatalog::assetUri)
        if (homeUri == null && lockUri == null) {
            return "Theme applied. No wallpaper asset found for this theme."
        }

        return if (homeUri != null && lockUri != null && homeUri == lockUri) {
            val result = applyWallpaper(
                context = context,
                imageUrl = homeUri,
                fallbackResId = fallbackResId,
                target = WallpaperSetter.Target.BOTH,
            )
            if (result.isFailure) "Theme applied. Unable to set wallpaper on this device." else null
        } else {
            val homeResult = if (homeUri != null) {
                applyWallpaper(
                    context = context,
                    imageUrl = homeUri,
                    fallbackResId = fallbackResId,
                    target = WallpaperSetter.Target.HOME,
                )
            } else {
                Result.success(Unit)
            }
            val lockResult = if (lockUri != null) {
                applyWallpaper(
                    context = context,
                    imageUrl = lockUri,
                    fallbackResId = fallbackResId,
                    target = WallpaperSetter.Target.LOCK,
                )
            } else {
                Result.success(Unit)
            }
            when {
                homeResult.isFailure && lockResult.isFailure ->
                    "Theme applied. Unable to set home/lock wallpaper on this device."
                homeResult.isFailure ->
                    "Theme applied. Lock screen set, but failed to set home wallpaper."
                lockResult.isFailure ->
                    "Theme applied. Home wallpaper set, but lock screen is not supported on this device."
                else -> null
            }
        }
    }

    private fun firstExistingAsset(
        context: Context,
        candidates: List<String>,
    ): String? {
        return candidates
            .map { it.trim() }
            .firstOrNull { path ->
                path.isNotBlank() && runCatching { context.assets.open(path).use { } }.isSuccess
            }
    }
}
