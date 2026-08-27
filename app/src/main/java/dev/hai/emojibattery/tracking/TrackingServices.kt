package dev.hai.emojibattery.tracking

import android.content.Context
import android.os.Bundle
import android.util.Log
import co.q7labs.co.emoji.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Firebase Analytics facade for the user-behavior funnel.
 *
 * Keep events sparse and named for analysis, not click-spam.
 * Funnel: splash → language → onboarding → home → feature → apply → paywall → purchase.
 */
object TrackingServices {
    private const val TAG = "TrackingServices"
    private const val USER_PROPERTY_MAX_LEN = 36

    private const val EVENT_SPLASH_COMPLETE = "splash_complete"
    private const val EVENT_LANGUAGE_SELECTED = "language_selected"
    private const val EVENT_ONBOARDING_START = "onboarding_start"
    private const val EVENT_ONBOARDING_STEP = "onboarding_step"
    private const val EVENT_ONBOARDING_COMPLETE = "onboarding_complete"
    private const val EVENT_ONBOARDING_SKIP = "onboarding_skip"
    private const val EVENT_TUTORIAL_START = "tutorial_start"
    private const val EVENT_TUTORIAL_COMPLETE = "tutorial_complete"
    private const val EVENT_HOME_REACHED = "home_reached"
    private const val EVENT_TAB_SELECT = "tab_select"
    private const val EVENT_FEATURE_OPEN = "feature_open"
    private const val EVENT_CONTENT_SELECT = "content_select"
    private const val EVENT_PERMISSION_PROMPT = "permission_prompt"
    private const val EVENT_PERMISSION_RESULT = "permission_result"
    private const val EVENT_APPLY_ATTEMPT = "apply_attempt"
    private const val EVENT_APPLY_SUCCESS = "apply_success"
    private const val EVENT_APPLY_FAIL = "apply_fail"
    private const val EVENT_AD_INTERSTITIAL = "ad_interstitial"
    private const val EVENT_PAYWALL_IMPRESSION = "paywall_impression"
    private const val EVENT_PAYWALL_ITEM_SELECTED = "paywall_item_selected"
    private const val EVENT_PAYWALL_PURCHASE_STARTED = "paywall_purchase_started"
    private const val EVENT_PAYWALL_PURCHASE_SUCCESS = "paywall_purchase_success"
    private const val EVENT_PAYWALL_PURCHASE_ERROR = "paywall_purchase_error"
    private const val EVENT_PAYWALL_EXIT = "paywall_exit"

    private const val PREFS_NAME = "emoji_battery_tracking"
    private const val KEY_HOME_REACHED = "home_reached_logged"
    private const val KEY_ONBOARDING_START = "onboarding_start_logged"

    private var lastScreenName: String? = null

    fun trackScreenView(context: Context, route: String?) {
        val screenName = screenNameFromRoute(route)
        if (screenName == lastScreenName) return
        val previousScreen = lastScreenName
        lastScreenName = screenName
        logEvent(
            context = context,
            name = FirebaseAnalytics.Event.SCREEN_VIEW,
            params = bundleOf(
                FirebaseAnalytics.Param.SCREEN_NAME to screenName,
                FirebaseAnalytics.Param.SCREEN_CLASS to screenName,
                "previous_screen" to previousScreen,
            ),
        )
        if (screenName == "home") {
            trackHomeReachedOnce(context)
        }
    }

    fun trackSplashComplete(context: Context, nextRoute: String) {
        logEvent(
            context = context,
            name = EVENT_SPLASH_COMPLETE,
            params = bundleOf("next_route" to screenNameFromRoute(nextRoute)),
        )
    }

    fun trackLanguageSelected(context: Context, localeTag: String, localeChanged: Boolean) {
        logEvent(
            context = context,
            name = EVENT_LANGUAGE_SELECTED,
            params = bundleOf(
                "locale_tag" to localeTag,
                "locale_changed" to if (localeChanged) 1 else 0,
            ),
        )
    }

