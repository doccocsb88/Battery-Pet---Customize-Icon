package dev.hai.emojibattery.app

import dev.hai.emojibattery.app.screens.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import dev.hai.emojibattery.billing.GooglePlayPurchaseService
import dev.hai.emojibattery.billing.PurchaseService
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.MainSection
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.paywall.LegalWebViewScreen
import dev.hai.emojibattery.paywall.PaywallScreen
import dev.hai.emojibattery.service.AccessibilityBridge
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore
import dev.hai.emojibattery.ui.navigation.AppRoute

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
                    onFinish = {
                        viewModel.finishSplash()
                        val latest = viewModel.uiState.value
                        val nextRoute = when {
                            !latest.languageChosen -> AppRoute.Language.route
                            !latest.onboardingCompleted -> AppRoute.Onboarding.route
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
                        val latest = viewModel.uiState.value
                        val nextRoute = if (latest.onboardingCompleted) AppRoute.Home.route else AppRoute.Onboarding.route
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
                    onBack = { navController.popBackStack() },
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
                    onPurchase = { productId, offerToken ->
                        rawContext.findActivity()?.let { activity ->
                            purchaseService.purchase(activity, productId, offerToken)
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
                    onOpenTutorial = {
                        viewModel.replayTutorial()
                        navController.navigate(AppRoute.Tutorial.route)
                    },
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
                    onOpenTutorial = {
                        viewModel.replayTutorial()
                        navController.navigate(AppRoute.Tutorial.route)
                    },
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
        if (uiState.paywallState == null) return@LaunchedEffect
        if (route == AppRoute.Paywall.route) return@LaunchedEffect
        // Paywall → Terms/Privacy uses legal/*; do not force paywall again or the legal screen flashes closed.
        if (route != null && route.startsWith("legal/")) return@LaunchedEffect
        navController.navigate(AppRoute.Paywall.route)
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
