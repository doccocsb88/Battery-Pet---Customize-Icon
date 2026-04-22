package dev.hai.emojibattery.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.os.SystemClock
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import co.q7labs.co.emoji.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import dev.hai.emojibattery.app.EmojiBatteryApplication
import dev.hai.emojibattery.service.UserEntitlementManager
import kotlinx.coroutines.delay

class GoogleMobileAdsService(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val entitlementManager = UserEntitlementManager(appContext)

    @Volatile
    private var initialized = false

    // --- Interstitial ---
    @Volatile
    private var interstitialAd: InterstitialAd? = null
    @Volatile
    private var isInterstitialLoading = false
    @Volatile
    private var lastInterstitialShownAtMs = 0L

    fun initialize() {
        if (initialized) {
            Log.d(TAG, "initialize: already initialized")
            return
        }
        Log.d(TAG, "initialize: start")
        // Register test device IDs so AdMob serves test ads on dev/QA devices
        // (prevents ERROR_CODE_NO_FILL = 3 during local testing)
        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(DEBUG_TEST_DEVICE_IDS)
                    .build(),
            )
        }
        MobileAds.initialize(appContext) {
            Log.d(TAG, "Mobile Ads initialized")
        }
        initialized = true
        preloadInterstitial()
    }

    fun isPremiumUser(): Boolean = entitlementManager.readState().isPremium

    fun shouldShowAds(isPremium: Boolean = isPremiumUser()): Boolean = !isPremium

    // ── Banner ──────────────────────────────────────────────────────────────

    fun createBannerAdView(
        context: Context,
        adSize: AdSize = AdSize.BANNER,
        adUnitId: String = ADMOB_BANNER_AD_UNIT_ID,
        isPremium: Boolean = isPremiumUser(),
    ): AdView? {
        if (!shouldShowAds(isPremium)) {
            Log.d(TAG, "createBannerAdView: skip (premium user)")
            return null
        }
        if (adUnitId.isBlank()) {
            Log.w(TAG, "createBannerAdView: skip (empty adUnitId)")
            return null
        }
        initialize()
        Log.d(TAG, "createBannerAdView: loading banner ad")
        return AdView(context).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }
    }

    // ── Native ──────────────────────────────────────────────────────────────

    /**
     * Load a single native ad and deliver it via [onLoaded].
     * The caller owns the [NativeAd] lifecycle and must call [NativeAd.destroy] when done.
     */
    fun loadNativeAd(
        context: Context,
        adUnitId: String = ADMOB_NATIVE_AD_UNIT_ID,
        isPremium: Boolean = isPremiumUser(),
        onLoaded: (NativeAd) -> Unit,
        onFailed: () -> Unit = {},
    ) {
        if (!shouldShowAds(isPremium)) {
            Log.d(TAG, "loadNativeAd: skip (premium user)")
            onFailed()
            return
        }
        if (adUnitId.isBlank()) {
            Log.w(TAG, "loadNativeAd: skip (empty adUnitId)")
            onFailed()
            return
        }
        initialize()
        Log.d(TAG, "loadNativeAd: loading")
        AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                Log.d(TAG, "loadNativeAd: loaded")
                onLoaded(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "loadNativeAd: failed code=${error.code} msg=${error.message}")
                    onFailed()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build(),
            )
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    /**
     * Pre-loads [count] native ads concurrently.
     * Use this at the screen level to cache ads before the list renders.
     * Each loaded ad is delivered individually via [onEachLoaded].
     *
     * The caller owns every [NativeAd] and must destroy all of them when done
     * (e.g. in a DisposableEffect tied to the screen composable).
     */
    fun preloadNativeAds(
        context: Context,
        count: Int,
        adUnitId: String = ADMOB_NATIVE_AD_UNIT_ID,
        isPremium: Boolean = isPremiumUser(),
        onEachLoaded: (index: Int, ad: NativeAd) -> Unit,
    ) {
        if (!shouldShowAds(isPremium) || count <= 0) return
        repeat(count) { index ->
            loadNativeAd(
                context = context,
                adUnitId = adUnitId,
                isPremium = isPremium,
                onLoaded = { ad -> onEachLoaded(index, ad) },
                onFailed = {},
            )
        }
    }

    // ── Interstitial ────────────────────────────────────────────────────────

    fun preloadInterstitial(
        adUnitId: String = ADMOB_INTERSTITIAL_AD_UNIT_ID,
        isPremium: Boolean = isPremiumUser(),
    ) {
        if (!shouldShowAds(isPremium)) {
            Log.d(TAG, "preloadInterstitial: skip (premium user)")
            return
        }
        if (adUnitId.isBlank()) {
            Log.w(TAG, "preloadInterstitial: skip (empty adUnitId)")
            return
        }
        initialize()
        synchronized(this) {
            if (interstitialAd != null) {
                Log.d(TAG, "preloadInterstitial: skip (already has cached ad)")
                return
            }
            if (isInterstitialLoading) {
                Log.d(TAG, "preloadInterstitial: skip (already loading)")
                return
            }
            isInterstitialLoading = true
        }
        Log.d(TAG, "preloadInterstitial: start load")
        InterstitialAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loadedAd: InterstitialAd) {
                    interstitialAd = loadedAd
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial preloaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(
                        TAG,
                        "Interstitial preload failed: code=${loadAdError.code}, " +
                            "domain=${loadAdError.domain}, message=${loadAdError.message}",
                    )
                }
            },
        )
    }

    fun showInterstitial(
        activity: Activity,
        adUnitId: String = ADMOB_INTERSTITIAL_AD_UNIT_ID,
        isPremium: Boolean = isPremiumUser(),
        onUnavailable: () -> Unit = {},
        onDismissed: () -> Unit = {},
    ) {
        if (!shouldShowAds(isPremium)) {
            Log.d(TAG, "showInterstitial: unavailable (premium user)")
            onUnavailable()
            return
        }
        if (adUnitId.isBlank()) {
            Log.w(TAG, "showInterstitial: unavailable (empty adUnitId)")
            onUnavailable()
            return
        }
        initialize()
        val canShowNow = canShowInterstitialNow()
        Log.d(
            TAG,
            "showInterstitial: request, cached=${interstitialAd != null}, " +
                "loading=$isInterstitialLoading, canShowNow=$canShowNow",
        )
        if (!canShowNow) {
            val remainingMs = remainingThrottleMs()
            Log.d(TAG, "showInterstitial: throttled, remainingMs=$remainingMs")
            preloadInterstitial(adUnitId = adUnitId, isPremium = isPremium)
            onUnavailable()
            return
        }
        val readyAd = interstitialAd
        if (readyAd == null) {
            Log.d(TAG, "showInterstitial: unavailable (no cached ad yet)")
            preloadInterstitial(adUnitId = adUnitId, isPremium = isPremium)
            onUnavailable()
            return
        }
        interstitialAd = null
        Log.d(TAG, "showInterstitial: showing cached interstitial")
        readyAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                lastInterstitialShownAtMs = SystemClock.elapsedRealtime()
                Log.d(TAG, "showInterstitial: dismissed, scheduling next preload")
                preloadInterstitial(adUnitId = adUnitId, isPremium = isPremium)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                Log.w(
                    TAG,
                    "Interstitial failed to show: code=${adError.code}, " +
                        "domain=${adError.domain}, message=${adError.message}",
                )
                preloadInterstitial(adUnitId = adUnitId, isPremium = isPremium)
                onUnavailable()
            }
        }
        readyAd.setImmersiveMode(true)
        readyAd.show(activity)
    }

    private fun canShowInterstitialNow(): Boolean {
        if (lastInterstitialShownAtMs <= 0L) return true
        val now = SystemClock.elapsedRealtime()
        val elapsed = now - lastInterstitialShownAtMs
        return elapsed >= ADMOB_INTERSTITIAL_THROTTLE_MS
    }

    private fun remainingThrottleMs(): Long {
        if (lastInterstitialShownAtMs <= 0L) return 0L
        val now = SystemClock.elapsedRealtime()
        val elapsed = now - lastInterstitialShownAtMs
        return (ADMOB_INTERSTITIAL_THROTTLE_MS - elapsed).coerceAtLeast(0L)
    }

    companion object {
        private const val TAG = "GoogleMobileAds"
        const val ADMOB_APP_ID = "ca-app-pub-9552312736312538~2877897667"

        // Production ad unit IDs
        private const val ADMOB_BANNER_AD_UNIT_ID_PROD = "ca-app-pub-9552312736312538/2424553201"
        private const val ADMOB_NATIVE_AD_UNIT_ID_PROD = "ca-app-pub-9552312736312538/1690056037"
        private const val ADMOB_INTERSTITIAL_AD_UNIT_ID_PROD = "ca-app-pub-9552312736312538/1417273078"

        // Google's official test ad unit IDs – always fill in debug builds
        private const val ADMOB_BANNER_AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/6300978111"
        private const val ADMOB_NATIVE_AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/2247696110"
        private const val ADMOB_INTERSTITIAL_AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/1033173712"

        val ADMOB_BANNER_AD_UNIT_ID: String
            get() = if (BuildConfig.DEBUG) ADMOB_BANNER_AD_UNIT_ID_TEST else ADMOB_BANNER_AD_UNIT_ID_PROD
        val ADMOB_NATIVE_AD_UNIT_ID: String
            get() = if (BuildConfig.DEBUG) ADMOB_NATIVE_AD_UNIT_ID_TEST else ADMOB_NATIVE_AD_UNIT_ID_PROD
        val ADMOB_INTERSTITIAL_AD_UNIT_ID: String
            get() = if (BuildConfig.DEBUG) ADMOB_INTERSTITIAL_AD_UNIT_ID_TEST else ADMOB_INTERSTITIAL_AD_UNIT_ID_PROD

        const val ADMOB_INTERSTITIAL_THROTTLE_MS = 45_000L

        /**
         * Test device hardware IDs extracted from AdMob logcat hints.
         * Add your device's ID here to receive test ads in debug builds.
         * The ID is printed on first launch: "Use RequestConfiguration.Builder()
         * .setTestDeviceIds(Arrays.asList("<ID>")) to get test ads on this device."
         */
        val DEBUG_TEST_DEVICE_IDS: List<String> = listOf(
            "917FC216104222DE623057444EF1C8CC", // dev device (from logcat)
        )

        fun from(context: Context): GoogleMobileAdsService {
            val app = context.applicationContext as EmojiBatteryApplication
            return app.googleMobileAdsService
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composable helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun rememberGoogleMobileAdsService(): GoogleMobileAdsService {
    val context = LocalContext.current
    return remember(context) { GoogleMobileAdsService.from(context) }
}

// ── Banner ────────────────────────────────────────────────────────────────────

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    isPremium: Boolean? = null,
    adSize: AdSize = AdSize.BANNER,
    adUnitId: String = GoogleMobileAdsService.ADMOB_BANNER_AD_UNIT_ID,
    service: GoogleMobileAdsService = rememberGoogleMobileAdsService(),
) {
    val context = LocalContext.current
    val resolvedPremium = isPremium ?: service.isPremiumUser()
    val adView = remember(context, resolvedPremium, adSize, adUnitId) {
        service.createBannerAdView(
            context = context,
            adSize = adSize,
            adUnitId = adUnitId,
            isPremium = resolvedPremium,
        )
    } ?: return

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    AndroidView(
        modifier = modifier,
        factory = { adView },
    )
}

// ── Native ────────────────────────────────────────────────────────────────────

/**
 * Loads and displays a native ad using programmatic [NativeAdView].
 * The ad is destroyed automatically when the composable leaves composition.
 *
 * @param modifier     Modifier applied to the outer Compose container.
 * @param isPremium    Override premium check; null → auto-detect.
 * @param adUnitId     AdMob native ad unit ID.
 * @param service      [GoogleMobileAdsService] instance.
 */
@Composable
fun AdMobNativeAd(
    modifier: Modifier = Modifier,
    isPremium: Boolean? = null,
    adUnitId: String = GoogleMobileAdsService.ADMOB_NATIVE_AD_UNIT_ID,
    service: GoogleMobileAdsService = rememberGoogleMobileAdsService(),
) {
    val context = LocalContext.current
    val resolvedPremium = isPremium ?: service.isPremiumUser()
    val showAd = service.shouldShowAds(resolvedPremium)

    // Always call hooks unconditionally (Compose rules)
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(adUnitId, resolvedPremium, showAd) {
        if (!showAd) {
            nativeAd?.destroy()
            nativeAd = null
            return@LaunchedEffect
        }
        // Retry with exponential backoff on network/no-fill errors
        // Delays (ms): attempt 1 → wait 5s, attempt 2 → wait 15s, attempt 3 → wait 30s
        val retryDelaysMs = longArrayOf(5_000L, 15_000L, 30_000L)
        var attempt = 0
        var loaded = false
        while (!loaded && attempt <= retryDelaysMs.size) {
            if (attempt > 0) {
                val waitMs = retryDelaysMs[(attempt - 1).coerceAtMost(retryDelaysMs.lastIndex)]
                Log.d("GoogleMobileAds", "loadNativeAd: retry #$attempt in ${waitMs}ms")
                delay(waitMs)
            }
            service.loadNativeAd(
                context = context,
                adUnitId = adUnitId,
                isPremium = resolvedPremium,
                onLoaded = { ad ->
                    nativeAd = ad
                    loaded = true
                },
                onFailed = { /* loaded stays false → loop continues */ },
            )
            // AdMob callbacks are async — wait for them before checking loaded
            delay(12_000L)
            attempt++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    // Do not render anything for premium users or before the ad loads
    val ad = nativeAd ?: return
    if (!showAd) return

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Build NativeAdView entirely in code – no XML layout required
            val nativeAdView = NativeAdView(ctx)

            // Headline
            val headlineView = TextView(ctx).apply {
                id = android.view.View.generateViewId()
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#1A1A2E"))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            // Body
            val bodyView = TextView(ctx).apply {
                id = android.view.View.generateViewId()
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#555577"))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            // Advertiser label
            val advertiserView = TextView(ctx).apply {
                id = android.view.View.generateViewId()
                textSize = 10f
                setTextColor(android.graphics.Color.parseColor("#888899"))
            }

            // CTA button
            val ctaButton = android.widget.Button(ctx).apply {
                id = android.view.View.generateViewId()
                textSize = 12f
                setTextColor(android.graphics.Color.WHITE)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#6C63FF"))
                    cornerRadius = 40f
                }
                setPadding(32, 12, 32, 12)
            }

            // Ad label badge
            val adLabel = TextView(ctx).apply {
                text = "Ad"
                textSize = 9f
                setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#FF9800"))
                    cornerRadius = 8f
                }
                setPadding(12, 4, 12, 4)
            }

            // Container layout
            val outerContainer = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#F8F8FF"))
                    cornerRadius = 24f
                    setStroke(1, android.graphics.Color.parseColor("#E0DFFE"))
                }
                setPadding(32, 24, 32, 24)
            }

            val headerRow = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            headerRow.addView(
                headlineView,
                android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
            )
            headerRow.addView(adLabel)

            outerContainer.addView(headerRow)
            outerContainer.addView(
                bodyView,
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = 8 },
            )
            outerContainer.addView(
                advertiserView,
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = 4 },
            )
            outerContainer.addView(
                ctaButton,
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = 16 },
            )

            nativeAdView.addView(outerContainer)

            // Wire up
            nativeAdView.headlineView = headlineView
            nativeAdView.bodyView = bodyView
            nativeAdView.advertiserView = advertiserView
            nativeAdView.callToActionView = ctaButton

            headlineView.text = ad.headline
            bodyView.text = ad.body ?: ""
            advertiserView.text = ad.advertiser ?: ""
            ctaButton.text = ad.callToAction ?: "Learn More"

