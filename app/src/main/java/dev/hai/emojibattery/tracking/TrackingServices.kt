package dev.hai.emojibattery.tracking

import android.content.Context
import android.os.Bundle
import android.util.Log
import co.q7labs.co.emoji.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics

object TrackingServices {
    private const val TAG = "TrackingServices"

    private const val EVENT_PAYWALL_IMPRESSION = "paywall_impression"
    private const val EVENT_PAYWALL_ITEM_SELECTED = "paywall_item_selected"
    private const val EVENT_PAYWALL_PURCHASE_STARTED = "paywall_purchase_started"
    private const val EVENT_PAYWALL_PURCHASE_SUCCESS = "paywall_purchase_success"
    private const val EVENT_PAYWALL_PURCHASE_ERROR = "paywall_purchase_error"
    private const val EVENT_PAYWALL_EXIT = "paywall_exit"

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
                putString("paywall_id", paywallId?.trim().orEmpty().ifBlank { null })
                putString("feature_key", featureKey?.trim().orEmpty().ifBlank { null })
                putString("launch_mode", launchMode?.trim().orEmpty().ifBlank { null })
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
            params = Bundle().apply {
                putString("product_id", productId.trim())
                putString("plan_type", planType?.trim().orEmpty().ifBlank { null })
                putInt("has_offer_token", if (hasOfferToken) 1 else 0)
            },
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
            params = Bundle().apply {
                putString("product_id", productId.trim())
                putString("plan_type", planType?.trim().orEmpty().ifBlank { null })
                putInt("has_offer_token", if (hasOfferToken) 1 else 0)
            },
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
            params = Bundle().apply {
                putString("product_id", productId.trim())
                putString("plan_type", planType?.trim().orEmpty().ifBlank { null })
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
            params = Bundle().apply {
                putString("product_id", productId?.trim().orEmpty().ifBlank { null })
                putString("message", message?.let { truncate(it.trim(), 100) }?.ifBlank { null })
            },
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
                putString("reason", reason.trim())
                if (dwellMs != null && dwellMs >= 0) putLong("dwell_ms", dwellMs)
                putString("product_id", productId?.trim().orEmpty().ifBlank { null })
            },
        )
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
