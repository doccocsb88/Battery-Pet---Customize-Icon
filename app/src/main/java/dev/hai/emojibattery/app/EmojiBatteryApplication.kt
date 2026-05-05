package dev.hai.emojibattery.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
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
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            },
        )
        FirebaseAnalytics.getInstance(this)
        googleMobileAdsService = GoogleMobileAdsService(this)
        googleMobileAdsService.initialize()
    }
}
