package dev.hai.emojibattery.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dev.hai.emojibattery.app.EmojiBatteryApplication
import dev.hai.emojibattery.service.UserEntitlementManager

class GoogleMobileAdsService(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val entitlementManager = UserEntitlementManager(appContext)

    @Volatile
    private var initialized = false
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
        MobileAds.initialize(appContext) {
            Log.d(TAG, "Mobile Ads initialized")
        }
        initialized = true
        preloadInterstitial()
    }

    fun isPremiumUser(): Boolean = entitlementManager.readState().isPremium

    fun shouldShowAds(isPremium: Boolean = isPremiumUser()): Boolean = !isPremium

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
        const val ADMOB_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val ADMOB_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9552312736312538/1417273078"
        const val ADMOB_INTERSTITIAL_THROTTLE_MS = 45_000L

        fun from(context: Context): GoogleMobileAdsService {
            val app = context.applicationContext as EmojiBatteryApplication
            return app.googleMobileAdsService
        }
    }
}

@Composable
fun rememberGoogleMobileAdsService(): GoogleMobileAdsService {
    val context = LocalContext.current
    return remember(context) { GoogleMobileAdsService.from(context) }
}

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
