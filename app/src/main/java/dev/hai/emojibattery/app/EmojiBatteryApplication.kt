package dev.hai.emojibattery.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
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
        FirebaseAnalytics.getInstance(this)
        googleMobileAdsService = GoogleMobileAdsService(this)
        googleMobileAdsService.initialize()
    }
}
