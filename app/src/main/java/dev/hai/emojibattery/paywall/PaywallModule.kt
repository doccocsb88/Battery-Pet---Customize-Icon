package dev.hai.emojibattery.paywall

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.billing.BillingUiState
import dev.hai.emojibattery.billing.PurchaseService
import dev.hai.emojibattery.model.PaywallState
import dev.hai.emojibattery.model.SampleCatalog

// ─── Alpine Design Tokens ────────────────────────────────────────
private object Alpine {
    // Requested palette
    private val Blue = Color(0xFF8FB6D4)
    private val Green = Color(0xFF76916B)
    private val Sand = Color(0xFFD9B99B)
    private val Ink = Color(0xFF3C3C3C)

    // Surface hierarchy (stacked paper metaphor)
    val Surface = Color.White
    val SurfaceLow = Sand.copy(alpha = 0.16f)
    val SurfaceLowest = Color.White
    val SurfaceHigh = Blue.copy(alpha = 0.18f)

    // Tonal atmosphere
    val Primary = Blue
    val PrimaryDeep = Green
    val Secondary = Sand
    val OnSurface = Ink
    val OnSurfaceVariant = Ink.copy(alpha = 0.72f)
    val OnPrimary = Color.White

    // Decorative
    val Accent = Sand
    val GlassWhite = Color(0x33FFFFFF)

    // Radii (no 90° corners)
    val RoundFull = RoundedCornerShape(50)
    val RoundXL = RoundedCornerShape(28.dp)
    val RoundLG = RoundedCornerShape(22.dp)
    val RoundMD = RoundedCornerShape(16.dp)

    // Gradients
    val HeroGradient: Brush
        get() = Brush.verticalGradient(
            listOf(Primary, Secondary),
        )
    val CtaGradient: Brush
        get() = Brush.horizontalGradient(
            listOf(Primary, PrimaryDeep),
        )
    val AmbientShadowColor = Ink.copy(alpha = 0.09f)
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
    onPurchase: (productId: String, subscriptionOfferToken: String?) -> Unit,
) {
    val monthly = billingState.monthlyPlan
    val weekly = billingState.weeklyPlan
    val lifetime = billingState.lifetimePlan
    val loadingPrices = billingState.loading
    val monthPriceLabel = when {
        loadingPrices -> stringResource(R.string.paywall_price_loading)
        monthly != null -> monthly.displayPrice
        else -> stringResource(R.string.paywall_price_unavailable)
    }
    val lifetimePriceLabel = when {
        loadingPrices -> stringResource(R.string.paywall_price_loading)
        lifetime != null -> lifetime.displayPrice
        else -> stringResource(R.string.paywall_price_unavailable)
    }
    val weeklyTrialDays = billingState.weeklyTrialDays
    val monthlyTrialDays = billingState.monthlyTrialDays
    val weeklyLabel = if (billingState.weeklyHasFreeTrial) "Weekly Trial" else "Weekly"
    val monthlyLabel = if (billingState.monthlyHasFreeTrial) "Monthly Trial" else "Monthly"
    val lifetimeLabel = "Lifetime"
    val weeklyFootnote = when {
        weekly == null -> null
        billingState.weeklyHasFreeTrial && weeklyTrialDays != null -> "Free for $weeklyTrialDays days"
        else -> "Auto-renew every week"
    }
    val weeklyDescription = when {
        weekly == null -> null
        billingState.weeklyHasFreeTrial && weeklyTrialDays != null ->
            "Free for $weeklyTrialDays days, then auto-renew weekly."
        else -> "Auto-renew every week"
    }
    val monthlyDescription = when {
        monthly == null -> null
        billingState.monthlyHasFreeTrial && monthlyTrialDays != null ->
            "Free for $monthlyTrialDays days, then auto-renew monthly."
        monthly.description.isNotBlank() -> monthly.description
        else -> "Auto-renew every month"
    }
    val lifetimeDescription = when {
        lifetime == null -> null
        else -> "One-time billing"
    }
    val showContextCopy = paywall?.featureKey != SampleCatalog.FEATURE_EXTRA_STICKER_SLOT && paywall?.featureKey != "settings:store"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Alpine.SurfaceLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFAF4EF),
                            Color(0xFFF9F5F1),
                            Alpine.Surface,
                        ),
                        startY = 180f,
                    ),
                )
                .verticalScroll(rememberScrollState()),
        ) {
            // ─── Hero Section with gradient + header image ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            ) {
                // Header image with overlay blend
                Image(
                    painter = painterResource(R.drawable.img_header_iap_full),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x33FFF8F1),
                                    Color(0xCCFCF7F2),
                                    Color(0xFFFCF7F2),
                                ),
                            ),
                        ),
                )
            }

            Spacer(Modifier.height(18.dp))

            // ─── Benefits Section (tonal layer) ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AlpineBenefitRow(glyph = "💎", text = stringResource(R.string.unlimited_library_icon_sticker))
