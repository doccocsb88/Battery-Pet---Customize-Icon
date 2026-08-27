package dev.hai.emojibattery.service

import android.content.Context
import dev.hai.emojibattery.model.LimitedFeature
import dev.hai.emojibattery.model.UserEntitlementState

class UserEntitlementManager(context: Context) {
    private val appContext = context.applicationContext

    fun readState(): UserEntitlementState {
        val prefs = prefs()
        val usageCounts = LimitedFeature.entries.associateWith { feature ->
            prefs.getInt(KEY_USAGE_PREFIX + feature.storageKey, 0).coerceAtLeast(0)
        }
        return UserEntitlementState(
            isPremium = prefs.getBoolean(KEY_IS_PREMIUM, false),
            usageCounts = usageCounts,
        )
    }

    fun syncPremium(isPremium: Boolean): UserEntitlementState {
        val updated = readState().copy(isPremium = isPremium)
        persist(updated)
        return updated
    }

    fun canApplySticker(state: UserEntitlementState): Boolean = state.canUse(LimitedFeature.ApplySticker)

    fun canApplyBattery(state: UserEntitlementState): Boolean = state.canUse(LimitedFeature.ApplyBattery)

    fun recordApplySticker(state: UserEntitlementState): UserEntitlementState = consume(state, LimitedFeature.ApplySticker)

    fun recordApplyBattery(state: UserEntitlementState): UserEntitlementState = consume(state, LimitedFeature.ApplyBattery)

    private fun consume(state: UserEntitlementState, feature: LimitedFeature): UserEntitlementState {
        if (state.isPremium) return state
        val currentCount = state.usageCount(feature)
        if (currentCount >= feature.freeLimit) return state
        val updated = state.copy(
            usageCounts = state.usageCounts + (feature to currentCount + 1),
        )
        persist(updated)
        return updated
    }

    private fun persist(state: UserEntitlementState) {
        val editor = prefs().edit()
            .putBoolean(KEY_IS_PREMIUM, state.isPremium)
        LimitedFeature.entries.forEach { feature ->
            editor.putInt(KEY_USAGE_PREFIX + feature.storageKey, state.usageCount(feature).coerceAtLeast(0))
        }
        editor.apply()
    }

    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private companion object {
        private const val PREFS = "user_entitlement"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_USAGE_PREFIX = "usage_"
    }
}
