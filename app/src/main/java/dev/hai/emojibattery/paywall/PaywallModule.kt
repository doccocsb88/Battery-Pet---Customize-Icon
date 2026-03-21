package dev.hai.emojibattery.paywall

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.billing.BillingPlan
import dev.hai.emojibattery.billing.BillingUiState
import dev.hai.emojibattery.billing.PurchaseService
import dev.hai.emojibattery.model.PaywallState

private val PaywallBackground = Color(0xFFFFF6F4)
private val PaywallTextMuted = Color(0xFF5C4B51)
private val PaywallAccent = Color(0xFFD47DFE)
private val PaywallBody = Color(0xFF303030)
private val PaywallFooter = Color(0xFF979797)
private val PopularBadge = Color(0xFF7915F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    paywall: PaywallState?,
    billingState: BillingUiState,
    purchaseService: PurchaseService,
    onClose: () -> Unit,
    onRestore: () -> Unit,
    onManageSubscriptions: () -> Unit,
    onOpenPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onPurchase: (productId: String, subscriptionOfferToken: String?) -> Unit,
) {
    val monthly = billingState.monthlyPlan
    val weekly = billingState.weeklyPlan
    val lifetime = billingState.lifetimePlan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaywallBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_header_iap_full),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentScale = ContentScale.Crop,
            )

            PaywallBenefitRow()

            Spacer(modifier = Modifier.height(8.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(166.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PaywallPriceCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.monthly),
                        price = monthly?.displayPrice ?: "…",
                        footnote = stringResource(R.string.auto_renew_monthly_n_cancel_anytime),
                        onClick = {
                            monthly?.let { onPurchase(it.productId, it.offerToken) }
                        },
                        enabled = monthly != null && !billingState.purchaseInFlight,
                    )
                    PaywallPriceCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.life_time),
                        price = lifetime?.displayPrice ?: "…",
                        footnote = stringResource(R.string.one_time_payment),
                        onClick = {
                            lifetime?.let { onPurchase(it.productId, null) }
                        },
                        enabled = lifetime != null && !billingState.purchaseInFlight,
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 0.dp),
                    shape = RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = 8.dp,
                        bottomEnd = 0.dp,
                    ),
                    color = PopularBadge,
                ) {
                    Text(
                        text = stringResource(R.string.popular),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PaywallWeeklyCta(
                weekly = weekly,
                hasFreeTrial = billingState.weeklyHasFreeTrial,
                loading = billingState.loading,
                purchaseInFlight = billingState.purchaseInFlight,
                onClick = {
                    weekly?.let { onPurchase(it.productId, it.offerToken) }
                },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.terms_amp_conditions),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenTerms),
                    color = PaywallFooter,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(13.dp)
                        .background(PaywallFooter),
                )
                Text(
                    text = stringResource(R.string.privacy_policy),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenPolicy),
                    color = PaywallFooter,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            paywall?.let {
                Text(
                    text = it.title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = PaywallBody,
                )
                Text(
                    text = it.message,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PaywallTextMuted,
                )
            }

            billingState.errorMessage?.let { message ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TextButton(onClick = onRestore) {
                    Text(stringResource(R.string.paywall_restore))
                }
                TextButton(onClick = onManageSubscriptions) {
                    Text(stringResource(R.string.paywall_manage_subscriptions))
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .padding(4.dp)
                .alpha(0.5f),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close_40_2),
                contentDescription = null,
                tint = PaywallTextMuted,
            )
        }
    }
}

@Composable
private fun PaywallBenefitRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        PaywallBenefitCell(
            icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = PaywallBody) },
            label = stringResource(R.string.unlimited_library_icon_sticker),
        )
        PaywallBenefitCell(
            icon = { Icon(Icons.Outlined.Block, contentDescription = null, tint = PaywallBody) },
            label = stringResource(R.string.no_ad_experience_2),
        )
        PaywallBenefitCell(
            icon = { Icon(Icons.Outlined.NewReleases, contentDescription = null, tint = PaywallBody) },
            label = stringResource(R.string.early_update_new_feature),
        )
    }
}

@Composable
private fun PaywallBenefitCell(
    icon: @Composable () -> Unit,
    label: String,
) {
    Column(
        modifier = Modifier.width(110.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = PaywallBody,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun PaywallPriceCard(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    footnote: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Box(
        modifier = modifier
            .height(152.dp)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.bg_package_price),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PaywallTextMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = price,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PaywallAccent,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = footnote,
                style = MaterialTheme.typography.bodySmall,
                color = PaywallTextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PaywallWeeklyCta(
    weekly: BillingPlan?,
    hasFreeTrial: Boolean,
    loading: Boolean,
    purchaseInFlight: Boolean,
    onClick: () -> Unit,
) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFFFFABE5), Color(0xFFD47DFE)))
    val weekWord = stringResource(R.string.week11)
    val priceLine = weekly?.let { w ->
        "${w.displayPrice}/$weekWord"
    } ?: "…"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .defaultMinSize(minHeight = 56.dp)
            .background(gradient, RoundedCornerShape(50.dp))
            .clickable(
                enabled = weekly != null && !purchaseInFlight && !loading,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (hasFreeTrial && weekly != null) {
            Text(
                text = stringResource(R.string.start_3_days_free_trial),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = "${stringResource(R.string.try_free_for_3_days_then)} $priceLine",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
            )
        } else {
            Text(
                text = priceLine,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalWebViewScreen(
    title: String,
    assetPath: String,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close legal page")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = false
                    loadUrl("file:///android_asset/$assetPath")
                }
            },
        )
    }
}
