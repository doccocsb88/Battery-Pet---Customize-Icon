package dev.hai.emojibattery.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.webkit.WebView
import android.webkit.WebViewClient
import dev.hai.emojibattery.billing.BillingPlan
import dev.hai.emojibattery.billing.BillingUiState
import dev.hai.emojibattery.billing.PurchaseService
import dev.hai.emojibattery.model.PaywallState

private enum class SelectedPlan {
    Weekly,
    Lifetime,
}

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
    onPurchase: (String) -> Unit,
) {
    var selectedPlan by remember { mutableStateOf(SelectedPlan.Weekly) }
    val weekly = billingState.weeklyPlan
    val lifetime = billingState.lifetimePlan

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Upgrade to Premium") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close paywall")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PaywallHero(
                title = paywall?.title ?: "Unlock Premium",
                body = paywall?.message ?: "Unlock all premium content, better overlay packs, and higher sticker capacity.",
            )
            BenefitCard("Premium stickers", "Access exclusive sticker packs and animated premium content.")
            BenefitCard("Premium templates", "Use PRO real-time templates and advanced overlay packs.")
            BenefitCard("More sticker slots", "Go beyond the free sticker limit and build denser scenes.")

            PlanCard(
                title = "Weekly",
                subtitle = weekly?.description ?: "Unlock all premium content with weekly billing.",
                price = weekly?.displayPrice ?: "Loading...",
                billingNote = weekly?.billingLabel ?: "Auto-renews until canceled on Google Play.",
                selected = selectedPlan == SelectedPlan.Weekly,
                extraNote = "Cancel anytime",
                onSelect = { selectedPlan = SelectedPlan.Weekly },
                onPurchase = { onPurchase(purchaseService.weeklyProductId) },
                enabled = !billingState.purchaseInFlight,
                actionLabel = "Start Weekly",
            )

            PlanCard(
                title = "Lifetime",
                subtitle = lifetime?.description ?: "One-time purchase for lifetime premium access.",
                price = lifetime?.displayPrice ?: "Loading...",
                billingNote = lifetime?.billingLabel ?: "No recurring charge.",
                selected = selectedPlan == SelectedPlan.Lifetime,
                extraNote = "Best long-term value",
                onSelect = { selectedPlan = SelectedPlan.Lifetime },
                onPurchase = { onPurchase(purchaseService.lifetimeProductId) },
                enabled = !billingState.purchaseInFlight,
                actionLabel = "Buy Lifetime",
            )

            if (selectedPlan == SelectedPlan.Weekly) {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                    Text(
                        "Cancel anytime in Google Play subscriptions. Weekly plan renews automatically unless canceled before renewal.",
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Policy", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Subscription renews automatically until canceled. Charges are handled by Google Play. One-time lifetime purchase does not renew.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onOpenPolicy, modifier = Modifier.weight(1f)) {
                            Text("Privacy Policy")
                        }
                        TextButton(onClick = onOpenTerms, modifier = Modifier.weight(1f)) {
                            Text("Terms of Use")
                        }
                    }
                }
            }

            billingState.errorMessage?.let { message ->
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.errorContainer) {
                    Text(
                        message,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                    Text("Close")
                }
                OutlinedButton(onClick = onRestore, modifier = Modifier.weight(1f)) {
                    Text("Restore")
                }
            }

            TextButton(onClick = onManageSubscriptions, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Manage subscriptions")
            }
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

@Composable
private fun PaywallHero(
    title: String,
    body: String,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BenefitCard(
    title: String,
    body: String,
) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    subtitle: String,
    price: String,
    billingNote: String,
    selected: Boolean,
    extraNote: String,
    onSelect: () -> Unit,
    onPurchase: () -> Unit,
    enabled: Boolean,
    actionLabel: String,
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (selected) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Box(
                            modifier = Modifier.size(26.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Text(price, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(extraNote, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text(billingNote, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = onPurchase,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(actionLabel)
            }
        }
    }
}