    fun trackOnboardingStart(context: Context) {
        if (!markOnce(context, KEY_ONBOARDING_START)) return
        logEvent(context = context, name = EVENT_ONBOARDING_START, params = Bundle())
    }

    fun trackOnboardingStep(context: Context, pageIndex: Int, pageCount: Int, action: String) {
        logEvent(
            context = context,
            name = EVENT_ONBOARDING_STEP,
            params = bundleOf(
                "page_index" to pageIndex,
                "page_count" to pageCount,
                "action" to action,
            ),
        )
    }

    fun trackOnboardingComplete(context: Context, pageCount: Int) {
        logEvent(
            context = context,
            name = EVENT_ONBOARDING_COMPLETE,
            params = bundleOf("page_count" to pageCount),
        )
    }

    fun trackOnboardingSkip(context: Context, pageIndex: Int) {
        logEvent(
            context = context,
            name = EVENT_ONBOARDING_SKIP,
            params = bundleOf("page_index" to pageIndex),
        )
    }

    fun trackTutorialStart(context: Context) {
        logEvent(context = context, name = EVENT_TUTORIAL_START, params = Bundle())
    }

    fun trackTutorialComplete(context: Context, skipped: Boolean) {
        logEvent(
            context = context,
            name = EVENT_TUTORIAL_COMPLETE,
            params = bundleOf("skipped" to if (skipped) 1 else 0),
        )
    }

    fun trackTabSelect(context: Context, tabName: String, fromTab: String?) {
        logEvent(
            context = context,
            name = EVENT_TAB_SELECT,
            params = bundleOf(
                "tab_name" to tabName,
                "from_tab" to fromTab,
            ),
        )
    }

    fun trackFeatureOpen(context: Context, featureKey: String, source: String?) {
        logEvent(
            context = context,
            name = EVENT_FEATURE_OPEN,
            params = bundleOf(
                "feature_key" to featureKey,
                "source" to source,
            ),
        )
    }

    fun trackContentSelect(
        context: Context,
        contentType: String,
        contentId: String?,
        categoryId: String? = null,
        locked: Boolean = false,
    ) {
        logEvent(
            context = context,
            name = EVENT_CONTENT_SELECT,
            params = bundleOf(
                "content_type" to contentType,
                "content_id" to contentId,
                "category_id" to categoryId,
                "locked" to if (locked) 1 else 0,
            ),
        )
    }

    fun trackPermissionPrompt(context: Context, permissionType: String, fromScreen: String?) {
        logEvent(
            context = context,
            name = EVENT_PERMISSION_PROMPT,
            params = bundleOf(
                "permission_type" to permissionType,
                "from_screen" to fromScreen,
            ),
        )
    }

    fun trackPermissionResult(context: Context, permissionType: String, granted: Boolean) {
        logEvent(
            context = context,
            name = EVENT_PERMISSION_RESULT,
            params = bundleOf(
                "permission_type" to permissionType,
                "granted" to if (granted) 1 else 0,
            ),
        )
        if (permissionType == "accessibility") {
            setUserProperty(context, "has_accessibility", if (granted) "true" else "false")
        }
    }

    fun trackApplyAttempt(context: Context, contentType: String, contentId: String? = null) {
        logEvent(
            context = context,
            name = EVENT_APPLY_ATTEMPT,
            params = bundleOf(
                "content_type" to contentType,
                "content_id" to contentId,
            ),
        )
    }

    fun trackApplySuccess(context: Context, contentType: String, contentId: String? = null) {
        logEvent(
            context = context,
            name = EVENT_APPLY_SUCCESS,
            params = bundleOf(
                "content_type" to contentType,
                "content_id" to contentId,
            ),
        )
    }

