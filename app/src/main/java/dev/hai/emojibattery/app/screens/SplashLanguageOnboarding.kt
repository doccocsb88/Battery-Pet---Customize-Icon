package dev.hai.emojibattery.app.screens


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.HomeCategoryTab
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.AchievementTask
import dev.hai.emojibattery.model.BatteryPreset
import dev.hai.emojibattery.model.BatteryTrollTemplate
import dev.hai.emojibattery.model.ContentTemplate
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.EmojiPreset
import dev.hai.emojibattery.model.FeatureConfig
import dev.hai.emojibattery.model.GestureAction
import dev.hai.emojibattery.model.GestureTrigger
import dev.hai.emojibattery.model.MainSection
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush
import dev.hai.emojibattery.model.SearchTemplate
import dev.hai.emojibattery.model.StickerPlacement
import dev.hai.emojibattery.model.StickerPreset
import dev.hai.emojibattery.model.batteryTrollTemplateForId
import dev.hai.emojibattery.model.stickerPresetForId
import dev.hai.emojibattery.model.StatusBarTab
import dev.hai.emojibattery.model.ThemePreset
import dev.hai.emojibattery.billing.BillingUiState
import dev.hai.emojibattery.billing.GooglePlayPurchaseService
import dev.hai.emojibattery.billing.PurchaseService
import dev.hai.emojibattery.paywall.LegalWebViewScreen
import dev.hai.emojibattery.paywall.PaywallScreen
import dev.hai.emojibattery.service.AccessibilityBridge
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore
import dev.hai.emojibattery.locale.SUPPORTED_APP_LANGUAGES
import dev.hai.emojibattery.locale.displayNameForLocaleTag
import dev.hai.emojibattery.ui.navigation.AppRoute
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
internal fun SplashRoute(
    fastForward: Boolean,
    onFinish: () -> Unit,
) {
    // Original SplashActivity: ~700ms logo overshoot + ValueAnimator on progress; total ~1.2s before ads/navigation.
    var launchLogo by remember { mutableStateOf(false) }
    var progressTarget by remember { mutableStateOf(0f) }
    LaunchedEffect(fastForward) {
        if (fastForward) {
            launchLogo = true
            progressTarget = 1f
            onFinish()
            return@LaunchedEffect
        }
        launchLogo = true
        progressTarget = 1f
        delay(1200)
        onFinish()
    }
    val logoScale by animateFloatAsState(
        targetValue = if (launchLogo) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "splash_logo_scale",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (launchLogo) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "splash_logo_alpha",
    )
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
        label = "splash_progress",
    )
    // Shape/gradient XML drawables are not supported by Compose painterResource (vector/bitmap only).
    // Match res/drawable/bg_splash_screen.xml via the same color stops.
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorResource(R.color.splash_gradient_top),
                        colorResource(R.color.splash_gradient_mid),
                        colorResource(R.color.splash_gradient_bottom),
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = colorResource(R.color.splash_logo_plate),
                tonalElevation = 0.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.splash_logo_box))
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha
                    },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.img_btn_status_bar_new),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.splash_title),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.splash_subtitle),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.loading_dot),
                color = colorResource(R.color.splash_loading_accent),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(0.55f),
                color = MaterialTheme.colorScheme.primary,
                trackColor = colorResource(R.color.splash_progress_track),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.this_action_may_be_content_ads),
                color = colorResource(R.color.splash_ads_hint),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

