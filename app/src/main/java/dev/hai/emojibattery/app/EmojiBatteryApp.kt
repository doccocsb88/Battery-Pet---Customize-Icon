package dev.hai.emojibattery.app

import dev.hai.emojibattery.ui.screen.*

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.util.Log
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
import dev.hai.emojibattery.model.StatusBarTab
import dev.hai.emojibattery.paywall.LegalWebViewScreen
import dev.hai.emojibattery.paywall.PaywallScreen
import dev.hai.emojibattery.service.AccessibilityBridge
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore
import dev.hai.emojibattery.locale.AppLanguageConfig
import dev.hai.emojibattery.ui.accessibility.AccessibilityServiceUsageDialog
import dev.hai.emojibattery.ui.navigation.AppRoute
import co.q7labs.co.emoji.R
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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val showBottomBar = route in setOf(
        AppRoute.Home.route,
        AppRoute.Customize.route,
        AppRoute.Settings.route,
    )
    var showAccessibilityConsent by remember { mutableStateOf(false) }
    var transientSuccessMessage by remember { mutableStateOf<String?>(null) }
    var successToastResetToken by remember { mutableStateOf(0L) }
    val onSetOverlayEnabled: (Boolean) -> Unit = { enabled ->
        viewModel.setStatusBarOverlayEnabled(enabled)
        OverlayConfigStore.setStatusBarEnabled(context, enabled)
        if (AccessibilityBridge.isEnabled(context)) {
            OverlayAccessibilityService.requestRefresh(context)
        }
    }

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

    LaunchedEffect(uiState.applyMessage) {
        val message = uiState.applyMessage ?: return@LaunchedEffect
        transientSuccessMessage = message
        successToastResetToken += 1L
        viewModel.clearApplyMessage()
    }

    LaunchedEffect(successToastResetToken) {
        val message = transientSuccessMessage ?: return@LaunchedEffect
        delay(2000)
        if (transientSuccessMessage == message) {
            transientSuccessMessage = null
        }
    }

    Scaffold(
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
        val navHostContainerModifier = if (route == null || route == AppRoute.Splash.route) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .padding(padding)
                .fillMaxSize()
        }
        Box(navHostContainerModifier) {
            NavHost(
                navController = navController,
                startDestination = initialRoute ?: AppRoute.Splash.route,
                modifier = Modifier.fillMaxSize(),
            ) {
            composable(AppRoute.Splash.route) {
                SplashRoute(
                    fastForward = uiState.splashDone,
                    onFinish = {
                        if (!uiState.splashDone) {
                            viewModel.finishSplash()
                        }
                        val latest = viewModel.uiState.value
                        val nextRoute = when {
                            AppLanguageConfig.isLanguagePickerFlowEnabled && !latest.languageChosen ->
                                AppRoute.Language.route
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
                    selectedLocaleTag = uiState.selectedLocaleTag,
                    onSelectLocaleTag = viewModel::selectLocaleForLanguagePicker,
                    onNext = {
                        val localeChanged = viewModel.confirmLanguageSelection()
                        if (!localeChanged) {
                            if (viewModel.uiState.value.onboardingCompleted) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(AppRoute.Onboarding.route) {
                                    popUpTo(AppRoute.Language.route) { inclusive = true }
                                }
                            }
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
                        showAccessibilityConsent = true
                    },
                )
            }
            composable(AppRoute.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onSelectCategory = viewModel::selectHomeCategory,
                    onOpenAccessibility = { showAccessibilityConsent = true },
                    onOpenStatusBarCustom = { selectedItem ->
                        viewModel.selectMainSection(MainSection.Home)
                        viewModel.selectStatusTab(StatusBarTab.Battery)
                        if (selectedItem != null) {
                            viewModel.stageStatusBarSelectionFromHome(
                                categoryId = selectedItem.categoryId,
                                selectedItemId = selectedItem.id,
                            )
                        }
                        navController.navigate(AppRoute.StatusBarCustom.route)
                    },
                    onOpenLegacyBattery = { navController.navigate(AppRoute.LegacyBattery.route) },
                    onOpenSearch = { navController.navigate(AppRoute.Search.route) },
                    onOpenSticker = { navController.navigate(AppRoute.EmojiSticker.route) },
                    onOpenBatteryTroll = { navController.navigate(AppRoute.BatteryTroll.route) },
                    onOpenFeedback = { navController.navigate(AppRoute.Feedback.route) },
                    onOpenPremium = viewModel::openStore,
                    onSetOverlayEnabled = onSetOverlayEnabled,
                )
            }
            composable(AppRoute.Customize.route) {
                CustomizeHubScreen(
                    uiState = uiState,
                    onOpenSticker = { navController.navigate(AppRoute.EmojiSticker.route) },
                    onOpenFeature = { entry ->
                        when (entry) {
                            CustomizeEntry.Theme,
                            CustomizeEntry.Settings,
                            -> {
                                customizeEntryToStatusBarTab(entry)?.let { viewModel.selectStatusTab(it) }
                                navController.navigate(AppRoute.StatusBarCustom.route)
                            }

                            else -> navController.navigate(AppRoute.FeatureDetail.create(entry.title))
                        }
                    },
                    onOpenStatusBarCustom = {
                        viewModel.selectStatusTab(StatusBarTab.Battery)
                        navController.navigate(AppRoute.StatusBarCustom.route)
                    },
                    onOpenAccessibility = { showAccessibilityConsent = true },
                    onOpenSearch = { navController.navigate(AppRoute.Search.route) },
                    onOpenNotch = { navController.navigate(AppRoute.Notch.route) },
                    onOpenAnimation = { navController.navigate(AppRoute.Animation.route) },
                    onOpenFeedback = { navController.navigate(AppRoute.Feedback.route) },
                    onOpenBatteryTroll = { navController.navigate(AppRoute.BatteryTroll.route) },
                    onOpenPremium = viewModel::openStore,
                    onSetOverlayEnabled = onSetOverlayEnabled,
                )
            }
            composable(AppRoute.Notch.route) {
                NotchScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppRoute.Animation.route) { entry ->
                val selectedFromList = entry.savedStateHandle.get<Int>("animation_selected_id")
                AnimationScreen(
                    onBack = { navController.popBackStack() },
                    selectedFromList = selectedFromList,
                    onConsumeListSelection = { entry.savedStateHandle.remove<Int>("animation_selected_id") },
                    onOpenAnimationList = { selectedId ->
                        navController.navigate(AppRoute.AnimationList.create(selectedId))
                    },
                    onApply = { enabled, sizePercent, offsetX, templateId ->
                        val accessibilityEnabled = AccessibilityBridge.isEnabled(context)
                        Log.d(
                            "AnimationApply",
                            "apply clicked enabled=$enabled sizePercent=$sizePercent offsetX=$offsetX templateId=$templateId accessibility=$accessibilityEnabled",
                        )
                        viewModel.syncAccessibilityGranted(accessibilityEnabled)
                        OverlayConfigStore.saveAnimationPrefs(
                            context = context,
                            enabled = enabled,
                            sizePercent = sizePercent,
                            offsetX = offsetX,
                            templateId = templateId,
                        )
                        if (accessibilityEnabled) {
                            OverlayAccessibilityService.requestRefresh(context)
                            viewModel.postInfoMessage(rawContext.getString(R.string.animation_apply_success))
                        } else {
                            showAccessibilityConsent = true
                            viewModel.postInfoMessage(rawContext.getString(R.string.accessibility_bridge_required_short))
                        }
                    },
                )
            }
            composable(
                route = AppRoute.AnimationList.route,
                arguments = listOf(navArgument("selectedId") { type = NavType.IntType }),
            ) { entry ->
                val selectedId = entry.arguments?.getInt("selectedId") ?: 0
                AnimationListScreen(
                    selectedId = selectedId,
                    onBack = { navController.popBackStack() },
                    onSelect = { id ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("animation_selected_id", id)
                        navController.popBackStack()
                    },
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
                LaunchedEffect(Unit) {
                    viewModel.loadStatusBarCatalog()
                }
                StatusBarCustomScreen(
                    uiState = uiState,
                    onBack = {
                        viewModel.clearStagedStatusBarSelectionFromHome()
                        navController.popBackStack()
                    },
                    onSelectTab = viewModel::selectStatusTab,
                    onSelectBattery = viewModel::selectBatteryPreset,
                    onSelectEmoji = viewModel::selectEmojiPreset,
                    onSelectTheme = viewModel::selectTheme,
                    onViewMoreBatteryChoices = { navController.navigate(AppRoute.StatusBarBatteryList.route) },
                    onViewMoreEmojiChoices = { navController.navigate(AppRoute.StatusBarEmojiList.route) },
                    onSetThemeBackgroundColor = viewModel::setThemeBackgroundColor,
                    onSetBackgroundTemplatePhoto = viewModel::setBackgroundTemplatePhoto,
                    onViewMoreBackgroundTemplates = {
                        navController.navigate(AppRoute.BackgroundTemplateList.route)
                    },
                    onSetAccentColor = viewModel::setAccentColor,
                    onSetStatusBarHeight = viewModel::setStatusBarHeight,
                    onSetLeftMargin = viewModel::setStatusBarLeftMargin,
                    onSetRightMargin = viewModel::setStatusBarRightMargin,
                    onSetBatteryScale = viewModel::setBatteryPercentScale,
                    onSetEmojiScale = viewModel::setEmojiScale,
                    onSetEmojiAdjustmentScale = viewModel::setEmojiAdjustmentScale,
                    onSetEmojiOffset = viewModel::setEmojiOffset,
                    onTogglePercentage = viewModel::setShowPercentage,
                    onToggleAnimate = viewModel::setAnimateCharge,
                    onToggleStroke = viewModel::setShowStroke,
                    onRestore = viewModel::restoreApplied,
                    onApply = {
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        viewModel.applyConfig()
                        if (AccessibilityBridge.isEnabled(context)) {
                            val snapshot = viewModel.uiState.value
                            OverlayConfigStore.saveStatusBarConfig(
                                context,
                                snapshot.editingConfig,
                                snapshot.statusBarCatalogItems,
                            )
                            OverlayConfigStore.setBatteryEmojiSource(
                                context,
                                OverlayConfigStore.BATTERY_EMOJI_SOURCE_STATUS_BAR_CUSTOM,
                            )
                            OverlayAccessibilityService.requestRefresh(context)
                        }
                    },
                    onAccessibilityChanged = { checked ->
                        if (!uiState.accessibilityGranted && checked) {
                            showAccessibilityConsent = true
                        } else {
                            AccessibilityBridge.openSettings(context)
                            viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        }
                    },
                    onSetOverlayEnabled = onSetOverlayEnabled,
                )
            }
            composable(AppRoute.BackgroundTemplateList.route) {
                LaunchedEffect(Unit) {
                    viewModel.loadStatusBarCatalog()
                }
                BackgroundTemplateListScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectPhotoUrl = viewModel::setBackgroundTemplatePhoto,
                )
            }
            composable(AppRoute.StatusBarBatteryList.route) {
                LaunchedEffect(Unit) {
                    viewModel.loadStatusBarCatalog()
                }
                StatusBarCatalogListScreen(
                    uiState = uiState,
                    title = StatusBarTab.Battery.title,
                    selectedId = uiState.editingConfig.batteryPresetId,
                    onBack = { navController.popBackStack() },
                    onSelectId = viewModel::selectBatteryPreset,
                    previewImageUrl = { it.batteryArtUrl ?: it.thumbnailUrl },
                )
            }
            composable(AppRoute.StatusBarEmojiList.route) {
                LaunchedEffect(Unit) {
                    viewModel.loadStatusBarCatalog()
                }
                StatusBarCatalogListScreen(
                    uiState = uiState,
                    title = StatusBarTab.Emoji.title,
                    selectedId = uiState.editingConfig.emojiPresetId,
                    onBack = { navController.popBackStack() },
                    onSelectId = viewModel::selectEmojiPreset,
                    previewImageUrl = { it.emojiArtUrl ?: it.thumbnailUrl },
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
                            val snapshot = viewModel.uiState.value
                            OverlayConfigStore.saveStatusBarConfig(
                                context,
                                snapshot.editingConfig,
                                snapshot.statusBarCatalogItems,
                            )
                            OverlayConfigStore.setBatteryEmojiSource(
                                context,
                                OverlayConfigStore.BATTERY_EMOJI_SOURCE_STATUS_BAR_CUSTOM,
                            )
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
                    onOpenStore = viewModel::openStore,
                    onToggleProtection = viewModel::setProtectFromRecentApps,
                    onOpenPrivacy = {
                        viewModel.openPrivacyPolicy()
                        navController.navigate(AppRoute.Legal.create("privacy"))
                    },
                    onOpenTerms = {
                        viewModel.openTermsOfUse()
                        navController.navigate(AppRoute.Legal.create("terms"))
                    },
                    onShareApp = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, AppStoreConfig.playStoreWebUrl)
                        }
                        val chooser = Intent.createChooser(shareIntent, null)
                        rawContext.findActivity()?.startActivity(chooser)
                            ?: rawContext.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    },
                    onOpenFeedback = { navController.navigate(AppRoute.Feedback.route) },
                    onRateApp = {
                        val activity = rawContext.findActivity()
                        val openMarket = Intent(Intent.ACTION_VIEW, Uri.parse(AppStoreConfig.playStoreMarketUrl))
                        val openWeb = Intent(Intent.ACTION_VIEW, Uri.parse(AppStoreConfig.playStoreWebUrl))
                        try {
                            if (activity != null) {
                                activity.startActivity(openMarket)
                            } else {
                                rawContext.startActivity(openMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        } catch (_: ActivityNotFoundException) {
                            if (activity != null) {
                                activity.startActivity(openWeb)
                            } else {
                                rawContext.startActivity(openWeb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                    },
                    onSelectRating = viewModel::setRatingSelection,
                    onCheckUpdate = viewModel::checkForUpdates,
                    onToggleAccessibility = {
                        showAccessibilityConsent = true
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
                    onToggleAccessibility = { checked ->
                        if (!uiState.accessibilityGranted && checked) {
                            showAccessibilityConsent = true
                        } else {
                            AccessibilityBridge.openSettings(context)
                            viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        }
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
                    onSetOverlayEnabled = onSetOverlayEnabled,
                    onSetFeatureEnabled = { enabled ->
                        viewModel.setBatteryTrollEnabled(enabled)
                        if (!enabled) {
                            OverlayConfigStore.clearBatteryTroll(context)
                            OverlayAccessibilityService.requestRefresh(context)
                        } else if (AccessibilityBridge.isEnabled(context)) {
                            val snapshot = viewModel.uiState.value
                            OverlayConfigStore.saveBatteryTroll(context, snapshot)
                            OverlayAccessibilityService.requestRefresh(context)
                        }
                    },
                    onSetUseRealBattery = viewModel::setBatteryTrollUseRealBattery,
                    onSetShowPercentage = viewModel::setTrollShowPercentage,
                    onSetPercentageSize = viewModel::setTrollPercentageSizeDp,
                    onSetEmojiSize = viewModel::setTrollEmojiSizeDp,
                    onSetRandomizedMode = viewModel::setTrollRandomizedMode,
                    onSetShowEmoji = viewModel::setTrollShowEmoji,
                    onSelectEmojiOption = viewModel::selectBatteryTrollEmoji,
                    onSelectBatteryOption = viewModel::selectBatteryTrollBattery,
                    onToggleAutoDrop = viewModel::setTrollAutoDrop,
                    onOpenTutorial = {
                        viewModel.replayTutorial()
                        navController.navigate(AppRoute.Tutorial.route)
                    },
                    onRefreshBatteryTrollCatalog = viewModel::refreshBatteryTrollCatalog,
                    onToggleAccessibility = { checked ->
                        if (!uiState.accessibilityGranted && checked) {
                            showAccessibilityConsent = true
                        } else {
                            AccessibilityBridge.openSettings(context)
                            viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        }
                    },
                    onApply = {
                        viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        viewModel.applyBatteryTroll()
                        if (AccessibilityBridge.isEnabled(context)) {
                            val snapshot = viewModel.uiState.value
                            OverlayConfigStore.saveBatteryTroll(context, snapshot)
                            OverlayConfigStore.setBatteryEmojiSource(
                                context,
                                OverlayConfigStore.BATTERY_EMOJI_SOURCE_BATTERY_TROLL,
                            )
                            OverlayAccessibilityService.requestRefresh(context)
                        }
                    },
                    onTurnOff = {
                        if (uiState.trollFeatureEnabled) {
                            viewModel.turnOffBatteryTroll()
                            OverlayConfigStore.clearBatteryTroll(context)
                            OverlayAccessibilityService.requestRefresh(context)
                        } else {
                            viewModel.setBatteryTrollEnabled(true)
                            viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                            viewModel.applyBatteryTroll()
                            if (AccessibilityBridge.isEnabled(context)) {
                                val snapshot = viewModel.uiState.value
                                OverlayConfigStore.saveBatteryTroll(context, snapshot)
                                OverlayConfigStore.setBatteryEmojiSource(
                                    context,
                                    OverlayConfigStore.BATTERY_EMOJI_SOURCE_BATTERY_TROLL,
                                )
                                OverlayAccessibilityService.requestRefresh(context)
                            }
                        }
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
                    onUpdateStickerRotation = viewModel::updateSelectedStickerRotation,
                    onSetStickerPosition = viewModel::updateSelectedStickerPosition,
                    onDismissStickerAdjustment = viewModel::dismissStickerAdjustmentPanel,
                    onRefreshStickerCatalog = viewModel::ensureStickerCatalogLoaded,
                    onLoadStickerCatalogPage = viewModel::loadStickerCatalogPage,
                    onSetOverlayEnabled = onSetOverlayEnabled,
                    onToggleAccessibility = { checked ->
                        if (!uiState.accessibilityGranted && checked) {
                            showAccessibilityConsent = true
                        } else {
                            AccessibilityBridge.openSettings(context)
                            viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                        }
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
                        onToggleEnabled = { value ->
                            viewModel.updateFeatureEnabled(customizeEntry, value)
                            val snapshot = viewModel.uiState.value
                            OverlayConfigStore.saveStatusBarConfig(
                                context,
                                snapshot.editingConfig,
                                snapshot.statusBarCatalogItems,
                            )
                            OverlayConfigStore.saveFeatureConfigs(context, snapshot.featureConfigs)
                            if (AccessibilityBridge.isEnabled(context)) {
                                OverlayAccessibilityService.requestRefresh(context)
                            }
                        },
                        onSetIntensity = { value ->
                            viewModel.updateFeatureIntensity(customizeEntry, value)
                            val snapshot = viewModel.uiState.value
                            OverlayConfigStore.saveFeatureConfigs(context, snapshot.featureConfigs)
                            if (AccessibilityBridge.isEnabled(context)) {
                                OverlayAccessibilityService.requestRefresh(context)
                            }
                        },
                        onSelectVariant = { variant ->
                            viewModel.updateFeatureVariant(customizeEntry, variant)
                            val snapshot = viewModel.uiState.value
                            OverlayConfigStore.saveFeatureConfigs(context, snapshot.featureConfigs)
                            if (AccessibilityBridge.isEnabled(context)) {
                                OverlayAccessibilityService.requestRefresh(context)
                            }
                        },
                        onReset = { viewModel.resetFeature(customizeEntry) },
                        onApply = {
                            viewModel.applyFeature(customizeEntry)
                            val snapshot = viewModel.uiState.value
                            OverlayConfigStore.saveFeatureConfigs(context, snapshot.featureConfigs)
                            if (snapshot.featureConfigs[customizeEntry]?.enabled == true) {
                                OverlayConfigStore.saveStatusBarConfig(
                                    context,
                                    snapshot.editingConfig,
                                    snapshot.statusBarCatalogItems,
                                )
                            }
                            if (AccessibilityBridge.isEnabled(context)) {
                                OverlayAccessibilityService.requestRefresh(context)
                            }
                        },
                    )
                } else {
                    PlaceholderScreen(
                        title = feature,
                        subtitle = "Unknown feature route.",
                    )
                }
            }
        }
        Box(Modifier.fillMaxSize()) {
            SuccessToastOverlay(
                message = transientSuccessMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (showBottomBar) 92.dp else 28.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            )
        }
        if (showAccessibilityConsent) {
            AccessibilityServiceUsageDialog(
                onDismiss = { showAccessibilityConsent = false },
                onConfirmOpenSettings = {
                    showAccessibilityConsent = false
                    AccessibilityBridge.openSettings(context)
                    viewModel.syncAccessibilityGranted(AccessibilityBridge.isEnabled(context))
                },
                onMissingConsent = {
                    val msg = rawContext.getString(R.string.please_read_and_click) + " " + rawContext.getString(R.string.i_agree)
                    viewModel.postInfoMessage(msg)
                },
            )
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

/**
 * Maps Customize hub entries to the status-bar editor tab (original [StatusBarCustomFragment] ViewPager
 * `setCurrentItem` from navigation args).
 */
private fun customizeEntryToStatusBarTab(entry: CustomizeEntry): StatusBarTab? = when (entry) {
    CustomizeEntry.Charge -> StatusBarTab.Battery
    CustomizeEntry.Emotion -> StatusBarTab.Emoji
    CustomizeEntry.Theme -> StatusBarTab.Theme
    CustomizeEntry.Settings -> StatusBarTab.Settings
    else -> null
}

@Composable
private fun SuccessToastOverlay(
    message: String?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x1F000000), spotColor = Color(0x1F000000))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE6EEF5), RoundedCornerShape(18.dp))
                .heightIn(min = 52.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFEAF8EF))
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF1F9D62),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = message.orEmpty(),
                color = Color(0xFF233547),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