    fun trackApplyFail(
        context: Context,
        contentType: String,
        reason: String,
        contentId: String? = null,
    ) {
        logEvent(
            context = context,
            name = EVENT_APPLY_FAIL,
            params = bundleOf(
                "content_type" to contentType,
                "reason" to reason,
                "content_id" to contentId,
            ),
        )
    }

    fun trackAdInterstitial(context: Context, result: String, reason: String?, placement: String?) {
        logEvent(
            context = context,
            name = EVENT_AD_INTERSTITIAL,
            params = bundleOf(
                "result" to result,
                "reason" to reason,
                "placement" to placement,
            ),
        )
    }

    fun trackPaywallImpression(
        context: Context,
        paywallId: String?,
        featureKey: String?,
        launchMode: String?,
        hasWeekly: Boolean,
        hasMonthly: Boolean,
        hasLifetime: Boolean,
    ) {
        logEvent(
            context = context,
            name = EVENT_PAYWALL_IMPRESSION,
            params = Bundle().apply {
                putParam("paywall_id", paywallId)
                putParam("feature_key", featureKey)
                putParam("launch_mode", launchMode)
                putInt("has_weekly", if (hasWeekly) 1 else 0)
                putInt("has_monthly", if (hasMonthly) 1 else 0)
                putInt("has_lifetime", if (hasLifetime) 1 else 0)
            },
        )
    }

    fun trackPaywallItemSelected(
        context: Context,
        productId: String,
        planType: String?,
        hasOfferToken: Boolean,
    ) {
        logEvent(
            context = context,
            name = EVENT_PAYWALL_ITEM_SELECTED,
            params = bundleOf(
                "product_id" to productId,
                "plan_type" to planType,
                "has_offer_token" to if (hasOfferToken) 1 else 0,
            ),
        )
    }

    fun trackPaywallPurchaseStarted(
        context: Context,
        productId: String,
        planType: String?,
        hasOfferToken: Boolean,
    ) {
        logEvent(
            context = context,
            name = EVENT_PAYWALL_PURCHASE_STARTED,
            params = bundleOf(
                "product_id" to productId,
                "plan_type" to planType,
                "has_offer_token" to if (hasOfferToken) 1 else 0,
            ),
        )
    }

    fun trackPaywallPurchaseSuccess(
        context: Context,
        productId: String,
        planType: String?,
    ) {
        logEvent(
            context = context,
            name = EVENT_PAYWALL_PURCHASE_SUCCESS,
            params = bundleOf(
                "product_id" to productId,
                "plan_type" to planType,
            ),
        )
        setUserProperty(context, "is_premium", "true")
    }

    fun trackBillingPurchaseRevenue(
        context: Context,
        productId: String,
        planType: String?,
        valueMicros: Long?,
        currencyCode: String?,
    ) {
        logEvent(
            context = context,
            name = FirebaseAnalytics.Event.PURCHASE,
            params = Bundle().apply {
                putParam(FirebaseAnalytics.Param.ITEM_ID, productId)
                putParam("product_id", productId)
                putParam("plan_type", planType)
                if (valueMicros != null && valueMicros > 0 && !currencyCode.isNullOrBlank()) {
                    putDouble(FirebaseAnalytics.Param.VALUE, valueMicros / 1_000_000.0)
                    putParam(FirebaseAnalytics.Param.CURRENCY, currencyCode)
                }
            },
        )
    }

    fun trackPaywallPurchaseError(
        context: Context,
        productId: String?,
        message: String?,
    ) {
        logEvent(
            context = context,
            name = EVENT_PAYWALL_PURCHASE_ERROR,
            params = bundleOf(
                "product_id" to productId,
                "message" to message?.let { truncate(it, 100) },
            ),
        )
    }

    fun trackPaywallExit(
        context: Context,
        reason: String,
        dwellMs: Long?,
        productId: String?,
    ) {
        logEvent(
            context = context,
            name = EVENT_PAYWALL_EXIT,
            params = Bundle().apply {
                putParam("reason", reason)
                if (dwellMs != null && dwellMs >= 0) putLong("dwell_ms", dwellMs)
                putParam("product_id", productId)
            },
        )
    }