@Composable
internal fun LanguageScreen(
    selectedLocaleTag: String,
    onSelectLocaleTag: (String) -> Unit,
    onNext: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val displayLocale = remember(configuration) {
        configuration.locales.get(0) ?: Locale.getDefault()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.select_your_language),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(SUPPORTED_APP_LANGUAGES, key = { it.localeTag }) { option ->
                    val active = option.localeTag == selectedLocaleTag
                    val label = displayNameForLocaleTag(option.localeTag, displayLocale)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSelectLocaleTag(option.localeTag) }
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(option.flagResId),
                                contentDescription = label,
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.size(width = 16.dp, height = 0.dp))
                            Text(
                                label,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (option.localeTag == "en") {
                                Text(
                                    stringResource(R.string.language_default),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                            // Icon Check
                            Image(
                                painter = painterResource(
                                    if (active) R.drawable.ic_checked_language_enable
                                    else R.drawable.ic_checked_language_disable
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            // Bottom fade + Next — decompiled bg_btn_instagram: gradient #ffabe5 → #d47dfe, pill shape
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White),
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(StrawberryCtaGradientBrush)
                        .clickable { onNext() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.next).uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnboardingScreen(
    uiState: AppUiState,
    onSkip: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val page = SampleCatalog.onboardingPages[uiState.onboardingPage.coerceIn(0, SampleCatalog.onboardingPages.lastIndex)]
    val isFirst = uiState.onboardingPage == 0
    val isLast = uiState.onboardingPage == SampleCatalog.onboardingPages.lastIndex
    val gradient = Brush.horizontalGradient(
        listOf(
            colorResource(R.color.onboarding_cta_gradient_start),
            colorResource(R.color.onboarding_cta_gradient_end),
        ),
    )
    val onboardingBg = colorResource(R.color.onboarding_scaffold)
    Scaffold(
        containerColor = onboardingBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    if (!isFirst) {
                        TextButton(onClick = onPrevious) { Text(stringResource(R.string.onboarding_back)) }
                    }
                },
                actions = { TextButton(onClick = onSkip) { Text(stringResource(R.string.onboarding_skip)) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = onboardingBg,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = colorResource(R.color.onboarding_hero_card),
                    border = BorderStroke(2.dp, colorResource(R.color.onboarding_hero_border)),
                    shadowElevation = 6.dp,
                ) {
                    AnimatedContent(
                        targetState = page.id,
                        transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(280)) },
                        label = "onboarding_hero",
                    ) { pageId ->
                        val p = SampleCatalog.onboardingPages.first { it.id == pageId }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(R.dimen.onboarding_hero_height))
                                .background(colorResource(R.color.onboarding_hero_inner)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Text(p.accentGlyph, style = MaterialTheme.typography.displayLarge)
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = colorResource(R.color.onboarding_chip_surface),
                                ) {
                                    Text(
                                        stringResource(R.string.app_name).uppercase(),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = colorResource(R.color.onboarding_chip_magenta),
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                }
                            }
                        }
                    }
                }
                AnimatedContent(
                    targetState = page.id,
                    transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(280)) },
                    label = "onboarding_copy",
                ) { pageId ->
                    val p = SampleCatalog.onboardingPages.first { it.id == pageId }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            p.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorResource(R.color.onboarding_title),
                        )
                        Text(
                            p.body,
                            color = colorResource(R.color.onboarding_body),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SampleCatalog.onboardingPages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(width = if (index == uiState.onboardingPage) 28.dp else 10.dp, height = 10.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (index == uiState.onboardingPage) {
                                        colorResource(R.color.onboarding_dot_active)
                                    } else {
                                        colorResource(R.color.onboarding_dot_inactive)
                                    },
                                ),
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = !isFirst,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(stringResource(R.string.onboarding_back))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(gradient),
                ) {
                    TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            if (isLast) {
                                stringResource(R.string.onboarding_get_started)
                            } else {
                                stringResource(R.string.onboarding_next)
                            },
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TutorialScreen(
    uiState: AppUiState,
    onClose: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenAccessibility: () -> Unit,
) {
    val page = SampleCatalog.tutorialPages[uiState.tutorialPage.coerceIn(0, SampleCatalog.tutorialPages.lastIndex)]
    val isFirst = uiState.tutorialPage == 0
    val isLast = uiState.tutorialPage == SampleCatalog.tutorialPages.lastIndex
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.how_to_use_title)) },
                navigationIcon = { TextButton(onClick = onClose) { Text(stringResource(R.string.close)) } },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TemplatePreviewCard(
                title = page.title,
                summary = page.body,
                glyph = page.accentGlyph,
                tag = stringResource(R.string.tutorial_step_format, uiState.tutorialPage + 1, SampleCatalog.tutorialPages.size),
            )
            if (uiState.tutorialPage == 0) {
                PermissionBanner(enabled = uiState.accessibilityGranted, onToggle = { onOpenAccessibility() })
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onPrevious, enabled = !isFirst, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.previous))
                }
                Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                    Text(if (isLast) stringResource(R.string.got_it) else stringResource(R.string.onboarding_next))
                }
            }
        }
    }
}
