package dev.hai.emojibattery.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.model.ThemeBatteryRuntime
import dev.hai.emojibattery.model.ThemeStatusIcons
import dev.hai.emojibattery.service.OverlayConfigStore.BATTERY_EMOJI_SOURCE_BATTERY_TROLL
import dev.hai.emojibattery.ui.screen.chargeVariantDrawableName
import dev.hai.emojibattery.ui.screen.parseChargeVariant
import dev.hai.emojibattery.ui.screen.EmotionOptions
import dev.hai.emojibattery.ui.screen.parseDateTimeVariant
import dev.hai.emojibattery.ui.screen.parseEmotionVariant
import dev.hai.emojibattery.ui.screen.parseRingerVariant
import dev.hai.emojibattery.ui.screen.ringerDrawableName
import kotlin.math.abs
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
        private const val MIN_STATUS_BAR_HEIGHT_FACTOR = 0.5f
        private const val MAX_STATUS_BAR_HEIGHT_FACTOR = 2f
    }

    private enum class ThemeStatusBarBackgroundType {
        WALLPAPER,
        BLUE_BACKGROUND,
        COLOR,
    }

    data class LiveStatus(
        val batteryPercent: Int = 0,
        val charging: Boolean = false,
        val hotspotEnabled: Boolean = false,
        val wifiEnabled: Boolean = false,
        val wifiLevel: Int = 0,
        val mobileConnected: Boolean = false,
        val airplaneMode: Boolean = false,
        val signalLevel: Int = 0,
        val ringerMode: Int = AudioManager.RINGER_MODE_NORMAL,
    )

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val baseArtSizePx = (18 * context.resources.displayMetrics.density).toInt().coerceAtLeast(14)

    private val root = FrameLayout(context)
    private val stickerRoot = FrameLayout(context)
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
    private var stickerAttached = false
    private var gestureEnabled = false
    private var layoutParams: WindowManager.LayoutParams? = null
    private var stickerLayoutParams: WindowManager.LayoutParams? = null
    private var currentWindowHeightPx: Int = WindowManager.LayoutParams.MATCH_PARENT
    private var currentAnimationKey: String? = null
    private var currentAnimationIsLottie: Boolean? = null
    private var currentStickerLottieUrl: String? = null
    private var trollShuffleVersion: Long = 0L
    private var appliedTrollShuffleVersion: Long = -1L
    private var currentTrollBatteryArtUrl: String? = null
    private var currentTrollEmojiArtUrl: String? = null
    private val themeAssetExistenceCache = mutableMapOf<String, Boolean>()
    private val themeWallpaperStripCache = mutableMapOf<String, BitmapDrawable?>()
    private val themeBlueBackgroundStripCache = mutableMapOf<String, BitmapDrawable?>()
    private val themeIconDominantColorCache = mutableMapOf<String, Int?>()

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
        stickerRoot.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        stickerRoot.clipChildren = false
        stickerRoot.clipToPadding = false
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
        clockView.typeface = Typeface.DEFAULT
        dateView.textSize = 10f
        dateView.format12Hour = "EEE, MMM d"
        dateView.format24Hour = "EEE, MMM d"
        dateView.alpha = 0.75f
        dateView.typeface = Typeface.DEFAULT
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
        stickerRoot.addView(
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
        syncWindowHeight(snapshot)
        syncStickerWindow(snapshot)
        ensureAttached()
        if (!snapshot.statusBarEnabled) {
            root.alpha = 0f
            stickerRoot.alpha = 0f
            return
        }
        root.alpha = 1f
        stickerRoot.alpha = 1f
        val effectiveWindowHeightPx = currentWindowHeightPx
            .takeIf { it > 0 && it != WindowManager.LayoutParams.MATCH_PARENT }
            ?: resolveOverlayWindowHeight(snapshot)
        val statusLayerHeightPx = effectiveWindowHeightPx.coerceAtLeast(resolveSystemStatusBarHeightPx())
        val templateDrawableRes = snapshot.backgroundTemplateDrawableRes?.takeIf { it != 0 }
        val templateUrl = snapshot.backgroundTemplatePhotoUrl?.takeIf { it.isNotBlank() }
        val resolvedBackgroundColor = snapshot.backgroundColor
            .toInt()
            .takeIf { it != 0 }
            ?: SampleCatalog.defaultConfig.backgroundColor.toInt()
        applyThemeStatusBarBackground(
            snapshot = snapshot,
            statusLayerHeightPx = statusLayerHeightPx,
            templateDrawableRes = templateDrawableRes,
            templateUrl = templateUrl,
            resolvedBackgroundColor = resolvedBackgroundColor,
        )
        val featureConfigs = snapshot.featureConfigs
        val wifiConfig = featureConfigs[CustomizeEntry.Wifi] ?: defaultFeatureConfig
        val signalConfig = featureConfigs[CustomizeEntry.Signal] ?: defaultFeatureConfig
        val dataConfig = featureConfigs[CustomizeEntry.Data] ?: defaultFeatureConfig
        val hotspotConfig = featureConfigs[CustomizeEntry.Hotspot] ?: defaultFeatureConfig
        val airplaneConfig = featureConfigs[CustomizeEntry.Airplane] ?: defaultFeatureConfig
        val chargeConfig = featureConfigs[CustomizeEntry.Charge] ?: defaultFeatureConfig
        val chargeState = parseChargeVariant(chargeConfig.variant)
        val chargeTintColor = resolveColorFromVariant(chargeState.colorId, "#333333".toColorInt())
        val ringerConfig = featureConfigs[CustomizeEntry.Ringer] ?: defaultFeatureConfig
        val dateTimeConfig = featureConfigs[CustomizeEntry.DateTime] ?: defaultFeatureConfig
        val emotionConfig = featureConfigs[CustomizeEntry.Emotion] ?: defaultFeatureConfig
        val parsedRinger = parseRingerVariant(ringerConfig.variant)
        val themeStatusIcons = snapshot.themeStatusIcons
        val themedWifiCandidates = themeStatusIcons?.wifi.orEmpty()
        val themedSignalCandidates = themeStatusIcons?.signal.orEmpty()
        val themedDataCandidates = themeStatusIcons?.data.orEmpty()
        val themedHotspotCandidates = themeStatusIcons?.hotspot.orEmpty()
        val themedRingerCandidates = themeStatusIcons?.ringer.orEmpty()
        val dataSystemEnabled = !liveStatus.wifiEnabled && liveStatus.mobileConnected
        val wifiHasOffAsset = hasThemedToggleOffAsset(
            candidates = themedWifiCandidates,
            offMarkers = listOf("wifi_off"),
        )
        val dataHasOffAsset = hasThemedToggleOffAsset(
            candidates = themedDataCandidates,
            offMarkers = listOf("data_off"),
        )
        val hotspotHasOffAsset = hasThemedToggleOffAsset(
            candidates = themedHotspotCandidates,
            offMarkers = listOf("wifi_ap_off", "hotspot_off"),
        )
        val themedAirplaneAsset = themeStatusIcons
            ?.airplane
            ?.takeIf { it.isNotBlank() }
            ?.takeIf { themeAssetExists(it) }
        val themedHotspotAsset = selectThemedHotspotAsset(
            candidates = themedHotspotCandidates,
            enabled = liveStatus.hotspotEnabled,
        )
        val themedWifiAsset = selectThemedWifiAsset(
            candidates = themedWifiCandidates,
            enabled = liveStatus.wifiEnabled,
            level = liveStatus.wifiLevel,
        )
        val themedDataAsset = selectThemedDataAsset(
            candidates = themedDataCandidates,
            styleHint = dataStyleVariant(dataConfig.variant),
            systemEnabled = dataSystemEnabled,
        )
        val themedSignalAsset = selectThemedSignalAsset(
            candidates = themedSignalCandidates,
            level = liveStatus.signalLevel,
            systemConnected = liveStatus.mobileConnected,
        )
        val themedRingerAsset = selectThemedRingerAsset(
            candidates = themedRingerCandidates,
            ringerMode = liveStatus.ringerMode,
        )
        val themedChargeAssetRaw = themeStatusIcons
            ?.charge
            ?.takeIf { it.isNotBlank() }
            ?.takeIf { themeAssetExists(it) }
        val themedBatteryDrawable = decodeThemedBatteryDrawable(
            battery = themeStatusIcons?.battery,
            batteryPercent = liveStatus.batteryPercent,
            charging = liveStatus.charging,
        )
        val themedBatteryStaticAssetFromSprite = selectThemedBatteryStaticAsset(
            battery = themeStatusIcons?.battery,
            charging = liveStatus.charging,
        )
        val themedBatteryStaticAsset = themedBatteryStaticAssetFromSprite
            ?: themedChargeAssetRaw.takeIf { themeStatusIcons?.battery == null }
        val themedChargeAsset = themedChargeAssetRaw.takeUnless {
            themeStatusIcons?.battery == null && it != null && it == themedBatteryStaticAsset
        }
        if (themeStatusIcons?.battery == null && themedBatteryStaticAsset != null && themedBatteryStaticAsset == themedChargeAssetRaw) {
            Log.d(
                TAG,
                "themeBatteryFallback useChargeAsBattery asset=$themedBatteryStaticAsset optionId=${themeStatusIcons.optionId}",
            )
        }

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
        (statusRow.layoutParams as? FrameLayout.LayoutParams)?.also { params ->
            if (params.height != statusLayerHeightPx) {
                params.height = statusLayerHeightPx
                statusRow.layoutParams = params
            }
        }
        (statusBackgroundImageView.layoutParams as? FrameLayout.LayoutParams)?.also { params ->
            if (params.height != statusLayerHeightPx) {
                params.height = statusLayerHeightPx
                statusBackgroundImageView.layoutParams = params
            }
        }
        (gestureLayer.layoutParams as? FrameLayout.LayoutParams)?.also { params ->
            if (params.height != statusLayerHeightPx) {
                params.height = statusLayerHeightPx
                gestureLayer.layoutParams = params
            }
        }
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
            val statusRowRefHeightPx = effectiveWindowHeightPx.coerceAtLeast((14f * density).roundToInt())
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
                clearThemedIconTag(chargeArtView)
            } else {
                trollArtContainer.visibility = View.GONE
                trollBatteryArtView.setImageDrawable(null)
                trollEmojiArtView.setImageDrawable(null)
                batteryView.text = formatTrollPercentageText(snapshot.trollMessage, snapshot.trollShowPercentage)
                chargeView.text = ""
                chargeView.visibility = View.GONE
                chargeArtView.visibility = View.GONE
                chargeArtView.setImageDrawable(null)
                clearThemedIconTag(chargeArtView)
            }
        } else {
            trollArtContainer.visibility = View.GONE
            trollBatteryArtView.setImageDrawable(null)
            trollEmojiArtView.setImageDrawable(null)
            val hasThemeBatteryArt = themedBatteryDrawable != null || themedBatteryStaticAsset != null
            val hasBatteryArt = hasThemeBatteryArt || batteryUrl != null || batteryDrawable != null
            val hasBatteryEmojiArt = emojiUrl != null || emojiDrawable != null
            val showLeftEmoji = forceEmotionGlyph
            leftEmojiContainer.visibility = if (showLeftEmoji) View.VISIBLE else View.GONE
            batteryArtContainer.visibility = if (hasBatteryArt) View.VISIBLE else View.GONE
            when {
                themedBatteryDrawable != null -> {
                    batteryArtView.visibility = View.VISIBLE
                    batteryArtView.setImageDrawable(themedBatteryDrawable)
                    clearThemedIconTag(batteryArtView)
                }
                themedBatteryStaticAsset != null -> {
                    batteryArtView.visibility = View.VISIBLE
                    batteryArtView.clearColorFilter()
                    loadThemedIconAsset(batteryArtView, themedBatteryStaticAsset)
                }
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
            if (hasThemeBatteryArt) {
                batteryEmojiContainer.visibility = View.GONE
                batteryEmojiArtView.visibility = View.GONE
                batteryEmojiArtView.setImageDrawable(null)
                batteryEmojiTextView.visibility = View.GONE
                batteryEmojiTextView.text = ""
            } else {
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
            if (chargeConfig.enabled && liveStatus.charging && themedChargeAsset != null) {
                chargeArtView.visibility = View.VISIBLE
                chargeArtView.clearColorFilter()
                loadThemedIconAsset(chargeArtView, themedChargeAsset)
                chargeView.visibility = View.GONE
                chargeView.text = ""
            } else if (chargeDrawableName != null && chargeDrawableName.isNotBlank()) {
                val resId = context.resources.getIdentifier(chargeDrawableName, "drawable", context.packageName)
                if (resId != 0 && chargeConfig.enabled && liveStatus.charging) {
                    chargeArtView.visibility = View.VISIBLE
                    chargeArtView.setImageDrawable(
                        AppCompatResources.getDrawable(context, resId)?.mutate(),
                    )
                    clearThemedIconTag(chargeArtView)
                    chargeArtView.setColorFilter(chargeTintColor, PorterDuff.Mode.SRC_IN)
                    chargeView.visibility = View.GONE
                    chargeView.text = ""
                } else {
                    chargeArtView.visibility = View.GONE
                    chargeArtView.setImageDrawable(null)
                    clearThemedIconTag(chargeArtView)
                    chargeView.text = chargeSuffix
                    chargeView.visibility = if (chargeSuffix.isNotBlank()) View.VISIBLE else View.GONE
                }
            } else {
                chargeArtView.visibility = View.GONE
                chargeArtView.setImageDrawable(null)
                clearThemedIconTag(chargeArtView)
                chargeView.text = chargeSuffix
                chargeView.visibility = if (chargeSuffix.isNotBlank()) View.VISIBLE else View.GONE
            }
        }
        batteryView.setTextColor(snapshot.accentColor.toInt())
        chargeView.setTextColor(chargeTintColor)
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
        statusRow.scaleY = 1f
        val leftPadding = ((5f + snapshot.leftMargin.coerceIn(0f, 1f) * 80f) * density).roundToInt()
        val rightPadding = ((5f + snapshot.rightMargin.coerceIn(0f, 1f) * 80f) * density).roundToInt()
        statusRow.setPadding(leftPadding, 0, rightPadding, 0)
        val parsedDateTime = parseDateTimeVariant(dateTimeConfig.variant)
        applyDateTimeStyle(parsedDateTime.styleId)
        clockView.textSize = 8f + (dateTimeConfig.intensity.coerceIn(0f, 1f) * 8f)
        dateView.textSize = (clockView.textSize * 0.78f).coerceAtLeast(6.5f)
        dateView.visibility = if (parsedDateTime.showDate) View.VISIBLE else View.GONE
        val (clockColor, dateColor) = resolveDateTimeTextColors(
            colorVariant = parsedDateTime.colorId,
            themeStatusIcons = snapshot.themeStatusIcons,
            fallbackClockColor = "#111111".toColorInt(),
            fallbackDateColor = "#555555".toColorInt(),
        )
        clockView.setTextColor(clockColor)
        dateView.setTextColor(dateColor)

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
            if (themedAirplaneAsset != null) {
                airplaneIconView.clearColorFilter()
                loadThemedIconAsset(airplaneIconView, themedAirplaneAsset)
            } else {
                airplaneIconView.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.galaxy_airplane)?.mutate(),
                )
                clearThemedIconTag(airplaneIconView)
                airplaneIconView.setColorFilter(
                    resolveColorFromVariant(airplaneConfig.variant, "#333333".toColorInt()),
                    PorterDuff.Mode.SRC_IN,
                )
            }
        } else {
            airplaneIconView.setImageDrawable(null)
            clearThemedIconTag(airplaneIconView)
        }
        val wifiVisible = wifiConfig.enabled && (liveStatus.wifiEnabled || wifiHasOffAsset)
        val dataVisible = dataConfig.enabled &&
            !liveStatus.wifiEnabled &&
            (liveStatus.mobileConnected || dataHasOffAsset)
        val signalVisible = signalConfig.enabled && !liveStatus.airplaneMode
        val hotspotVisible = hotspotConfig.enabled && (liveStatus.hotspotEnabled || hotspotHasOffAsset)
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
            if (themedHotspotAsset != null) {
                hotspotIconView.clearColorFilter()
                loadThemedIconAsset(hotspotIconView, themedHotspotAsset)
            } else {
                hotspotIconView.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_item_hotspot)?.mutate(),
                )
                clearThemedIconTag(hotspotIconView)
                hotspotIconView.setColorFilter(
                    resolveColorFromVariant(hotspotConfig.variant, "#333333".toColorInt()),
                    PorterDuff.Mode.SRC_IN,
                )
            }
        } else {
            hotspotIconView.setImageDrawable(null)
            clearThemedIconTag(hotspotIconView)
        }
        wifiIconView.visibility = if (wifiVisible) View.VISIBLE else View.GONE
        if (wifiVisible) {
            if (themedWifiAsset != null) {
                wifiIconView.clearColorFilter()
                loadThemedIconAsset(wifiIconView, themedWifiAsset)
            } else {
                wifiIconView.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.galaxy_wifi_4s)?.mutate(),
                )
                clearThemedIconTag(wifiIconView)
                wifiIconView.setColorFilter(
                    resolveColorFromVariant(wifiConfig.variant, "#333333".toColorInt()),
                    PorterDuff.Mode.SRC_IN,
                )
            }
        } else {
            wifiIconView.setImageDrawable(null)
            clearThemedIconTag(wifiIconView)
        }

        dataIconView.visibility = if (dataVisible) View.VISIBLE else View.GONE
        if (dataVisible) {
            if (themedDataAsset != null) {
                dataIconView.clearColorFilter()
                loadThemedIconAsset(dataIconView, themedDataAsset)
            } else {
                dataIconView.setImageDrawable(
                    AppCompatResources.getDrawable(context, dataIconRes(dataConfig.variant))?.mutate(),
                )
                clearThemedIconTag(dataIconView)
                dataIconView.setColorFilter(
                    resolveColorFromVariant(dataColorVariant(dataConfig.variant), "#333333".toColorInt()),
                    PorterDuff.Mode.SRC_IN,
                )
            }
        } else {
            dataIconView.setImageDrawable(null)
            clearThemedIconTag(dataIconView)
        }

        signalIconView.visibility = if (signalVisible) View.VISIBLE else View.GONE
        if (signalVisible) {
            if (themedSignalAsset != null) {
                signalIconView.clearColorFilter()
                loadThemedIconAsset(signalIconView, themedSignalAsset)
            } else {
                signalIconView.setImageDrawable(
                    AppCompatResources.getDrawable(context, signalIconRes(liveStatus.signalLevel))?.mutate(),
                )
                clearThemedIconTag(signalIconView)
                signalIconView.setColorFilter(
                    resolveColorFromVariant(signalConfig.variant, "#333333".toColorInt()),
                    PorterDuff.Mode.SRC_IN,
                )
            }
        } else {
            signalIconView.setImageDrawable(null)
            clearThemedIconTag(signalIconView)
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
            if (themedRingerAsset != null) {
                ringerIconView.clearColorFilter()
                loadThemedIconAsset(ringerIconView, themedRingerAsset)
            } else {
                val customName = ringerDrawableName(parsedRinger.styleId, liveStatus.ringerMode)
                val customRes = customName?.let { context.resources.getIdentifier(it, "drawable", context.packageName) }?.takeIf { it != 0 }
                ringerIconView.setImageDrawable(
                    AppCompatResources.getDrawable(context, customRes ?: ringerIconRes(effectiveRingerStyle))?.mutate(),
                )
                clearThemedIconTag(ringerIconView)
                ringerIconView.setColorFilter(
                    resolveFeatureTintColor(
                        colorVariant = parsedRinger.colorId,
                        themeStatusIcons = themeStatusIcons,
                        fallback = "#333333".toColorInt(),
                    ),
                    PorterDuff.Mode.SRC_IN,
                )
            }
        } else {
            ringerIconView.setImageDrawable(null)
            clearThemedIconTag(ringerIconView)
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
        applyNotch(
            templateId = snapshot.notchTemplateId,
            colorVariant = snapshot.notchColorVariant,
            scale = snapshot.notchScale,
            offsetX = snapshot.notchOffsetX,
            offsetY = snapshot.notchOffsetY,
            statusEnabled = snapshot.statusBarEnabled,
        )
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

    private fun applyThemeStatusBarBackground(
        snapshot: OverlaySnapshot,
        statusLayerHeightPx: Int,
        templateDrawableRes: Int?,
        templateUrl: String?,
        resolvedBackgroundColor: Int,
    ) {
        if (templateUrl != null) {
            statusBackgroundImageView.visibility = View.VISIBLE
            statusBackgroundImageView.clearColorFilter()
            statusBackgroundImageView.load(templateUrl) {
                crossfade(true)
            }
            applyStatusRowBackground(
                baseColor = android.graphics.Color.TRANSPARENT,
                showStroke = snapshot.showStroke,
                rounded = true,
            )
            return
        }
        if (templateDrawableRes != null) {
            statusBackgroundImageView.visibility = View.VISIBLE
            statusBackgroundImageView.clearColorFilter()
            statusBackgroundImageView.setImageResource(templateDrawableRes)
            applyStatusRowBackground(
                baseColor = android.graphics.Color.TRANSPARENT,
                showStroke = snapshot.showStroke,
                rounded = true,
            )
            return
        }

        val themeStatusIcons = snapshot.themeStatusIcons
        val backgroundType = resolveThemeStatusBarBackgroundType(themeStatusIcons?.statusBarBackgroundType)
        val wallpaperAsset = themeStatusIcons?.wallpaper
        val themeRequestedColor = parseThemeStatusBarColor(themeStatusIcons?.statusBarColor)
        val iconReferenceColor = resolveThemeIconReferenceColor(themeStatusIcons)
        val contrastedThemeColor = ensureStatusBarBackgroundContrast(
            backgroundColor = themeRequestedColor ?: android.graphics.Color.parseColor("#1A2748"),
            iconColor = iconReferenceColor,
        )
        val wallpaperDrawable = if (backgroundType == ThemeStatusBarBackgroundType.WALLPAPER) {
            decodeThemedWallpaperStatusStrip(
                wallpaperAsset = wallpaperAsset,
                statusBarHeightPx = statusLayerHeightPx,
                cropScale = snapshot.themeWallpaperCropScale,
                cropOffsetX = snapshot.themeWallpaperCropOffsetX,
                cropOffsetY = snapshot.themeWallpaperCropOffsetY,
            )
        } else {
            null
        }
        val blueBackgroundDrawable = if (backgroundType == ThemeStatusBarBackgroundType.BLUE_BACKGROUND) {
            decodeThemedBlueBackgroundStrip(
                statusBarHeightPx = statusLayerHeightPx,
                baseColor = contrastedThemeColor,
            )
        } else {
            null
        }

        when {
            wallpaperDrawable != null -> applyStatusBackgroundDrawable(
                drawable = wallpaperDrawable,
                showStroke = snapshot.showStroke,
                rounded = false,
            )
            blueBackgroundDrawable != null -> applyStatusBackgroundDrawable(
                drawable = blueBackgroundDrawable,
                showStroke = snapshot.showStroke,
                rounded = false,
            )
            backgroundType == ThemeStatusBarBackgroundType.BLUE_BACKGROUND -> applyStatusBackgroundColor(
                baseColor = contrastedThemeColor,
                showStroke = snapshot.showStroke,
                rounded = false,
            )
            else -> applyStatusBackgroundColor(
                baseColor = if (backgroundType == ThemeStatusBarBackgroundType.COLOR) {
                    ensureStatusBarBackgroundContrast(
                        backgroundColor = themeRequestedColor ?: resolvedBackgroundColor,
                        iconColor = iconReferenceColor,
                    )
                } else {
                    resolvedBackgroundColor
                },
                showStroke = snapshot.showStroke,
                rounded = false,
            )
        }
        Log.d(
            TAG,
            "applyStatusBackground type=${backgroundType.name.lowercase()} wallpaperAsset=${wallpaperAsset.orEmpty()} " +
                "statusBarColor=${themeStatusIcons?.statusBarColor.orEmpty()} " +
                "contrastedColor=#${Integer.toHexString(contrastedThemeColor)} iconColor=#${Integer.toHexString(iconReferenceColor)} " +
                "wallpaperDrawable=${wallpaperDrawable != null} blueDrawable=${blueBackgroundDrawable != null}",
        )
    }

    private fun applyStatusBackgroundDrawable(
        drawable: Drawable,
        showStroke: Boolean,
        rounded: Boolean,
    ) {
        statusBackgroundImageView.visibility = View.VISIBLE
        statusBackgroundImageView.clearColorFilter()
        statusBackgroundImageView.setImageDrawable(drawable)
        applyStatusRowBackground(
            baseColor = android.graphics.Color.TRANSPARENT,
            showStroke = showStroke,
            rounded = rounded,
        )
    }

    private fun applyStatusBackgroundColor(
        baseColor: Int,
        showStroke: Boolean,
        rounded: Boolean,
    ) {
        statusBackgroundImageView.visibility = View.GONE
        statusBackgroundImageView.setImageDrawable(null)
        applyStatusRowBackground(
            baseColor = baseColor,
            showStroke = showStroke,
            rounded = rounded,
        )
    }

    private fun resolveThemeStatusBarBackgroundType(raw: String?): ThemeStatusBarBackgroundType {
        return when (raw.orEmpty().trim().lowercase()) {
            "wallpaper" -> ThemeStatusBarBackgroundType.WALLPAPER
            "color" -> ThemeStatusBarBackgroundType.COLOR
            "bluebackground", "blue_background", "blue-bg", "" -> ThemeStatusBarBackgroundType.BLUE_BACKGROUND
            else -> ThemeStatusBarBackgroundType.BLUE_BACKGROUND
        }
    }

    private fun parseThemeStatusBarColor(raw: String?): Int? {
        val normalized = raw.orEmpty().trim()
        if (normalized.isBlank()) return null
        return runCatching { android.graphics.Color.parseColor(normalized) }.getOrNull()
    }

    private fun resolveThemeIconReferenceColor(themeStatusIcons: ThemeStatusIcons?): Int {
        if (themeStatusIcons == null) return android.graphics.Color.WHITE
        val candidates = buildList {
            addAll(themeStatusIcons.wifi)
            addAll(themeStatusIcons.signal)
            addAll(themeStatusIcons.data)
            addAll(themeStatusIcons.hotspot)
            addAll(themeStatusIcons.ringer)
            addAll(themeStatusIcons.bluetooth)
            add(themeStatusIcons.airplane)
            add(themeStatusIcons.charge)
            add(themeStatusIcons.battery?.normalAsset.orEmpty())
        }.filter { it.isNotBlank() }

        for (asset in candidates) {
            val color = resolveDominantColorFromAsset(asset) ?: continue
            return color
        }
        return android.graphics.Color.WHITE
    }

    private fun resolveDominantColorFromAsset(assetPath: String): Int? {
        val normalized = normalizeAssetPath(assetPath)
        if (normalized.isBlank() || !themeAssetExists(normalized)) return null
        if (themeIconDominantColorCache.containsKey(normalized)) {
            return themeIconDominantColorCache[normalized]
        }
        val dominant = runCatching {
            context.assets.open(normalized).use(BitmapFactory::decodeStream)
        }.getOrNull()?.let { bitmap ->
            val stepX = (bitmap.width / 18).coerceAtLeast(1)
            val stepY = (bitmap.height / 18).coerceAtLeast(1)
            var count = 0L
            var red = 0L
            var green = 0L
            var blue = 0L
            var y = 0
            while (y < bitmap.height) {
                var x = 0
                while (x < bitmap.width) {
                    val pixel = bitmap.getPixel(x, y)
                    val alpha = android.graphics.Color.alpha(pixel)
                    if (alpha >= 60) {
                        red += android.graphics.Color.red(pixel)
                        green += android.graphics.Color.green(pixel)
                        blue += android.graphics.Color.blue(pixel)
                        count++
                    }
                    x += stepX
                }
                y += stepY
            }
            if (count > 0) {
                android.graphics.Color.rgb(
                    (red / count).toInt(),
                    (green / count).toInt(),
                    (blue / count).toInt(),
                )
            } else {
                null
            }
        }
        themeIconDominantColorCache[normalized] = dominant
        return dominant
    }

    private fun ensureStatusBarBackgroundContrast(
        backgroundColor: Int,
        iconColor: Int,
        minContrast: Double = 3.2,
    ): Int {
        val opaqueBackground = ColorUtils.setAlphaComponent(backgroundColor, 0xFF)
        if (ColorUtils.calculateContrast(iconColor, opaqueBackground) >= minContrast) {
            return opaqueBackground
        }
        val iconIsLight = ColorUtils.calculateLuminance(iconColor) >= 0.5
        var bestColor = opaqueBackground
        var bestContrast = ColorUtils.calculateContrast(iconColor, opaqueBackground)
        for (step in 1..12) {
            val ratio = step / 12f
            val blended = if (iconIsLight) {
                ColorUtils.blendARGB(opaqueBackground, android.graphics.Color.BLACK, ratio)
            } else {
                ColorUtils.blendARGB(opaqueBackground, android.graphics.Color.WHITE, ratio)
            }
            val candidate = ColorUtils.setAlphaComponent(blended, 0xFF)
            val candidateContrast = ColorUtils.calculateContrast(iconColor, candidate)
            if (candidateContrast > bestContrast) {
                bestContrast = candidateContrast
                bestColor = candidate
            }
            if (candidateContrast >= minContrast) break
        }
        return bestColor
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
        if (attached) {
            windowManager.removeView(root)
            attached = false
            layoutParams = null
        }
        if (stickerAttached) {
            windowManager.removeView(stickerRoot)
            stickerAttached = false
            stickerLayoutParams = null
        }
        currentWindowHeightPx = WindowManager.LayoutParams.MATCH_PARENT
        singleTapHandler.removeCallbacks(singleTapRunnable)
    }

    fun setGestureEnabled(enabled: Boolean) {
        if (gestureEnabled == enabled) return
        gestureEnabled = enabled
        if (attached) {
            val updated = createLayoutParams(gestureEnabled, currentWindowHeightPx)
            windowManager.updateViewLayout(root, updated)
            layoutParams = updated
        }
        if (stickerAttached) {
            val updatedSticker = createLayoutParams(false, WindowManager.LayoutParams.MATCH_PARENT)
            windowManager.updateViewLayout(stickerRoot, updatedSticker)
            stickerLayoutParams = updatedSticker
        }
    }

    private fun ensureAttached() {
        if (attached) return
        val params = createLayoutParams(gestureEnabled, currentWindowHeightPx)
        layoutParams = params
        windowManager.addView(root, params)
        Log.i(
            TAG,
            "ensureAttached addView height=${params.height} flags=0x${params.flags.toString(16)} type=${params.type} y=${params.y} gravity=${params.gravity}",
        )
        attached = true
    }

    private fun ensureStickerAttached() {
        if (stickerAttached) return
        val params = createLayoutParams(false, WindowManager.LayoutParams.MATCH_PARENT)
        stickerLayoutParams = params
        windowManager.addView(stickerRoot, params)
        Log.i(
            TAG,
            "ensureStickerAttached addView height=${params.height} flags=0x${params.flags.toString(16)} type=${params.type} y=${params.y} gravity=${params.gravity}",
        )
        stickerAttached = true
    }

    private fun syncWindowHeight(snapshot: OverlaySnapshot) {
        val desiredHeight = resolveOverlayWindowHeight(snapshot)
        if (currentWindowHeightPx == desiredHeight) return
        currentWindowHeightPx = desiredHeight
        if (!attached) return
        val updated = createLayoutParams(gestureEnabled, desiredHeight)
        layoutParams = updated
        windowManager.updateViewLayout(root, updated)
        Log.i(TAG, "syncWindowHeight height=${updated.height}")
    }

    private fun resolveOverlayWindowHeight(snapshot: OverlaySnapshot): Int {
        val systemStatusBarHeightPx = resolveSystemStatusBarHeightPx()
        val heightFactor = snapshot.statusBarHeight.coerceIn(MIN_STATUS_BAR_HEIGHT_FACTOR, MAX_STATUS_BAR_HEIGHT_FACTOR)
        val minHeightPx = (systemStatusBarHeightPx * MIN_STATUS_BAR_HEIGHT_FACTOR).roundToInt().coerceAtLeast(1)
        return (systemStatusBarHeightPx * heightFactor).roundToInt().coerceAtLeast(minHeightPx)
    }

    private fun syncStickerWindow(snapshot: OverlaySnapshot) {
        val shouldAttachStickerWindow = snapshot.stickerEnabled
        if (shouldAttachStickerWindow) {
            ensureStickerAttached()
            if (stickerRoot.alpha != root.alpha) {
                stickerRoot.alpha = root.alpha
            }
        } else if (stickerAttached) {
            windowManager.removeView(stickerRoot)
            stickerAttached = false
            stickerLayoutParams = null
            Log.i(TAG, "syncStickerWindow detach")
        }
    }

    private fun resolveSystemStatusBarHeightPx(): Int {
        val statusBarResId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (statusBarResId != 0) {
            return context.resources.getDimensionPixelSize(statusBarResId)
        }
        return (24f * context.resources.displayMetrics.density).roundToInt()
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

    private fun chargeGlyph(variant: String): String = when (parseChargeVariant(variant).itemId.lowercase()) {
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

    private fun resolveDateTimeTextColors(
        colorVariant: String?,
        themeStatusIcons: ThemeStatusIcons?,
        fallbackClockColor: Int,
        fallbackDateColor: Int,
    ): Pair<Int, Int> {
        val normalized = colorVariant.orEmpty().trim().lowercase()
        if (normalized == "system") {
            val systemColor = resolveThemeIconReferenceColor(themeStatusIcons)
            val clockColor = if (android.graphics.Color.alpha(systemColor) == 0) {
                fallbackClockColor
            } else {
                systemColor
            }
            val dateColor = ColorUtils.setAlphaComponent(
                clockColor,
                (android.graphics.Color.alpha(clockColor) * 0.78f).roundToInt().coerceIn(90, 255),
            )
            return clockColor to dateColor
        }

        return resolveColorFromVariant(colorVariant, fallbackClockColor) to
            resolveColorFromVariant(colorVariant, fallbackDateColor)
    }

    private fun resolveFeatureTintColor(
        colorVariant: String?,
        themeStatusIcons: ThemeStatusIcons?,
        fallback: Int,
    ): Int {
        if (colorVariant.orEmpty().trim().equals("system", ignoreCase = true)) {
            val systemColor = resolveThemeIconReferenceColor(themeStatusIcons)
            return if (android.graphics.Color.alpha(systemColor) == 0) fallback else systemColor
        }
        return resolveColorFromVariant(colorVariant, fallback)
    }

    private fun selectThemedStaticAsset(candidates: List<String>): String? {
        if (candidates.isEmpty()) return null
        return candidates.firstOrNull { themeAssetExists(it) }
            ?: candidates.firstOrNull()
    }

    private fun selectThemedWifiAsset(
        candidates: List<String>,
        enabled: Boolean,
        level: Int,
    ): String? {
        if (candidates.isEmpty()) return null
        val validAssets = candidates.filter { themeAssetExists(it) }.ifEmpty { candidates }
        val levelAssets = validAssets
            .mapNotNull { asset ->
                parseTrailingLevel(assetBaseName(asset))?.let { parsedLevel -> parsedLevel to asset }
            }
            .sortedBy { it.first }
        if (enabled && levelAssets.isNotEmpty()) {
            val index = level.coerceIn(0, levelAssets.lastIndex)
            return levelAssets[index].second
        }
        val hasToggleState = validAssets.any { assetHasAnyMarker(it, listOf("wifi_on", "wifi_off")) }
        if (hasToggleState) {
            return selectAssetByStateMarkers(
                candidates = validAssets,
                enabled = enabled,
                onMarkers = listOf("wifi_on"),
                offMarkers = listOf("wifi_off"),
            ) ?: if (enabled) validAssets.lastOrNull() else validAssets.firstOrNull()
        }
        return validAssets.lastOrNull()
    }

    private fun selectThemedHotspotAsset(
        candidates: List<String>,
        enabled: Boolean,
    ): String? {
        if (candidates.isEmpty()) return null
        val validAssets = candidates.filter { themeAssetExists(it) }.ifEmpty { candidates }
        val hasToggleState = validAssets.any {
            assetHasAnyMarker(it, listOf("wifi_ap_on", "wifi_ap_off", "hotspot_on", "hotspot_off"))
        }
        if (hasToggleState) {
            return selectAssetByStateMarkers(
                candidates = validAssets,
                enabled = enabled,
                onMarkers = listOf("wifi_ap_on", "hotspot_on"),
                offMarkers = listOf("wifi_ap_off", "hotspot_off"),
            ) ?: if (enabled) validAssets.lastOrNull() else validAssets.firstOrNull()
        }
        return validAssets.firstOrNull()
    }

    private fun selectThemedSignalAsset(
        candidates: List<String>,
        level: Int,
        systemConnected: Boolean,
    ): String? {
        if (candidates.isEmpty()) return null
        val validAssets = candidates.filter { themeAssetExists(it) }.ifEmpty { candidates }
        val hasToggleState = validAssets.any {
            assetHasAnyMarker(it, listOf("signal_on", "signal_off", "data_on", "data_off"))
        }
        if (hasToggleState) {
            return selectAssetByStateMarkers(
                candidates = validAssets,
                enabled = systemConnected,
                onMarkers = listOf("signal_on", "data_on"),
                offMarkers = listOf("signal_off", "data_off"),
            ) ?: validAssets.firstOrNull()
        }
        val index = level.coerceIn(0, validAssets.lastIndex)
        return validAssets.getOrNull(index) ?: validAssets.lastOrNull()
    }

    private fun selectThemedDataAsset(
        candidates: List<String>,
        styleHint: String,
        systemEnabled: Boolean,
    ): String? {
        if (candidates.isEmpty()) return null
        val validAssets = candidates.filter { themeAssetExists(it) }.ifEmpty { candidates }
        val hasToggleState = validAssets.any { assetHasAnyMarker(it, listOf("data_on", "data_off")) }
        if (hasToggleState) {
            if (!systemEnabled) {
                return selectAssetByStateMarkers(
                    candidates = validAssets,
                    enabled = false,
                    onMarkers = listOf("data_on"),
                    offMarkers = listOf("data_off"),
                ) ?: validAssets.firstOrNull()
            }

            val styleScoped = validAssets.filterNot { assetHasAnyMarker(it, listOf("data_on", "data_off")) }
            val normalizedStyle = styleHint.lowercase()
            val styleMatch = styleScoped.firstOrNull { candidate ->
                assetBaseName(candidate).contains(normalizedStyle)
            }
            if (styleMatch != null) return styleMatch

            return selectAssetByStateMarkers(
                candidates = validAssets,
                enabled = true,
                onMarkers = listOf("data_on"),
                offMarkers = listOf("data_off"),
            ) ?: styleScoped.firstOrNull() ?: validAssets.firstOrNull()
        }
        val normalizedStyle = styleHint.lowercase()
        val match = validAssets.firstOrNull { candidate ->
            assetBaseName(candidate).contains(normalizedStyle)
        }
        return match ?: validAssets.firstOrNull()
    }

    private fun selectThemedRingerAsset(
        candidates: List<String>,
        ringerMode: Int,
    ): String? {
        if (candidates.isEmpty()) return null
        val validAssets = candidates.filter { themeAssetExists(it) }.ifEmpty { candidates }
        return when (ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> selectAssetByStateMarkers(
                candidates = validAssets,
                enabled = false,
                onMarkers = listOf("ringer_vibrate", "vibrate_on", "qs_vibrate_on"),
                offMarkers = listOf("ringer_silent", "mute_on", "qs_mute_on"),
            ) ?: validAssets.firstOrNull { assetHasAnyMarker(it, listOf("mute", "silent")) }
                ?: validAssets.firstOrNull()

            AudioManager.RINGER_MODE_VIBRATE -> selectAssetByStateMarkers(
                candidates = validAssets,
                enabled = true,
                onMarkers = listOf("ringer_vibrate", "vibrate_on", "qs_vibrate_on"),
                offMarkers = listOf("ringer_silent", "mute_on", "qs_mute_on"),
            ) ?: validAssets.firstOrNull { assetHasAnyMarker(it, listOf("vibrate", "wave")) }
                ?: validAssets.firstOrNull()

            else -> null
        }
    }

    private fun hasThemedToggleOffAsset(
        candidates: List<String>,
        offMarkers: List<String>,
    ): Boolean {
        if (candidates.isEmpty()) return false
        val validAssets = candidates.filter { themeAssetExists(it) }
        return validAssets.any { assetHasAnyMarker(it, offMarkers) }
    }

    private fun selectAssetByStateMarkers(
        candidates: List<String>,
        enabled: Boolean,
        onMarkers: List<String>,
        offMarkers: List<String>,
    ): String? {
        val markers = if (enabled) onMarkers else offMarkers
        return candidates.firstOrNull { assetHasAnyMarker(it, markers) }
    }

    private fun assetHasAnyMarker(
        asset: String,
        markers: List<String>,
    ): Boolean {
        val baseName = assetBaseName(asset)
        return markers.any { marker -> baseName.contains(marker) }
    }

    private fun assetBaseName(asset: String): String = asset.substringAfterLast('/').lowercase()

    private fun parseTrailingLevel(baseName: String): Int? {
        val token = baseName.substringBeforeLast('.').substringAfterLast('_', "")
        return token.toIntOrNull()
    }

    private fun selectThemedBatteryStaticAsset(
        battery: ThemeBatteryRuntime?,
        charging: Boolean,
    ): String? {
        if (battery == null) return null
        val candidate = resolveThemeBatteryModeAsset(
            battery = battery,
            charging = charging,
        ) ?: return null
        val frames = resolveThemeBatteryModeFrames(
            battery = battery,
            charging = charging,
        )
        if (frames > 1) return null
        return candidate.takeIf { themeAssetExists(it) }
    }

    private fun decodeThemedBatteryDrawable(
        battery: ThemeBatteryRuntime?,
        batteryPercent: Int,
        charging: Boolean,
    ): BitmapDrawable? {
        if (battery == null) return null
        val modeAsset = resolveThemeBatteryModeAsset(battery, charging) ?: return null
        if (!themeAssetExists(modeAsset)) return null
        val modeFrames = resolveThemeBatteryModeFrames(battery, charging)
        if (modeFrames <= 1) return null

        val sourceBitmap = runCatching {
            context.assets.open(normalizeAssetPath(modeAsset)).use(BitmapFactory::decodeStream)
        }.getOrNull() ?: return null
        val frameWidth = deriveThemedBatteryFrameWidth(sourceBitmap, battery, modeFrames)
        val frameHeight = deriveThemedBatteryFrameHeight(sourceBitmap, battery, modeFrames)
        if (frameWidth <= 0 || frameHeight <= 0) return null

        val frameIndex = if (batteryPercent <= 0) {
            0
        } else {
            (((batteryPercent.coerceIn(0, 100) - 1) * modeFrames) / 100).coerceIn(0, modeFrames - 1)
        }
        val (x, y) = when (battery.indexing.lowercase()) {
            "left_to_right" -> frameIndex * frameWidth to 0
            else -> 0 to (frameIndex * frameHeight)
        }
        if (x + frameWidth > sourceBitmap.width || y + frameHeight > sourceBitmap.height) return null
        val frameBitmap = runCatching {
            Bitmap.createBitmap(sourceBitmap, x, y, frameWidth, frameHeight)
        }.getOrNull() ?: return null
        return BitmapDrawable(context.resources, frameBitmap)
    }

    private fun resolveThemeBatteryModeAsset(
        battery: ThemeBatteryRuntime,
        charging: Boolean,
    ): String? {
        val preferred = if (charging) battery.chargingAsset else null
        return preferred?.takeIf { it.isNotBlank() } ?: battery.normalAsset.takeIf { it.isNotBlank() }
    }

    private fun resolveThemeBatteryModeFrames(
        battery: ThemeBatteryRuntime,
        charging: Boolean,
    ): Int {
        return if (charging && !battery.chargingAsset.isNullOrBlank()) {
            battery.chargingFrames.coerceAtLeast(1)
        } else {
            battery.normalFrames.coerceAtLeast(1)
        }
    }

    private fun deriveThemedBatteryFrameWidth(
        sourceBitmap: Bitmap,
        battery: ThemeBatteryRuntime,
        frames: Int,
    ): Int {
        return when (battery.indexing.lowercase()) {
            "left_to_right" -> {
                val inferred = if (frames > 0) sourceBitmap.width / frames else sourceBitmap.width
                battery.frameWidth.takeIf { it in 1..sourceBitmap.width } ?: inferred
            }
            else -> battery.frameWidth.takeIf { it in 1..sourceBitmap.width } ?: sourceBitmap.width
        }
    }

    private fun deriveThemedBatteryFrameHeight(
        sourceBitmap: Bitmap,
        battery: ThemeBatteryRuntime,
        frames: Int,
    ): Int {
        return when (battery.indexing.lowercase()) {
            "left_to_right" -> battery.frameHeight.takeIf { it in 1..sourceBitmap.height } ?: sourceBitmap.height
            else -> {
                val inferred = if (frames > 0) sourceBitmap.height / frames else sourceBitmap.height
                battery.frameHeight
                    .takeIf { it > 0 && it * frames <= sourceBitmap.height }
                    ?: inferred
            }
        }
    }

    private fun decodeThemedWallpaperStatusStrip(
        wallpaperAsset: String?,
        statusBarHeightPx: Int,
        cropScale: Float,
        cropOffsetX: Float,
        cropOffsetY: Float,
    ): BitmapDrawable? {
        val assetPath = wallpaperAsset?.trim().orEmpty()
        if (assetPath.isBlank()) return null
        val normalized = normalizeAssetPath(assetPath)
        if (normalized.isBlank() || !themeAssetExists(normalized)) return null
        val viewport = WallpaperCropMath.resolveViewport(context)
        val targetWidth = viewport.displayWidthPx.coerceAtLeast(1)
        val targetHeight = statusBarHeightPx.coerceAtLeast(1)
        val screenHeight = viewport.displayHeightPx.coerceAtLeast(targetHeight)
        val wallpaperViewportWidth = viewport.wallpaperWidthPx.coerceAtLeast(targetWidth)
        val wallpaperViewportHeight = viewport.wallpaperHeightPx.coerceAtLeast(screenHeight)
        val normalizedScale = "%.3f".format(cropScale.coerceIn(0.75f, 1.35f))
        val normalizedOffsetX = "%.3f".format(cropOffsetX.coerceIn(-1f, 1f))
        val normalizedOffsetY = "%.3f".format(cropOffsetY.coerceIn(-1f, 1f))
        val cacheKey =
            "$normalized|$targetWidth|$targetHeight|$screenHeight|$wallpaperViewportWidth|$wallpaperViewportHeight|$normalizedScale|$normalizedOffsetX|$normalizedOffsetY"
        if (themeWallpaperStripCache.containsKey(cacheKey)) {
            Log.d(
                TAG,
                "decodeThemedWallpaperStatusStrip cacheHit asset=$normalized key=$cacheKey",
            )
            return themeWallpaperStripCache[cacheKey]
        }

        val drawable = runCatching {
            context.assets.open(normalized).use(BitmapFactory::decodeStream)
        }.getOrNull()?.let { sourceBitmap ->
            val dm = context.resources.displayMetrics
            val statusBarResHeight = resolveSystemStatusBarHeightPx()
            val windowBounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowManager.currentWindowMetrics.bounds
            } else {
                null
            }
            Log.d(
                TAG,
                "decodeThemedWallpaperStatusStrip cacheMiss asset=$normalized source=${sourceBitmap.width}x${sourceBitmap.height} " +
                    "display=${viewport.displayWidthPx}x${viewport.displayHeightPx} " +
                    "dm=${dm.widthPixels}x${dm.heightPixels}@${"%.2f".format(dm.density)} " +
                    "window=${windowBounds?.width() ?: -1}x${windowBounds?.height() ?: -1} " +
                    "desired=${viewport.wallpaperWidthPx}x${viewport.wallpaperHeightPx} " +
                    "statusLayer=$targetHeight systemStatus=$statusBarResHeight " +
                    "calib(scale=$normalizedScale,x=$normalizedOffsetX,y=$normalizedOffsetY)",
            )
            buildTopCropWallpaperStrip(
                sourceBitmap = sourceBitmap,
                screenWidthPx = targetWidth,
                screenHeightPx = screenHeight,
                wallpaperViewportWidthPx = wallpaperViewportWidth,
                wallpaperViewportHeightPx = wallpaperViewportHeight,
                statusBarHeightPx = targetHeight,
                cropScale = cropScale,
                cropOffsetX = cropOffsetX,
                cropOffsetY = cropOffsetY,
            )
        }?.let { stripBitmap ->
            BitmapDrawable(context.resources, stripBitmap)
        }

        if (themeWallpaperStripCache.size > 24) {
            themeWallpaperStripCache.clear()
        }
        themeWallpaperStripCache[cacheKey] = drawable
        return drawable
    }

    private fun decodeThemedBlueBackgroundStrip(
        statusBarHeightPx: Int,
        baseColor: Int,
    ): BitmapDrawable? {
        val viewport = WallpaperCropMath.resolveViewport(context)
        val targetWidth = viewport.displayWidthPx.coerceAtLeast(1)
        val targetHeight = statusBarHeightPx.coerceAtLeast(1)
        val cacheKey = "solid_blue_blur|$targetWidth|$targetHeight|${ColorUtils.setAlphaComponent(baseColor, 0xFF)}"
        if (themeBlueBackgroundStripCache.containsKey(cacheKey)) {
            return themeBlueBackgroundStripCache[cacheKey]
        }

        val drawable = runCatching {
            val seed = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val seedCanvas = Canvas(seed)
            val opaqueBase = ColorUtils.setAlphaComponent(baseColor, 0xFF)
            seedCanvas.drawColor(opaqueBase)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.style = Paint.Style.FILL
            paint.color = ColorUtils.blendARGB(opaqueBase, android.graphics.Color.WHITE, 0.16f)
            paint.alpha = 90
            seedCanvas.drawCircle(
                targetWidth * 0.18f,
                targetHeight * 0.24f,
                targetWidth * 0.34f,
                paint,
            )
            paint.color = ColorUtils.blendARGB(opaqueBase, android.graphics.Color.BLACK, 0.22f)
            paint.alpha = 84
            seedCanvas.drawCircle(
                targetWidth * 0.82f,
                targetHeight * 0.70f,
                targetWidth * 0.42f,
                paint,
            )
            paint.color = ColorUtils.blendARGB(opaqueBase, android.graphics.Color.WHITE, 0.10f)
            paint.alpha = 68
            seedCanvas.drawRect(
                0f,
                targetHeight * 0.58f,
                targetWidth.toFloat(),
                targetHeight.toFloat(),
                paint,
            )
            val blurSeed = Bitmap.createScaledBitmap(
                seed,
                (targetWidth / 12).coerceAtLeast(1),
                (targetHeight / 6).coerceAtLeast(1),
                true,
            )
            val blurred = Bitmap.createScaledBitmap(blurSeed, targetWidth, targetHeight, true)
            val tinted = blurred.copy(Bitmap.Config.ARGB_8888, true)
            Canvas(tinted).apply {
                drawColor(ColorUtils.blendARGB(opaqueBase, android.graphics.Color.BLACK, 0.34f), PorterDuff.Mode.SRC_ATOP)
                drawColor(android.graphics.Color.argb(54, 0x32, 0x60, 0xA8), PorterDuff.Mode.SRC_ATOP)
            }
            Log.d(
                TAG,
                "decodeThemedBlueBackgroundStrip mode=solid_blue_blur target=${targetWidth}x${targetHeight} base=#${Integer.toHexString(opaqueBase)}",
            )
            BitmapDrawable(context.resources, tinted)
        }.getOrNull()

        if (themeBlueBackgroundStripCache.size > 24) {
            themeBlueBackgroundStripCache.clear()
        }
        themeBlueBackgroundStripCache[cacheKey] = drawable
        return drawable
    }

    private fun buildTopCropWallpaperStrip(
        sourceBitmap: Bitmap,
        screenWidthPx: Int,
        screenHeightPx: Int,
        wallpaperViewportWidthPx: Int,
        wallpaperViewportHeightPx: Int,
        statusBarHeightPx: Int,
        cropScale: Float,
        cropOffsetX: Float,
        cropOffsetY: Float,
    ): Bitmap? {
        if (sourceBitmap.width <= 0 || sourceBitmap.height <= 0) return null
        val safeScreenWidthPx = screenWidthPx.coerceAtLeast(1)
        val safeScreenHeightPx = screenHeightPx.coerceAtLeast(1)
        val safeWallpaperViewportWidthPx = wallpaperViewportWidthPx.coerceAtLeast(safeScreenWidthPx)
        val safeWallpaperViewportHeightPx = wallpaperViewportHeightPx.coerceAtLeast(safeScreenHeightPx)
        val safeStatusBarHeightPx = statusBarHeightPx.coerceAtLeast(1)
        val sourceAspect = sourceBitmap.width.toFloat() / sourceBitmap.height.toFloat()
        val displayAspect = safeScreenWidthPx.toFloat() / safeScreenHeightPx.toFloat()
        val desiredWidthFactor = safeWallpaperViewportWidthPx.toFloat() / safeScreenWidthPx.toFloat()
        val aspectDelta = abs(sourceAspect - displayAspect)
        val useDisplayAnchoredFallback =
            desiredWidthFactor >= 2f && aspectDelta <= 0.025f
        val modeLabel: String
        val wallpaperViewportCropRect: Rect
        val visibleDisplayRect: Rect
        if (useDisplayAnchoredFallback) {
            modeLabel = "single_stage_fit_start"
            wallpaperViewportCropRect = WallpaperCropMath.computeStartCropRect(
                sourceWidth = sourceBitmap.width,
                sourceHeight = sourceBitmap.height,
                viewportWidthPx = safeScreenWidthPx,
                viewportHeightPx = safeScreenHeightPx,
            )
            visibleDisplayRect = wallpaperViewportCropRect
        } else {
            modeLabel = "two_stage_center"
            // Two-stage model:
            // 1) center-crop source into wallpaper virtual viewport (desired min size from system).
            // 2) take current display window inside that viewport (default center page).
            wallpaperViewportCropRect = WallpaperCropMath.computeCenterCropRect(
                sourceWidth = sourceBitmap.width,
                sourceHeight = sourceBitmap.height,
                viewportWidthPx = safeWallpaperViewportWidthPx,
                viewportHeightPx = safeWallpaperViewportHeightPx,
            )
            visibleDisplayRect = WallpaperCropMath.computeVisibleDisplayRect(
                cropRect = wallpaperViewportCropRect,
                wallpaperViewportWidthPx = safeWallpaperViewportWidthPx,
                wallpaperViewportHeightPx = safeWallpaperViewportHeightPx,
                displayWidthPx = safeScreenWidthPx,
                displayHeightPx = safeScreenHeightPx,
                viewportOffsetX = 0.5f,
                viewportOffsetY = 0.5f,
            )
        }
        val calibratedCropRect = applyWallpaperCropCalibration(
            baseRect = visibleDisplayRect,
            sourceWidth = sourceBitmap.width,
            sourceHeight = sourceBitmap.height,
            cropScale = cropScale,
            cropOffsetX = cropOffsetX,
            cropOffsetY = cropOffsetY,
        )
        val stripRect = WallpaperCropMath.computeTopStripRect(
            cropRect = calibratedCropRect,
            viewportHeightPx = safeScreenHeightPx,
            stripHeightPx = safeStatusBarHeightPx,
        )
        if (stripRect.width() <= 0 || stripRect.height() <= 0) {
            return null
        }
        if (stripRect.left < 0 || stripRect.top < 0 || stripRect.right > sourceBitmap.width || stripRect.bottom > sourceBitmap.height) {
            return null
        }

        Log.d(
            TAG,
                "buildTopCropWallpaperStrip source=${sourceBitmap.width}x${sourceBitmap.height} " +
                "display=${safeScreenWidthPx}x${safeScreenHeightPx} " +
                "wallpaperViewport=${safeWallpaperViewportWidthPx}x${safeWallpaperViewportHeightPx} " +
                "statusBar=$safeStatusBarHeightPx mode=$modeLabel " +
                "aspect(source=${"%.4f".format(sourceAspect)},display=${"%.4f".format(displayAspect)},delta=${"%.4f".format(aspectDelta)}) " +
                "desiredWidthFactor=${"%.3f".format(desiredWidthFactor)} " +
                "calib(scale=${"%.3f".format(cropScale)},x=${"%.3f".format(cropOffsetX)},y=${"%.3f".format(cropOffsetY)}) " +
                "viewportCrop=${wallpaperViewportCropRect.toShortString()} visible=${visibleDisplayRect.toShortString()} " +
                "calibrated=${calibratedCropRect.toShortString()} " +
                "strip=${stripRect.toShortString()} " +
                "visibleRatio=${"%.4f".format(visibleDisplayRect.width().toFloat() / sourceBitmap.width.toFloat())}x${"%.4f".format(visibleDisplayRect.height().toFloat() / sourceBitmap.height.toFloat())} " +
                "stripRatio=${"%.4f".format(stripRect.height().toFloat() / calibratedCropRect.height().toFloat())}",
        )

        val stripBitmap = runCatching {
            Bitmap.createBitmap(
                sourceBitmap,
                stripRect.left,
                stripRect.top,
                stripRect.width(),
                stripRect.height(),
            )
        }.getOrNull() ?: return null
        if (stripBitmap.width == safeScreenWidthPx && stripBitmap.height == safeStatusBarHeightPx) {
            return stripBitmap
        }
        return runCatching {
            Bitmap.createScaledBitmap(stripBitmap, safeScreenWidthPx, safeStatusBarHeightPx, true)
        }.getOrNull() ?: stripBitmap
    }

    private fun applyWallpaperCropCalibration(
        baseRect: Rect,
        sourceWidth: Int,
        sourceHeight: Int,
        cropScale: Float,
        cropOffsetX: Float,
        cropOffsetY: Float,
    ): Rect {
        val safeScale = cropScale.coerceIn(0.75f, 1.35f)
        val safeOffsetX = cropOffsetX.coerceIn(-1f, 1f)
        val safeOffsetY = cropOffsetY.coerceIn(-1f, 1f)
        val baseWidth = baseRect.width().coerceAtLeast(1)
        val baseHeight = baseRect.height().coerceAtLeast(1)
        val targetWidth = (baseWidth / safeScale).roundToInt().coerceIn(1, sourceWidth)
        val targetHeight = (baseHeight / safeScale).roundToInt().coerceIn(1, sourceHeight)
        val centeredLeft = baseRect.left + ((baseWidth - targetWidth) / 2f).roundToInt()
        val centeredTop = baseRect.top + ((baseHeight - targetHeight) / 2f).roundToInt()
        val shiftX = (safeOffsetX * baseWidth * 0.5f).roundToInt()
        val shiftY = (safeOffsetY * baseHeight * 0.5f).roundToInt()
        val maxLeft = (sourceWidth - targetWidth).coerceAtLeast(0)
        val maxTop = (sourceHeight - targetHeight).coerceAtLeast(0)
        val left = (centeredLeft + shiftX).coerceIn(0, maxLeft)
        val top = (centeredTop + shiftY).coerceIn(0, maxTop)
        return Rect(left, top, left + targetWidth, top + targetHeight)
    }

    private fun themeAssetExists(assetPath: String): Boolean {
        val normalized = normalizeAssetPath(assetPath)
        if (normalized.isBlank()) return false
        return themeAssetExistenceCache.getOrPut(normalized) {
            runCatching { context.assets.open(normalized).use { } }.isSuccess
        }
    }

    private fun normalizeAssetPath(assetPath: String): String {
        val trimmed = assetPath.trim()
        val prefix = "file:///android_asset/"
        return if (trimmed.startsWith(prefix)) {
            trimmed.removePrefix(prefix)
        } else {
            trimmed
        }
    }

    private fun loadThemedIconAsset(
        imageView: ImageView,
        assetPath: String,
    ) {
        val normalized = normalizeAssetPath(assetPath)
        if (normalized.isBlank()) return
        val model = "file:///android_asset/$normalized"
        val currentTag = imageView.tag as? String
        if (currentTag == model && imageView.drawable != null) return
        imageView.tag = model
        imageView.load(model) {
            crossfade(false)
        }
    }

    private fun clearThemedIconTag(imageView: ImageView) {
        imageView.tag = null
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

    private fun applyNotch(
        templateId: Int,
        colorVariant: String,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        statusEnabled: Boolean,
    ) {
        val notch = NotchTemplateCatalog.resolve(templateId)
        val drawable = notch.drawableRes
        if (!statusEnabled || drawable == null) {
            notchContainer.visibility = View.GONE
            return
        }
        val notchScale = scale.coerceIn(0.5f, 2.2f)
        val notchHeight = ((18 * context.resources.displayMetrics.density) * notchScale).toInt().coerceAtLeast(12)
        val notchDrawable = AppCompatResources.getDrawable(context, drawable)
        val intrinsicWidth = notchDrawable?.intrinsicWidth ?: 0
        val intrinsicHeight = notchDrawable?.intrinsicHeight ?: 0
        val aspect = if (intrinsicWidth > 0 && intrinsicHeight > 0) {
            intrinsicWidth.toFloat() / intrinsicHeight.toFloat()
        } else {
            960f / 132f
        }
        val notchWidth = (notchHeight * aspect).toInt().coerceAtLeast(notchHeight)
        val parentWidth = root.width.takeIf { it > 0 } ?: context.resources.displayMetrics.widthPixels
        val parentHeight = root.height.takeIf { it > 0 } ?: resolveSystemStatusBarHeightPx()
        val left = ((parentWidth - notchWidth) * offsetX.coerceIn(0f, 1f)).roundToInt()
            .coerceIn(0, (parentWidth - notchWidth).coerceAtLeast(0))
        val top = ((parentHeight - notchHeight) * offsetY.coerceIn(0f, 1f)).roundToInt()
            .coerceIn(0, (parentHeight - notchHeight).coerceAtLeast(0))
        val params = notchContainer.layoutParams as FrameLayout.LayoutParams
        params.width = notchWidth
        params.height = notchHeight
        params.gravity = Gravity.TOP or Gravity.START
        params.leftMargin = left
        params.topMargin = top
        notchContainer.layoutParams = params
        notchView.clearColorFilter()
        notchView.setImageResource(drawable)
        notchView.setColorFilter(resolveColorFromVariant(colorVariant, "#11111A".toColorInt()), PorterDuff.Mode.SRC_IN)
        notchContainer.visibility = View.VISIBLE
    }

    private fun createLayoutParams(gestureEnabled: Boolean, height: Int): WindowManager.LayoutParams {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        // Original app toggles FLAG_NOT_TOUCHABLE based on gesture switch:
        // enabled -> 0x880328, disabled -> 0x880338.
        // Keep the original base flags, but explicitly add layout flags so the overlay
        // is allowed to draw in the real status-bar area on OEM ROMs.
        val baseFlags = if (gestureEnabled) 0x880328 else 0x880338
        val flags = baseFlags or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            height,
            overlayType,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }
    }
}