    fun syncUserProperties(
        context: Context,
        isPremium: Boolean,
        hasAccessibility: Boolean,
        onboardingCompleted: Boolean,
    ) {
        setUserProperty(context, "is_premium", if (isPremium) "true" else "false")
        setUserProperty(context, "has_accessibility", if (hasAccessibility) "true" else "false")
        setUserProperty(context, "onboarded", if (onboardingCompleted) "true" else "false")
    }

    fun screenNameFromRoute(route: String?): String {
        val raw = route?.substringBefore("?")?.trim().orEmpty()
        if (raw.isBlank()) return "unknown"
        return when {
            raw == "splash" -> "splash"
            raw == "language" -> "language"
            raw == "onboarding" -> "onboarding"
            raw == "tutorial" -> "tutorial"
            raw == "home" -> "home"
            raw == "customize" -> "customize"
            raw == "wallpaper" -> "wallpaper"
            raw.startsWith("wallpaper/category/") -> "wallpaper_category"
            raw.startsWith("wallpaper/preview/") -> "wallpaper_preview"
            raw == "gesture" -> "gesture"
            raw == "achievement" -> "achievement"
            raw == "statusbar_custom" -> "statusbar_custom"
            raw == "notch" -> "notch"
            raw == "animation" -> "animation"
            raw.startsWith("animation_list/") -> "animation_list"
            raw == "statusbar_battery_list" -> "statusbar_battery_list"
            raw == "statusbar_emoji_list" -> "statusbar_emoji_list"
            raw == "background_template_list" -> "background_template_list"
            raw == "legacy_battery" -> "legacy_battery"
            raw == "search" -> "search"
            raw == "settings" -> "settings"
            raw == "feedback" -> "feedback"
            raw == "paywall" -> "paywall"
            raw.startsWith("legal/") -> "legal"
            raw == "real_time" -> "real_time"
            raw == "battery_troll" -> "battery_troll"
            raw == "emoji_sticker" -> "emoji_sticker"
            raw == "theme_list" -> "theme_list"
            raw.startsWith("theme_detail/") -> "theme_detail"
            raw.startsWith("feature/") -> "feature_detail"
            else -> raw.substringBefore("/").ifBlank { "unknown" }
        }
    }

    private fun trackHomeReachedOnce(context: Context) {
        if (!markOnce(context, KEY_HOME_REACHED)) return
        logEvent(context = context, name = EVENT_HOME_REACHED, params = Bundle())
    }

    private fun markOnce(context: Context, key: String): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(key, false)) return false
        prefs.edit().putBoolean(key, true).apply()
        return true
    }

    private fun setUserProperty(context: Context, name: String, value: String?) {
        if (value == null) return
        FirebaseAnalytics.getInstance(context.applicationContext)
            .setUserProperty(name, truncate(value, USER_PROPERTY_MAX_LEN))
    }

    private fun bundleOf(vararg pairs: Pair<String, Any?>): Bundle = Bundle().apply {
        pairs.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is String -> putParam(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Boolean -> putInt(key, if (value) 1 else 0)
                else -> putParam(key, value.toString())
            }
        }
    }

    private fun Bundle.putParam(key: String, value: String?) {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isNotEmpty()) putString(key, trimmed)
    }

    private fun logEvent(context: Context, name: String, params: Bundle) {
        FirebaseAnalytics.getInstance(context.applicationContext).logEvent(name, params)
        if (BuildConfig.DEBUG) {
            val pairs = params.keySet()
                .sorted()
                .joinToString(", ") { key ->
                    val value = runCatching { params.get(key) }.getOrNull()
                    "$key=$value"
                }
            Log.d(TAG, "logEvent($name) {$pairs}")
        }
    }

    private fun truncate(value: String, maxLen: Int): String =
        if (value.length <= maxLen) value else value.take(maxLen)
}
