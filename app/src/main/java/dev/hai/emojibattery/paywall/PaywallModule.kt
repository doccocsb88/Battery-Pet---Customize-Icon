package dev.hai.emojibattery.paywall

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateColorAsState
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
    val weekWord = stringResource(R.string.week11)
    val weeklyPrimaryLine: String
    val weeklySecondaryLine: String?
    when {
        loadingPrices -> {
            weeklyPrimaryLine = stringResource(R.string.paywall_price_loading)
            weeklySecondaryLine = null
        }
        weekly != null && billingState.weeklyHasFreeTrial -> {
            weeklyPrimaryLine = stringResource(R.string.start_3_days_free_trial)
            weeklySecondaryLine =
                "${stringResource(R.string.try_free_for_3_days_then)} ${weekly.displayPrice}/$weekWord"
        }
        weekly != null -> {
            weeklyPrimaryLine = "${weekly.displayPrice}/$weekWord"
            weeklySecondaryLine = null
        }
        else -> {
            weeklyPrimaryLine = stringResource(R.string.paywall_price_unavailable)
            weeklySecondaryLine = null
        }
    }

    // Track which plan is selected: 0=monthly, 1=lifetime
    var selectedPlan by remember { mutableStateOf(1) }
    val showContextCopy = paywall?.featureKey != SampleCatalog.FEATURE_EXTRA_STICKER_SLOT && paywall?.featureKey != "settings:store"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Alpine.SurfaceLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            }

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

            // ─── Plan Selection Cards ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AlpinePlanCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.monthly).uppercase(),
                    price = monthPriceLabel,
                    footnote = stringResource(R.string.auto_renew_monthly_n_cancel_anytime),
                    selected = selectedPlan == 0,
                    badge = null,
                    onClick = {
                        selectedPlan = 0
                    },
                    enabled = monthly != null && !billingState.purchaseInFlight,
                )
                AlpinePlanCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.life_time).uppercase(),
                    price = lifetimePriceLabel,
                    footnote = stringResource(R.string.one_time_payment),
                    selected = selectedPlan == 1,
                    badge = stringResource(R.string.popular),
                    onClick = {
                        selectedPlan = 1
                    },
                    enabled = lifetime != null && !billingState.purchaseInFlight,
                )
            }

            Spacer(Modifier.height(20.dp))

            // ─── Primary CTA (River Stone Pill) ───
            AlpineCtaButton(
                primaryLine = weeklyPrimaryLine,
                secondaryLine = weeklySecondaryLine,
                hasFreeTrial = weekly != null && billingState.weeklyHasFreeTrial,
                enabled = weekly != null && !billingState.purchaseInFlight && !billingState.loading,
                onClick = {
                    weekly?.let { onPurchase(it.productId, it.offerToken) }
                },
            )

            // ─── Subscribe Selected Plan ───
            if (selectedPlan == 0 && monthly != null) {
                AlpineSecondaryCtaButton(
                    text = "${stringResource(R.string.monthly)} · ${monthly.displayPrice}",
                    enabled = !billingState.purchaseInFlight,
                    onClick = { onPurchase(monthly.productId, monthly.offerToken) },
                )
            }

            if (!loadingPrices && monthly == null && lifetime == null && weekly == null && billingState.errorMessage == null) {
                Text(
                    text = stringResource(R.string.paywall_catalog_hint),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Alpine.OnSurfaceVariant,
                )
            }

            // ─── Error Message ───
            billingState.errorMessage?.let { message ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    shape = Alpine.RoundLG,
                    color = Alpine.Secondary.copy(alpha = 0.35f),
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = Alpine.OnSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
    }
}

// ─── Benefit Row ─────────────────────────────────────────────────
@Composable
private fun AlpineBenefitRow(
    glyph: String,
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
    price: String,
    footnote: String,
    selected: Boolean,
    badge: String?,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Alpine.SurfaceLowest else Alpine.SurfaceLow,
        animationSpec = tween(250),
        label = "planBg",
    )
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick),
            shape = Alpine.RoundXL,
            color = bgColor,
            shadowElevation = if (selected) 0.dp else 0.dp,
            tonalElevation = if (selected) 2.dp else 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 0.05.sp,
                    ),
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Alpine.Primary else Alpine.OnSurfaceVariant,
                )
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (selected) Alpine.PrimaryDeep else Alpine.OnSurface,
                )
                Text(
                    text = footnote,
                    style = MaterialTheme.typography.bodySmall,
                    color = Alpine.OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
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

// ─── Secondary CTA (subscribe selected plan) ────────────────────
@Composable
private fun AlpineSecondaryCtaButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(Alpine.RoundFull)
            .background(Alpine.SurfaceHigh)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Alpine.PrimaryDeep,
            textAlign = TextAlign.Center,
        )
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
