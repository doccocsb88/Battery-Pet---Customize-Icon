package dev.hai.emojibattery.service

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import coil.load
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import dev.hai.emojibattery.model.GestureTrigger
import kotlin.math.roundToInt

class StatusBarOverlayManager(
    private val context: Context,
    private val onGestureTrigger: (GestureTrigger) -> Unit = {},
) {
    companion object {
        private const val TAG = "AnimationOverlay"
    }

    data class LiveStatus(
        val batteryPercent: Int = 0,
        val charging: Boolean = false,
        val wifiEnabled: Boolean = false,
        val mobileConnected: Boolean = false,
        val airplaneMode: Boolean = false,
        val signalLevel: Int = 0,
    )

    private val windowManager = context.getSystemService(WindowManager::class.java)

    private val root = FrameLayout(context)
    private val statusBackgroundImageView = ImageView(context)
    private val statusRow = LinearLayout(context)
    private val leftCluster = LinearLayout(context)
    private val rightCluster = LinearLayout(context)
    private val clockView = TextClock(context)
    private val dateView = TextClock(context)
    private val wifiView = TextView(context)
    private val signalView = TextView(context)
    private val emojiArtView = ImageView(context)
    private val emojiTextView = TextView(context)
    private val batteryView = TextView(context)
    private val batteryArtView = ImageView(context)
    private val stickerEmojiView = TextView(context)
    private val stickerImageView = ImageView(context)
    private val trollView = TextView(context)
    private val realtimeView = TextView(context)
    private val animationImageView = ImageView(context)
    private val animationLottieView = LottieAnimationView(context)
    private val gestureLayer = FrameLayout(context)
    private val notchContainer = FrameLayout(context)
    private val notchView = ImageView(context)

    private var attached = false
    private var gestureEnabled = false
    private var layoutParams: WindowManager.LayoutParams? = null
    private var currentAnimationKey: String? = null
    private var currentAnimationIsLottie: Boolean? = null

    private val tapDelayMs = 200L
    private val singleTapHandler = Handler(Looper.getMainLooper())
    private var lastTapAt = 0L
    private var horizontalTriggered = false
    private var verticalTriggered = false
    private var ignoredDoubleTap = false
    private val singleTapRunnable = Runnable {
        if (!ignoredDoubleTap) triggerGesture(GestureTrigger.SingleTap)
    }

    init {
        root.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        )
        statusRow.orientation = LinearLayout.HORIZONTAL
        statusRow.gravity = Gravity.CENTER_VERTICAL
        statusRow.setPadding(28, 10, 28, 10)

        leftCluster.orientation = LinearLayout.VERTICAL
        leftCluster.gravity = Gravity.CENTER_VERTICAL

        clockView.textSize = 13f
        clockView.format12Hour = "HH:mm"
        clockView.format24Hour = "HH:mm"
        clockView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        dateView.textSize = 10f
        dateView.format12Hour = "EEE, MMM d"
        dateView.format24Hour = "EEE, MMM d"
        dateView.alpha = 0.75f
        leftCluster.addView(clockView)
        leftCluster.addView(dateView)
        val left = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        statusRow.addView(leftCluster, left)

        rightCluster.orientation = LinearLayout.HORIZONTAL
        rightCluster.gravity = Gravity.CENTER_VERTICAL
        batteryView.textSize = 13f
        batteryView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        wifiView.textSize = 11f
        signalView.textSize = 11f
        wifiView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        signalView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        emojiTextView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        signalView.setPadding(12, 0, 0, 0)
        emojiArtView.setPadding(12, 0, 0, 0)
        emojiTextView.setPadding(12, 0, 0, 0)
        batteryView.setPadding(12, 0, 0, 0)
        batteryArtView.setPadding(12, 0, 0, 0)
        emojiArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        emojiArtView.adjustViewBounds = true
        batteryArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        batteryArtView.adjustViewBounds = true
        emojiTextView.textSize = 13f
        val emojiArtSize = (16 * context.resources.displayMetrics.density).toInt().coerceAtLeast(12)
        emojiArtView.layoutParams = LinearLayout.LayoutParams(emojiArtSize, emojiArtSize)
        val artSize = (18 * context.resources.displayMetrics.density).toInt().coerceAtLeast(14)
        batteryArtView.layoutParams = LinearLayout.LayoutParams(artSize, artSize)
        rightCluster.addView(wifiView)
        rightCluster.addView(signalView)
        rightCluster.addView(emojiArtView)
        rightCluster.addView(emojiTextView)
        rightCluster.addView(batteryArtView)
        rightCluster.addView(batteryView)
        statusRow.addView(rightCluster)

        statusBackgroundImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        statusBackgroundImageView.visibility = View.GONE
        root.addView(
            statusBackgroundImageView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ),
        )
        root.addView(statusRow)
        root.addView(
            gestureLayer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                (40 * context.resources.displayMetrics.density).toInt(),
                Gravity.TOP,
            ),
        )
        val stickerLp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply {
            topMargin = 64
        }
        stickerImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        stickerImageView.adjustViewBounds = true
        val stickerMax = (56 * context.resources.displayMetrics.density).toInt()
        stickerImageView.maxWidth = stickerMax
        stickerImageView.maxHeight = stickerMax
        root.addView(stickerImageView, FrameLayout.LayoutParams(stickerLp))
        root.addView(stickerEmojiView, FrameLayout.LayoutParams(stickerLp))
        root.addView(trollView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END).apply {
            topMargin = 64
            marginEnd = 24
        })
        root.addView(realtimeView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply {
            topMargin = 64
            marginStart = 24
        })
        animationImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        animationImageView.adjustViewBounds = true
        animationLottieView.scaleType = ImageView.ScaleType.FIT_CENTER
        animationLottieView.repeatCount = LottieDrawable.INFINITE
        root.addView(
            animationImageView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL,
            ).apply {
                topMargin = (2 * context.resources.displayMetrics.density).roundToInt()
            },
        )
        root.addView(
            animationLottieView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL,
            ).apply {
                topMargin = (2 * context.resources.displayMetrics.density).roundToInt()
            },
        )
        animationImageView.visibility = View.GONE
        animationLottieView.visibility = View.GONE

        listOf(stickerEmojiView, trollView, realtimeView).forEach { view ->
            view.textSize = 18f
            view.setPadding(18, 10, 18, 10)
            view.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        }
        stickerImageView.setPadding(18, 10, 18, 10)

        notchView.scaleType = ImageView.ScaleType.FIT_XY
        notchContainer.visibility = View.GONE
        notchContainer.addView(
            notchView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        root.addView(
            notchContainer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL,
            ),
        )

        setupGestureLayer()
    }

    fun render(snapshot: OverlaySnapshot, liveStatus: LiveStatus) {
        ensureAttached()
        if (!snapshot.statusBarEnabled && !snapshot.stickerEnabled && !snapshot.trollEnabled && !snapshot.realTimeEnabled && !snapshot.animationEnabled) {
            root.alpha = 0f
            return
        }
        root.alpha = 1f
        val templateDrawableRes = snapshot.backgroundTemplateDrawableRes?.takeIf { it != 0 }
        val templateUrl = snapshot.backgroundTemplatePhotoUrl?.takeIf { it.isNotBlank() }
        // Prefer Volio `photo` URL (raster) over local XML shape drawables.
        when {
            templateUrl != null -> {
                statusBackgroundImageView.visibility = View.VISIBLE
                statusBackgroundImageView.load(templateUrl) {
                    crossfade(true)
                }
                applyStatusRowBackground(
                    baseColor = ColorUtils.setAlphaComponent(snapshot.backgroundColor.toInt(), 0xA8),
                    showStroke = snapshot.showStroke,
                )
            }
            templateDrawableRes != null -> {
                statusBackgroundImageView.visibility = View.VISIBLE
                statusBackgroundImageView.setImageResource(templateDrawableRes)
                applyStatusRowBackground(
                    baseColor = ColorUtils.setAlphaComponent(snapshot.backgroundColor.toInt(), 0xA8),
                    showStroke = snapshot.showStroke,
                )
            }
            else -> {
                statusBackgroundImageView.visibility = View.GONE
                applyStatusRowBackground(
                    baseColor = snapshot.backgroundColor.toInt(),
                    showStroke = snapshot.showStroke,
                )
            }
        }
        val percentageText = if (snapshot.showPercentage) " ${liveStatus.batteryPercent}%" else ""
        val chargeSuffix = if (liveStatus.charging && snapshot.animateCharge) " ⚡" else ""
        val batteryLabel = snapshot.batteryBody.takeIf { it.isNotBlank() }
            ?: snapshot.batteryText.takeIf { it.isNotBlank() }
            ?: "▰▰▰▱"
        val emojiLabel = snapshot.emojiGlyph.takeIf { it.isNotBlank() } ?: "●"
        val batteryUrl = snapshot.batteryArtUrl?.takeIf { it.isNotBlank() }
        val batteryDrawable = snapshot.batteryArtDrawableRes?.takeIf { it != 0 }
        val emojiUrl = snapshot.emojiArtUrl?.takeIf { it.isNotBlank() }
        val emojiDrawable = snapshot.emojiArtDrawableRes?.takeIf { it != 0 }
        val emojiScale = snapshot.emojiScale.coerceIn(0f, 1f)
        val emojiSizePx = ((10f + emojiScale * 20f) * context.resources.displayMetrics.density).roundToInt().coerceAtLeast((12f * context.resources.displayMetrics.density).roundToInt())
        (emojiArtView.layoutParams as LinearLayout.LayoutParams).also { params ->
            params.width = emojiSizePx
            params.height = emojiSizePx
            emojiArtView.layoutParams = params
        }
        emojiTextView.textSize = (10f + emojiScale * 14f)
        when {
            emojiUrl != null -> {
                emojiArtView.visibility = View.VISIBLE
                emojiTextView.visibility = View.GONE
                emojiArtView.load(emojiUrl) {
                    crossfade(true)
                }
            }
            emojiDrawable != null -> {
                emojiArtView.visibility = View.VISIBLE
                emojiTextView.visibility = View.GONE
                emojiArtView.setImageResource(emojiDrawable)
            }
            else -> {
                emojiArtView.visibility = View.GONE
                emojiTextView.visibility = View.VISIBLE
                emojiTextView.text = emojiLabel
            }
        }
        if (batteryUrl != null) {
            batteryArtView.visibility = View.VISIBLE
            batteryArtView.load(batteryUrl) {
                crossfade(true)
            }
            batteryView.text = "${percentageText.trim()}$chargeSuffix".trim()
        } else if (batteryDrawable != null) {
            batteryArtView.visibility = View.VISIBLE
            batteryArtView.setImageResource(batteryDrawable)
            batteryView.text = "${percentageText.trim()}$chargeSuffix".trim()
        } else {
            batteryArtView.visibility = View.GONE
            batteryView.text = "$batteryLabel$percentageText$chargeSuffix".trim()
        }
        batteryView.setTextColor(snapshot.accentColor.toInt())
        batteryView.textSize = (11f + (snapshot.batteryPercentScale.coerceIn(0f, 1f) * 11f))
        val statusScale = 0.8f + (snapshot.statusBarHeight.coerceIn(0f, 1f) * 0.9f)
        statusRow.scaleY = statusScale
        val density = context.resources.displayMetrics.density
        val leftPadding = ((8f + snapshot.leftMargin.coerceIn(0f, 1f) * 88f) * density).roundToInt()
        val rightPadding = ((8f + snapshot.rightMargin.coerceIn(0f, 1f) * 88f) * density).roundToInt()
        val topBottomPadding = ((6f + snapshot.statusBarHeight.coerceIn(0f, 1f) * 10f) * density).roundToInt()
        statusRow.setPadding(leftPadding, topBottomPadding, rightPadding, topBottomPadding)
        clockView.setTextColor("#111111".toColorInt())
        dateView.setTextColor("#555555".toColorInt())
        wifiView.text = when {
            liveStatus.airplaneMode -> "AIR"
            liveStatus.wifiEnabled -> "WIFI"
            liveStatus.mobileConnected -> "LTE"
            else -> "OFF"
        }
        signalView.text = if (liveStatus.airplaneMode) "" else signalGlyph(liveStatus.signalLevel)
        wifiView.setTextColor("#333333".toColorInt())
        signalView.setTextColor("#333333".toColorInt())

        if (snapshot.stickerEnabled) {
            val stickerScale = (0.6f + snapshot.stickerSize * 0.8f).coerceIn(0.6f, 1.6f)
            stickerEmojiView.scaleX = stickerScale
            stickerEmojiView.scaleY = stickerScale
            stickerImageView.scaleX = stickerScale
            stickerImageView.scaleY = stickerScale
            stickerEmojiView.rotation = snapshot.stickerRotation
            stickerImageView.rotation = snapshot.stickerRotation
            updateStickerPosition(snapshot.stickerOffsetX, snapshot.stickerOffsetY)
            val url = snapshot.stickerThumbnailUrl?.takeIf { it.isNotBlank() }
            if (url != null) {
                stickerImageView.visibility = View.VISIBLE
                stickerEmojiView.visibility = View.GONE
                stickerImageView.load(url) {
                    crossfade(true)
                }
            } else {
                stickerImageView.visibility = View.GONE
                stickerEmojiView.visibility = View.VISIBLE
                stickerEmojiView.text = snapshot.stickerGlyph
            }
        } else {
            stickerImageView.visibility = View.GONE
            stickerEmojiView.visibility = View.GONE
        }

        trollView.text = "Fake ${snapshot.trollMessage}"
        trollView.visibility = if (snapshot.trollEnabled) View.VISIBLE else View.GONE
        trollView.setBackgroundColor("#FFF2D9".toColorInt())

        realtimeView.text = "${snapshot.realTimeGlyph} ${snapshot.realTimeTitle}"
        realtimeView.visibility = if (snapshot.realTimeEnabled) View.VISIBLE else View.GONE
        realtimeView.setBackgroundColor("#E7F0FF".toColorInt())

        renderAnimation(snapshot)

        statusRow.visibility = if (snapshot.statusBarEnabled) View.VISIBLE else View.GONE
        applyNotch(snapshot.notchTemplateId, snapshot.statusBarEnabled)
    }

    private fun applyStatusRowBackground(baseColor: Int, showStroke: Boolean) {
        val density = context.resources.displayMetrics.density
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 14f * density
            setColor(baseColor)
            if (showStroke) {
                setStroke((1f * density).roundToInt().coerceAtLeast(1), ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 0x55))
            }
        }
        statusRow.background = drawable
    }

    private fun renderAnimation(snapshot: OverlaySnapshot) {
        if (!snapshot.animationEnabled || snapshot.animationAssetPath.isNullOrBlank()) {
            if (animationImageView.visibility != View.GONE || animationLottieView.visibility != View.GONE) {
                Log.d(TAG, "Animation hidden (disabled or missing asset)")
            }
            animationImageView.visibility = View.GONE
            animationLottieView.cancelAnimation()
            animationLottieView.visibility = View.GONE
            currentAnimationKey = null
            currentAnimationIsLottie = null
            return
        }
        val density = context.resources.displayMetrics.density
        val sizeDp = 14 + (snapshot.animationSizePercent * 26 / 100)
        val sizePx = (sizeDp * density).roundToInt().coerceAtLeast((12 * density).roundToInt())
        (animationImageView.layoutParams as FrameLayout.LayoutParams).also { params ->
            params.width = sizePx
            params.height = sizePx
            animationImageView.layoutParams = params
        }
        (animationLottieView.layoutParams as FrameLayout.LayoutParams).also { params ->
            params.width = sizePx
            params.height = sizePx
            animationLottieView.layoutParams = params
        }

        val keyChanged = currentAnimationKey != snapshot.animationAssetPath || currentAnimationIsLottie != snapshot.animationIsLottie
        if (snapshot.animationIsLottie) {
            animationImageView.visibility = View.GONE
            animationLottieView.visibility = View.VISIBLE
            if (keyChanged) {
                animationLottieView.setAnimation(snapshot.animationAssetPath)
                animationLottieView.playAnimation()
                currentAnimationKey = snapshot.animationAssetPath
                currentAnimationIsLottie = true
                Log.d(TAG, "Applied Lottie animation asset=${snapshot.animationAssetPath} size=${snapshot.animationSizePercent}%")
            } else if (!animationLottieView.isAnimating) {
                animationLottieView.playAnimation()
            }
            return
        }

        animationLottieView.cancelAnimation()
        animationLottieView.visibility = View.GONE
        animationImageView.visibility = View.VISIBLE
        if (keyChanged) {
            val assetUrl = "file:///android_asset/${snapshot.animationAssetPath}"
            animationImageView.load(assetUrl) {
                crossfade(false)
            }
            currentAnimationKey = snapshot.animationAssetPath
            currentAnimationIsLottie = false
            Log.d(TAG, "Applied GIF animation asset=${snapshot.animationAssetPath} size=${snapshot.animationSizePercent}%")
        }
    }

    private fun updateStickerPosition(offsetX: Float, offsetY: Float) {
        val density = context.resources.displayMetrics.density
        val screenWidth = context.resources.displayMetrics.widthPixels
        val stickerFrameWidth = (56f * density).roundToInt()
        val minTop = (28f * density).roundToInt()
        val travelY = (96f * density).roundToInt()
        val left = ((screenWidth - stickerFrameWidth) * offsetX.coerceIn(0f, 1f)).roundToInt()
        val top = minTop + (travelY * offsetY.coerceIn(0f, 1f)).roundToInt()
        listOf(stickerImageView, stickerEmojiView).forEach { view ->
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP or Gravity.START
            params.leftMargin = left
            params.topMargin = top
            view.layoutParams = params
        }
    }

    fun detach() {
        if (!attached) return
        windowManager.removeView(root)
        attached = false
        layoutParams = null
        singleTapHandler.removeCallbacks(singleTapRunnable)
    }

    fun setGestureEnabled(enabled: Boolean) {
        if (gestureEnabled == enabled) return
        gestureEnabled = enabled
        if (attached) {
            val updated = createLayoutParams(gestureEnabled)
            windowManager.updateViewLayout(root, updated)
            layoutParams = updated
        }
    }

    private fun ensureAttached() {
        if (attached) return
        val params = createLayoutParams(gestureEnabled)
        layoutParams = params
        windowManager.addView(root, params)
        attached = true
    }

    private fun signalGlyph(level: Int): String = when (level.coerceIn(0, 4)) {
        0 -> "▱▱▱▱"
        1 -> "▰▱▱▱"
        2 -> "▰▰▱▱"
        3 -> "▰▰▰▱"
        else -> "▰▰▰▰"
    }

    private fun setupGestureLayer() {
        val detector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    horizontalTriggered = false
                    verticalTriggered = false
                    ignoredDoubleTap = false
                    return true
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    val now = System.currentTimeMillis()
                    singleTapHandler.removeCallbacks(singleTapRunnable)
                    if (now - lastTapAt < tapDelayMs) {
                        ignoredDoubleTap = true
                        lastTapAt = 0L
                    } else {
                        lastTapAt = now
                        singleTapHandler.postDelayed(singleTapRunnable, tapDelayMs)
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    triggerGesture(GestureTrigger.LongPress)
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float,
                ): Boolean {
                    val start = e1 ?: return false
                    val dx = e2.x - start.x
                    val dy = e2.y - start.y
                    val horizontalThresholdPx = 40f
                    val verticalThresholdPx = 10f
                    if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                        if (!horizontalTriggered && kotlin.math.abs(dx) > horizontalThresholdPx) {
                            triggerGesture(
                                if (dx > 0f) GestureTrigger.SwipeLeftToRight
                                else GestureTrigger.SwipeRightToLeft,
                            )
                            horizontalTriggered = true
                        }
                    } else if (!verticalTriggered && kotlin.math.abs(dy) > verticalThresholdPx) {
                        if (dy > 0f) {
                            triggerGesture(GestureTrigger.SwipeTopToBottom)
                        }
                        // Original app ignores bottom-to-top callback.
                        verticalTriggered = true
                    }
                    return true
                }
            },
        )
        gestureLayer.setOnTouchListener { _, event ->
            if (!gestureEnabled) return@setOnTouchListener false
            detector.onTouchEvent(event)
            true
        }
    }

    private fun triggerGesture(trigger: GestureTrigger) {
        if (!gestureEnabled) return
        playNotchTapAnimation()
        onGestureTrigger(trigger)
    }

    private fun playNotchTapAnimation() {
        val target = if (notchContainer.visibility == View.VISIBLE) notchContainer else statusRow
        target.animate().cancel()
        target.scaleX = 1f
        target.scaleY = 1f
        target.alpha = 1f
        target.animate()
            .scaleX(0.94f)
            .scaleY(0.94f)
            .alpha(0.9f)
            .setDuration(80L)
            .withEndAction {
                target.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(110L)
                    .start()
            }
            .start()
    }

    private fun applyNotch(templateId: Int, statusEnabled: Boolean) {
        val notch = NotchTemplateCatalog.resolve(templateId)
        val drawable = notch.drawableRes
        if (!statusEnabled || drawable == null) {
            notchContainer.visibility = View.GONE
            return
        }
        val notchHeight = (18 * context.resources.displayMetrics.density).toInt().coerceAtLeast(12)
        val notchWidth = (notchHeight * 960f / 132f).toInt()
        val params = notchContainer.layoutParams as FrameLayout.LayoutParams
        params.width = notchWidth
        params.height = notchHeight
        params.gravity = notch.gravity
        notchContainer.layoutParams = params
        notchView.setImageResource(drawable)
        notchContainer.visibility = View.VISIBLE
    }

    private fun createLayoutParams(gestureEnabled: Boolean): WindowManager.LayoutParams {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        // Original app toggles FLAG_NOT_TOUCHABLE based on gesture switch:
        // enabled -> 0x880328, disabled -> 0x880338.
        val flags = if (gestureEnabled) 0x880328 else 0x880338
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP
        }
    }
}
