package dev.hai.emojibattery.app

import android.app.Application
import dev.hai.emojibattery.ads.GoogleMobileAdsService
import dev.hai.emojibattery.locale.AppLocalePreferences

class EmojiBatteryApplication : Application() {
    lateinit var googleMobileAdsService: GoogleMobileAdsService
        private set

    override fun onCreate() {
        super.onCreate()
        AppLocalePreferences.applyAppLocalesAtStartup(this)
        googleMobileAdsService = GoogleMobileAdsService(this)
        googleMobileAdsService.initialize()
    }
}
