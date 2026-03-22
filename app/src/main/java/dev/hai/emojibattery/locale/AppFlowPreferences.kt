package dev.hai.emojibattery.locale

import android.content.Context

/** Persists first-run flow so activity recreation (locale change) does not reset navigation. */
object AppFlowPreferences {
    private const val PREFS_NAME = "emoji_battery_flow"
    private const val KEY_SPLASH_DONE = "splash_done"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isSplashDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SPLASH_DONE, false)

    fun setSplashDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_SPLASH_DONE, true).apply()
    }

    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }
}
