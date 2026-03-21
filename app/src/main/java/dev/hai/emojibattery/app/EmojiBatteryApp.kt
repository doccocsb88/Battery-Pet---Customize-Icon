package dev.hai.emojibattery.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
                        navController.navigate(if (uiState.languageChosen) AppRoute.Home.route else AppRoute.Language.route) {
                            popUpTo(AppRoute.Splash.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(AppRoute.Language.route) {
                LanguageScreen(
                    selected = uiState.selectedLanguage,
                    onChooseLanguage = {
                        viewModel.chooseLanguage(it)
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Language.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(AppRoute.Home.route) {
                HomeScreen(
                    onOpenStatusBarCustom = {
                        viewModel.selectMainSection(MainSection.Home)
                        navController.navigate(AppRoute.StatusBarCustom.route)
                    },
                    onOpenLegacyBattery = { navController.navigate(AppRoute.LegacyBattery.route) },
                    onOpenSearch = { navController.navigate(AppRoute.Search.route) },
                    onOpenSticker = { navController.navigate(AppRoute.EmojiSticker.route) },
                    onOpenBatteryTroll = { navController.navigate(AppRoute.BatteryTroll.route) },
                )
            }
            composable(AppRoute.Customize.route) {
                CustomizeHubScreen(
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
                    onReplayTutorial = viewModel::replayTutorial,
                    onToggleProtection = viewModel::setProtectFromRecentApps,
                    onOpenPrivacy = viewModel::openPrivacyPolicy,
                    onShareApp = viewModel::shareApp,
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
}

@Composable
private fun GestureScreen(
    uiState: AppUiState,
    onSetGestureEnabled: (Boolean) -> Unit,
    onSetVibrateFeedback: (Boolean) -> Unit,
    onSetGestureAction: (GestureTrigger, GestureAction) -> Unit,
) {
    ScreenContainer(
        title = "Gesture",
        subtitle = "Master gesture switch, five action bindings, and vibration feedback.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            HeroCard(
                title = "Status Bar Gestures",
                body = "Bind taps and swipes to the main routes in this port.",
                cta = if (uiState.gestureEnabled) "Gestures Enabled" else "Enable Gestures",
                onClick = { onSetGestureEnabled(!uiState.gestureEnabled) },
            )
            SettingToggle(
                label = "Use gestures in the status bar for custom actions",
                value = uiState.gestureEnabled,
                onChange = onSetGestureEnabled,
            )
            AnimatedVisibility(uiState.gestureEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GestureTrigger.entries.forEach { trigger ->
                        GestureActionCard(
                            trigger = trigger,
                            selectedAction = uiState.gestureActions[trigger] ?: GestureAction.None,
                            onSelectAction = { onSetGestureAction(trigger, it) },
                        )
                    }
                    SettingToggle(
                        label = "Vibrate feedback",
                        value = uiState.vibrateFeedback,
                        onChange = onSetVibrateFeedback,
                    )
                }
            }
            if (!uiState.gestureEnabled) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        "Enable gestures to reveal the five action rows, matching the original visibility flow.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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
    onToggleAccessibility: (Boolean) -> Unit,
    onSave: () -> Unit,
    onTurnOff: () -> Unit,
) {
    val selectedSticker = uiState.selectedStickerId?.let { id ->
        SampleCatalog.stickerPresets.firstOrNull { it.id == id }
    }
    val selectedPlacement = uiState.selectedStickerId?.let { id ->
        uiState.stickerPlacements.firstOrNull { it.stickerId == id }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Emoji Sticker") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    AssistChip(
                        onClick = {},
                        label = { Text("Tutorial") },
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StickerPreviewCard(
                selectedSticker = selectedSticker,
                selectedPlacement = selectedPlacement,
                overlayEnabled = uiState.stickerOverlayEnabled,
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PermissionBanner(
                enabled = uiState.accessibilityGranted,
                onToggle = onToggleAccessibility,
            )
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Add Sticker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        AssistChip(onClick = {}, label = { Text("Tutorial") })
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SampleCatalog.stickerPresets.forEach { sticker ->
                            StickerCatalogCard(
                                sticker = sticker,
                                selected = uiState.selectedStickerId == sticker.id,
                                added = uiState.stickerPlacements.any { it.stickerId == sticker.id },
                                onClick = { onAddSticker(sticker.id) },
                            )
                        }
                    }
                }
            }
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("My Sticker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${uiState.stickerPlacements.size} added", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (uiState.stickerPlacements.isEmpty()) {
                        Text("Add a sticker from the library above to start editing.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.stickerPlacements, key = { it.stickerId }) { placement ->
                                val sticker = SampleCatalog.stickerPresets.first { it.id == placement.stickerId }
                                AddedStickerChip(
                                    sticker = sticker,
                                    selected = uiState.selectedStickerId == sticker.id,
                                    onSelect = { onSelectSticker(sticker.id) },
                                    onRemove = { onRemoveSticker(sticker.id) },
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                    Text("Selected Sticker Controls", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (selectedSticker != null && selectedPlacement != null) {
                        Text("${selectedSticker.glyph} ${selectedSticker.name}", style = MaterialTheme.typography.bodyLarge)
                        SliderField("Sticker size", selectedPlacement.size, 0.2f..1f, onUpdateStickerSize)
                        SliderField("Sticker speed", selectedPlacement.speed, 0.2f..1f, onUpdateStickerSpeed)
                    } else {
                        Text("Select one of your added stickers to edit size and speed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onTurnOff, modifier = Modifier.weight(1f)) {
                            Text("Turn Off")
                        }
                        FilledTonalButton(onClick = onSave, modifier = Modifier.weight(1f)) {
                            Text("Save")
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
) {
    val options = listOf("English", "Vietnamese", "Spanish", "Portuguese")
    ScreenContainer(title = "Choose Language", subtitle = "Matches the original LanguageActivity gate after SplashActivity.") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(options) { language ->
                Card(
                    onClick = { onChooseLanguage(language) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (language == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(language, fontWeight = FontWeight.SemiBold)
                            Text("Tap to continue into MainActivity", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (language == selected) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onOpenStatusBarCustom: () -> Unit,
    onOpenLegacyBattery: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSticker: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
) {
    ScreenContainer(title = "Home", subtitle = "Main launcher with quick jumps into the strongest flows.") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HeroCard(
                title = "Status Bar Custom",
                body = "Battery, emoji, theme, and settings in one live editor.",
                cta = "Open Editor",
                onClick = onOpenStatusBarCustom,
            )
            Text("Quick access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallActionCard("Legacy Battery", "Classic battery picker", "🔋", onOpenLegacyBattery, modifier = Modifier.weight(1f))
                SmallActionCard("Search", "Find routes and packs", "🔎", onOpenSearch, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallActionCard("Emoji Sticker", "Floating sticker overlay", "✨", onOpenSticker, modifier = Modifier.weight(1f))
                SmallActionCard("Battery Troll", "Prank battery templates", "😈", onOpenBatteryTroll, modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomizeHubScreen(
    onOpenFeature: (CustomizeEntry) -> Unit,
    onOpenStatusBarCustom: () -> Unit,
    onOpenRealTime: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
) {
    ScreenContainer(title = "Battery Icon", subtitle = "Feature hub for the status-bar editor and isolated icon sections.") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            HeroCard(
                title = "Unified Status Bar Custom",
                body = "Battery, emoji, theme, and settings in one place.",
                cta = "Open Status Bar Custom",
                onClick = onOpenStatusBarCustom,
            )
            Text("Customize sections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CustomizeEntry.entries.forEach { entry ->
                    FeatureTileCard(entry = entry, onClick = { onOpenFeature(entry) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onOpenRealTime, modifier = Modifier.weight(1f)) {
                    Text("Real Time")
                }
                OutlinedButton(onClick = onOpenBatteryTroll, modifier = Modifier.weight(1f)) {
                    Text("Battery Troll")
                }
            }
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Status Bar Custom") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BatteryPreviewCard(uiState = uiState)
            PermissionBanner(
                enabled = uiState.accessibilityGranted,
                onToggle = onAccessibilityChanged,
            )
            TabStrip(
                tabs = StatusBarTab.entries,
                selected = uiState.activeStatusBarTab,
                onSelect = onSelectTab,
            )
            when (uiState.activeStatusBarTab) {
                StatusBarTab.Battery -> BatteryTabContent(
                    selectedId = config.batteryPresetId,
                    presets = batteryPresets,
                    batteryScale = config.batteryPercentScale,
                    showPercentage = config.showPercentage,
                    showStroke = config.showStroke,
                    onSelectBattery = onSelectBattery,
                    onSetBatteryScale = onSetBatteryScale,
                    onTogglePercentage = onTogglePercentage,
                    onToggleStroke = onToggleStroke,
                )

                StatusBarTab.Emoji -> EmojiTabContent(
                    selectedId = config.emojiPresetId,
                    presets = emojiPresets,
                    emojiScale = config.emojiScale,
                    animateCharge = config.animateCharge,
                    onSelectEmoji = onSelectEmoji,
                    onSetEmojiScale = onSetEmojiScale,
                    onToggleAnimate = onToggleAnimate,
                )

                StatusBarTab.Theme -> ThemeTabContent(
                    selectedId = config.themePresetId,
                    presets = themePresets,
                    onSelectTheme = onSelectTheme,
                )

                StatusBarTab.Settings -> SettingsTabContent(
                    showPercentage = config.showPercentage,
                    animateCharge = config.animateCharge,
                    showStroke = config.showStroke,
                    onTogglePercentage = onTogglePercentage,
                    onToggleAnimate = onToggleAnimate,
                    onToggleStroke = onToggleStroke,
                )
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

    ScreenContainer(title = "Search", subtitle = "Search by keyword, most-searched tags, or recommended packs.") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search template") },
                singleLine = true,
            )
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
                Text("${results.size} result(s) for \"$query\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.End)) {
                Text("Back")
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
    Card(onClick = onClick) {
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
    ScreenContainer(title = "Achievement", subtitle = "Progress cards, claim state, and route-linked rewards.") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            uiState.achievements.forEach { task ->
                val completed = task.progress >= task.target
                Card {
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
    onShareApp: () -> Unit,
    onCheckUpdate: () -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
) {
    ScreenContainer(title = "Settings", subtitle = "Language, tutorial, permission entry points, and app utilities.") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(onClick = onOpenLanguage) {
                Column(Modifier.padding(16.dp)) {
                    Text("Language", fontWeight = FontWeight.SemiBold)
                    Text(uiState.selectedLanguage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(onClick = onReplayTutorial) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tutorial", fontWeight = FontWeight.SemiBold)
                    Text("Replay permission onboarding and gesture/status-bar setup.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            SettingToggle("Accessibility bridge", uiState.accessibilityGranted, onToggleAccessibility)
            SettingToggle("Protect from recent-app cleaning", uiState.protectFromRecentApps, onToggleProtection)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallActionCard("Privacy", "Policy and data usage", "🔐", onOpenPrivacy, modifier = Modifier.weight(1f))
                SmallActionCard("Share App", "Invite a friend", "📤", onShareApp, modifier = Modifier.weight(1f))
            }
            OutlinedButton(onClick = onCheckUpdate, modifier = Modifier.fillMaxWidth()) {
                Text("Check Update")
            }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BatteryTrollScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onSetMessage: (String) -> Unit,
    onToggleAutoDrop: (Boolean) -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onApply: () -> Unit,
    onTurnOff: () -> Unit,
) {
    val selected = SampleCatalog.batteryTrollTemplates.first { it.id == uiState.selectedBatteryTrollTemplateId }
    ScreenContainer(title = "Battery Troll", subtitle = "Pick prank content, tweak the fake label, then apply.") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TemplatePreviewCard(
                title = selected.title,
                summary = selected.summary,
                glyph = selected.accentGlyph,
                tag = if (uiState.trollOverlayEnabled) "Overlay Active" else "Overlay Inactive",
            )
            PermissionBanner(enabled = uiState.accessibilityGranted, onToggle = onToggleAccessibility)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SampleCatalog.batteryTrollTemplates) { template ->
                    TrollTemplateCard(
                        template = template,
                        selected = template.id == uiState.selectedBatteryTrollTemplateId,
                        onClick = { onSelectTemplate(template.id) },
                    )
                }
            }
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Customize", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SampleCatalog.trollMessageOptions.forEach { option ->
                            ChoiceChip(
                                label = option,
                                selected = option == uiState.trollMessage,
                                onClick = { onSetMessage(option) },
                            )
                        }
                    }
                    SettingToggle("Auto drop animation", uiState.trollAutoDrop, onToggleAutoDrop)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Back")
                }
                OutlinedButton(onClick = onTurnOff, modifier = Modifier.weight(1f)) {
                    Text("Turn Off")
                }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text("Apply")
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
private fun StickerPreviewCard(
    selectedSticker: StickerPreset?,
    selectedPlacement: StickerPlacement?,
    overlayEnabled: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Sticker Preview", fontWeight = FontWeight.SemiBold)
            Text(
                if (overlayEnabled) "Overlay active" else "Overlay inactive",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.7f)),
            ) {
                Text(
                    text = selectedSticker?.glyph ?: "✨",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = ((1f - (selectedPlacement?.speed ?: 0.5f)) * 24f).dp),
                    style = MaterialTheme.typography.displayLarge,
                )
            }
            if (selectedSticker != null && selectedPlacement != null) {
                Text(
                    "${selectedSticker.name}  •  size ${(selectedPlacement.size * 100).toInt()}%  •  speed ${(selectedPlacement.speed * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text("Pick a sticker to preview it here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.size(width = 78.dp, height = 92.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (sticker.premium) {
                    Text("♦", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                } else {
                    Spacer(Modifier.size(10.dp))
                }
                if (sticker.animated) {
                    Text("GIF", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(sticker.glyph, style = MaterialTheme.typography.headlineMedium)
            Text(
                if (added) "Added" else sticker.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
        onClick = onSelect,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(sticker.glyph)
            Text(sticker.name)
            TextButton(onClick = onRemove) { Text("x") }
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
private fun TrollTemplateCard(
    template: BatteryTrollTemplate,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.size(width = 168.dp, height = 148.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(template.accentGlyph, style = MaterialTheme.typography.headlineMedium)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(template.title, fontWeight = FontWeight.SemiBold)
                Text(template.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AssistChip(onClick = onClick, label = { Text(template.prankMessage) })
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
