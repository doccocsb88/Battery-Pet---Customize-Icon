package dev.hai.emojibattery.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
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
import dev.hai.emojibattery.ui.navigation.AppRoute
import kotlinx.coroutines.delay

@Composable
fun EmojiBatteryApp(
    viewModel: EmojiBatteryViewModel = viewModel(),
    initialRoute: String? = null,
    purchaseService: PurchaseService = GooglePlayPurchaseService.shared,
) {
    val context = LocalContext.current.applicationContext
    val rawContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val billingState by purchaseService.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val showBottomBar = route in setOf(
        AppRoute.Home.route,
        AppRoute.Customize.route,
        AppRoute.Gesture.route,
        AppRoute.Achievement.route,
    )

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(context) {
        purchaseService.start(context)
        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
    }

    LaunchedEffect(billingState.ownedProductIds) {
        val hasPremium = purchaseService.hasPremiumAccess(billingState.ownedProductIds)
        viewModel.syncPremiumAccess(hasPremium)
    }

    LaunchedEffect(uiState.infoMessage) {
        val message = uiState.infoMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(showBottomBar) {
                MainBottomBar(
                    currentRoute = route,
                    onNavigate = { destination, section ->
                        viewModel.selectMainSection(section)
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute ?: AppRoute.Splash.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppRoute.Splash.route) {
                SplashRoute(
                    uiState = uiState,
                    onFinish = {
                        viewModel.finishSplash()
                        val nextRoute = when {
                            !uiState.languageChosen -> AppRoute.Language.route
                            !uiState.onboardingCompleted -> AppRoute.Onboarding.route
                            else -> AppRoute.Home.route
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(AppRoute.Splash.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(AppRoute.Language.route) {
                LanguageScreen(
                    selected = uiState.selectedLanguage,
                    onChooseLanguage = { viewModel.chooseLanguage(it) },
                    onNext = {
                        val nextRoute = if (uiState.onboardingCompleted) AppRoute.Home.route else AppRoute.Onboarding.route
                        navController.navigate(nextRoute) {
                            popUpTo(AppRoute.Language.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(AppRoute.Onboarding.route) {
                OnboardingScreen(
                    uiState = uiState,
                    onSkip = {
                        viewModel.skipOnboarding()
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                        }
                    },
                    onPrevious = viewModel::previousOnboardingPage,
                    onNext = {
                        val isLast = uiState.onboardingPage >= SampleCatalog.onboardingPages.lastIndex
                        viewModel.nextOnboardingPage()
                        if (isLast) {
                            navController.navigate(AppRoute.Home.route) {
                                popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                            }
                        }
                    },
                )
            }
            composable(AppRoute.Tutorial.route) {
                TutorialScreen(
                    uiState = uiState,
                    onClose = { navController.popBackStack() },
                    onPrevious = viewModel::previousTutorialPage,
                    onNext = {
                        val isLast = uiState.tutorialPage >= SampleCatalog.tutorialPages.lastIndex
                        viewModel.nextTutorialPage()
                        if (isLast) navController.popBackStack()
                    },
                    onOpenAccessibility = {
                        AccessibilityBridge.openSettings(context)
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                    },
                )
            }
            composable(AppRoute.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onSelectCategory = viewModel::selectHomeCategory,
                    onOpenStatusBarCustom = {
                        viewModel.selectMainSection(MainSection.Home)
                        navController.navigate(AppRoute.StatusBarCustom.route)
                    },
                    onOpenLegacyBattery = { navController.navigate(AppRoute.LegacyBattery.route) },
                    onOpenSearch = { navController.navigate(AppRoute.Search.route) },
                    onOpenSticker = { navController.navigate(AppRoute.EmojiSticker.route) },
                    onOpenBatteryTroll = { navController.navigate(AppRoute.BatteryTroll.route) },
                    onOpenSettings = { navController.navigate(AppRoute.Settings.route) },
                    onOpenFeedback = { navController.navigate(AppRoute.Feedback.route) },
                )
            }
            composable(AppRoute.Customize.route) {
                CustomizeHubScreen(
                    onOpenSticker = { navController.navigate(AppRoute.EmojiSticker.route) },
                    onOpenFeature = { entry ->
                        when (entry) {
                            CustomizeEntry.Charge,
                            CustomizeEntry.Emotion,
                            CustomizeEntry.Theme,
                            CustomizeEntry.Settings,
                            -> navController.navigate(AppRoute.StatusBarCustom.route)

                            else -> navController.navigate(AppRoute.FeatureDetail.create(entry.title))
                        }
                    },
                    onOpenStatusBarCustom = { navController.navigate(AppRoute.StatusBarCustom.route) },
                    onOpenRealTime = { navController.navigate(AppRoute.RealTime.route) },
                    onOpenBatteryTroll = { navController.navigate(AppRoute.BatteryTroll.route) },
                    onOpenSettings = { navController.navigate(AppRoute.Settings.route) },
                )
            }
            composable(AppRoute.Gesture.route) {
                GestureScreen(
                    uiState = uiState,
                    onSetGestureEnabled = viewModel::setGestureEnabled,
                    onSetVibrateFeedback = viewModel::setVibrateFeedback,
                    onSetGestureAction = viewModel::setGestureAction,
                )
            }
            composable(AppRoute.Achievement.route) {
                AchievementScreen(
                    uiState = uiState,
                    onClaim = viewModel::claimAchievement,
                    onNavigate = { route -> navController.navigate(route) },
                )
            }
            composable(AppRoute.StatusBarCustom.route) {
                StatusBarCustomScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectTab = viewModel::selectStatusTab,
                    onSelectBattery = viewModel::selectBatteryPreset,
                    onSelectEmoji = viewModel::selectEmojiPreset,
                    onSelectTheme = viewModel::selectTheme,
                    onSetStatusBarHeight = viewModel::setStatusBarHeight,
                    onSetLeftMargin = viewModel::setStatusBarLeftMargin,
                    onSetRightMargin = viewModel::setStatusBarRightMargin,
                    onSetBatteryScale = viewModel::setBatteryPercentScale,
                    onSetEmojiScale = viewModel::setEmojiScale,
                    onTogglePercentage = viewModel::setShowPercentage,
                    onToggleAnimate = viewModel::setAnimateCharge,
                    onToggleStroke = viewModel::setShowStroke,
                    onRestore = viewModel::restoreApplied,
                    onApply = {
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        viewModel.applyConfig()
                        if (AccessibilityBridge.isEnabled(context)) {
                            OverlayConfigStore.saveStatusBarConfig(context, uiState.editingConfig)
                            OverlayAccessibilityService.requestRefresh(context)
                        }
                    },
                    onAccessibilityChanged = {
                        AccessibilityBridge.openSettings(context)
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                    },
                )
            }
            composable(AppRoute.LegacyBattery.route) {
                LegacyBatteryScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectBattery = viewModel::selectBatteryPreset,
                    onSelectEmoji = viewModel::selectEmojiPreset,
                    onSetBatteryScale = viewModel::setBatteryPercentScale,
                    onSetEmojiScale = viewModel::setEmojiScale,
                    onApply = {
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        viewModel.applyLegacyBatteryConfig()
                        if (AccessibilityBridge.isEnabled(context)) {
                            OverlayConfigStore.saveStatusBarConfig(context, uiState.editingConfig)
                            OverlayAccessibilityService.requestRefresh(context)
                        }
                    },
                )
            }
            composable(AppRoute.Search.route) {
                SearchScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onQueryChange = viewModel::setSearchQuery,
                    onNavigate = { route -> navController.navigate(route) },
                )
            }
            composable(AppRoute.Settings.route) {
                SettingsScreen(
                    uiState = uiState,
                    onOpenLanguage = { navController.navigate(AppRoute.Language.route) },
                    onReplayTutorial = {
                        viewModel.replayTutorial()
                        navController.navigate(AppRoute.Tutorial.route)
                    },
                    onToggleProtection = viewModel::setProtectFromRecentApps,
                    onOpenPrivacy = {
                        viewModel.openPrivacyPolicy()
                        navController.navigate(AppRoute.Legal.create("privacy"))
                    },
                    onOpenTerms = {
                        viewModel.openTermsOfUse()
                        navController.navigate(AppRoute.Legal.create("terms"))
                    },
                    onShareApp = viewModel::shareApp,
                    onOpenFeedback = { navController.navigate(AppRoute.Feedback.route) },
                    onRateApp = viewModel::rateApp,
                    onSelectRating = viewModel::setRatingSelection,
                    onCheckUpdate = viewModel::checkForUpdates,
                    onToggleAccessibility = {
                        AccessibilityBridge.openSettings(context)
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                    },
                )
            }
            composable(AppRoute.Paywall.route) {
                PaywallScreen(
                    paywall = uiState.paywallState,
                    billingState = billingState,
                    onClose = {
                        viewModel.dismissPaywall()
                        navController.popBackStack()
                    },
                    purchaseService = purchaseService,
                    onRestore = purchaseService::restorePurchases,
                    onManageSubscriptions = { purchaseService.openManageSubscriptions(rawContext) },
                    onOpenPolicy = { navController.navigate(AppRoute.Legal.create("privacy")) },
                    onOpenTerms = { navController.navigate(AppRoute.Legal.create("terms")) },
                    onPurchase = { productId ->
                        rawContext.findActivity()?.let { activity ->
                            purchaseService.purchase(activity, productId)
                        }
                    },
                )
            }
            composable(AppRoute.Feedback.route) {
                FeedbackScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectRating = viewModel::setRatingSelection,
                    onToggleReason = viewModel::toggleFeedbackReason,
                    onNoteChange = viewModel::setFeedbackNote,
                    onSubmit = viewModel::submitFeedback,
                )
            }
            composable(
                route = AppRoute.Legal.route,
                arguments = listOf(navArgument("document") { type = NavType.StringType }),
            ) { entry ->
                when (entry.arguments?.getString("document").orEmpty()) {
                    "privacy" -> LegalWebViewScreen(
                        title = "Privacy Policy",
                        assetPath = "legal/privacy_policy.html",
                        onBack = { navController.popBackStack() },
                    )
                    "terms" -> LegalWebViewScreen(
                        title = "Terms of Use",
                        assetPath = "legal/terms_of_use.html",
                        onBack = { navController.popBackStack() },
                    )
                    else -> PlaceholderScreen(
                        title = "Legal",
                        subtitle = "Unknown legal document route.",
                    )
                }
            }
            composable(AppRoute.RealTime.route) {
                RealTimeScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectTemplate = viewModel::selectRealTimeTemplate,
                    onToggleAccessibility = {
                        AccessibilityBridge.openSettings(context)
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                    },
                    onApply = {
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        viewModel.applyRealTimeTemplate()
                        if (AccessibilityBridge.isEnabled(context)) {
                            OverlayConfigStore.saveRealTime(context, uiState.selectedRealTimeTemplateId)
                            OverlayAccessibilityService.requestRefresh(context)
                        }
                    },
                )
            }
            composable(AppRoute.BatteryTroll.route) {
                BatteryTrollScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectTemplate = viewModel::selectBatteryTrollTemplate,
                    onSetMessage = viewModel::setTrollMessage,
                    onToggleAutoDrop = viewModel::setTrollAutoDrop,
                    onRefreshBatteryTrollCatalog = viewModel::refreshBatteryTrollCatalog,
                    onToggleAccessibility = {
                        AccessibilityBridge.openSettings(context)
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                    },
                    onApply = {
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        viewModel.applyBatteryTroll()
                        if (AccessibilityBridge.isEnabled(context)) {
                            OverlayConfigStore.saveBatteryTroll(context, uiState)
                            OverlayAccessibilityService.requestRefresh(context)
                        }
                    },
                    onTurnOff = {
                        viewModel.turnOffBatteryTroll()
                        OverlayConfigStore.clearBatteryTroll(context)
                        OverlayAccessibilityService.requestRefresh(context)
                    },
                )
            }
            composable(AppRoute.EmojiSticker.route) {
                EmojiStickerScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onAddSticker = viewModel::addSticker,
                    onSelectSticker = viewModel::selectSticker,
                    onRemoveSticker = viewModel::removeSticker,
                    onUpdateStickerSize = viewModel::updateSelectedStickerSize,
                    onUpdateStickerSpeed = viewModel::updateSelectedStickerSpeed,
                    onRefreshStickerCatalog = viewModel::refreshStickerCatalog,
                    onToggleAccessibility = {
                        AccessibilityBridge.openSettings(context)
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                    },
                    onSave = {
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        viewModel.saveStickerOverlay()
                        if (AccessibilityBridge.isEnabled(context)) {
                            OverlayConfigStore.saveStickerOverlay(context, uiState)
                            OverlayAccessibilityService.requestRefresh(context)
                        }
                    },
                    onTurnOff = {
                        viewModel.turnOffStickerOverlay()
                        OverlayConfigStore.clearStickerOverlay(context)
                        OverlayAccessibilityService.requestRefresh(context)
                    },
                )
            }
            composable(
                route = AppRoute.FeatureDetail.route,
                arguments = listOf(navArgument("feature") { type = NavType.StringType }),
            ) { entry ->
                val feature = entry.arguments?.getString("feature").orEmpty()
                val customizeEntry = CustomizeEntry.entries.firstOrNull { it.title == feature }
                if (customizeEntry != null) {
                    FeatureDetailScreen(
                        entry = customizeEntry,
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                        onToggleEnabled = { value -> viewModel.updateFeatureEnabled(customizeEntry, value) },
                        onSetIntensity = { value -> viewModel.updateFeatureIntensity(customizeEntry, value) },
                        onSelectVariant = { variant -> viewModel.updateFeatureVariant(customizeEntry, variant) },
                        onReset = { viewModel.resetFeature(customizeEntry) },
                        onApply = { viewModel.applyFeature(customizeEntry) },
                    )
                } else {
                    PlaceholderScreen(
                        title = feature,
                        subtitle = "Unknown feature route.",
                    )
                }
            }
        }
    }

    LaunchedEffect(uiState.paywallState, route) {
        if (uiState.paywallState != null && route != AppRoute.Paywall.route) {
            navController.navigate(AppRoute.Paywall.route)
        }
    }

    LaunchedEffect(initialRoute) {
        val target = initialRoute ?: return@LaunchedEffect
        if (target != route) {
            navController.navigate(target) {
                launchSingleTop = true
            }
        }
    }
}

@Composable
private fun GestureScreen(
    uiState: AppUiState,
    onSetGestureEnabled: (Boolean) -> Unit,
    onSetVibrateFeedback: (Boolean) -> Unit,
    onSetGestureAction: (GestureTrigger, GestureAction) -> Unit,
) {
    val rowDescription = { action: GestureAction ->
        when (action) {
            GestureAction.None -> "Not selected"
            GestureAction.OpenCustomize -> "Open customize screen"
            GestureAction.OpenEmojiSticker -> "Open emoji sticker"
            GestureAction.OpenSearch -> "Open search"
            GestureAction.OpenBatteryTroll -> "Open battery troll"
            GestureAction.ToggleOverlay -> "Toggle overlay"
        }
    }
    Scaffold(
        containerColor = Color(0xFFFEF5FA),
        topBar = {
            OriginalTopShell(
                title = "Battery Icon",
                onLeftPrimary = {},
                onLeftSecondary = {},
                onSearch = {},
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp) {
                Column(Modifier.padding(vertical = 6.dp)) {
                    GestureSwitchRow(
                        iconRes = R.drawable.ic_guesture_32,
                        title = "Gesture",
                        description = "Use gestures on the status bar to trigger your favourite action.",
                        enabled = uiState.gestureEnabled,
                        onToggle = onSetGestureEnabled,
                    )
                    AnimatedVisibility(uiState.gestureEnabled) {
                        Column {
                            GestureTrigger.entries.forEach { trigger ->
                                GestureSelectionRow(
                                    title = trigger.title,
                                    description = rowDescription(uiState.gestureActions[trigger] ?: GestureAction.None),
                                    selectedAction = uiState.gestureActions[trigger] ?: GestureAction.None,
                                    onSelectAction = { onSetGestureAction(trigger, it) },
                                )
                            }
                        }
                    }
                }
            }
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp) {
                GestureSwitchRow(
                    iconRes = R.drawable.ic_vibrate_feedback_32,
                    title = "Vibrate feedback",
                    description = "Vibrate when a gesture action is recognized.",
                    enabled = uiState.vibrateFeedback,
                    onToggle = onSetVibrateFeedback,
                )
            }
            Spacer(Modifier.height(72.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EmojiStickerScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onAddSticker: (String) -> Unit,
    onSelectSticker: (String) -> Unit,
    onRemoveSticker: (String) -> Unit,
    onUpdateStickerSize: (Float) -> Unit,
    onUpdateStickerSpeed: (Float) -> Unit,
    onRefreshStickerCatalog: () -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onSave: () -> Unit,
    onTurnOff: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onRefreshStickerCatalog()
    }

    val stickerLibrary = if (uiState.stickerCatalogRemote.isNotEmpty()) {
        uiState.stickerCatalogRemote
    } else {
        SampleCatalog.stickerPresets
    }
    val selectedSticker = uiState.selectedStickerId?.let { uiState.stickerPresetForId(it) }
    val selectedPlacement = uiState.selectedStickerId?.let { id ->
        uiState.stickerPlacements.firstOrNull { it.stickerId == id }
    }
    val stickerScroll = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(stickerScroll)
                .padding(horizontal = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_40_new),
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(40.dp)
                        .clickable(onClick = onBack),
                )
            }
            StickerPreviewCard(
                selectedSticker = selectedSticker,
                selectedPlacement = selectedPlacement,
                overlayEnabled = uiState.stickerOverlayEnabled,
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                Text(
                    if (uiState.premiumUnlocked) {
                        "Premium unlocked. You can add up to ${SampleCatalog.PREMIUM_STICKER_SLOTS} stickers."
                    } else if (uiState.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_EXTRA_STICKER_SLOT)) {
                        "Reward unlocked. You can add up to ${SampleCatalog.REWARD_EXTRA_STICKER_SLOTS} stickers."
                    } else {
                        "Free mode allows ${SampleCatalog.FREE_STICKER_SLOTS} sticker. Premium stickers trigger paywall."
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Color(0xFF5C4B51),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            PermissionBanner(
                enabled = uiState.accessibilityGranted,
                onToggle = onToggleAccessibility,
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Add Sticker",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFE5FC),
                            modifier = Modifier.clickable { },
                        ) {
                            Text(
                                "Tutorial",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color(0xFF5C4B51),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                    if (uiState.stickerCatalogLoading && uiState.stickerCatalogRemote.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val loadingComposition by rememberLottieComposition(
                                LottieCompositionSpec.Asset("cute_loading.json"),
                            )
                            LottieAnimation(
                                composition = loadingComposition,
                                iterations = LottieConstants.IterateForever,
                                speed = 2f,
                                modifier = Modifier.size(120.dp),
                            )
                            Text(
                                "Loading…",
                                color = Color(0xFF5C4B51),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stickerLibrary.chunked(4).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    row.forEach { sticker ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            StickerCatalogCard(
                                                sticker = sticker,
                                                selected = uiState.selectedStickerId == sticker.id,
                                                added = uiState.stickerPlacements.any { it.stickerId == sticker.id },
                                                onClick = { onAddSticker(sticker.id) },
                                            )
                                        }
                                    }
                                    repeat(4 - row.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "My Sticker",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "${uiState.stickerPlacements.size} added",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (uiState.stickerPlacements.isEmpty()) {
                        Text(
                            "Add a sticker from the library above to start editing.",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(88.dp),
                        ) {
                            items(uiState.stickerPlacements, key = { it.stickerId }) { placement ->
                                val sticker = uiState.stickerPresetForId(placement.stickerId)
                                if (sticker != null) {
                                    AddedStickerChip(
                                        sticker = sticker,
                                        selected = uiState.selectedStickerId == sticker.id,
                                        onSelect = { onSelectSticker(sticker.id) },
                                        onRemove = { onRemoveSticker(sticker.id) },
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.Black)
                    Text(
                        "Selected Sticker Controls",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5C4B51),
                    )
                    if (selectedSticker != null && selectedPlacement != null) {
                        Text(
                            "${selectedSticker.glyph} ${selectedSticker.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF5C4B51),
                        )
                        SliderField("Sticker size", selectedPlacement.size, 0.2f..1f, onUpdateStickerSize)
                        SliderField("Sticker speed", selectedPlacement.speed, 0.2f..1f, onUpdateStickerSpeed)
                    } else {
                        Text(
                            "Select one of your added stickers to edit size and speed.",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFFFFE5FC),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable(onClick = onTurnOff),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_turn_off_shimeji),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Turn Off",
                                    color = Color(0xFFD47DFE),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFABE5), Color(0xFFD47DFE)),
                                    ),
                                )
                                .clickable(onClick = onSave),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Save",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GestureActionCard(
    trigger: GestureTrigger,
    selectedAction: GestureAction,
    onSelectAction: (GestureAction) -> Unit,
) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(trigger.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(trigger.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SampleCatalog.gestureActionOptions.forEach { action ->
                    FilterChip(
                        selected = selectedAction == action,
                        onClick = { onSelectAction(action) },
                        label = { Text(action.title) },
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            ) {
                Text(
                    selectedAction.subtitle,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SplashRoute(
    uiState: AppUiState,
    onFinish: () -> Unit,
) {
    LaunchedEffect(uiState.splashDone) {
        if (!uiState.splashDone) {
            delay(1200)
            onFinish()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text("🔋", style = MaterialTheme.typography.displaySmall)
            }
            Spacer(Modifier.height(20.dp))
            Text("Emoji Battery Port", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Splash -> language gate -> main shell -> battery icon editor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.55f))
        }
    }
}

@Composable
private fun LanguageScreen(
    selected: String,
    onChooseLanguage: (String) -> Unit,
    onNext: () -> Unit,
) {
    val options = listOf(
        "English" to R.drawable.flag_united_kingdom,
        "Hindi" to R.drawable.flag_india,
        "Spanish" to R.drawable.flag_spain,
        "French" to R.drawable.flag_france,
        "Arabic" to R.drawable.flag_saudi_arabia,
        "Portuguese" to R.drawable.flag_portugal,
        "Indonesian" to R.drawable.flag_indonesia,
        "German" to R.drawable.flag_germany,
        "Vietnamese" to R.drawable.flag_vietnam,
        "Russian" to R.drawable.flag_russia,
        "Japanese" to R.drawable.flag_japan,
        "Korean" to R.drawable.flag_south_korea,
        "Filipino" to R.drawable.flag_philippines,
        "Uzbek" to R.drawable.flag_uzbekistn,
        "Persian" to R.drawable.flag_partian,
        "Chinese" to R.drawable.flag_china,
        "Thai" to R.drawable.flag_thailand,
        "Turkish" to R.drawable.flag_turkey
    )

    Scaffold(containerColor = Color.White) { innerPadding ->
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
                    "Select Your Language",
                    color = Color(0xFF5C4B51),
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
                items(options) { (language, flagRes) ->
                    val active = language == selected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF2F2F2))
                            .clickable { onChooseLanguage(language) }
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(flagRes),
                                contentDescription = language,
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.size(width = 16.dp, height = 0.dp))
                            Text(
                                language,
                                color = Color(0xFF5C4B51),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (language == "English") {
                                Text(
                                    "Default",
                                    color = Color(0xFF5C4B51).copy(alpha = 0.8f),
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
            
            // Bottom Gradient & Next Button Area
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
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF5C4B51)) // Or primary button color
                        .clickable { onNext() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "NEXT",
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
private fun OnboardingScreen(
    uiState: AppUiState,
    onSkip: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val page = SampleCatalog.onboardingPages[uiState.onboardingPage.coerceIn(0, SampleCatalog.onboardingPages.lastIndex)]
    val isFirst = uiState.onboardingPage == 0
    val isLast = uiState.onboardingPage == SampleCatalog.onboardingPages.lastIndex
    val gradient = Brush.horizontalGradient(listOf(Color(0xFFF6A2D8), Color(0xFFB765F5)))
    Scaffold(
        containerColor = Color(0xFFFFF7FB),
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    if (!isFirst) {
                        TextButton(onClick = onPrevious) { Text("Back") }
                    }
                },
                actions = { TextButton(onClick = onSkip) { Text("Skip") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFF7FB),
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
                    color = Color.White,
                    border = BorderStroke(2.dp, Color(0xFFF0D6EA)),
                    shadowElevation = 6.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                            .background(Color(0xFFFFFBFE)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(page.accentGlyph, style = MaterialTheme.typography.displayLarge)
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFFFEFF7),
                            ) {
                                Text(
                                    "EMOJI BATTERY",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = Color(0xFFB26AD9),
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                        }
                    }
                }
                Text(page.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5F4B54))
                Text(page.body, color = Color(0xFF8D7680), style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SampleCatalog.onboardingPages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(width = if (index == uiState.onboardingPage) 28.dp else 10.dp, height = 10.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (index == uiState.onboardingPage) Color(0xFFE285EF)
                                    else Color(0xFFF2DDEB),
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
                    Text("Previous")
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(gradient),
                ) {
                    TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            if (isLast) "Get Started" else "Next",
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
private fun TutorialScreen(
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
                title = { Text("How To Use") },
                navigationIcon = { TextButton(onClick = onClose) { Text("Close") } },
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
                tag = "Step ${uiState.tutorialPage + 1}/${SampleCatalog.tutorialPages.size}",
            )
            if (uiState.tutorialPage == 0) {
                PermissionBanner(enabled = uiState.accessibilityGranted, onToggle = { onOpenAccessibility() })
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onPrevious, enabled = !isFirst, modifier = Modifier.weight(1f)) {
                    Text("Previous")
                }
                Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                    Text(if (isLast) "Got It" else "Next")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(
    uiState: AppUiState,
    onSelectCategory: (String) -> Unit,
    onOpenStatusBarCustom: () -> Unit,
    onOpenLegacyBattery: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSticker: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFeedback: () -> Unit,
) {
    val categories = uiState.homeTabs.takeIf { it.isNotEmpty() }
        ?: SampleCatalog.homeCategories.map { HomeCategoryTab(it.id, it.title) }
    if (categories.isEmpty()) return

    val initialPage = remember(uiState.selectedHomeCategoryId, categories) {
        categories.indexOfFirst { it.id == uiState.selectedHomeCategoryId }
            .coerceIn(0, (categories.size - 1).coerceAtLeast(0))
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { categories.size },
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(categories.map { it.id }.joinToString(), uiState.selectedHomeCategoryId) {
        val idx = categories.indexOfFirst { it.id == uiState.selectedHomeCategoryId }.coerceAtLeast(0)
        if (pagerState.currentPage != idx) {
            pagerState.scrollToPage(idx)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                categories.getOrNull(page)?.id?.let(onSelectCategory)
            }
    }

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
        topBar = {
            OriginalTopShell(
                title = "Battery Icon",
                onLeftPrimary = onOpenSettings,
                onLeftSecondary = onOpenFeedback,
                onSearch = onOpenSearch,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 3.dp,
            ) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(R.drawable.bg_sub_home_x_mas),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.06f),
                        contentScale = ContentScale.Crop,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 14.dp),
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            items(
                                count = categories.size,
                                key = { index -> categories[index].id },
                            ) { index ->
                                val category = categories[index]
                                val selected = pagerState.settledPage == index
                                Text(
                                    text = when {
                                        category.id == "hot" -> "🔥 ${category.title}"
                                        else -> category.title
                                    },
                                    color = if (selected) Color(0xFF5C4B51) else Color(0xFFD1D1D1),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 8.dp)
                                .size(width = 70.dp, height = 2.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFABE5), Color(0xFFD47DFE)),
                                    ),
                                ),
                        )
                        HorizontalPager(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            state = pagerState,
                            beyondViewportPageCount = 1,
                        ) { page ->
                            val categoryId = categories[page].id
                            val gridItems = uiState.homeItemsByCategoryId[categoryId].orEmpty()
                            val loading = uiState.homeCategoryLoadingId == categoryId && gridItems.isEmpty()

                            if (loading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = Color(0xFFD47DFE))
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 12.dp),
                                ) {
                                    items(gridItems.chunked(3)) { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                                        ) {
                                            rowItems.forEach { item ->
                                                HomeBatteryGridCard(
                                                    item = item,
                                                    onClick = {
                                                        when {
                                                            item.animated -> onOpenSticker()
                                                            item.title.contains("Troll", ignoreCase = true) -> onOpenBatteryTroll()
                                                            item.title.contains("Search", ignoreCase = true) -> onOpenSearch()
                                                            else -> onOpenStatusBarCustom()
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                            repeat(3 - rowItems.size) {
                                                Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomizeHubScreen(
    onOpenSticker: () -> Unit,
    onOpenFeature: (CustomizeEntry) -> Unit,
    onOpenStatusBarCustom: () -> Unit,
    onOpenRealTime: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val gridEntries = listOf(
        CustomizeEntry.Emotion,
        CustomizeEntry.Wifi,
        CustomizeEntry.Data,
        CustomizeEntry.Signal,
        CustomizeEntry.Airplane,
        CustomizeEntry.Hotspot,
        CustomizeEntry.Ringer,
        CustomizeEntry.Charge,
        CustomizeEntry.DateTime,
    )
    Scaffold(
        containerColor = Color(0xFFFFF7FB),
        topBar = {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
                shadowElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            HomeRoundIcon(R.drawable.ic_settings_new, onOpenSettings)
                            HomeRoundIcon(R.drawable.ic_feeb_back_home, onOpenRealTime)
                        }
                        Text("Battery Icon", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5F4B54))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            HomeRoundIcon(R.drawable.ic_home_search, onOpenStatusBarCustom)
                            Image(
                                painter = painterResource(R.drawable.no_ads_on),
                                contentDescription = "Ads on",
                                modifier = Modifier.size(width = 40.dp, height = 36.dp),
                            )
                        }
                    }
                    EnableBanner(onStart = onOpenStatusBarCustom)
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PromoBannerCard(
                backgroundRes = R.drawable.img_bg_emoji_sticker,
                title = "Status Bar Stickers",
                body = "Animated stickers for your status bar!",
                cta = "Customize Now",
                onClick = onOpenSticker,
            )
            FakeAdCard()
            PromoBannerCard(
                backgroundRes = R.drawable.image_battery_troll_customize,
                title = "Battery Troll",
                body = "Just for fun, fake your battery % to everyone",
                cta = "Troll Mode",
                leadingIconRes = R.drawable.ic_battery_troll_customize_32,
                badge = "NEW",
                onClick = onOpenBatteryTroll,
            )
            Surface(
                onClick = onOpenStatusBarCustom,
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 3.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_btn_status_bar_new),
                        contentDescription = "Status Bar Customize",
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        "Status Bar Customize",
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF5C4B51),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallCustomizeCard(
                    title = "Notch",
                    iconRes = R.drawable.ic_item_notch,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenStatusBarCustom,
                )
                SmallCustomizeCard(
                    title = "Animation",
                    iconRes = R.drawable.ic_item_animation,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenStatusBarCustom,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF5C4B51))
                Text(
                    "Customize Icon",
                    color = Color(0xFF5C4B51),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF5C4B51))
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                gridEntries.forEach { entry ->
                    CustomizeIconGridItem(
                        entry = entry,
                        modifier = Modifier.fillMaxWidth(0.31f),
                        onClick = { onOpenFeature(entry) },
                    )
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PromoBannerCard(
    backgroundRes: Int,
    title: String,
    body: String,
    cta: String,
    onClick: () -> Unit,
    leadingIconRes: Int? = null,
    badge: String? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
        ) {
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (badge != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(bottomStart = 18.dp),
                    color = Color(0xFFFF6A00),
                ) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, top = 18.dp, bottom = 16.dp)
                    .fillMaxWidth(0.58f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    if (leadingIconRes != null) {
                        Image(
                            painter = painterResource(leadingIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Text(
                    body,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF53A3FF))
                        .border(BorderStroke(3.dp, Color.White), RoundedCornerShape(999.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    Text(
                        cta,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun FakeAdCard() {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFF4FE)),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFD06AFF)) {
                            Text("Ad", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("CapCut", color = Color(0xFFD06AFF), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    }
                    Text("Pangle Test Ads - 2", color = Color(0xFF8F8790))
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF4DB24A))
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("VIEW NOW", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun SmallCustomizeCard(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = title,
                modifier = Modifier.size(40.dp),
            )
            Text(
                title,
                color = Color(0xFF5C4B51),
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun CustomizeIconGridItem(
    entry: CustomizeEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(customizeIconRes(entry)),
                        contentDescription = customizeLabel(entry),
                        modifier = Modifier.size(54.dp),
                    )
                }
            }
            Text(
                customizeLabel(entry),
                textAlign = TextAlign.Center,
                color = Color(0xFF08162D),
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusBarCustomScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectTab: (StatusBarTab) -> Unit,
    onSelectBattery: (String) -> Unit,
    onSelectEmoji: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onSetStatusBarHeight: (Float) -> Unit,
    onSetLeftMargin: (Float) -> Unit,
    onSetRightMargin: (Float) -> Unit,
    onSetBatteryScale: (Float) -> Unit,
    onSetEmojiScale: (Float) -> Unit,
    onTogglePercentage: (Boolean) -> Unit,
    onToggleAnimate: (Boolean) -> Unit,
    onToggleStroke: (Boolean) -> Unit,
    onRestore: () -> Unit,
    onApply: () -> Unit,
    onAccessibilityChanged: (Boolean) -> Unit,
) {
    val config = uiState.editingConfig
    val batteryPresets = SampleCatalog.batteryPresets
    val emojiPresets = SampleCatalog.emojiPresets
    val themePresets = SampleCatalog.themePresets

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(onClick = onBack) {
                    Image(
                        painter = painterResource(R.drawable.ic_back_40_new),
                        contentDescription = "Back",
                        modifier = Modifier.size(40.dp),
                    )
                }
                Text(
                    "Status Bar Custom",
                    color = Color(0xFF5C4B51),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                )
            }
            PermissionBanner(enabled = uiState.accessibilityGranted, onToggle = onAccessibilityChanged)
            BatteryPreviewCard(uiState = uiState)
            OriginalStatusTabStrip(
                selected = uiState.activeStatusBarTab,
                onSelect = onSelectTab,
            )
            Surface(shape = RoundedCornerShape(22.dp), color = Color.White) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    StatusSliderRow("Status bar height", config.statusBarHeight, onSetStatusBarHeight)
                    StatusSliderRow("Status bar left margin", config.leftMargin, onSetLeftMargin)
                    StatusSliderRow("Status bar right margin", config.rightMargin, onSetRightMargin)
                }
            }
            Surface(shape = RoundedCornerShape(22.dp), color = Color.White) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    StatusColorRow("Status bar icon color", Color(config.accentColor))
                    StatusColorRow("Status bar background color", Color(config.backgroundColor))
                }
            }
            Surface(shape = RoundedCornerShape(22.dp), color = Color.White, onClick = { onSelectTab(StatusBarTab.Theme) }) {
                StatusChevronRow("More template")
            }
            Text(
                "Customize Icon",
                color = Color(0xFF08162D),
                style = MaterialTheme.typography.titleLarge,
            )
            when (uiState.activeStatusBarTab) {
                StatusBarTab.Battery -> StatusBarChoiceGrid(
                    labels = batteryPresets.map { it.name },
                    selectedLabel = batteryPresets.firstOrNull { it.id == config.batteryPresetId }?.name.orEmpty(),
                    onClick = { label ->
                        batteryPresets.firstOrNull { it.name == label }?.let { onSelectBattery(it.id) }
                    },
                    icon = { label ->
                        Text(
                            text = batteryPresets.first { it.name == label }.body,
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                    },
                )
                StatusBarTab.Emoji -> StatusBarChoiceGrid(
                    labels = emojiPresets.map { it.name },
                    selectedLabel = emojiPresets.firstOrNull { it.id == config.emojiPresetId }?.name.orEmpty(),
                    onClick = { label ->
                        emojiPresets.firstOrNull { it.name == label }?.let { onSelectEmoji(it.id) }
                    },
                    icon = { label ->
                        Text(
                            text = emojiPresets.first { it.name == label }.glyph,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    },
                )
                StatusBarTab.Theme -> StatusBarChoiceGrid(
                    labels = themePresets.map { it.name },
                    selectedLabel = themePresets.firstOrNull { it.id == config.themePresetId }?.name.orEmpty(),
                    onClick = { label ->
                        themePresets.firstOrNull { it.name == label }?.let { onSelectTheme(it.id) }
                    },
                    icon = { label ->
                        val preset = themePresets.first { it.name == label }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(preset.accent), Color(preset.background)),
                                    ),
                                ),
                        )
                    },
                )
                StatusBarTab.Settings -> Surface(shape = RoundedCornerShape(22.dp), color = Color.White) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        StatusSwitchRow("Show percentage", config.showPercentage, onTogglePercentage)
                        StatusSwitchRow("Animate charge", config.animateCharge, onToggleAnimate)
                        StatusSwitchRow("Show stroke", config.showStroke, onToggleStroke)
                        StatusSliderRow("Battery text size", config.batteryPercentScale, onSetBatteryScale)
                        StatusSliderRow("Emoji size", config.emojiScale, onSetEmojiScale)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onRestore, modifier = Modifier.weight(1f)) {
                    Text("Restore Applied")
                }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun LegacyBatteryScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectBattery: (String) -> Unit,
    onSelectEmoji: (String) -> Unit,
    onSetBatteryScale: (Float) -> Unit,
    onSetEmojiScale: (Float) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.editingConfig
    ScreenContainer(title = "Legacy Battery Flow", subtitle = "Port of the older `BatteryFragment` path that separately chooses battery body and emoji.") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BatteryPreviewCard(uiState = uiState)
            Text("Battery body", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SampleCatalog.batteryPresets) { preset ->
                    ChoiceChip(
                        label = preset.name,
                        selected = config.batteryPresetId == preset.id,
                        onClick = { onSelectBattery(preset.id) },
                    )
                }
            }
            Text("Emoji", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SampleCatalog.emojiPresets) { preset ->
                    ChoiceChip(
                        label = "${preset.glyph} ${preset.name}",
                        selected = config.emojiPresetId == preset.id,
                        onClick = { onSelectEmoji(preset.id) },
                    )
                }
            }
            SliderField("Battery percentage size", config.batteryPercentScale, 0.3f..1f, onSetBatteryScale)
            SliderField("Emoji size", config.emojiScale, 0.3f..1f, onSetEmojiScale)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) { Text("Apply Legacy Flow") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val query = uiState.searchQuery.trim()
    val templates = SampleCatalog.searchTemplates
    val results = if (query.isEmpty()) emptyList() else templates.filter { template ->
        template.title.contains(query, ignoreCase = true) ||
            template.summary.contains(query, ignoreCase = true) ||
            template.tags.any { it.contains(query, ignoreCase = true) }
    }

    Scaffold(containerColor = Color.White) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBack) {
                    Image(
                        painter = painterResource(R.drawable.ic_back_40_new),
                        contentDescription = "Back",
                        modifier = Modifier.size(40.dp),
                    )
                }
                Text(
                    "Search template",
                    color = Color(0xFF5C4B51),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8F8F8),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painter = painterResource(R.drawable.ic_home_search), contentDescription = null, modifier = Modifier.size(20.dp))
                    BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF5C4B51), fontSize = 16.sp),
                        decorationBox = { inner ->
                            if (uiState.searchQuery.isBlank()) {
                                Text("Search template", color = Color(0xFFB4A7AF))
                            }
                            inner()
                        },
                    )
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(20.dp)) {
                            Image(painter = painterResource(R.drawable.ic_search_clear), contentDescription = "Clear")
                        }
                    }
                }
            }
            if (query.isEmpty()) {
                Text("Most searched", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SampleCatalog.mostSearchedTags.forEach { tag ->
                        ChoiceChip(
                            label = tag,
                            selected = false,
                            onClick = { onQueryChange(tag) },
                        )
                    }
                }
                Text("Recommend", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    items(SampleCatalog.recommendedSearchTemplates) { template ->
                        SearchTemplateCard(
                            template = template,
                            onClick = { onNavigate(template.route) },
                        )
                    }
                }
            } else {
                Text("${results.size} result(s) for \"$query\"", color = Color(0xFF8D7680))
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    if (results.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Text(
                                    "No templates matched this keyword.",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    items(results) { template ->
                        SearchTemplateCard(
                            template = template,
                            onClick = { onNavigate(template.route) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchTemplateCard(
    template: SearchTemplate,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(template.glyph, style = MaterialTheme.typography.headlineSmall)
                    Column {
                        Text(template.title, fontWeight = FontWeight.SemiBold)
                        Text(template.summary, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (template.premium) {
                        Text("PRO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (template.animated) {
                        Text("GIF", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                template.tags.take(4).forEach { tag ->
                    AssistChip(onClick = onClick, label = { Text(tag) })
                }
            }
            Text(
                template.category,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun AchievementScreen(
    uiState: AppUiState,
    onClaim: (String) -> Unit,
    onNavigate: (String) -> Unit,
) {
    Scaffold(containerColor = Color(0xFFFEF5FA)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiState.achievements.forEach { task ->
                val completed = task.progress >= task.target
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold)
                        Text(task.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(
                            progress = { task.progress / task.target.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text("${task.progress}/${task.target}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val route = when (task.id) {
                                        "apply_status_bar" -> AppRoute.StatusBarCustom.route
                                        "save_sticker" -> AppRoute.EmojiSticker.route
                                        "gesture_mapper" -> AppRoute.Gesture.route
                                        else -> AppRoute.RealTime.route
                                    }
                                    onNavigate(route)
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Open")
                            }
                            Button(
                                onClick = { onClaim(task.id) },
                                enabled = completed && !task.claimed,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(if (task.claimed) "Claimed" else "Claim")
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                task.reward,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    uiState: AppUiState,
    onOpenLanguage: () -> Unit,
    onReplayTutorial: () -> Unit,
    onToggleProtection: (Boolean) -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onShareApp: () -> Unit,
    onOpenFeedback: () -> Unit,
    onRateApp: () -> Unit,
    onSelectRating: (Int) -> Unit,
    onCheckUpdate: () -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
) {
    Scaffold(
        containerColor = Color(0xFFFEF5FA),
        topBar = {
            SettingsTopBar(onStart = { onToggleAccessibility(true) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SettingsRow("Language", R.drawable.ic_language_settings, uiState.selectedLanguage, onOpenLanguage)
            SettingsRow("Not-Allowed Apps", R.drawable.ic_not_allow, if (uiState.protectFromRecentApps) "Protected" else null) {
                onToggleProtection(!uiState.protectFromRecentApps)
            }
            SettingsRow("Tutorial", R.drawable.ic_tutorials_new, "Permission and gesture guide", onReplayTutorial)
            SettingsRow("Privacy policy", R.drawable.ic_privacy_settings, null, onOpenPrivacy)
            SettingsRow("Terms & Conditions", R.drawable.ic_privacy_settings, null, onOpenTerms)
            SettingsRow("Feedback", R.drawable.ic_feed_back_setting, null, onOpenFeedback)
            SettingsRow("Share app", R.drawable.ic_share_app_settings, null, onShareApp)
            SettingsRow("Rate us", R.drawable.ic_rate_us_setting, if (uiState.ratingSelection > 0) "${uiState.ratingSelection}/5" else null, onRateApp)
            SettingsRow("Check for update", R.drawable.ic_check_update_settings, "Version: 1.2.9", onCheckUpdate)
        }
    }
}

@Composable
private fun RealTimeScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onApply: () -> Unit,
) {
    val selected = SampleCatalog.realTimeTemplates.first { it.id == uiState.selectedRealTimeTemplateId }
    ScreenContainer(title = "Real Time", subtitle = "Template list, preview, and apply flow.") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TemplatePreviewCard(
                title = selected.title,
                summary = selected.summary,
                glyph = selected.accentGlyph,
                tag = selected.tag,
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    if (uiState.premiumUnlocked || uiState.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_PREMIUM_REALTIME_CAT_DIARY)) {
                        "Premium template access is available for this account state."
                    } else {
                        "Templates marked PRO open a paywall unless unlocked by reward or premium access."
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PermissionBanner(enabled = uiState.accessibilityGranted, onToggle = onToggleAccessibility)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SampleCatalog.realTimeTemplates) { template ->
                    ContentTemplateCard(
                        template = template,
                        selected = template.id == uiState.selectedRealTimeTemplateId,
                        onClick = { onSelectTemplate(template.id) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Back")
                }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text("Apply Template")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BatteryTrollScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onSetMessage: (String) -> Unit,
    onToggleAutoDrop: (Boolean) -> Unit,
    onRefreshBatteryTrollCatalog: () -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onApply: () -> Unit,
    onTurnOff: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onRefreshBatteryTrollCatalog()
    }

    val templateLibrary = if (uiState.batteryTrollCatalogRemote.isNotEmpty()) {
        uiState.batteryTrollCatalogRemote
    } else {
        SampleCatalog.batteryTrollTemplates
    }
    val selected = uiState.batteryTrollTemplateForId(uiState.selectedBatteryTrollTemplateId)
        ?: SampleCatalog.batteryTrollTemplates.first()
    val chipMessages = buildList {
        addAll(SampleCatalog.trollMessageOptions)
        templateLibrary.forEach { t ->
            if (t.prankMessage.isNotBlank() && t.prankMessage !in this) add(t.prankMessage)
        }
        if (uiState.trollMessage.isNotBlank() && uiState.trollMessage !in this) add(uiState.trollMessage)
    }
    val trollScroll = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(trollScroll)
                .padding(horizontal = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_40_new),
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(40.dp)
                        .clickable(onClick = onBack),
                )
            }
            BatteryTrollPreviewCard(
                template = selected,
                trollMessage = uiState.trollMessage,
                overlayEnabled = uiState.trollOverlayEnabled,
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_battery_troll_customize_32),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        "Pick a fake battery label and apply the overlay when accessibility is enabled.",
                        color = Color(0xFF5C4B51),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            PermissionBanner(
                enabled = uiState.accessibilityGranted,
                onToggle = onToggleAccessibility,
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Troll templates",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFE5FC),
                            modifier = Modifier.clickable { },
                        ) {
                            Text(
                                "Tutorial",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color(0xFF5C4B51),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                    if (uiState.batteryTrollCatalogLoading && uiState.batteryTrollCatalogRemote.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val loadingComposition by rememberLottieComposition(
                                LottieCompositionSpec.Asset("cute_loading.json"),
                            )
                            LottieAnimation(
                                composition = loadingComposition,
                                iterations = LottieConstants.IterateForever,
                                speed = 2f,
                                modifier = Modifier.size(120.dp),
                            )
                            Text(
                                "Loading…",
                                color = Color(0xFF5C4B51),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            templateLibrary.chunked(4).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    row.forEach { template ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            TrollTemplateCard(
                                                template = template,
                                                selected = template.id == uiState.selectedBatteryTrollTemplateId,
                                                onClick = { onSelectTemplate(template.id) },
                                            )
                                        }
                                    }
                                    repeat(4 - row.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Customize",
                        color = Color(0xFF5C4B51),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        chipMessages.forEach { option ->
                            ChoiceChip(
                                label = option,
                                selected = option == uiState.trollMessage,
                                onClick = { onSetMessage(option) },
                            )
                        }
                    }
                    SettingToggle("Auto drop animation", uiState.trollAutoDrop, onToggleAutoDrop)
                    HorizontalDivider(thickness = 1.dp, color = Color.Black)
                    Text(
                        "Fake label preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5C4B51),
                    )
                    Text(
                        "Fake ${uiState.trollMessage}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF5C4B51),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFFFFE5FC),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable(onClick = onTurnOff),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_turn_off_shimeji),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Turn Off",
                                    color = Color(0xFFD47DFE),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFABE5), Color(0xFFD47DFE)),
                                    ),
                                )
                                .clickable(onClick = onApply),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Save",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeatureDetailScreen(
    entry: CustomizeEntry,
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[entry] ?: FeatureConfig(variant = SampleCatalog.featureVariants[entry]?.first().orEmpty())
    val variants = SampleCatalog.featureVariants[entry].orEmpty()
    ScreenContainer(title = entry.title, subtitle = "Isolated editor with local config, variants, and apply/reset actions.") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TemplatePreviewCard(
                title = entry.title,
                summary = entry.subtitle,
                glyph = featureGlyph(entry),
                tag = if (config.enabled) config.variant else "Disabled",
            )
            SettingToggle("Enable ${entry.title}", config.enabled, onToggleEnabled)
            SliderField("${entry.title} intensity", config.intensity, 0.1f..1f, onSetIntensity)
            Text("Variants", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                variants.forEach { variant ->
                    ChoiceChip(
                        label = variant,
                        selected = variant == config.variant,
                        onClick = { onSelectVariant(variant) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Back")
                }
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                    Text("Reset")
                }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun HomeRoundIcon(
    iconRes: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun EnableBanner(
    onStart: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFFFE9FA),
        border = BorderStroke(2.dp, Color(0xFFE88EEA)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Enable emoji battery to begin", color = Color(0xFF5F4B54), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFE88EEA)),
            ) {
                TextButton(onClick = onStart) {
                    Text("Start", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun HomeBatteryGridCard(
    item: dev.hai.emojibattery.model.HomeBatteryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 7.dp, vertical = 7.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(color = Color(0xFFFCFCFC), shape = RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = Color(0xFFFFE5FC), shape = RoundedCornerShape(16.dp))
        ) {
            if (!item.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Image(
                    painter = painterResource(item.previewRes),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                )
            }
            if (item.premium) {
                Image(
                    painter = painterResource(R.drawable.ic_diamond),
                    contentDescription = "Premium",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp),
                )
            }
            if (item.animated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                ) {
                    Text(
                        "GIF",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFF5C4B51),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun OriginalStatusTabStrip(
    selected: StatusBarTab,
    onSelect: (StatusBarTab) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(StatusBarTab.entries) { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) Color(0xFFD47DFE) else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    tab.title,
                    color = if (isSelected) Color.White else Color(0xFFD47DFE),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StatusSliderRow(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(title, color = Color(0xFF111013), style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
            )
            Text("${(value * 100).toInt()}", color = Color.Black, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun StatusColorRow(
    title: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = Color(0xFF111013), style = MaterialTheme.typography.titleSmall)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color(0xFFA1A1A1), CircleShape),
        )
    }
}

@Composable
private fun StatusChevronRow(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = Color(0xFF111013), style = MaterialTheme.typography.titleSmall)
        Image(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusBarChoiceGrid(
    labels: List<String>,
    selectedLabel: String,
    onClick: (String) -> Unit,
    icon: @Composable (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        maxItemsInEachRow = 3,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        labels.forEach { label ->
            val selected = selectedLabel.equals(label, ignoreCase = true)
            Surface(
                onClick = { onClick(label) },
                modifier = Modifier.fillMaxWidth(0.31f),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(2.dp, if (selected) Color(0xFFD47DFE) else Color(0xFFF2E4ED)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    icon(label)
                    Text(
                        label,
                        color = Color(0xFF5C4B51),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusSwitchRow(
    title: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = Color(0xFF111013), style = MaterialTheme.typography.titleSmall)
        IconButton(onClick = { onToggle(!enabled) }) {
            Image(
                painter = painterResource(
                    if (enabled) R.drawable.ic_switch_button_enabled else R.drawable.ic_switch_button_disable,
                ),
                contentDescription = null,
                modifier = Modifier.size(width = 40.dp, height = 20.dp),
            )
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    iconRes: Int,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = Color(0xFF5F4B54))
                    if (subtitle != null) {
                        Text(subtitle, color = Color(0xFF8D7680), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Image(
                painter = painterResource(R.drawable.ic_end_setting),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedbackScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectRating: (Int) -> Unit,
    onToggleReason: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFFF6A2D8), Color(0xFFB765F5)))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.48f))
            .padding(horizontal = 18.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.size(40.dp))
                    Text("Feedback", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5F4B54))
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF6A5961)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("×", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val reasons = listOf(
                        "5 stars" to "⭐",
                        "Can't allow permission" to null,
                        "Feature error" to null,
                        "I can't close the ads" to null,
                        "I can't exit the app" to null,
                        "I can't navigate to the next screen" to null,
                    )
                    reasons.forEachIndexed { index, (label, icon) ->
                        val selected = if (index == 0) uiState.ratingSelection == 5 else uiState.feedbackReasons.contains(SampleCatalog.feedbackReasons.getOrNull((index - 1).coerceAtLeast(0))?.id)
                        Surface(
                            onClick = {
                                if (index == 0) onSelectRating(5) else SampleCatalog.feedbackReasons.getOrNull((index - 1).coerceAtLeast(0))?.let { onToggleReason(it.id) }
                            },
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFFF8F8F8),
                            border = BorderStroke(1.dp, if (selected) Color(0xFFE9A8EC) else Color.Transparent),
                        ) {
                            Text(
                                buildString {
                                    if (icon != null) append("$icon ")
                                    append(label)
                                },
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                color = Color(0xFF5F4B54),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Text(
                    "Do you have any additional feedback for us?\nWe're Listening.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5F4B54),
                )
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF8F8F8),
                ) {
                    OutlinedTextField(
                        value = uiState.feedbackNote,
                        onValueChange = onNoteChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        minLines = 5,
                        placeholder = { Text("Please describe your issue in detail.", color = Color(0xFFD4CFD5)) },
                    )
                }
                if (uiState.lastFeedbackSubmitted) {
                    Text("Your feedback was successfully submitted", color = Color(0xFF17A398), fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(gradient),
                ) {
                    TextButton(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
                        Text("Submit", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun OriginalTopShell(
    title: String,
    onLeftPrimary: () -> Unit,
    onLeftSecondary: () -> Unit,
    onSearch: () -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    HomeRoundIcon(R.drawable.ic_settings_new, onLeftPrimary)
                    HomeRoundIcon(R.drawable.ic_feeb_back_home, onLeftSecondary)
                }
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5C4B51))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    HomeRoundIcon(R.drawable.ic_home_search, onSearch)
                    Image(
                        painter = painterResource(R.drawable.no_ads_on),
                        contentDescription = "Ads on",
                        modifier = Modifier.size(width = 40.dp, height = 36.dp),
                    )
                }
            }
            EnableBanner(onStart = onSearch)
        }
    }
}

@Composable
private fun SettingsTopBar(
    onStart: () -> Unit,
) {
    Surface(color = Color.White, shadowElevation = 4.dp, shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_40_new),
                    contentDescription = "Back",
                    modifier = Modifier.size(40.dp),
                )
                Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5F4B54))
                Spacer(Modifier.size(40.dp))
            }
            EnableBanner(onStart = onStart)
        }
    }
}

@Composable
private fun GestureSwitchRow(
    iconRes: Int,
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painter = painterResource(iconRes), contentDescription = title, modifier = Modifier.size(32.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Color(0xFF08162D), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(description, color = Color(0xFF08162D), style = MaterialTheme.typography.bodyMedium)
            }
        }
        IconButton(onClick = { onToggle(!enabled) }) {
            Image(
                painter = painterResource(
                    if (enabled) R.drawable.ic_switch_button_enabled
                    else R.drawable.ic_switch_button_disable,
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 40.dp, height = 20.dp)
                    .graphicsLayer { alpha = 1f },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GestureSelectionRow(
    title: String,
    description: String,
    selectedAction: GestureAction,
    onSelectAction: (GestureAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, color = Color(0xFF08162D), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
        Text(description, color = Color(0xFF08162D), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GestureAction.entries.forEach { action ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF8F8F8))
                        .clickable { onSelectAction(action) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(
                            if (action == selectedAction) R.drawable.ic_radio_gesture_selected
                            else R.drawable.ic_radio_gesture_unselected,
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(action.title, color = Color(0xFF5C4B51), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}


@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
) {
    ScreenContainer(title = title, subtitle = subtitle) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = subtitle,
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BatteryTabContent(
    selectedId: String,
    presets: List<BatteryPreset>,
    batteryScale: Float,
    showPercentage: Boolean,
    showStroke: Boolean,
    onSelectBattery: (String) -> Unit,
    onSetBatteryScale: (Float) -> Unit,
    onTogglePercentage: (Boolean) -> Unit,
    onToggleStroke: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Battery Body", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets) { preset ->
                ChoiceChip(
                    label = "${preset.body} ${preset.name}",
                    selected = selectedId == preset.id,
                    onClick = { onSelectBattery(preset.id) },
                )
            }
        }
        SliderField("Percentage scale", batteryScale, 0.3f..1f, onSetBatteryScale)
        SettingToggle("Show percentage", showPercentage, onTogglePercentage)
        SettingToggle("Show stroke", showStroke, onToggleStroke)
    }
}

@Composable
private fun EmojiTabContent(
    selectedId: String,
    presets: List<EmojiPreset>,
    emojiScale: Float,
    animateCharge: Boolean,
    onSelectEmoji: (String) -> Unit,
    onSetEmojiScale: (Float) -> Unit,
    onToggleAnimate: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Emoji Battery", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets) { preset ->
                ChoiceChip(
                    label = "${preset.glyph} ${preset.name}",
                    selected = selectedId == preset.id,
                    onClick = { onSelectEmoji(preset.id) },
                )
            }
        }
        SliderField("Emoji size", emojiScale, 0.3f..1f, onSetEmojiScale)
        SettingToggle("Animate charge", animateCharge, onToggleAnimate)
    }
}

@Composable
private fun ThemeTabContent(
    selectedId: String,
    presets: List<ThemePreset>,
    onSelectTheme: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Theme Templates", style = MaterialTheme.typography.titleMedium)
        presets.forEach { preset ->
            Card(onClick = { onSelectTheme(preset.id) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(preset.accent)),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(preset.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (selectedId == preset.id) "Active template" else "Tap to load accent and surface colors",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTabContent(
    showPercentage: Boolean,
    animateCharge: Boolean,
    showStroke: Boolean,
    onTogglePercentage: (Boolean) -> Unit,
    onToggleAnimate: (Boolean) -> Unit,
    onToggleStroke: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingToggle("Show percentage", showPercentage, onTogglePercentage)
        SettingToggle("Animate charge", animateCharge, onToggleAnimate)
        SettingToggle("Show stroke", showStroke, onToggleStroke)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                "In the original app this tab is part of `StatusBarCustomFragment` and updates the same shared `BatteryConfig` preview state.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BatteryPreviewCard(
    uiState: AppUiState,
) {
    val config = uiState.editingConfig
    val battery = SampleCatalog.batteryPresets.first { it.id == config.batteryPresetId }
    val emoji = SampleCatalog.emojiPresets.first { it.id == config.emojiPresetId }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(config.backgroundColor)),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Live Preview", fontWeight = FontWeight.SemiBold, color = Color(config.accentColor))
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.55f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("12:45", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "Fri, Mar 20",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("WIFI", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("▰▰▰▱", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${battery.body} ${if (config.showPercentage) "56%" else ""}".trim(),
                                color = Color(config.accentColor),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (uiState.accessibilityGranted) "Accessibility bridge active" else "Accessibility bridge required for apply",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            emoji.glyph,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StickerMediaPreview(
    sticker: StickerPreset,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            sticker.lottieUrl != null -> {
                val composition by rememberLottieComposition(LottieCompositionSpec.Url(sticker.lottieUrl))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            sticker.thumbnailUrl != null -> {
                AsyncImage(
                    model = sticker.thumbnailUrl,
                    contentDescription = sticker.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            else -> {
                Text(
                    sticker.glyph,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        }
    }
}

@Composable
private fun StickerPreviewCard(
    selectedSticker: StickerPreset?,
    selectedPlacement: StickerPlacement?,
    overlayEnabled: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE5C7D2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Sticker Preview",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5C4B51),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                if (overlayEnabled) "Overlay active" else "Overlay inactive",
                color = Color(0xFF5C4B51),
                style = MaterialTheme.typography.bodySmall,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F8F8)),
            ) {
                if (selectedSticker != null) {
                    StickerMediaPreview(
                        selectedSticker,
                        Modifier
                            .align(Alignment.Center)
                            .padding(top = ((1f - (selectedPlacement?.speed ?: 0.5f)) * 24f).dp)
                            .fillMaxSize()
                            .padding(12.dp),
                    )
                } else {
                    Text(
                        text = "✨",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color(0xFF5C4B51),
                    )
                }
            }
            if (selectedSticker != null && selectedPlacement != null) {
                Text(
                    "${selectedSticker.name}  •  size ${(selectedPlacement.size * 100).toInt()}%  •  speed ${(selectedPlacement.speed * 100).toInt()}%",
                    color = Color(0xFF5C4B51),
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text("Pick a sticker to preview it here.", color = Color(0xFF5C4B51), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StickerCatalogCard(
    sticker: StickerPreset,
    selected: Boolean,
    added: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        border = if (selected) {
            BorderStroke(1.dp, Color(0xFFD47DFE))
        } else {
            BorderStroke(0.5.dp, Color(0xFFE0E0E0))
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            StickerMediaPreview(
                sticker,
                Modifier
                    .fillMaxSize()
                    .padding(6.dp),
            )
            if (added) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.08f)),
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (sticker.premium) {
                    Image(
                        painter = painterResource(R.drawable.ic_diamond),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                    )
                }
                if (sticker.animated) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x33FFFFFF),
                    ) {
                        Text(
                            "GIF",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5C4B51),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddedStickerChip(
    sticker: StickerPreset,
    selected: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFFFE5FC) else Color(0xFFF8F8F8),
        ),
        border = if (selected) BorderStroke(1.dp, Color(0xFFD47DFE)) else null,
        onClick = onSelect,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (sticker.thumbnailUrl != null) {
                AsyncImage(
                    model = sticker.thumbnailUrl,
                    contentDescription = sticker.name,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(sticker.glyph)
            }
            Text(sticker.name, color = Color(0xFF5C4B51), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onRemove) { Text("×", color = Color(0xFF5C4B51)) }
        }
    }
}

@Composable
private fun TemplatePreviewCard(
    title: String,
    summary: String,
    glyph: String,
    tag: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)),
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(glyph, style = MaterialTheme.typography.displayMedium)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AssistChip(onClick = {}, label = { Text(tag) })
            }
        }
    }
}

@Composable
private fun ContentTemplateCard(
    template: ContentTemplate,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.size(width = 168.dp, height = 150.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(template.accentGlyph, style = MaterialTheme.typography.headlineMedium)
                if (template.premium) {
                    Text("PRO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(template.title, fontWeight = FontWeight.SemiBold)
                Text(template.summary, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Text(template.tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TrollMediaPreview(
    template: BatteryTrollTemplate,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            template.lottieUrl != null -> {
                val composition by rememberLottieComposition(LottieCompositionSpec.Url(template.lottieUrl))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            template.thumbnailUrl != null -> {
                AsyncImage(
                    model = template.thumbnailUrl,
                    contentDescription = template.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            else -> {
                Text(
                    template.accentGlyph,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        }
    }
}

@Composable
private fun BatteryTrollPreviewCard(
    template: BatteryTrollTemplate,
    trollMessage: String,
    overlayEnabled: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE5C7D2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Battery Troll preview",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5C4B51),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                if (overlayEnabled) "Overlay active" else "Overlay inactive",
                color = Color(0xFF5C4B51),
                style = MaterialTheme.typography.bodySmall,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F8F8)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TrollMediaPreview(
                        template,
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    )
                    Text(
                        text = "Fake $trollMessage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5C4B51),
                    )
                }
            }
            Text(
                "${template.title} • ${template.prankMessage}",
                color = Color(0xFF5C4B51),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun TrollTemplateCard(
    template: BatteryTrollTemplate,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        border = if (selected) {
            BorderStroke(1.dp, Color(0xFFD47DFE))
        } else {
            BorderStroke(0.5.dp, Color(0xFFE0E0E0))
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TrollMediaPreview(template, Modifier.fillMaxSize())
                }
                Text(
                    template.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF5C4B51),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (template.premium) {
                    Image(
                        painter = painterResource(R.drawable.ic_diamond),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Accessibility Bridge", fontWeight = FontWeight.SemiBold)
                Text(
                    if (enabled) "Overlay service is active and ready to apply." else "Open Accessibility Settings to enable the overlay service.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun TabStrip(
    tabs: List<StatusBarTab>,
    selected: StatusBarTab,
    onSelect: (StatusBarTab) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tabs) { tab ->
            FilterChip(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                label = { Text(tab.title) },
            )
        }
    }
}

@Composable
private fun SettingToggle(
    label: String,
    value: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label)
            Switch(checked = value, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun SliderField(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text("${(value * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(value = value, onValueChange = onChange, valueRange = range)
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    AssistChip(onClick = onClick, label = { Text(label) }, leadingIcon = if (selected) {
        {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    } else {
        null
    })
}

@Composable
private fun HeroCard(
    title: String,
    body: String,
    cta: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onClick) { Text(cta) }
        }
    }
}

@Composable
private fun SmallActionCard(
    title: String,
    subtitle: String,
    glyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(glyph, style = MaterialTheme.typography.headlineSmall)
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContainer(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun FeatureTileCard(
    entry: CustomizeEntry,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.size(width = 164.dp, height = 92.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(featureGlyph(entry))
                }
            }
            Column {
                Text(entry.title, fontWeight = FontWeight.SemiBold)
                Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun featureGlyph(entry: CustomizeEntry): String = when (entry) {
    CustomizeEntry.Wifi -> "📶"
    CustomizeEntry.Data -> "📡"
    CustomizeEntry.Signal -> "📳"
    CustomizeEntry.Airplane -> "✈"
    CustomizeEntry.Hotspot -> "🛜"
    CustomizeEntry.Ringer -> "🔔"
    CustomizeEntry.Charge -> "🔋"
    CustomizeEntry.Emotion -> "😊"
    CustomizeEntry.DateTime -> "🕒"
    CustomizeEntry.Theme -> "🎨"
    CustomizeEntry.Settings -> "⚙"
}

private fun customizeLabel(entry: CustomizeEntry): String = when (entry) {
    CustomizeEntry.Emotion -> "Emotion"
    CustomizeEntry.Charge -> "Charge"
    else -> entry.title
}

private fun customizeIconRes(entry: CustomizeEntry): Int = when (entry) {
    CustomizeEntry.Emotion -> R.drawable.ic_item_emotion
    CustomizeEntry.Wifi -> R.drawable.ic_item_wifi
    CustomizeEntry.Data -> R.drawable.ic_item_data
    CustomizeEntry.Signal -> R.drawable.ic_item_signal
    CustomizeEntry.Airplane -> R.drawable.ic_item_airplane
    CustomizeEntry.Hotspot -> R.drawable.ic_item_hotspot
    CustomizeEntry.Ringer -> R.drawable.ic_item_ringer
    CustomizeEntry.Charge -> R.drawable.ic_item_charge
    CustomizeEntry.DateTime -> R.drawable.ic_item_date_time
    CustomizeEntry.Theme -> R.drawable.img_btn_status_bar_new
    CustomizeEntry.Settings -> R.drawable.ic_item_animation
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (AppRoute, MainSection) -> Unit,
) {
    val items = listOf(
        Triple(AppRoute.Home, MainSection.Home, Icons.Rounded.Home),
        Triple(AppRoute.Customize, MainSection.Customize, Icons.Rounded.AutoAwesome),
        Triple(AppRoute.Gesture, MainSection.Gesture, Icons.Rounded.TouchApp),
        Triple(AppRoute.Achievement, MainSection.Achievement, Icons.Rounded.EmojiEvents),
    )
    NavigationBar {
        items.forEach { (route, section, icon) ->
            NavigationBarItem(
                selected = currentRoute == route.route,
                onClick = { onNavigate(route, section) },
                icon = { Icon(icon, contentDescription = section.title) },
                label = { Text(section.title) },
            )
        }
    }
}
