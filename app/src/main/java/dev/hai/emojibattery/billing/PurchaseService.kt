package dev.hai.emojibattery.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface PurchaseService {
    val uiState: StateFlow<BillingUiState>
    val weeklyProductId: String
    val lifetimeProductId: String

    fun start(context: Context)
    fun purchase(activity: Activity, productId: String)
    fun restorePurchases()
    fun openManageSubscriptions(context: Context)
    fun hasPremiumAccess(ownedProductIds: Set<String>): Boolean
}
