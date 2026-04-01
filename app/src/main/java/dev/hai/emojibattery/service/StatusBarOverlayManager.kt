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
import android.media.AudioManager
import android.util.Log
import android.util.TypedValue
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
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.FeatureConfig
import dev.hai.emojibattery.model.GestureTrigger
import dev.hai.emojibattery.service.OverlayConfigStore.BATTERY_EMOJI_SOURCE_BATTERY_TROLL
import dev.hai.emojibattery.ui.screen.chargeVariantDrawableName
import dev.hai.emojibattery.ui.screen.parseChargeVariant
import dev.hai.emojibattery.ui.screen.EmotionOptions
import dev.hai.emojibattery.ui.screen.parseDateTimeVariant
import dev.hai.emojibattery.ui.screen.parseEmotionVariant
import dev.hai.emojibattery.ui.screen.parseRingerVariant
import dev.hai.emojibattery.ui.screen.ringerDrawableName
import kotlin.math.roundToInt
import kotlin.random.Random

class StatusBarOverlayManager(
    private val context: Context,
    private val onGestureTrigger: (GestureTrigger) -> Unit = {},
) {
    companion object {
        private const val TAG = "AnimationOverlay"
        private const val DEFAULT_EMOJI_SCALE = 0.64f
        private const val LOTTIE_TRACE_TAG = "LottieTrace"
    }

    data class LiveStatus(
        val batteryPercent: Int = 0,
        val charging: Boolean = false,
        val hotspotEnabled: Boolean = false,
        val wifiEnabled: Boolean = false,
        val mobileConnected: Boolean = false,
        val airplaneMode: Boolean = false,
        val signalLevel: Int = 0,
        val ringerMode: Int = AudioManager.RINGER_MODE_NORMAL,
    )

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val baseArtSizePx = (18 * context.resources.displayMetrics.density).toInt().coerceAtLeast(14)

    private val root = FrameLayout(context)
    private val statusLayer = FrameLayout(context)
    private val stickerLayer = FrameLayout(context)
    private val statusBackgroundImageView = ImageView(context)
    private val statusRow = LinearLayout(context)
    private val leftCluster = LinearLayout(context)
    private val leftTimeCluster = LinearLayout(context)
    private val leftEmojiContainer = FrameLayout(context)
    private val rightCluster = LinearLayout(context)
    private val clockView = TextClock(context)
    private val dateView = TextClock(context)
    private val hotspotIconView = ImageView(context)
    private val wifiIconView = ImageView(context)
    private val dataIconView = ImageView(context)
    private val signalIconView = ImageView(context)
    private val airplaneIconView = ImageView(context)
    private val batteryArtContainer = FrameLayout(context)
    private val emojiArtView = ImageView(context)
    private val emojiTextView = TextView(context)
    private val batteryEmojiContainer = FrameLayout(context)
    private val batteryEmojiArtView = ImageView(context)
    private val batteryEmojiTextView = TextView(context)
    private val batteryView = TextView(context)
    private val chargeView = TextView(context)
    private val chargeArtView = ImageView(context)
    private val ringerIconView = ImageView(context)
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
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        root.clipChildren = false
        root.clipToPadding = false
        statusLayer.clipChildren = false
        statusLayer.clipToPadding = false
        root.addView(
            statusLayer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP,
            ),
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
        chargeView.textSize = 13f
        chargeView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        chargeView.visibility = View.GONE
        chargeArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        chargeArtView.adjustViewBounds = true
        chargeArtView.visibility = View.GONE
        ringerIconView.scaleType = ImageView.ScaleType.FIT_CENTER
        ringerIconView.adjustViewBounds = true
        ringerIconView.visibility = View.GONE
        hotspotIconView.scaleType = ImageView.ScaleType.FIT_CENTER
        hotspotIconView.adjustViewBounds = true
        hotspotIconView.visibility = View.GONE
        wifiIconView.scaleType = ImageView.ScaleType.FIT_CENTER
        wifiIconView.adjustViewBounds = true
        wifiIconView.visibility = View.GONE
        dataIconView.scaleType = ImageView.ScaleType.FIT_CENTER
        dataIconView.adjustViewBounds = true
        dataIconView.visibility = View.GONE
        signalIconView.scaleType = ImageView.ScaleType.FIT_CENTER
        signalIconView.adjustViewBounds = true
        signalIconView.visibility = View.GONE
        airplaneIconView.scaleType = ImageView.ScaleType.FIT_CENTER
        airplaneIconView.adjustViewBounds = true
        airplaneIconView.visibility = View.GONE
        emojiTextView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        emojiTextView.includeFontPadding = false
        emojiTextView.gravity = Gravity.CENTER
        batteryView.setPadding(12, 0, 0, 0)
        chargeView.setPadding(4, 0, 0, 0)
        emojiArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        emojiArtView.adjustViewBounds = true
        batteryArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        batteryArtView.adjustViewBounds = true
        emojiTextView.textSize = 13f
        leftCluster.addView(
            ringerIconView,
            LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
                marginStart = 6
                marginEnd = 2
            },
        )
        leftEmojiContainer.layoutParams = LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
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

        batteryArtContainer.layoutParams = LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
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
        batteryEmojiContainer.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER,
        )
        batteryEmojiContainer.clipChildren = false
        batteryEmojiContainer.clipToPadding = false
        batteryEmojiArtView.scaleType = ImageView.ScaleType.FIT_CENTER
        batteryEmojiArtView.adjustViewBounds = true
        batteryEmojiTextView.textSize = 13f
        batteryEmojiTextView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        batteryEmojiTextView.includeFontPadding = false
        batteryEmojiTextView.gravity = Gravity.CENTER
        batteryEmojiTextView.setPadding(0, 0, 0, 0)
        batteryEmojiContainer.addView(
            batteryEmojiArtView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            ),
        )
        batteryEmojiContainer.addView(
            batteryEmojiTextView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            ),
        )
        batteryEmojiContainer.visibility = View.GONE
        batteryArtContainer.addView(
            batteryEmojiContainer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            ),
        )
        trollArtContainer.layoutParams = LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
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
        rightCluster.addView(
            airplaneIconView,
            LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
                marginEnd = 6
            },
        )
        rightCluster.addView(
            hotspotIconView,
            LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
                marginEnd = 6
            },
        )
        rightCluster.addView(
            wifiIconView,
            LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
                marginEnd = 6
            },
        )
        rightCluster.addView(
            dataIconView,
            LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
                marginEnd = 6
            },
        )
        rightCluster.addView(
            signalIconView,
            LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
                marginEnd = 6
            },
        )
        // Draw percentage/text before battery-emoji art cluster.
        rightCluster.addView(batteryView)
        rightCluster.addView(batteryArtContainer)
        rightCluster.addView(
            chargeArtView,
            LinearLayout.LayoutParams(baseArtSizePx, baseArtSizePx).apply {
                marginStart = 4
            },
        )
        rightCluster.addView(trollArtContainer)
        rightCluster.addView(chargeView)
        statusRow.addView(rightCluster)

        statusBackgroundImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        statusBackgroundImageView.visibility = View.GONE
        statusLayer.addView(
            statusBackgroundImageView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP,
            ),
        )
        statusLayer.addView(
            statusRow,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP,
            ),
        )
        statusLayer.addView(
            gestureLayer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                (40 * context.resources.displayMetrics.density).toInt(),
                Gravity.TOP,
            ),
        )
        stickerLayer.clipChildren = false
        stickerLayer.clipToPadding = false
        root.addView(
            stickerLayer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.TOP,
            ),
        )
        val stickerLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START,
        )
        stickerImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        stickerImageView.adjustViewBounds = true
        stickerLottieView.scaleType = ImageView.ScaleType.FIT_CENTER
        stickerLottieView.repeatCount = LottieDrawable.INFINITE
        val screenWidth = context.resources.displayMetrics.widthPixels
        val stickerMax = (screenWidth * 0.30f).roundToInt().coerceAtLeast((56 * context.resources.displayMetrics.density).toInt())
        stickerImageView.maxWidth = stickerMax
        stickerImageView.maxHeight = stickerMax
        stickerLayer.addView(stickerImageView, FrameLayout.LayoutParams(stickerLp))
        stickerLayer.addView(stickerLottieView, FrameLayout.LayoutParams(stickerLp))
        stickerLayer.addView(stickerEmojiView, FrameLayout.LayoutParams(stickerLp))
        statusLayer.addView(trollView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END).apply {
            topMargin = 64
            marginEnd = 24
        })
        statusLayer.addView(realtimeView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply {
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
        statusLayer.addView(
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
        if (!snapshot.statusBarEnabled) {
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
        val chargeState = parseChargeVariant(chargeConfig.variant)
        val ringerConfig = featureConfigs[CustomizeEntry.Ringer] ?: defaultFeatureConfig
        val dateTimeConfig = featureConfigs[CustomizeEntry.DateTime] ?: defaultFeatureConfig
        val emotionConfig = featureConfigs[CustomizeEntry.Emotion] ?: defaultFeatureConfig
        val parsedRinger = parseRingerVariant(ringerConfig.variant)

        val percentageText = if (snapshot.showPercentage) " ${liveStatus.batteryPercent}%" else ""
        val chargeSuffix = if (chargeConfig.enabled && liveStatus.charging) {
            chargeGlyph(chargeConfig.variant)
        } else {
            ""
        }
        val chargeDrawableName = chargeVariantDrawableName(chargeState)
        val batteryLabel = snapshot.batteryBody.takeIf { it.isNotBlank() }
            ?: snapshot.batteryText.takeIf { it.isNotBlank() }
            ?: "▰▰▰▱"
        val emojiLabel = snapshot.emojiGlyph.takeIf { it.isNotBlank() } ?: "●"
        val emotionState = parseEmotionVariant(emotionConfig.variant)
        val emotionGlyph = EmotionOptions.firstOrNull { it.id == emotionState.emotionId }?.glyph ?: emojiLabel
        val forceEmotionGlyph = emotionConfig.enabled
        val batteryUrl = snapshot.batteryArtUrl?.takeIf { it.isNotBlank() }
        val batteryDrawable = snapshot.batteryArtDrawableRes?.takeIf { it != 0 }
        val emojiUrl = snapshot.emojiArtUrl?.takeIf { it.isNotBlank() }
        val emojiDrawable = snapshot.emojiArtDrawableRes?.takeIf { it != 0 }
        val useBatteryTrollSource = snapshot.trollEnabled &&
            snapshot.batteryEmojiSource == BATTERY_EMOJI_SOURCE_BATTERY_TROLL
        val density = context.resources.displayMetrics.density
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        val statusScale = 0.8f + (snapshot.statusBarHeight.coerceIn(0f, 1f) * 0.9f)
        val baseStatusRowHeightPx = ((24f * density)).roundToInt().coerceAtLeast((14f * density).roundToInt())
        val (effectiveTrollBatteryUrl, effectiveTrollEmojiUrl) = resolveTrollArtSelection(snapshot)
        val effectiveEmojiUrl = if (useBatteryTrollSource) {
            effectiveTrollEmojiUrl?.takeIf { snapshot.trollShowEmoji }
        } else {
            emojiUrl
        }
        val commonScaleFactor = (snapshot.emojiScale.coerceIn(0f, 1f) / DEFAULT_EMOJI_SCALE).coerceIn(0.35f, 2.2f)
        val batteryArtSizePx = (baseArtSizePx * commonScaleFactor).roundToInt().coerceAtLeast((12f * context.resources.displayMetrics.density).roundToInt())
        val leftEmojiContainerSizePx = batteryArtSizePx.coerceAtLeast((20f * context.resources.displayMetrics.density).roundToInt())
        (leftEmojiContainer.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = leftEmojiContainerSizePx
            params.height = leftEmojiContainerSizePx
            leftEmojiContainer.layoutParams = params
        }
        val emojiSizePx = (batteryArtSizePx * snapshot.emojiAdjustmentScale.coerceIn(0.35f, 2.2f))
            .roundToInt()
            .coerceAtLeast((10f * context.resources.displayMetrics.density).roundToInt())
        val batteryStackSizePx = maxOf(batteryArtSizePx, emojiSizePx)
        (batteryArtContainer.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = batteryStackSizePx
            params.height = batteryStackSizePx
            batteryArtContainer.layoutParams = params
        }
        (emojiArtView.layoutParams as FrameLayout.LayoutParams).also { params ->
            params.width = emojiSizePx
            params.height = emojiSizePx
            emojiArtView.layoutParams = params
        }
        (batteryEmojiArtView.layoutParams as FrameLayout.LayoutParams).also { params ->
            params.width = emojiSizePx
            params.height = emojiSizePx
            batteryEmojiArtView.layoutParams = params
        }
        emojiTextView.textSize = (8f + snapshot.emojiAdjustmentScale.coerceIn(0.35f, 2.2f) * 5f)
        batteryEmojiTextView.textSize = (8f + snapshot.emojiAdjustmentScale.coerceIn(0.35f, 2.2f) * 5f)
        val emojiTravelRangePx = batteryStackSizePx * 0.55f
        val emojiTranslationX = ((snapshot.emojiOffsetX.coerceIn(0f, 1f) - 0.5f) * 2f * emojiTravelRangePx)
        val emojiTranslationY = ((snapshot.emojiOffsetY.coerceIn(0f, 1f) - 0.5f) * 2f * emojiTravelRangePx)
        emojiArtView.translationX = emojiTranslationX
        emojiArtView.translationY = emojiTranslationY
        emojiTextView.translationX = emojiTranslationX
        emojiTextView.translationY = emojiTranslationY
        batteryEmojiArtView.translationX = emojiTranslationX
        batteryEmojiArtView.translationY = emojiTranslationY
        batteryEmojiTextView.translationX = emojiTranslationX
        batteryEmojiTextView.translationY = emojiTranslationY
        if (useBatteryTrollSource) {
            // Use config-based reference height to avoid feedback loop where each render reads a previously scaled view size.
            val statusRowRefHeightPx = (baseStatusRowHeightPx * statusScale).roundToInt().coerceAtLeast((14f * density).roundToInt())
            val trollScale = snapshot.trollEmojiSizeDp.coerceIn(1, 50) / 50f
            val trollArtSizePx = (statusRowRefHeightPx * trollScale)
                .roundToInt()
                .coerceAtLeast((10f * density).roundToInt())
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
            batteryEmojiContainer.visibility = View.GONE
            batteryEmojiArtView.setImageDrawable(null)
            batteryEmojiTextView.visibility = View.GONE
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
                val trollRealBatteryText = if (snapshot.trollShowPercentage) {
                    "${liveStatus.batteryPercent}%"
                } else {
                    liveStatus.batteryPercent.toString()
                }
                val trollChargeSuffix = if (snapshot.trollUseRealBattery && liveStatus.charging && snapshot.animateCharge && chargeConfig.enabled) {
                    chargeGlyph(chargeConfig.variant)
                } else {
                    ""
                }
                batteryView.text = if (snapshot.trollUseRealBattery) trollRealBatteryText else formatTrollPercentageText(snapshot.trollMessage, snapshot.trollShowPercentage)
                chargeView.text = trollChargeSuffix
                chargeView.visibility = if (trollChargeSuffix.isNotBlank()) View.VISIBLE else View.GONE
                chargeArtView.visibility = View.GONE
                chargeArtView.setImageDrawable(null)
            } else {
                trollArtContainer.visibility = View.GONE
                trollBatteryArtView.setImageDrawable(null)
                trollEmojiArtView.setImageDrawable(null)
                batteryView.text = formatTrollPercentageText(snapshot.trollMessage, snapshot.trollShowPercentage)
                chargeView.text = ""
                chargeView.visibility = View.GONE
                chargeArtView.visibility = View.GONE
                chargeArtView.setImageDrawable(null)
            }
        } else {
            trollArtContainer.visibility = View.GONE
            trollBatteryArtView.setImageDrawable(null)
            trollEmojiArtView.setImageDrawable(null)
            val hasBatteryArt = batteryUrl != null || batteryDrawable != null
            val hasBatteryEmojiArt = emojiUrl != null || emojiDrawable != null
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
            batteryEmojiContainer.visibility = if (hasBatteryEmojiArt || emojiLabel.isNotBlank()) View.VISIBLE else View.GONE
            when {
                emojiUrl != null -> {
                    batteryEmojiArtView.visibility = View.VISIBLE
                    batteryEmojiArtView.load(emojiUrl) {
                        crossfade(true)
                    }
                    batteryEmojiTextView.visibility = View.GONE
                    batteryEmojiTextView.text = ""
                }
                emojiDrawable != null -> {
                    batteryEmojiArtView.visibility = View.VISIBLE
                    batteryEmojiArtView.setImageResource(emojiDrawable)
                    batteryEmojiTextView.visibility = View.GONE
                    batteryEmojiTextView.text = ""
                }
                else -> {
                    batteryEmojiArtView.visibility = View.GONE
                    batteryEmojiArtView.setImageDrawable(null)
                    batteryEmojiTextView.visibility = View.VISIBLE
                    batteryEmojiTextView.text = emojiLabel
                }
            }
            if (forceEmotionGlyph) {
                emojiArtView.visibility = View.GONE
                emojiArtView.setImageDrawable(null)
                emojiTextView.visibility = View.VISIBLE
                emojiTextView.text = emotionGlyph
            } else {
                emojiArtView.visibility = View.GONE
                emojiArtView.setImageDrawable(null)
                emojiTextView.visibility = View.GONE
            }
            batteryView.text = if (hasBatteryArt) {
                percentageText.trim()
            } else {
                "$batteryLabel$percentageText".trim()
            }
            if (chargeDrawableName != null && chargeDrawableName.isNotBlank()) {
                val resId = context.resources.getIdentifier(chargeDrawableName, "drawable", context.packageName)
                if (resId != 0 && chargeConfig.enabled && liveStatus.charging) {
                    chargeArtView.visibility = View.VISIBLE
                    chargeArtView.setImageResource(resId)
                    chargeView.visibility = View.GONE
                    chargeView.text = ""
                } else {
                    chargeArtView.visibility = View.GONE
                    chargeArtView.setImageDrawable(null)
                    chargeView.text = chargeSuffix
                    chargeView.visibility = if (chargeSuffix.isNotBlank()) View.VISIBLE else View.GONE
                }
            } else {
                chargeArtView.visibility = View.GONE
                chargeArtView.setImageDrawable(null)
                chargeView.text = chargeSuffix
                chargeView.visibility = if (chargeSuffix.isNotBlank()) View.VISIBLE else View.GONE
            }
        }
        batteryView.setTextColor(snapshot.accentColor.toInt())
        chargeView.setTextColor(snapshot.accentColor.toInt())
        if (useBatteryTrollSource) {
            val desiredTextPx = snapshot.trollPercentageSizeDp.coerceIn(5, 40) * scaledDensity
            // Keep percentage text from exceeding the status-bar row height (before row scale transform).
            val maxTextPx = baseStatusRowHeightPx * 0.9f
            batteryView.setTextSize(TypedValue.COMPLEX_UNIT_PX, desiredTextPx.coerceAtMost(maxTextPx))
            chargeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, desiredTextPx.coerceAtMost(maxTextPx))
        } else {
            batteryView.textSize = 11f + (snapshot.batteryPercentScale.coerceIn(0f, 1f) * 11f)
            chargeView.textSize = 11f + (snapshot.batteryPercentScale.coerceIn(0f, 1f) * 11f)
        }
        statusRow.scaleY = statusScale
        val leftPadding = ((8f + snapshot.leftMargin.coerceIn(0f, 1f) * 88f) * density).roundToInt()
        val rightPadding = ((8f + snapshot.rightMargin.coerceIn(0f, 1f) * 88f) * density).roundToInt()
        val topBottomPadding = ((6f + snapshot.statusBarHeight.coerceIn(0f, 1f) * 10f) * density).roundToInt()
        statusRow.setPadding(leftPadding, topBottomPadding, rightPadding, topBottomPadding)
        val parsedDateTime = parseDateTimeVariant(dateTimeConfig.variant)
        applyDateTimeStyle(parsedDateTime.styleId)
        clockView.textSize = 8f + (dateTimeConfig.intensity.coerceIn(0f, 1f) * 8f)
        dateView.textSize = (clockView.textSize * 0.78f).coerceAtLeast(6.5f)
        dateView.visibility = if (parsedDateTime.showDate) View.VISIBLE else View.GONE
        clockView.setTextColor(resolveColorFromVariant(parsedDateTime.colorId, "#111111".toColorInt()))
        dateView.setTextColor(resolveColorFromVariant(parsedDateTime.colorId, "#555555".toColorInt()))

        val airplaneVisible = airplaneConfig.enabled && liveStatus.airplaneMode
        val airplaneSizePx = ((8f + (airplaneConfig.intensity.coerceIn(0f, 1f) * 12f)) * density)
            .roundToInt()
            .coerceAtLeast((12f * density).roundToInt())
        (airplaneIconView.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = airplaneSizePx
            params.height = airplaneSizePx
            airplaneIconView.layoutParams = params
        }
        airplaneIconView.visibility = if (airplaneVisible) View.VISIBLE else View.GONE
        if (airplaneVisible) {
            airplaneIconView.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.galaxy_airplane)?.mutate(),
            )
            airplaneIconView.setColorFilter(
                resolveColorFromVariant(airplaneConfig.variant, "#333333".toColorInt()),
                PorterDuff.Mode.SRC_IN,
            )
        } else {
            airplaneIconView.setImageDrawable(null)
        }
        val wifiVisible = wifiConfig.enabled && liveStatus.wifiEnabled
        val dataVisible = dataConfig.enabled && !liveStatus.wifiEnabled && liveStatus.mobileConnected
        val signalVisible = signalConfig.enabled && !liveStatus.airplaneMode
        val hotspotVisible = hotspotConfig.enabled && liveStatus.hotspotEnabled
        val hotspotSizePx = ((8f + (hotspotConfig.intensity.coerceIn(0f, 1f) * 12f)) * density)
            .roundToInt()
            .coerceAtLeast((12f * density).roundToInt())
        val wifiSizePx = ((8f + (wifiConfig.intensity.coerceIn(0f, 1f) * 12f)) * density)
            .roundToInt()
            .coerceAtLeast((12f * density).roundToInt())
        val dataSizePx = ((8f + (dataConfig.intensity.coerceIn(0f, 1f) * 12f)) * density)
            .roundToInt()
            .coerceAtLeast((12f * density).roundToInt())
        val signalSizePx = ((8f + (signalConfig.intensity.coerceIn(0f, 1f) * 12f)) * density)
            .roundToInt()
            .coerceAtLeast((12f * density).roundToInt())
        (hotspotIconView.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = hotspotSizePx
            params.height = hotspotSizePx
            hotspotIconView.layoutParams = params
        }
        (wifiIconView.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = wifiSizePx
            params.height = wifiSizePx
            wifiIconView.layoutParams = params
        }
        (dataIconView.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = dataSizePx
            params.height = dataSizePx
            dataIconView.layoutParams = params
        }
        (signalIconView.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = signalSizePx
            params.height = signalSizePx
            signalIconView.layoutParams = params
        }
        hotspotIconView.visibility = if (hotspotVisible) View.VISIBLE else View.GONE
        if (hotspotVisible) {
            hotspotIconView.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_item_hotspot)?.mutate(),
            )
            hotspotIconView.setColorFilter(
                resolveColorFromVariant(hotspotConfig.variant, "#333333".toColorInt()),
                PorterDuff.Mode.SRC_IN,
            )
        } else {
            hotspotIconView.setImageDrawable(null)
        }
        wifiIconView.visibility = if (wifiVisible) View.VISIBLE else View.GONE
        if (wifiVisible) {
            wifiIconView.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.galaxy_wifi_4s)?.mutate(),
            )
            wifiIconView.setColorFilter(
                resolveColorFromVariant(wifiConfig.variant, "#333333".toColorInt()),
                PorterDuff.Mode.SRC_IN,
            )
        } else {
            wifiIconView.setImageDrawable(null)
        }

        dataIconView.visibility = if (dataVisible) View.VISIBLE else View.GONE
        if (dataVisible) {
            dataIconView.setImageDrawable(
                AppCompatResources.getDrawable(context, dataIconRes(dataConfig.variant))?.mutate(),
            )
            dataIconView.setColorFilter(
                resolveColorFromVariant(dataColorVariant(dataConfig.variant), "#333333".toColorInt()),
                PorterDuff.Mode.SRC_IN,
            )
        } else {
            dataIconView.setImageDrawable(null)
        }

        signalIconView.visibility = if (signalVisible) View.VISIBLE else View.GONE
        if (signalVisible) {
            signalIconView.setImageDrawable(
                AppCompatResources.getDrawable(context, signalIconRes(liveStatus.signalLevel))?.mutate(),
            )
            signalIconView.setColorFilter(
                resolveColorFromVariant(signalConfig.variant, "#333333".toColorInt()),
                PorterDuff.Mode.SRC_IN,
            )
        } else {
            signalIconView.setImageDrawable(null)
        }
        val effectiveRingerStyle = when (liveStatus.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> "mute"
            AudioManager.RINGER_MODE_VIBRATE -> "wave"
            else -> parsedRinger.styleId
        }
        val ringerVisible = ringerConfig.enabled && liveStatus.ringerMode != AudioManager.RINGER_MODE_NORMAL
        val ringerSizePx = ((8f + (ringerConfig.intensity.coerceIn(0f, 1f) * 12f)) * density)
            .roundToInt()
            .coerceAtLeast((12f * density).roundToInt())
        (ringerIconView.layoutParams as? LinearLayout.LayoutParams)?.also { params ->
            params.width = ringerSizePx
            params.height = ringerSizePx
            ringerIconView.layoutParams = params
        }
        ringerIconView.visibility = if (ringerVisible) View.VISIBLE else View.GONE
        if (ringerVisible) {
            val customName = ringerDrawableName(parsedRinger.styleId, liveStatus.ringerMode)
            val customRes = customName?.let { context.resources.getIdentifier(it, "drawable", context.packageName) }?.takeIf { it != 0 }
            ringerIconView.setImageDrawable(
                AppCompatResources.getDrawable(context, customRes ?: ringerIconRes(effectiveRingerStyle))?.mutate(),
            )
            ringerIconView.setColorFilter(
                resolveColorFromVariant(parsedRinger.colorId, "#333333".toColorInt()),
                PorterDuff.Mode.SRC_IN,
            )
        } else {
            ringerIconView.setImageDrawable(null)
        }

        if (snapshot.stickerEnabled) {
            val screenWidth = context.resources.displayMetrics.widthPixels
            val maxStickerSide = (screenWidth * 0.30f).roundToInt().coerceAtLeast(1)
            // Sticker always lives in a square container; max side is 30% of screen width.
            val stickerSide = (maxStickerSide * snapshot.stickerSize.coerceIn(0.2f, 1f))
                .roundToInt()
                .coerceIn((maxStickerSide * 0.2f).roundToInt().coerceAtLeast(1), maxStickerSide)
            listOf(stickerEmojiView, stickerImageView, stickerLottieView).forEach { view ->
                val params = (view.layoutParams as FrameLayout.LayoutParams)
                params.width = stickerSide
                params.height = stickerSide
                view.layoutParams = params
                view.scaleX = 1f
                view.scaleY = 1f
            }
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
                    Log.d(
                        LOTTIE_TRACE_TAG,
                        "overlaySticker: load lottieUrl=$lottieUrl fromUrl=${lottieUrl.startsWith("http://", ignoreCase = true) || lottieUrl.startsWith("https://", ignoreCase = true)}",
                    )
                    if (lottieUrl.startsWith("http://", ignoreCase = true) || lottieUrl.startsWith("https://", ignoreCase = true)) {
                        stickerLottieView.setAnimationFromUrl(lottieUrl)
                    } else {
                        stickerLottieView.setAnimation(lottieUrl)
                    }
                    stickerLottieView.playAnimation()
                    currentStickerLottieUrl = lottieUrl
                    Log.d(TAG, "Applied sticker lottie=$lottieUrl")
                } else if (!stickerLottieView.isAnimating) {
                    Log.d(
                        LOTTIE_TRACE_TAG,
                        "overlaySticker: reuse existing lottieUrl=$lottieUrl (resume animation)",
                    )
                    stickerLottieView.playAnimation()
                }
            } else if (url != null) {
                stickerLottieView.cancelAnimation()
                stickerLottieView.visibility = View.GONE
                currentStickerLottieUrl = null
                stickerImageView.visibility = View.VISIBLE
                stickerEmojiView.visibility = View.GONE
                Log.d(LOTTIE_TRACE_TAG, "overlaySticker: no lottie, fallback imageUrl=$url")
                loadAnimatedImage(stickerImageView, url, crossfade = true, source = "sticker")
            } else {
                stickerLottieView.cancelAnimation()
                stickerLottieView.visibility = View.GONE
                currentStickerLottieUrl = null
                stickerImageView.visibility = View.GONE
                stickerEmojiView.visibility = View.VISIBLE
                Log.d(LOTTIE_TRACE_TAG, "overlaySticker: no lottie/image, fallback glyph=${snapshot.stickerGlyph}")
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

        stickerLayer.bringToFront()
        listOf(stickerImageView, stickerLottieView, stickerEmojiView).forEach { it.bringToFront() }

        statusRow.visibility = if (snapshot.statusBarEnabled || useBatteryTrollSource) View.VISIBLE else View.GONE
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
        val stickerSide = (stickerImageView.layoutParams as? FrameLayout.LayoutParams)
            ?.width
            ?.takeIf { it > 0 }
            ?: (56f * density).roundToInt()
        val statusBandHeight = statusRow.height.takeIf { it > 0 }
            ?: (40f * density).roundToInt()
        val maxTop = (statusBandHeight - stickerSide).coerceAtLeast(0)
        val left = ((screenWidth - stickerSide) * offsetX.coerceIn(0f, 1f)).roundToInt()
        val top = (maxTop * offsetY.coerceIn(0f, 1f)).roundToInt()
        listOf(stickerImageView, stickerLottieView, stickerEmojiView).forEach { view ->
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP or Gravity.START
            params.leftMargin = 0
            params.topMargin = 0
            view.layoutParams = params
            view.translationX = left.toFloat()
            view.translationY = top.toFloat()
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

    private fun signalIconRes(level: Int): Int = when (level.coerceIn(0, 4)) {
        0 -> R.drawable.galaxy_signal_0
        1 -> R.drawable.galaxy_signal_1
        2 -> R.drawable.galaxy_signal_2
        3 -> R.drawable.galaxy_signal_3
        else -> R.drawable.galaxy_signal_4
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

    private fun dataIconRes(variant: String): Int = when (dataStyleVariant(variant).lowercase()) {
        "2g" -> R.drawable.galaxy_data_2g
        "3g" -> R.drawable.galaxy_data_3g
        "4g" -> R.drawable.galaxy_data_4g
        "6g" -> R.drawable.galaxy_data_6g
        else -> R.drawable.galaxy_data_5g
    }

    private fun dataStyleVariant(variant: String): String = variant.substringBefore("::").ifBlank { "5G" }

    private fun dataColorVariant(variant: String): String {
        val color = variant.substringAfter("::", missingDelimiterValue = "")
        return if (color.isBlank()) "blue" else color
    }

    private fun ringerIconRes(variant: String): Int = when (variant.lowercase()) {
        "mute" -> android.R.drawable.ic_lock_silent_mode
        "wave" -> R.drawable.ic_vibrate_feedback_32
        else -> android.R.drawable.ic_lock_silent_mode_off
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

    private fun formatTrollPercentageText(raw: String, showPercent: Boolean): String {
        val value = raw.trim().ifBlank { "100" }
        val normalized = if (value.endsWith("%")) value.dropLast(1).trim() else value
        return if (showPercent) "$normalized%" else normalized
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
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP
        }
    }
}
