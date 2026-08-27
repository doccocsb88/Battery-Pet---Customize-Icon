package dev.hai.emojibattery.service

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Rect
import kotlin.math.abs
import kotlin.math.roundToInt

object WallpaperCropMath {
    enum class CropAlignment {
        Center,
        Start,
    }

    data class Viewport(
        val displayWidthPx: Int,
        val displayHeightPx: Int,
        val wallpaperWidthPx: Int,
        val wallpaperHeightPx: Int,
    )

    fun resolveViewport(context: Context): Viewport {
        val displayMetrics = context.resources.displayMetrics
        val displayWidth = displayMetrics.widthPixels.coerceAtLeast(1)
        val displayHeight = displayMetrics.heightPixels.coerceAtLeast(1)
        val wallpaperManager = WallpaperManager.getInstance(context)
        val desiredWidth = wallpaperManager.desiredMinimumWidth
        val desiredHeight = wallpaperManager.desiredMinimumHeight
        val wallpaperWidth = desiredWidth.takeIf { it > 0 }?.coerceAtLeast(displayWidth) ?: displayWidth
        val wallpaperHeight = desiredHeight.takeIf { it > 0 }?.coerceAtLeast(displayHeight) ?: displayHeight
        return Viewport(
            displayWidthPx = displayWidth,
            displayHeightPx = displayHeight,
            wallpaperWidthPx = wallpaperWidth,
            wallpaperHeightPx = wallpaperHeight,
        )
    }

    fun computeCenterCropRect(
        sourceWidth: Int,
        sourceHeight: Int,
        viewportWidthPx: Int,
        viewportHeightPx: Int,
    ): Rect {
        return computeCropRect(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            viewportWidthPx = viewportWidthPx,
            viewportHeightPx = viewportHeightPx,
            alignment = CropAlignment.Center,
        )
    }

    fun computeStartCropRect(
        sourceWidth: Int,
        sourceHeight: Int,
        viewportWidthPx: Int,
        viewportHeightPx: Int,
    ): Rect {
        return computeCropRect(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            viewportWidthPx = viewportWidthPx,
            viewportHeightPx = viewportHeightPx,
            alignment = CropAlignment.Start,
        )
    }

    private fun computeCropRect(
        sourceWidth: Int,
        sourceHeight: Int,
        viewportWidthPx: Int,
        viewportHeightPx: Int,
        alignment: CropAlignment,
    ): Rect {
        val safeSourceWidth = sourceWidth.coerceAtLeast(1)
        val safeSourceHeight = sourceHeight.coerceAtLeast(1)
        val safeViewportWidth = viewportWidthPx.coerceAtLeast(1)
        val safeViewportHeight = viewportHeightPx.coerceAtLeast(1)

        val sourceAspect = safeSourceWidth.toFloat() / safeSourceHeight.toFloat()
        val viewportAspect = safeViewportWidth.toFloat() / safeViewportHeight.toFloat()
        val epsilon = 0.0001f

        if (abs(sourceAspect - viewportAspect) <= epsilon) {
            return Rect(0, 0, safeSourceWidth, safeSourceHeight)
        }

        return if (sourceAspect > viewportAspect) {
            val cropWidth = (safeSourceHeight * viewportAspect)
                .roundToInt()
                .coerceIn(1, safeSourceWidth)
            val left = when (alignment) {
                CropAlignment.Start -> 0
                CropAlignment.Center -> ((safeSourceWidth - cropWidth) / 2f)
                    .roundToInt()
                    .coerceIn(0, safeSourceWidth - cropWidth)
            }
            Rect(left, 0, left + cropWidth, safeSourceHeight)
        } else {
            val cropHeight = (safeSourceWidth / viewportAspect)
                .roundToInt()
                .coerceIn(1, safeSourceHeight)
            val top = when (alignment) {
                CropAlignment.Start -> 0
                CropAlignment.Center -> ((safeSourceHeight - cropHeight) / 2f)
                    .roundToInt()
                    .coerceIn(0, safeSourceHeight - cropHeight)
            }
            Rect(0, top, safeSourceWidth, top + cropHeight)
        }
    }

    fun computeTopStripRect(
        cropRect: Rect,
        viewportHeightPx: Int,
        stripHeightPx: Int,
    ): Rect {
        val safeViewportHeight = viewportHeightPx.coerceAtLeast(1)
        val safeStripHeight = stripHeightPx.coerceAtLeast(1)
        val cropHeight = cropRect.height().coerceAtLeast(1)
        val sourceStripHeight = ((safeStripHeight.toFloat() / safeViewportHeight.toFloat()) * cropHeight)
            .roundToInt()
            .coerceIn(1, cropHeight)
        val bottom = (cropRect.top + sourceStripHeight).coerceAtMost(cropRect.bottom)
        return Rect(cropRect.left, cropRect.top, cropRect.right, bottom)
    }

    fun computeVisibleDisplayRect(
        cropRect: Rect,
        wallpaperViewportWidthPx: Int,
        wallpaperViewportHeightPx: Int,
        displayWidthPx: Int,
        displayHeightPx: Int,
        viewportOffsetX: Float = 0.5f,
        viewportOffsetY: Float = 0.5f,
    ): Rect {
        val safeWallpaperWidth = wallpaperViewportWidthPx.coerceAtLeast(1)
        val safeWallpaperHeight = wallpaperViewportHeightPx.coerceAtLeast(1)
        val safeDisplayWidth = displayWidthPx.coerceAtLeast(1)
        val safeDisplayHeight = displayHeightPx.coerceAtLeast(1)
        val safeOffsetX = viewportOffsetX.coerceIn(0f, 1f)
        val safeOffsetY = viewportOffsetY.coerceIn(0f, 1f)
        val cropWidth = cropRect.width().coerceAtLeast(1)
        val cropHeight = cropRect.height().coerceAtLeast(1)

        val visibleWidth = ((safeDisplayWidth.toFloat() / safeWallpaperWidth.toFloat()) * cropWidth)
            .roundToInt()
            .coerceIn(1, cropWidth)
        val visibleHeight = ((safeDisplayHeight.toFloat() / safeWallpaperHeight.toFloat()) * cropHeight)
            .roundToInt()
            .coerceIn(1, cropHeight)

        val left = (cropRect.left + ((cropWidth - visibleWidth) * safeOffsetX).roundToInt())
            .coerceIn(cropRect.left, cropRect.right - visibleWidth)
        val top = (cropRect.top + ((cropHeight - visibleHeight) * safeOffsetY).roundToInt())
            .coerceIn(cropRect.top, cropRect.bottom - visibleHeight)
        return Rect(left, top, left + visibleWidth, top + visibleHeight)
    }
}
