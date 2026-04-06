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
import co.q7labs.co.emoji.BuildConfig
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
    private var lastInterstitialShownAtMs = Long.MIN_VALUE

    fun initialize() {
        if (initialized) return
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
        adUnitId: String = BuildConfig.ADMOB_BANNER_AD_UNIT_ID,
        isPremium: Boolean = isPremiumUser(),
    ): AdView? {
        if (!shouldShowAds(isPremium)) return null
        if (adUnitId.isBlank()) return null
        initialize()
        return AdView(context).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }
    }

    fun preloadInterstitial(
        adUnitId: String = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID,
        isPremium: Boolean = isPremiumUser(),
    ) {
        if (!shouldShowAds(isPremium) || adUnitId.isBlank()) return
        initialize()
        synchronized(this) {
            if (interstitialAd != null || isInterstitialLoading) return
            isInterstitialLoading = true
        }
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
                    Log.w(TAG, "Interstitial preload failed: ${loadAdError.message}")
                }
            },
        )
    }

    fun showInterstitial(
        activity: Activity,
        adUnitId: String = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID,
        isPremium: Boolean = isPremiumUser(),
        onUnavailable: () -> Unit = {},
        onDismissed: () -> Unit = {},
    ) {
        if (!shouldShowAds(isPremium) || adUnitId.isBlank()) {
            onUnavailable()
            return
        }
        initialize()
        if (!canShowInterstitialNow()) {
            preloadInterstitial(adUnitId = adUnitId, isPremium = isPremium)
            onUnavailable()
            return
        }
        val readyAd = interstitialAd
        if (readyAd == null) {
            preloadInterstitial(adUnitId = adUnitId, isPremium = isPremium)
            onUnavailable()
            return
        }
        interstitialAd = null
        readyAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                lastInterstitialShownAtMs = SystemClock.elapsedRealtime()
                preloadInterstitial(adUnitId = adUnitId, isPremium = isPremium)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                Log.w(TAG, "Interstitial failed to show: ${adError.message}")
                preloadInterstitial(adUnitId = adUnitId, isPremium = isPremium)
                onUnavailable()
            }
        }
        readyAd.show(activity)
    }

    private fun canShowInterstitialNow(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val elapsed = now - lastInterstitialShownAtMs
        return elapsed >= BuildConfig.ADMOB_INTERSTITIAL_THROTTLE_MS
    }

    companion object {
        private const val TAG = "GoogleMobileAds"

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
    adUnitId: String = BuildConfig.ADMOB_BANNER_AD_UNIT_ID,
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