//                AlpineBenefitRow(glyph = "🚫", text = stringResource(R.string.no_ad_experience_2))
                AlpineBenefitRow(glyph = "🚀", text = stringResource(R.string.early_update_new_feature))
            }

            Spacer(Modifier.height(28.dp))

            // ─── Plan List ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AlpinePlanCard(
                    modifier = Modifier.fillMaxWidth(),
                    label = weeklyLabel,
                    description = weeklyDescription,
                    price = weekly?.displayPrice ?: stringResource(R.string.paywall_price_unavailable),
                    footnote = weeklyFootnote,
                    badge = null,
                    onClick = {
                        weekly?.let { onPurchase(it.productId, it.offerToken) }
                    },
                    enabled = weekly != null && !billingState.purchaseInFlight,
                )
                AlpinePlanCard(
                    modifier = Modifier.fillMaxWidth(),
                    label = monthlyLabel,
                    description = monthlyDescription,
                    price = monthPriceLabel,
                    footnote = when {
                        monthly != null && billingState.monthlyHasFreeTrial && monthlyTrialDays != null ->
                            "Free for $monthlyTrialDays days"
                        else -> "Auto-renew every month"
                    },
                    badge = if (monthly != null && billingState.monthlyHasFreeTrial) "Trial" else null,
                    onClick = {
                        monthly?.let { onPurchase(it.productId, it.offerToken) }
                    },
                    enabled = monthly != null && !billingState.purchaseInFlight,
                )
                AlpinePlanCard(
                    modifier = Modifier.fillMaxWidth(),
                    label = lifetimeLabel,
                    description = lifetimeDescription,
                    price = lifetimePriceLabel,
                    footnote = null,
                    badge = stringResource(R.string.popular),
                    onClick = {
                        lifetime?.let { onPurchase(it.productId, null) }
                    },
                    enabled = lifetime != null && !billingState.purchaseInFlight,
                )
            }

            Spacer(Modifier.height(20.dp))

            if (!loadingPrices && monthly == null && lifetime == null && weekly == null && billingState.errorMessage == null) {
                Text(
                    text = stringResource(R.string.paywall_catalog_hint),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Alpine.OnSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            // ─── Paywall context ───
            if (showContextCopy) {
                paywall?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Alpine.OnSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = it.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Alpine.OnSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── Legal Links (no dividers — Alpine rule) ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.terms_amp_conditions),
                    modifier = Modifier.clickable(onClick = onOpenTerms),
                    color = Alpine.OnSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "  ·  ",
                    color = Alpine.OnSurfaceVariant.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = stringResource(R.string.privacy_policy),
                    modifier = Modifier.clickable(onClick = onOpenPolicy),
                    color = Alpine.OnSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(8.dp))

            // ─── Restore / Manage ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(onClick = onRestore) {
                    Text(
                        stringResource(R.string.paywall_restore),
                        color = Alpine.Primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                TextButton(onClick = onManageSubscriptions) {
                    Text(
                        stringResource(R.string.paywall_manage_subscriptions),
                        color = Alpine.Primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ─── Close button (glassmorphism pill) ───
        Box(
            modifier = Modifier
                .padding(top = 12.dp, start = 12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Alpine.OnSurface.copy(alpha = 0.28f))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null,
                tint = Alpine.OnPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        billingState.errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = purchaseService::clearError,
                containerColor = Alpine.Surface,
                shape = Alpine.RoundLG,
                title = {
                    Text(
                        text = "Purchase error",
                        color = Alpine.OnSurface,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Text(
                        text = message,
                        color = Alpine.OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(onClick = purchaseService::clearError) {
                        Text(
                            text = "OK",
                            color = Alpine.Primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
            )
        }
    }
}

// ─── Benefit Row ─────────────────────────────────────────────────
@Composable
private fun AlpineBenefitRow(
    glyph: String,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Alpine.PrimaryDeep),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Alpine.OnSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─── Plan Selection Card ─────────────────────────────────────────
@Composable
private fun AlpinePlanCard(
    modifier: Modifier = Modifier,
    label: String,
    description: String?,
    price: String,
    footnote: String?,
    badge: String?,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Box(modifier = modifier) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 84.dp)
                .clickable(enabled = enabled, onClick = onClick),
            shape = Alpine.RoundXL,
            colors = CardDefaults.outlinedCardColors(containerColor = Alpine.SurfaceLowest),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.25.dp,
                color = Alpine.Secondary.copy(alpha = 0.42f),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Alpine.Primary,
                    )
                    val supportingCopy = description?.takeIf { it.isNotBlank() } ?: footnote
                    if (!supportingCopy.isNullOrBlank()) {
                        Text(
                            text = supportingCopy,
                            style = MaterialTheme.typography.bodySmall,
                            color = Alpine.OnSurfaceVariant,
                            lineHeight = 16.sp,
                        )
                    }
                }
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Alpine.OnSurface,
                    textAlign = TextAlign.End,
                )
            }
        }

        // "Popular" badge — floating pill
        if (badge != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp),
                shape = Alpine.RoundFull,
                color = Alpine.PrimaryDeep,
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = Alpine.OnPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ─── Primary CTA (weekly / free trial) ───────────────────────────
@Composable
private fun AlpineCtaButton(
    primaryLine: String,
    secondaryLine: String?,
    hasFreeTrial: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "ctaScale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(Alpine.RoundFull)
            .background(Alpine.CtaGradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = primaryLine,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Alpine.OnPrimary,
                textAlign = TextAlign.Center,
            )
            if (hasFreeTrial && secondaryLine != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = secondaryLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = Alpine.OnPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
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
