package dev.hai.emojibattery.service

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
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
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.FeatureConfig
import dev.hai.emojibattery.model.GestureTrigger
import dev.hai.emojibattery.ui.screen.EmotionOptions
import dev.hai.emojibattery.ui.screen.parseDateTimeVariant
import dev.hai.emojibattery.ui.screen.parseEmotionVariant
import dev.hai.emojibattery.ui.screen.parseRingerVariant
import kotlin.math.roundToInt
import kotlin.random.Random

class StatusBarOverlayManager(
    private val context: Context,
    private val onGestureTrigger: (GestureTrigger) -> Unit = {},
) {
    companion object {
        private const val TAG = "AnimationOverlay"
        private const val DEFAULT_EMOJI_SCALE = 0.64f
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
    private val leftTimeCluster = LinearLayout(context)
    private val leftEmojiContainer = FrameLayout(context)
    private val rightCluster = LinearLayout(context)
    private val clockView = TextClock(context)
    private val dateView = TextClock(context)
    private val wifiView = TextView(context)
    private val signalView = TextView(context)
    private val batteryArtContainer = FrameLayout(context)
    private val emojiArtView = ImageView(context)
    private val emojiTextView = TextView(context)
    private val batteryView = TextView(context)
    private val ringerView = TextView(context)
    private val batteryArtView = ImageView(context)
    private val trollArtContainer = FrameLayout(context)
    private val trollBatteryArtView = ImageView(context)
    private val trollEmojiArtView = ImageView(context)
    private val stickerEmojiView = TextView(context)
    private val stickerImageView = ImageView(context)
    private val stickerLottieView = LottieAnimationView(context)
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
    private var currentStickerLottieUrl: String? = null
    private var trollShuffleVersion: Long = 0L
    private var appliedTrollShuffleVersion: Long = -1L
    private var currentTrollBatteryArtUrl: String? = null
    private var currentTrollEmojiArtUrl: String? = null

    private val tapDelayMs = 200L
    private val singleTapHandler = Handler(Looper.getMainLooper())
    private var lastTapAt = 0L
    private var horizontalTriggered = false
    private var verticalTriggered = false
    private var ignoredDoubleTap = false
    private val singleTapRunnable = Runnable {
        if (!ignoredDoubleTap) triggerGesture(GestureTrigger.SingleTap)
    }
    private val defaultFeatureConfig = FeatureConfig(enabled = true, intensity = 0.55f, variant = "")

    init {
        root.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        )
        statusRow.orientation = LinearLayout.HORIZONTAL
        statusRow.gravity = Gravity.CENTER_VERTICAL
        statusRow.setPadding(28, 10, 28, 10)
        statusRow.clipChildren = false
        statusRow.clipToPadding = false

        leftCluster.orientation = LinearLayout.HORIZONTAL
        leftCluster.gravity = Gravity.CENTER_VERTICAL
        leftCluster.setPadding(0, 0, 10, 0)
        leftCluster.clipChildren = false
        leftCluster.clipToPadding = false

        leftTimeCluster.orientation = LinearLayout.VERTICAL
        leftTimeCluster.gravity = Gravity.CENTER_VERTICAL

        clockView.textSize = 13f
        clockView.format12Hour = "HH:mm"
        clockView.format24Hour = "HH:mm"
        clockView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        dateView.textSize = 10f
        dateView.format12Hour = "EEE, MMM d"
        dateView.format24Hour = "EEE, MMM d"
        dateView.alpha = 0.75f
        leftTimeCluster.addView(clockView)
        leftTimeCluster.addView(dateView)
        leftCluster.addView(leftTimeCluster)
        val left = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        rightCluster.orientation = LinearLayout.HORIZONTAL
        rightCluster.gravity = Gravity.CENTER_VERTICAL
        batteryView.textSize = 13f
        batteryView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        ringerView.textSize = 11f
        ringerView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        wifiView.textSize = 11f
        signalView.textSize = 11f
        wifiView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        signalView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        emojiTextView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        emojiTextView.includeFontPadding = false
        emojiTextView.gravity = Gravity.CENTER
        signalView.setPadding(12, 0, 0, 0)
        batteryView.setPadding(12, 0, 0, 0)
        ringerView.setPadding(6, 0, 0, 0)
        emojiArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        emojiArtView.adjustViewBounds = true
        batteryArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        batteryArtView.adjustViewBounds = true
        emojiTextView.textSize = 13f
        val artSize = (18 * context.resources.displayMetrics.density).toInt().coerceAtLeast(14)
        leftEmojiContainer.layoutParams = LinearLayout.LayoutParams(artSize, artSize).apply {
            marginStart = 6
        }
        leftEmojiContainer.clipChildren = false
        leftEmojiContainer.clipToPadding = false
        leftEmojiContainer.addView(
            emojiArtView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            ),
        )
        leftEmojiContainer.addView(
            emojiTextView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            ),
        )
        leftEmojiContainer.visibility = View.GONE
        leftCluster.addView(leftEmojiContainer)
        statusRow.addView(leftCluster, left)

        batteryArtContainer.layoutParams = LinearLayout.LayoutParams(artSize, artSize).apply {
            marginStart = 12
        }
        batteryArtContainer.addView(
            batteryArtView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            ),
        )
        trollArtContainer.layoutParams = LinearLayout.LayoutParams(artSize, artSize).apply {
            marginStart = 12
        }
        trollBatteryArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        trollBatteryArtView.adjustViewBounds = true
        trollEmojiArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        trollEmojiArtView.adjustViewBounds = true
        trollArtContainer.addView(
            trollBatteryArtView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            ),
        )
        trollArtContainer.addView(
            trollEmojiArtView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            ),
        )
        trollArtContainer.visibility = View.GONE
        rightCluster.addView(wifiView)
        rightCluster.addView(signalView)
        rightCluster.addView(batteryArtContainer)
        rightCluster.addView(trollArtContainer)
        rightCluster.addView(batteryView)
        rightCluster.addView(ringerView)
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
        stickerLottieView.scaleType = ImageView.ScaleType.FIT_CENTER
        stickerLottieView.repeatCount = LottieDrawable.INFINITE
        val stickerMax = (56 * context.resources.displayMetrics.density).toInt()
        stickerImageView.maxWidth = stickerMax
        stickerImageView.maxHeight = stickerMax
        root.addView(stickerImageView, FrameLayout.LayoutParams(stickerLp))
        root.addView(stickerLottieView, FrameLayout.LayoutParams(stickerLp))
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
        stickerLottieView.visibility = View.GONE

        listOf(stickerEmojiView, trollView, realtimeView).forEach { view ->
            view.textSize = 18f
            view.setPadding(18, 10, 18, 10)
            view.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        }
        stickerImageView.setPadding(18, 10, 18, 10)

        notchView.scaleType = ImageView.ScaleType.FIT_CENTER
        notchView.adjustViewBounds = true
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
                    baseColor = android.graphics.Color.TRANSPARENT,
                    showStroke = snapshot.showStroke,
                    rounded = true,
                )
            }
            templateDrawableRes != null -> {
                statusBackgroundImageView.visibility = View.VISIBLE
                statusBackgroundImageView.setImageResource(templateDrawableRes)
                applyStatusRowBackground(
                    baseColor = android.graphics.Color.TRANSPARENT,
                    showStroke = snapshot.showStroke,
                    rounded = true,
                )
            }
            else -> {
                // Solid colors are more reliable when applied directly to the status row background.
                // Using an ImageView with WRAP_CONTENT can lead to missing fill on some runtime layouts.
                statusBackgroundImageView.visibility = View.GONE
                statusBackgroundImageView.setImageDrawable(null)
                applyStatusRowBackground(
                    baseColor = snapshot.backgroundColor.toInt(),
                    showStroke = snapshot.showStroke,
                    rounded = false,
                )
            }
        }
        val featureConfigs = snapshot.featureConfigs
        val wifiConfig = featureConfigs[CustomizeEntry.Wifi] ?: defaultFeatureConfig
        val signalConfig = featureConfigs[CustomizeEntry.Signal] ?: defaultFeatureConfig
        val dataConfig = featureConfigs[CustomizeEntry.Data] ?: defaultFeatureConfig
        val hotspotConfig = featureConfigs[CustomizeEntry.Hotspot] ?: defaultFeatureConfig
        val airplaneConfig = featureConfigs[CustomizeEntry.Airplane] ?: defaultFeatureConfig
        val chargeConfig = featureConfigs[CustomizeEntry.Charge] ?: defaultFeatureConfig
        val ringerConfig = featureConfigs[CustomizeEntry.Ringer] ?: defaultFeatureConfig
        val dateTimeConfig = featureConfigs[CustomizeEntry.DateTime] ?: defaultFeatureConfig
        val emotionConfig = featureConfigs[CustomizeEntry.Emotion] ?: defaultFeatureConfig
        val parsedRinger = parseRingerVariant(ringerConfig.variant)

        val percentageText = if (snapshot.showPercentage) " ${liveStatus.batteryPercent}%" else ""
        val chargeSuffix = if (liveStatus.charging && snapshot.animateCharge && chargeConfig.enabled) {
            " ${chargeGlyph(chargeConfig.variant)}"
        } else {
            ""
        }
        val batteryLabel = snapshot.batteryBody.takeIf { it.isNotBlank() }
            ?: snapshot.batteryText.takeIf { it.isNotBlank() }
            ?: "▰▰▰▱"
        val emojiLabel = snapshot.emojiGlyph.takeIf { it.isNotBlank() } ?: "●"
        val emotionState = parseEmotionVariant(emotionConfig.variant)
        val emotionGlyph = EmotionOptions.firstOrNull { it.id == emotionState.emotionId }?.glyph ?: emojiLabel
        val forceEmotionGlyph = emotionConfig.enabled && emotionState.enabled
        val batteryUrl = snapshot.batteryArtUrl?.takeIf { it.isNotBlank() }
        val batteryDrawable = snapshot.batteryArtDrawableRes?.takeIf { it != 0 }
        val emojiUrl = snapshot.emojiArtUrl?.takeIf { it.isNotBlank() }
        val emojiDrawable = snapshot.emojiArtDrawableRes?.takeIf { it != 0 }
        val (effectiveTrollBatteryUrl, effectiveTrollEmojiUrl) = resolveTrollArtSelection(snapshot)
        val effectiveEmojiUrl = if (snapshot.trollEnabled) {
            effectiveTrollEmojiUrl?.takeIf { snapshot.trollShowEmoji }
        } else {
            emojiUrl
        }
        val baseArtSizePx = ((leftEmojiContainer.layoutParams as? LinearLayout.LayoutParams)?.width ?: 0).coerceAtLeast(1)
        val commonScaleFactor = (snapshot.emojiScale.coerceIn(0f, 1f) / DEFAULT_EMOJI_SCALE).coerceIn(0.35f, 2.2f)
        val batteryArtSizePx = (baseArtSizePx * commonScaleFactor).roundToInt().coerceAtLeast((12f * context.resources.displayMetrics.density).roundToInt())
        val leftEmojiContainerSizePx = batteryArtSizePx.coerceAtLeast((20f * context.resources.displayMetrics.density).roundToInt())
        (leftEmojiContainer.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = leftEmojiContainerSizePx
            params.height = leftEmojiContainerSizePx
            leftEmojiContainer.layoutParams = params
        }
        (batteryArtContainer.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = batteryArtSizePx
            params.height = batteryArtSizePx
            batteryArtContainer.layoutParams = params
        }
        val emojiSizePx = (batteryArtSizePx * snapshot.emojiAdjustmentScale.coerceIn(0.35f, 2.2f))
            .roundToInt()
            .coerceAtLeast((10f * context.resources.displayMetrics.density).roundToInt())
        (emojiArtView.layoutParams as FrameLayout.LayoutParams).also { params ->
            params.width = emojiSizePx
            params.height = emojiSizePx
            emojiArtView.layoutParams = params
        }
        emojiTextView.textSize = (8f + snapshot.emojiAdjustmentScale.coerceIn(0.35f, 2.2f) * 5f)
        val emojiTravelRangePx = (leftEmojiContainerSizePx * 0.2f)
        val emojiTranslationX = ((snapshot.emojiOffsetX.coerceIn(0f, 1f) - 0.5f) * 2f * emojiTravelRangePx)
        val emojiTranslationY = ((snapshot.emojiOffsetY.coerceIn(0f, 1f) - 0.5f) * 2f * emojiTravelRangePx)
        emojiArtView.translationX = emojiTranslationX
        emojiArtView.translationY = emojiTranslationY
        emojiTextView.translationX = emojiTranslationX
        emojiTextView.translationY = emojiTranslationY
        if (snapshot.trollEnabled) {
            val density = context.resources.displayMetrics.density
            val trollArtSizePx = (snapshot.trollEmojiSizeDp.coerceIn(20, 80) * density).roundToInt().coerceAtLeast((14f * density).roundToInt())
            (trollArtContainer.layoutParams as LinearLayout.LayoutParams).also { params ->
                params.width = trollArtSizePx
                params.height = trollArtSizePx
                trollArtContainer.layoutParams = params
            }
            leftEmojiContainer.visibility = View.GONE
            batteryArtView.setImageDrawable(null)
            emojiArtView.visibility = View.GONE
            emojiArtView.setImageDrawable(null)
            emojiTextView.visibility = View.GONE
            batteryArtContainer.visibility = View.GONE
            if (effectiveTrollBatteryUrl != null) {
                trollArtContainer.visibility = View.VISIBLE
                trollBatteryArtView.load(effectiveTrollBatteryUrl) {
                    crossfade(true)
                }
                if (effectiveEmojiUrl != null) {
                    trollEmojiArtView.visibility = View.VISIBLE
                    trollEmojiArtView.load(effectiveEmojiUrl) {
                        crossfade(true)
                    }
                } else {
                    trollEmojiArtView.visibility = View.GONE
                    trollEmojiArtView.setImageDrawable(null)
                }
                val trollPercentageText = if (snapshot.trollShowPercentage && snapshot.trollUseRealBattery) {
                    " ${liveStatus.batteryPercent}%"
                } else {
                    ""
                }
                val trollChargeSuffix = if (snapshot.trollUseRealBattery && liveStatus.charging && snapshot.animateCharge && chargeConfig.enabled) {
                    " ${chargeGlyph(chargeConfig.variant)}"
                } else {
                    ""
                }
                batteryView.text = if (snapshot.trollUseRealBattery) {
                    "${trollPercentageText.trim()}$trollChargeSuffix".trim()
                } else if (snapshot.trollShowPercentage) {
                    snapshot.trollMessage.trim()
                } else {
                    ""
                }
            } else {
                trollArtContainer.visibility = View.GONE
                trollBatteryArtView.setImageDrawable(null)
                trollEmojiArtView.setImageDrawable(null)
                batteryView.text = if (snapshot.trollShowPercentage) snapshot.trollMessage.trim() else ""
            }
        } else {
            trollArtContainer.visibility = View.GONE
            trollBatteryArtView.setImageDrawable(null)
            trollEmojiArtView.setImageDrawable(null)
            val hasBatteryArt = batteryUrl != null || batteryDrawable != null
            val showLeftEmoji = forceEmotionGlyph
            leftEmojiContainer.visibility = if (showLeftEmoji) View.VISIBLE else View.GONE
            batteryArtContainer.visibility = if (hasBatteryArt) View.VISIBLE else View.GONE
            when {
                batteryUrl != null -> {
                    batteryArtView.visibility = View.VISIBLE
                    batteryArtView.load(batteryUrl) {
                        crossfade(true)
                    }
                }
                batteryDrawable != null -> {
                    batteryArtView.visibility = View.VISIBLE
                    batteryArtView.setImageResource(batteryDrawable)
                }
                else -> {
                    batteryArtView.visibility = View.GONE
                    batteryArtView.setImageDrawable(null)
                }
            }
            when {
                forceEmotionGlyph -> {
                    emojiArtView.visibility = View.GONE
                    emojiArtView.setImageDrawable(null)
                    emojiTextView.visibility = View.VISIBLE
                    emojiTextView.text = emotionGlyph
                }
                else -> {
                    emojiArtView.visibility = View.GONE
                    emojiArtView.setImageDrawable(null)
                    emojiTextView.visibility = View.GONE
                }
            }
            batteryView.text = if (hasBatteryArt) {
                "${percentageText.trim()}$chargeSuffix".trim()
            } else {
                "$batteryLabel$percentageText$chargeSuffix".trim()
            }
        }
        batteryView.setTextColor(snapshot.accentColor.toInt())
        batteryView.textSize = if (snapshot.trollEnabled) {
            snapshot.trollPercentageSizeDp.coerceIn(5, 40).toFloat()
        } else {
            11f + (snapshot.batteryPercentScale.coerceIn(0f, 1f) * 11f)
        }
        val statusScale = 0.8f + (snapshot.statusBarHeight.coerceIn(0f, 1f) * 0.9f)
        statusRow.scaleY = statusScale
        val density = context.resources.displayMetrics.density
        val leftPadding = ((8f + snapshot.leftMargin.coerceIn(0f, 1f) * 88f) * density).roundToInt()
        val rightPadding = ((8f + snapshot.rightMargin.coerceIn(0f, 1f) * 88f) * density).roundToInt()
        val topBottomPadding = ((6f + snapshot.statusBarHeight.coerceIn(0f, 1f) * 10f) * density).roundToInt()
        statusRow.setPadding(leftPadding, topBottomPadding, rightPadding, topBottomPadding)
        val parsedDateTime = parseDateTimeVariant(dateTimeConfig.variant)
        applyDateTimeStyle(parsedDateTime.styleId)
        clockView.textSize = 8f + (dateTimeConfig.intensity.coerceIn(0f, 1f) * 8f)
        dateView.textSize = (clockView.textSize * 0.78f).coerceAtLeast(6.5f)
        dateView.visibility = if (dateTimeConfig.enabled && parsedDateTime.showDate) View.VISIBLE else View.GONE
        clockView.setTextColor(resolveColorFromVariant(parsedDateTime.colorId, "#111111".toColorInt()))
        dateView.setTextColor(resolveColorFromVariant(parsedDateTime.colorId, "#555555".toColorInt()))

        val wifiLabel = when {
            liveStatus.airplaneMode -> airplaneLabel(airplaneConfig.variant)
            liveStatus.wifiEnabled -> if (hotspotConfig.enabled) hotspotLabel(hotspotConfig.variant) else "WIFI"
            liveStatus.mobileConnected -> dataLabel(dataConfig.variant)
            else -> "OFF"
        }
        wifiView.visibility = if (wifiConfig.enabled) View.VISIBLE else View.GONE
        wifiView.text = wifiLabel
        wifiView.textSize = 8f + (wifiConfig.intensity.coerceIn(0f, 1f) * 12f)
        wifiView.setTextColor(resolveColorFromVariant(wifiConfig.variant, "#333333".toColorInt()))

        signalView.visibility = if (signalConfig.enabled && !liveStatus.airplaneMode) View.VISIBLE else View.GONE
        signalView.text = if (liveStatus.airplaneMode) "" else signalGlyph(liveStatus.signalLevel)
        signalView.textSize = 8f + (signalConfig.intensity.coerceIn(0f, 1f) * 12f)
        signalView.setTextColor(resolveColorFromVariant(signalConfig.variant, "#333333".toColorInt()))
        ringerView.visibility = if (ringerConfig.enabled) View.VISIBLE else View.GONE
        ringerView.text = ringerGlyph(parsedRinger.styleId)
        ringerView.textSize = 8f + (ringerConfig.intensity.coerceIn(0f, 1f) * 12f)
        ringerView.setTextColor(resolveColorFromVariant(parsedRinger.colorId, "#333333".toColorInt()))

        if (snapshot.stickerEnabled) {
            val stickerScale = (0.6f + snapshot.stickerSize * 0.8f).coerceIn(0.6f, 1.6f)
            stickerEmojiView.scaleX = stickerScale
            stickerEmojiView.scaleY = stickerScale
            stickerImageView.scaleX = stickerScale
            stickerImageView.scaleY = stickerScale
            stickerLottieView.scaleX = stickerScale
            stickerLottieView.scaleY = stickerScale
            stickerEmojiView.rotation = snapshot.stickerRotation
            stickerImageView.rotation = snapshot.stickerRotation
            stickerLottieView.rotation = snapshot.stickerRotation
            updateStickerPosition(snapshot.stickerOffsetX, snapshot.stickerOffsetY)
            val lottieUrl = snapshot.stickerLottieUrl?.takeIf { it.isNotBlank() }
            val url = snapshot.stickerThumbnailUrl?.takeIf { it.isNotBlank() }
            if (lottieUrl != null) {
                stickerLottieView.visibility = View.VISIBLE
                stickerImageView.visibility = View.GONE
                stickerEmojiView.visibility = View.GONE
                val lottieChanged = currentStickerLottieUrl != lottieUrl
                if (lottieChanged) {
                    if (lottieUrl.startsWith("http://", ignoreCase = true) || lottieUrl.startsWith("https://", ignoreCase = true)) {
                        stickerLottieView.setAnimationFromUrl(lottieUrl)
                    } else {
                        stickerLottieView.setAnimation(lottieUrl)
                    }
                    stickerLottieView.playAnimation()
                    currentStickerLottieUrl = lottieUrl
                    Log.d(TAG, "Applied sticker lottie=$lottieUrl")
                } else if (!stickerLottieView.isAnimating) {
                    stickerLottieView.playAnimation()
                }
            } else if (url != null) {
                stickerLottieView.cancelAnimation()
                stickerLottieView.visibility = View.GONE
                currentStickerLottieUrl = null
                stickerImageView.visibility = View.VISIBLE
                stickerEmojiView.visibility = View.GONE
                loadAnimatedImage(stickerImageView, url, crossfade = true, source = "sticker")
            } else {
                stickerLottieView.cancelAnimation()
                stickerLottieView.visibility = View.GONE
                currentStickerLottieUrl = null
                stickerImageView.visibility = View.GONE
                stickerEmojiView.visibility = View.VISIBLE
                stickerEmojiView.text = snapshot.stickerGlyph
            }
        } else {
            stickerLottieView.cancelAnimation()
            stickerLottieView.visibility = View.GONE
            currentStickerLottieUrl = null
            stickerImageView.visibility = View.GONE
            stickerEmojiView.visibility = View.GONE
        }

        trollView.text = snapshot.trollMessage.trim()
        // Keep legacy floating badge hidden; troll now renders in the status-row battery slot.
        trollView.visibility = View.GONE
        trollView.setBackgroundColor("#FFF2D9".toColorInt())

        realtimeView.text = "${snapshot.realTimeGlyph} ${snapshot.realTimeTitle}"
        realtimeView.visibility = if (snapshot.realTimeEnabled) View.VISIBLE else View.GONE
        realtimeView.setBackgroundColor("#E7F0FF".toColorInt())

        renderAnimation(snapshot)

        statusRow.visibility = if (snapshot.statusBarEnabled || snapshot.trollEnabled) View.VISIBLE else View.GONE
        applyNotch(snapshot.notchTemplateId, snapshot.notchColorVariant, snapshot.statusBarEnabled)
    }

    /**
     * Mirror original randomized behavior: update to a new funny pair on screen on/off events,
     * not continuously during every periodic overlay refresh.
     */
    fun requestTrollShuffle() {
        trollShuffleVersion += 1L
    }

    private fun resolveTrollArtSelection(snapshot: OverlaySnapshot): Pair<String?, String?> {
        if (!snapshot.trollEnabled) {
            currentTrollBatteryArtUrl = null
            currentTrollEmojiArtUrl = null
            appliedTrollShuffleVersion = -1L
            return null to null
        }
        val selectedBattery = snapshot.trollBatteryArtUrl?.takeIf { it.isNotBlank() }
        val selectedEmoji = snapshot.trollEmojiArtUrl?.takeIf { it.isNotBlank() }
        if (!snapshot.trollRandomizedMode) {
            currentTrollBatteryArtUrl = selectedBattery
            currentTrollEmojiArtUrl = selectedEmoji
            appliedTrollShuffleVersion = -1L
            return currentTrollBatteryArtUrl to currentTrollEmojiArtUrl
        }
        val batteryPool = snapshot.trollBatteryOptionsUrls.filter { it.isNotBlank() }
        val emojiPool = snapshot.trollEmojiOptionsUrls.filter { it.isNotBlank() }
        if (batteryPool.isEmpty() && emojiPool.isEmpty()) {
            currentTrollBatteryArtUrl = selectedBattery
            currentTrollEmojiArtUrl = selectedEmoji
            appliedTrollShuffleVersion = -1L
            return currentTrollBatteryArtUrl to currentTrollEmojiArtUrl
        }
        val needsRefresh = currentTrollBatteryArtUrl == null ||
            (trollShuffleVersion != appliedTrollShuffleVersion)
        if (needsRefresh) {
            currentTrollBatteryArtUrl = batteryPool.randomOrNull() ?: selectedBattery
            currentTrollEmojiArtUrl = emojiPool.randomOrNull() ?: selectedEmoji
            appliedTrollShuffleVersion = trollShuffleVersion
        }
        return currentTrollBatteryArtUrl to currentTrollEmojiArtUrl
    }

    private fun <T> List<T>.randomOrNull(): T? {
        if (isEmpty()) return null
        return this[Random.nextInt(size)]
    }

    private fun applyStatusRowBackground(baseColor: Int, showStroke: Boolean, rounded: Boolean) {
        val density = context.resources.displayMetrics.density
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = if (rounded) 14f * density else 0f
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
        val screenWidth = context.resources.displayMetrics.widthPixels
        val left = ((screenWidth - sizePx) * snapshot.animationOffsetX.coerceIn(0f, 1f)).roundToInt().coerceAtLeast(0)
        (animationImageView.layoutParams as FrameLayout.LayoutParams).also { params ->
            params.width = sizePx
            params.height = sizePx
            params.gravity = Gravity.TOP or Gravity.START
            params.leftMargin = left
            animationImageView.layoutParams = params
        }
        (animationLottieView.layoutParams as FrameLayout.LayoutParams).also { params ->
            params.width = sizePx
            params.height = sizePx
            params.gravity = Gravity.TOP or Gravity.START
            params.leftMargin = left
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
            loadAnimatedImage(animationImageView, assetUrl, crossfade = false, source = "animation")
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
        listOf(stickerImageView, stickerLottieView, stickerEmojiView).forEach { view ->
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP or Gravity.START
            params.leftMargin = left
            params.topMargin = top
            view.layoutParams = params
        }
    }

    private fun loadAnimatedImage(
        imageView: ImageView,
        model: String,
        crossfade: Boolean,
        source: String,
    ) {
        imageView.load(model) {
            allowHardware(false)
            decoderFactory(
                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoderDecoder.Factory()
                } else {
                    GifDecoder.Factory()
                },
            )
            crossfade(crossfade)
            listener(
                onSuccess = { _, result ->
                    (result.drawable as? Animatable)?.start()
                    Log.d(TAG, "loadAnimatedImage success source=$source model=$model drawable=${result.drawable::class.java.simpleName}")
                },
                onError = { _, result ->
                    Log.w(TAG, "loadAnimatedImage error source=$source model=$model", result.throwable)
                },
            )
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

    private fun applyDateTimeStyle(styleId: String) {
        val datePattern = when (styleId) {
            "style_1" -> "EEE, MMM d"
            "style_2" -> "EEE, MMM\n d"
            "style_3" -> "EEE\n d"
            "style_4" -> "MMM d"
            "style_5" -> "EEEE"
            "style_6" -> "EEEE\n d"
            else -> "MMM d"
        }
        dateView.format12Hour = datePattern
        dateView.format24Hour = datePattern
    }

    private fun hotspotLabel(variant: String): String = when (variant.lowercase()) {
        "dot" -> "HS•"
        "ring" -> "◎HS"
        else -> "HOT"
    }

    private fun airplaneLabel(variant: String): String = when (variant.lowercase()) {
        "solid" -> "🛫"
        "tiny" -> "AIR"
        else -> "✈"
    }

    private fun dataLabel(variant: String): String {
        val style = variant.substringBefore("::").ifBlank { "LTE" }
        return style.uppercase()
    }

    private fun ringerGlyph(variant: String): String = when (variant.lowercase()) {
        "mute" -> "🔕"
        "wave" -> "🔔~"
        else -> "🔔"
    }

    private fun chargeGlyph(variant: String): String = when (variant.lowercase()) {
        "chg_2" -> "↯"
        "chg_3" -> "⌁"
        "chg_4" -> "⏻"
        "chg_5" -> "🔌"
        "chg_6" -> "⏚"
        "chg_7" -> "ϟ"
        "chg_8" -> "⌬"
        "chg_9" -> "⎓"
        "chg_10" -> "⟡"
        "chg_11" -> "⌇"
        "chg_12" -> "⋇"
        else -> "⚡"
    }

    private fun resolveColorFromVariant(variant: String?, fallback: Int): Int {
        val raw = variant.orEmpty()
        if (raw.startsWith("picker#", ignoreCase = true)) {
            val parsed = raw.removePrefix("picker#").toLongOrNull(16)
            if (parsed != null) return parsed.toInt()
        }
        return when (raw.lowercase()) {
            "blue" -> 0xFF2952F4.toInt()
            "green" -> 0xFF2BDF52.toInt()
            "orange" -> 0xFFF18410.toInt()
            "black" -> 0xFF11111A.toInt()
            "yellow" -> 0xFFF1DF1E.toInt()
            else -> fallback
        }
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

    private fun applyNotch(templateId: Int, colorVariant: String, statusEnabled: Boolean) {
        val notch = NotchTemplateCatalog.resolve(templateId)
        val drawable = notch.drawableRes
        if (!statusEnabled || drawable == null) {
            notchContainer.visibility = View.GONE
            return
        }
        val notchHeight = (18 * context.resources.displayMetrics.density).toInt().coerceAtLeast(12)
        val notchDrawable = AppCompatResources.getDrawable(context, drawable)
        val intrinsicWidth = notchDrawable?.intrinsicWidth ?: 0
        val intrinsicHeight = notchDrawable?.intrinsicHeight ?: 0
        val aspect = if (intrinsicWidth > 0 && intrinsicHeight > 0) {
            intrinsicWidth.toFloat() / intrinsicHeight.toFloat()
        } else {
            960f / 132f
        }
        val notchWidth = (notchHeight * aspect).toInt().coerceAtLeast(notchHeight)
        val params = notchContainer.layoutParams as FrameLayout.LayoutParams
        params.width = notchWidth
        params.height = notchHeight
        params.gravity = notch.gravity
        notchContainer.layoutParams = params
        notchView.clearColorFilter()
        notchView.setImageResource(drawable)
        notchView.setColorFilter(resolveColorFromVariant(colorVariant, "#11111A".toColorInt()), PorterDuff.Mode.SRC_IN)
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
