package dev.hai.emojibattery.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BillingPlan(
    val productId: String,
    val title: String,
    val description: String,
    val displayPrice: String,
    val billingLabel: String,
    val productType: String,
    val offerToken: String? = null,
)

data class BillingUiState(
    val connected: Boolean = false,
    val loading: Boolean = true,
    val weeklyPlan: BillingPlan? = null,
    val lifetimePlan: BillingPlan? = null,
    val ownedProductIds: Set<String> = emptySet(),
    val purchaseInFlight: Boolean = false,
    val errorMessage: String? = null,
)

class GooglePlayPurchaseService private constructor() : PurchaseService, PurchasesUpdatedListener {
    override val weeklyProductId: String = "$APP_ID.weekly"
    override val lifetimeProductId: String = "$APP_ID.lifetime"

    private var billingClient: BillingClient? = null
    private val productCache = mutableMapOf<String, ProductDetails>()
    private val ownedProducts = mutableSetOf<String>()

    private val _uiState = MutableStateFlow(BillingUiState())
    override val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    override fun start(context: Context) {
        if (billingClient?.isReady == true) return
        val client = BillingClient.newBuilder(context.applicationContext)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
            )
            .setListener(this)
            .build()
        billingClient = client
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _uiState.value = _uiState.value.copy(connected = true, loading = true, errorMessage = null)
                    queryCatalog()
                    refreshPurchases()
                } else {
                    _uiState.value = _uiState.value.copy(
                        connected = false,
                        loading = false,
                        errorMessage = result.debugMessage.ifBlank { "Billing setup failed." },
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _uiState.value = _uiState.value.copy(connected = false)
            }
        })
    }

    override fun purchase(activity: Activity, productId: String) {
        val details = productCache[productId]
        val client = billingClient
        if (details == null || client?.isReady != true) {
            _uiState.value = _uiState.value.copy(errorMessage = "Billing is not ready yet.", purchaseInFlight = false)
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply {
                if (productId == weeklyProductId) {
                    val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    if (offerToken != null) setOfferToken(offerToken)
                }
            }
            .build()
        _uiState.value = _uiState.value.copy(purchaseInFlight = true, errorMessage = null)
        client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build(),
        )
    }

    override fun restorePurchases() {
        refreshPurchases()
    }

    override fun openManageSubscriptions(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/account/subscriptions?packageName=$APP_ID&sku=$weeklyProductId"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun hasPremiumAccess(ownedProductIds: Set<String>): Boolean {
        return ownedProductIds.any { it == weeklyProductId || it == lifetimeProductId }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach(::acknowledgeIfNeeded)
            refreshPurchases()
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            _uiState.value = _uiState.value.copy(
                purchaseInFlight = false,
                errorMessage = result.debugMessage.ifBlank { "Purchase failed." },
            )
        } else {
            _uiState.value = _uiState.value.copy(purchaseInFlight = false)
        }
    }

    private fun queryCatalog() {
        queryProductDetails(listOf(weeklyProductId), BillingClient.ProductType.SUBS) { details ->
            val plan = details.firstOrNull()?.let(::weeklyPlanFrom)
            details.forEach { productCache[it.productId] = it }
            _uiState.value = _uiState.value.copy(weeklyPlan = plan, loading = false)
        }
        queryProductDetails(listOf(lifetimeProductId), BillingClient.ProductType.INAPP) { details ->
            val plan = details.firstOrNull()?.let(::lifetimePlanFrom)
            details.forEach { productCache[it.productId] = it }
            _uiState.value = _uiState.value.copy(lifetimePlan = plan, loading = false)
        }
    }

    private fun queryProductDetails(
        productIds: List<String>,
        productType: String,
        onLoaded: (List<ProductDetails>) -> Unit,
    ) {
        val client = billingClient ?: return
        val products = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        }
        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(products).build(),
        ) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                onLoaded(details)
            } else {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = result.debugMessage.ifBlank { "Could not load billing products." },
                )
            }
        }
    }

    private fun refreshPurchases() {
        val client = billingClient ?: return
        ownedProducts.clear()
        var pendingCallbacks = 2
        val listener = PurchasesResponseListener { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    acknowledgeIfNeeded(purchase)
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        ownedProducts += purchase.products
                    }
                }
            }
            pendingCallbacks -= 1
            if (pendingCallbacks == 0) {
                _uiState.value = _uiState.value.copy(
                    ownedProductIds = ownedProducts.toSet(),
                    purchaseInFlight = false,
                    loading = false,
                )
            }
        }
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
            listener,
        )
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
            listener,
        )
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        val client = billingClient ?: return
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED || purchase.isAcknowledged) return
        client.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(),
        ) { }
    }

    private fun weeklyPlanFrom(details: ProductDetails): BillingPlan {
        val offer = details.subscriptionOfferDetails?.firstOrNull()
        val phase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
        return BillingPlan(
            productId = details.productId,
            title = details.name.ifBlank { "Weekly Premium" },
            description = details.description.ifBlank { "Unlock all premium content with weekly billing." },
            displayPrice = phase?.formattedPrice ?: "Unavailable",
            billingLabel = "Weekly subscription, auto-renews until canceled",
            productType = BillingClient.ProductType.SUBS,
            offerToken = offer?.offerToken,
        )
    }

    private fun lifetimePlanFrom(details: ProductDetails): BillingPlan {
        val oneTime = details.oneTimePurchaseOfferDetails
        return BillingPlan(
            productId = details.productId,
            title = details.name.ifBlank { "Lifetime Premium" },
            description = details.description.ifBlank { "One-time purchase for lifetime premium access." },
            displayPrice = oneTime?.formattedPrice ?: "Unavailable",
            billingLabel = "One-time purchase, no recurring charge",
            productType = BillingClient.ProductType.INAPP,
        )
    }

    companion object {
        const val APP_ID = "co.q7labs.co.emoji"
        val shared: GooglePlayPurchaseService = GooglePlayPurchaseService()
    }
}
