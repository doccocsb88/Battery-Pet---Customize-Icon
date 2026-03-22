package dev.hai.emojibattery

import android.app.Application
import dev.hai.emojibattery.locale.AppLocalePreferences

class EmojiBatteryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLocalePreferences.applyAppLocalesAtStartup(this)
    }
}
