package dev.hai.emojibattery.locale

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Mirrors the original app (emoji-battery-icon-customize 1.2.8) SharedPreferences:
 * [hungvv.C2536It0] — prefs name `language_setting`, keys `key_language` / `key_country`.
 */
object AppLocalePreferences {
    const val PREFS_NAME = "language_setting"
    const val KEY_LANGUAGE = "key_language"
    const val KEY_COUNTRY = "key_country"

    /** App-specific: user finished the language picker flow at least once. */
    private const val KEY_LANGUAGE_FLOW_COMPLETED = "emoji_battery_language_flow_done"

    fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPersistedLocale(context: Context): Locale? {
        val lang = prefs(context).getString(KEY_LANGUAGE, null)?.trim().orEmpty()
        if (lang.isEmpty()) return null
        val country = prefs(context).getString(KEY_COUNTRY, "").orEmpty()
        return Locale(lang, country)
    }

    fun getPersistedLocaleTag(context: Context): String? {
        val locale = getPersistedLocale(context) ?: return null
        return locale.toLanguageTag().replace('_', '-')
    }

    fun setPersistedLocale(context: Context, locale: Locale) {
        prefs(context).edit()
            .putString(KEY_LANGUAGE, locale.language)
            .putString(KEY_COUNTRY, locale.country.orEmpty())
            .apply()
    }

    fun isLanguageFlowCompleted(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LANGUAGE_FLOW_COMPLETED, false)

    fun setLanguageFlowCompleted(context: Context, completed: Boolean) {
        prefs(context).edit().putBoolean(KEY_LANGUAGE_FLOW_COMPLETED, completed).apply()
    }

    /**
     * Applies persisted locale to AppCompat / framework app locales (recreates activities when the
     * tag changes). Call with no saved locale to follow the system default.
     */
    fun applyAppCompatFromPersistedLocales(context: Context) {
        val tag = getPersistedLocaleTag(context)
        if (tag.isNullOrBlank()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }

    /**
     * Call from [android.app.Application] and [android.app.Activity] so locale is applied before UI.
     * When [AppLanguageConfig.isLanguagePickerFlowEnabled] is false, persists English, marks the
     * language step complete, and applies [AppLanguageConfig.fixedAppLocaleTag].
     */
    fun applyAppLocalesAtStartup(context: Context) {
        val appCtx = context.applicationContext
        if (!AppLanguageConfig.isLanguagePickerFlowEnabled) {
            setPersistedLocale(appCtx, Locale.ENGLISH)
            setLanguageFlowCompleted(appCtx, true)
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(AppLanguageConfig.fixedAppLocaleTag),
            )
            return
        }
        applyAppCompatFromPersistedLocales(appCtx)
    }
}
