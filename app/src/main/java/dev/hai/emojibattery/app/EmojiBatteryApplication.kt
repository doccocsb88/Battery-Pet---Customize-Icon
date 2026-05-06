package dev.hai.emojibattery.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import co.q7labs.co.emoji.BuildConfig
import dev.hai.emojibattery.ads.GoogleMobileAdsService
import dev.hai.emojibattery.locale.AppLocalePreferences

class EmojiBatteryApplication : Application() {
    lateinit var googleMobileAdsService: GoogleMobileAdsService
        private set

    override fun onCreate() {
        super.onCreate()
        AppLocalePreferences.applyAppLocalesAtStartup(this)
        // Ensure Firebase is initialized early for Analytics + Storage.
        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            createAppCheckProviderFactory(),
        )
        FirebaseAnalytics.getInstance(this)
        googleMobileAdsService = GoogleMobileAdsService(this)
    }

    private fun createAppCheckProviderFactory(): AppCheckProviderFactory {
        if (!BuildConfig.DEBUG) {
            return PlayIntegrityAppCheckProviderFactory.getInstance()
        }

        val debugFactory = runCatching {
            val clazz = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
            clazz.getMethod("getInstance").invoke(null) as AppCheckProviderFactory
        }.getOrNull()

        return debugFactory ?: PlayIntegrityAppCheckProviderFactory.getInstance()
    }
}