// ... (keep the existing AdMobNativeAd)
            nativeAdView.setNativeAd(ad)
            nativeAdView
        },
        update = { nativeAdView ->
            // Nothing to update; ad is loaded once per composition
        },
    )
}

/**
 * Renders an already-loaded [NativeAd] without managing its lifecycle.
 * Use this in conjunction with [GoogleMobileAdsService.preloadNativeAds]
 * to prevent flickering/reloading when scrolling lists.
 */
@Composable
fun AdMobNativeAdView(
    ad: NativeAd,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Build NativeAdView entirely in code – no XML layout required
            val nativeAdView = NativeAdView(ctx)

            // Headline
            val headlineView = TextView(ctx).apply {
                id = android.view.View.generateViewId()
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#1A1A2E"))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            // Body
            val bodyView = TextView(ctx).apply {
                id = android.view.View.generateViewId()
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#555577"))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            // Advertiser label
            val advertiserView = TextView(ctx).apply {
                id = android.view.View.generateViewId()
                textSize = 10f
                setTextColor(android.graphics.Color.parseColor("#888899"))
            }

            // CTA button
            val ctaButton = android.widget.Button(ctx).apply {
                id = android.view.View.generateViewId()
                textSize = 12f
                setTextColor(android.graphics.Color.WHITE)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#6C63FF"))
                    cornerRadius = 40f
                }
                setPadding(32, 12, 32, 12)
            }

            // Ad label badge
            val adLabel = TextView(ctx).apply {
                text = "Ad"
                textSize = 9f
                setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#FF9800"))
                    cornerRadius = 8f
                }
                setPadding(12, 4, 12, 4)
            }

            // Container layout
            val outerContainer = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#F8F8FF"))
                    cornerRadius = 24f
                    setStroke(1, android.graphics.Color.parseColor("#E0DFFE"))
                }
                setPadding(32, 24, 32, 24)
            }

            val headerRow = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            headerRow.addView(
                headlineView,
                android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
            )
            headerRow.addView(adLabel)

            outerContainer.addView(headerRow)
            outerContainer.addView(
                bodyView,
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = 8 },
            )
            outerContainer.addView(
                advertiserView,
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = 4 },
            )
            outerContainer.addView(
                ctaButton,
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = 16 },
            )

            nativeAdView.addView(outerContainer)

            // Wire up
            nativeAdView.headlineView = headlineView
            nativeAdView.bodyView = bodyView
            nativeAdView.advertiserView = advertiserView
            nativeAdView.callToActionView = ctaButton

            headlineView.text = ad.headline
            bodyView.text = ad.body ?: ""
            advertiserView.text = ad.advertiser ?: ""
            ctaButton.text = ad.callToAction ?: "Learn More"

            nativeAdView.setNativeAd(ad)
            nativeAdView
        },
        update = { nativeAdView ->
            // Update fields in case 'ad' instance changes
            (nativeAdView.headlineView as? TextView)?.text = ad.headline
            (nativeAdView.bodyView as? TextView)?.text = ad.body ?: ""
            (nativeAdView.advertiserView as? TextView)?.text = ad.advertiser ?: ""
            (nativeAdView.callToActionView as? android.widget.Button)?.text = ad.callToAction ?: "Learn More"
            nativeAdView.setNativeAd(ad)
        },
    )
}
