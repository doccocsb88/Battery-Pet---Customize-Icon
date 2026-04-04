package dev.hai.emojibattery.ui.screen


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
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
import dev.hai.emojibattery.model.LimitedFeature
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
import dev.hai.emojibattery.ui.theme.OceanSerenity
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush
import dev.hai.emojibattery.service.AccessibilityBridge
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore
import dev.hai.emojibattery.ui.navigation.AppRoute
import kotlinx.coroutines.delay

private const val STICKER_CATALOG_COLUMNS = 4
private const val STICKER_CATALOG_ROWS = 4
private const val STICKERS_PER_CATALOG_PAGE = STICKER_CATALOG_COLUMNS * STICKER_CATALOG_ROWS
private const val LOTTIE_TRACE_TAG = "LottieTrace"
private val StickerUiPrimary = Color(0xFF8FB6D4)
private val StickerUiSecondary = Color(0xFF76916B)
private val StickerUiTertiary = Color(0xFFD9B99B)
private val StickerUiText = Color(0xFF3C3C3C)
private val StickerUiPremiumBadge = Color(0xFFF2C76E)

private fun maxStickerSlotsForUi(state: AppUiState): Int = when {
    state.premiumUnlocked -> SampleCatalog.PREMIUM_STICKER_SLOTS
    state.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_EXTRA_STICKER_SLOT) -> SampleCatalog.REWARD_EXTRA_STICKER_SLOTS
    else -> SampleCatalog.FREE_STICKER_SLOTS
}

private fun hasStickerFeatureAccess(state: AppUiState, featureKey: String): Boolean {
    return state.premiumUnlocked || state.unlockedFeatureKeys.contains(featureKey)
}

private fun shouldShowStickerPaywallBadge(state: AppUiState, sticker: StickerPreset): Boolean {
    val premiumLocked = sticker.premium && !hasStickerFeatureAccess(state, "sticker:${sticker.id}")
    val alreadyAdded = state.stickerPlacements.any { it.stickerId == sticker.id }
    val slotsLocked = !alreadyAdded && state.stickerPlacements.size >= maxStickerSlotsForUi(state)
    return premiumLocked || slotsLocked
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
internal fun EmojiStickerScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onAddSticker: (String) -> Unit,
    onSelectSticker: (String) -> Unit,
    onRemoveSticker: (String) -> Unit,
    onUpdateStickerSize: (Float) -> Unit,
    onUpdateStickerRotation: (Float) -> Unit,
    onSetStickerPosition: (Float, Float) -> Unit,
    onDismissStickerAdjustment: () -> Unit,
    onRefreshStickerCatalog: () -> Unit,
    onLoadStickerCatalogPage: (Int) -> Unit,
    onSetOverlayEnabled: (Boolean) -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onSave: () -> Unit,
    onTurnOff: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onRefreshStickerCatalog()
    }

    LaunchedEffect(uiState.stickerCatalogLoading, uiState.stickerCatalogRemote) {
        Log.d(
            "StickerCatalogUI",
            "EmojiStickerScreen: loading=${uiState.stickerCatalogLoading} remoteCount=${uiState.stickerCatalogRemote.size}",
        )
    }

    val stickerLibrary = if (uiState.stickerCatalogRemote.isNotEmpty()) {
        uiState.stickerCatalogRemote
    } else {
        SampleCatalog.stickerPresets
    }
    LaunchedEffect(stickerLibrary) {
        val withThumb = stickerLibrary.count { !it.thumbnailUrl.isNullOrBlank() }
        val withLottie = stickerLibrary.count { !it.lottieUrl.isNullOrBlank() }
        val emptyMedia = stickerLibrary.count { it.thumbnailUrl.isNullOrBlank() && it.lottieUrl.isNullOrBlank() }
        Log.d(
            "StickerCatalogUI",
            "EmojiStickerScreen: activeLibraryCount=${stickerLibrary.size} withThumb=$withThumb withLottie=$withLottie emptyMedia=$emptyMedia",
        )
    }
    val selectedSticker = uiState.selectedStickerId?.let { uiState.stickerPresetForId(it) }
    val selectedPlacement = uiState.selectedStickerId?.let { id ->
        uiState.stickerPlacements.firstOrNull { it.stickerId == id }
    }
    val maxStickerSlots = when {
        uiState.premiumUnlocked -> SampleCatalog.PREMIUM_STICKER_SLOTS
        uiState.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_EXTRA_STICKER_SLOT) -> SampleCatalog.REWARD_EXTRA_STICKER_SLOTS
        else -> SampleCatalog.FREE_STICKER_SLOTS
    }
    val stickerScroll = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Image(
                        painter = painterResource(R.drawable.ic_back_40_new),
                        contentDescription = stringResource(R.string.cd_back),
                        modifier = Modifier.size(36.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.status_bar_sticker_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        bottomBar = {
            StickerActionBar(
                onTurnOff = onTurnOff,
                onSave = onSave,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(stickerScroll)
                .padding(horizontal = 8.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EmojiBatteryOverlayAccessCard(
                accessibilityGranted = uiState.accessibilityGranted,
                enabled = uiState.statusBarOverlayEnabled,
                onToggle = onSetOverlayEnabled,
                onRequestAccessibility = { onToggleAccessibility(true) },
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            StickerPreviewCard(
                selectedSticker = selectedSticker,
                selectedPlacement = selectedPlacement,
                overlayEnabled = uiState.stickerOverlayEnabled,
            )
            if (!uiState.premiumUnlocked) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                ) {
                    Text(
                        if (uiState.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_EXTRA_STICKER_SLOT)) {
                            stringResource(R.string.sticker_reward_unlocked_slots, SampleCatalog.REWARD_EXTRA_STICKER_SLOTS)
                        } else {
                            stringResource(R.string.sticker_free_mode_hint, LimitedFeature.ApplySticker.freeLimit)
                        },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
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
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.sticker_add_sticker),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (uiState.stickerCatalogLoading && uiState.stickerCatalogRemote.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val loadingComposition by rememberLottieComposition(
                                LottieCompositionSpec.Asset("snail_loader.json"),
                            )
                            LottieAnimation(
                                composition = loadingComposition,
                                iterations = LottieConstants.IterateForever,
                                speed = 2f,
                                modifier = Modifier.size(120.dp),
                            )
                            Text(
                                stringResource(R.string.common_loading_ellipsis),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    } else {
                        val totalRemotePages = uiState.stickerCatalogTotalPageCount
                        val usingRemotePages = totalRemotePages > 0
                        val fallbackPages = remember(stickerLibrary) {
                            stickerLibrary.chunked(STICKERS_PER_CATALOG_PAGE)
                        }
                        val pageCount = if (usingRemotePages) totalRemotePages else fallbackPages.size
                        if (pageCount == 0) {
                            Text(
                                stringResource(R.string.sticker_no_stickers),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            val pagerState = rememberPagerState(pageCount = { pageCount })
                            LaunchedEffect(pageCount) {
                                if (pagerState.currentPage >= pageCount) {
                                    pagerState.scrollToPage((pageCount - 1).coerceAtLeast(0))
                                }
                            }
                            LaunchedEffect(pagerState, usingRemotePages, pageCount) {
                                if (!usingRemotePages) return@LaunchedEffect
                                snapshotFlow { pagerState.currentPage }
                                    .distinctUntilChanged()
                                    .collect { pageIndex ->
                                        onLoadStickerCatalogPage(pageIndex)
                                    }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxWidth(),
                                    beyondViewportPageCount = 1,
                                    pageSpacing = 0.dp,
                                ) { pageIndex ->
                                    val stickers = if (usingRemotePages) {
                                        uiState.stickerCatalogPages[pageIndex]
                                    } else {
                                        fallbackPages.getOrNull(pageIndex)
                                    }
                                    if (usingRemotePages && stickers == null) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                strokeWidth = 2.dp,
                                                color = StickerUiPrimary,
                                            )
                                            Text(
                                                stringResource(R.string.common_loading_ellipsis),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                        }
                                    } else {
                                        StickerCatalogGridPage(
                                            stickers = stickers.orEmpty(),
                                            uiState = uiState,
                                            onAddSticker = onAddSticker,
                                        )
                                    }
                                }
                                Text(
                                    stringResource(
                                        R.string.sticker_page_indicator,
                                        pagerState.currentPage + 1,
                                        pageCount,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelMedium,
                                )
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
                            stringResource(R.string.sticker_my_sticker),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(
                                R.string.sticker_added_progress_count,
                                uiState.stickerPlacements.size,
                                maxStickerSlots,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (uiState.stickerPlacements.isEmpty()) {
                        Text(
                            stringResource(R.string.sticker_add_from_library_hint),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(112.dp),
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
                        stringResource(R.string.sticker_selected_controls),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (selectedSticker != null && selectedPlacement != null) {
//                        Text(
//                            "${selectedSticker.glyph} ${selectedSticker.name}",
//                            style = MaterialTheme.typography.bodyLarge,
//                            color = MaterialTheme.colorScheme.onSurface,
//                        )
//                        Text(
//                            stringResource(R.string.sticker_adjustment_inline_hint),
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        )
                    } else {
//                        Text(
//                            stringResource(R.string.sticker_select_to_edit_hint),
//                            color = MaterialTheme.colorScheme.onSurface,
//                            style = MaterialTheme.typography.bodyMedium,
//                        )
                    }
                }
            }
        }
        if (uiState.showStickerAdjustmentPanel && selectedSticker != null && selectedPlacement != null) {
            StickerAdjustmentOverlay(
                sticker = selectedSticker,
                placement = selectedPlacement,
                onDismiss = onDismissStickerAdjustment,
                onUpdateSize = onUpdateStickerSize,
                onUpdateRotation = onUpdateStickerRotation,
                onSetPosition = onSetStickerPosition,
            )
        }
    }
}

@Composable
private fun StickerActionBar(
    onTurnOff: () -> Unit,
    onSave: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
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
                        color = MaterialTheme.colorScheme.secondary,
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
                    .background(StrawberryCtaGradientBrush)
                    .clickable(onClick = onSave),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.common_save),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Composable
private fun StickerCatalogGridPage(
    stickers: List<StickerPreset>,
    uiState: AppUiState,
    onAddSticker: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stickers.chunked(STICKER_CATALOG_COLUMNS).forEach { rowStickers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowStickers.forEach { sticker ->
                    Box(modifier = Modifier.weight(1f)) {
                        val showPaywallBadge = shouldShowStickerPaywallBadge(uiState, sticker)
                        StickerCatalogCard(
                            sticker = sticker,
                            selected = uiState.selectedStickerId == sticker.id,
                            added = uiState.stickerPlacements.any { it.stickerId == sticker.id },
                            showPaywallBadge = showPaywallBadge,
                            onClick = { onAddSticker(sticker.id) },
                        )
                    }
                }
                repeat(STICKER_CATALOG_COLUMNS - rowStickers.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun StickerMediaPreview(
    sticker: StickerPreset,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val lottieUrl = sticker.lottieUrl
        if (lottieUrl != null) {
            val composition by rememberLottieComposition(LottieCompositionSpec.Url(lottieUrl))
            LaunchedEffect(lottieUrl, composition) {
                Log.d(
                    LOTTIE_TRACE_TAG,
                    "StickerMediaPreview: stickerId=${sticker.id} name=${sticker.name} lottieUrl=$lottieUrl compositionReady=${composition != null}",
                )
            }
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (sticker.thumbnailUrl != null) {
                AsyncImage(
                    model = sticker.thumbnailUrl,
                    contentDescription = sticker.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    sticker.glyph,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        } else if (sticker.thumbnailUrl != null) {
            AsyncImage(
                model = sticker.thumbnailUrl,
                contentDescription = sticker.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(
                sticker.glyph,
                style = MaterialTheme.typography.displaySmall,
            )
        }
    }
}

@Composable
internal fun StickerPreviewCard(
    selectedSticker: StickerPreset?,
    selectedPlacement: StickerPlacement?,
    overlayEnabled: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.sticker_preview_title),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                if (overlayEnabled) {
                    stringResource(R.string.overlay_active)
                } else {
                    stringResource(R.string.overlay_inactive)
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (selectedSticker != null) {
                    val placement = selectedPlacement
                    val normalizedSize = placement?.size?.coerceIn(0.2f, 1f) ?: 0.5f
                    val stickerSize = (36.dp + (normalizedSize * 56f).dp)
                    val x = (maxWidth - stickerSize) * (placement?.offsetX?.coerceIn(0f, 1f) ?: 0.5f)
                    val y = (maxHeight - stickerSize) * (placement?.offsetY?.coerceIn(0f, 1f) ?: 0.5f)
                    StickerMediaPreview(
                        selectedSticker,
                        Modifier
                            .offset(x = x, y = y)
                            .size(stickerSize)
                            .graphicsLayer(rotationZ = placement?.rotation ?: 0f),
                    )
                } else {
                    Text(
                        text = "✨",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (selectedSticker != null && selectedPlacement != null) {
                Text(
                    "size ${(selectedPlacement.size * 100).toInt()}% · rotate ${selectedPlacement.rotation.toInt()}°",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text(
                    stringResource(R.string.sticker_pick_preview_hint),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun StickerAdjustmentOverlay(
    sticker: StickerPreset,
    placement: StickerPlacement,
    onDismiss: () -> Unit,
    onUpdateSize: (Float) -> Unit,
    onUpdateRotation: (Float) -> Unit,
    onSetPosition: (Float, Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .zIndex(10f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StickerUiText.copy(alpha = 0.28f))
                .clickable(onClick = onDismiss),
        )
        StickerAdjustmentPanel(
            sticker = sticker,
            placement = placement,
            onDismiss = onDismiss,
            onUpdateSize = onUpdateSize,
            onUpdateRotation = onUpdateRotation,
            onSetPosition = onSetPosition,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun StickerAdjustmentPanel(
    sticker: StickerPreset,
    placement: StickerPlacement,
    onDismiss: () -> Unit,
    onUpdateSize: (Float) -> Unit,
    onUpdateRotation: (Float) -> Unit,
    onSetPosition: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var containerWidthPx by remember(sticker.id) { mutableStateOf(1f) }
    var containerHeightPx by remember(sticker.id) { mutableStateOf(1f) }
    var localSize by remember(sticker.id) { mutableStateOf(placement.size.coerceIn(0.2f, 1f)) }
    var localOffsetX by remember(sticker.id) { mutableStateOf(placement.offsetX.coerceIn(0f, 1f)) }
    var localOffsetY by remember(sticker.id) { mutableStateOf(placement.offsetY.coerceIn(0f, 1f)) }

    LaunchedEffect(sticker.id, placement.size, placement.offsetX, placement.offsetY) {
        localSize = placement.size.coerceIn(0.2f, 1f)
        localOffsetX = placement.offsetX.coerceIn(0f, 1f)
        localOffsetY = placement.offsetY.coerceIn(0f, 1f)
    }
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = OceanSerenity.Surface,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = OceanSerenity.ModuleShadow,
                spotColor = OceanSerenity.ModuleShadow,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OceanAdjustmentHeader(
                title = stringResource(R.string.sticker_adjustment_title),
                subtitle = "Drag sticker to move. Drag 4 corner handles to resize.",
            )
            OceanAdjustmentStage(
                modifier = Modifier
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .onSizeChanged {
                        containerWidthPx = it.width.toFloat().coerceAtLeast(1f)
                        containerHeightPx = it.height.toFloat().coerceAtLeast(1f)
                    },
            ) {
                val normalizedSize = localSize.coerceIn(0.2f, 1f)
                val baseStickerPx = with(density) { (36.dp + (normalizedSize * 56f).dp).toPx() }
                val framePaddingPx = with(density) { 14.dp.toPx() }
                val frameWidthPx = baseStickerPx + (framePaddingPx * 2f)
                val frameHeightPx = baseStickerPx + (framePaddingPx * 2f)
                val availableWidthPx = (containerWidthPx - frameWidthPx).coerceAtLeast(0f)
                val availableHeightPx = (containerHeightPx - frameHeightPx).coerceAtLeast(0f)
                val leftPx = (availableWidthPx * localOffsetX.coerceIn(0f, 1f)).coerceIn(0f, availableWidthPx)
                val topPx = (availableHeightPx * localOffsetY.coerceIn(0f, 1f)).coerceIn(0f, availableHeightPx)
                val resizeScaleFactor = 260f

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                        .size(
                            width = with(density) { frameWidthPx.toDp() },
                            height = with(density) { frameHeightPx.toDp() },
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(containerWidthPx, containerHeightPx, frameWidthPx, frameHeightPx) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val nextX = if (availableWidthPx > 0f) {
                                        localOffsetX + (dragAmount.x / availableWidthPx)
                                    } else {
                                        localOffsetX
                                    }
                                    val nextY = if (availableHeightPx > 0f) {
                                        localOffsetY + (dragAmount.y / availableHeightPx)
                                    } else {
                                        localOffsetY
                                    }
                                    localOffsetX = nextX.coerceIn(0f, 1f)
                                    localOffsetY = nextY.coerceIn(0f, 1f)
                                    onSetPosition(localOffsetX, localOffsetY)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        StickerMediaPreview(
                            sticker = sticker,
                            modifier = Modifier
                                .size(with(density) { baseStickerPx.toDp() })
                                .graphicsLayer(rotationZ = placement.rotation),
                        )
                    }
                    OceanAdjustmentDashFrame(modifier = Modifier.fillMaxSize())
                    OceanAdjustmentResizeHandle(
                        modifier = Modifier.align(Alignment.TopStart),
                        onDrag = { drag ->
                            localSize = (localSize + ((-drag.x - drag.y) / resizeScaleFactor)).coerceIn(0.2f, 1f)
                            onUpdateSize(localSize)
                        },
                    )
                    OceanAdjustmentResizeHandle(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onDrag = { drag ->
                            localSize = (localSize + ((drag.x - drag.y) / resizeScaleFactor)).coerceIn(0.2f, 1f)
                            onUpdateSize(localSize)
                        },
                    )
                    OceanAdjustmentResizeHandle(
                        modifier = Modifier.align(Alignment.BottomStart),
                        onDrag = { drag ->
                            localSize = (localSize + ((-drag.x + drag.y) / resizeScaleFactor)).coerceIn(0.2f, 1f)
                            onUpdateSize(localSize)
                        },
                    )
                    OceanAdjustmentResizeHandle(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        onDrag = { drag ->
                            localSize = (localSize + ((drag.x + drag.y) / resizeScaleFactor)).coerceIn(0.2f, 1f)
                            onUpdateSize(localSize)
                        },
                    )
                }
            }
            OceanSectionAccentLabel(stringResource(R.string.sticker_rotate_slider))
            AppBasicSlider(
                value = placement.rotation.coerceIn(-180f, 180f),
                valueRange = -180f..180f,
                onValueChange = onUpdateRotation,
                activeColor = OceanSerenity.Secondary,
                thumbColor = OceanSerenity.Secondary,
                inactiveColor = OceanSerenity.PrimaryContainer,
            )
        }
    }
}

@Composable
internal fun StickerCatalogCard(
    sticker: StickerPreset,
    selected: Boolean,
    added: Boolean,
    showPaywallBadge: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = if (selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        } else {
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
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
            if (showPaywallBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(
                            RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 12.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 12.dp,
                            ),
                        )
                        .background(StickerUiPremiumBadge)
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_sticker_premium_badge),
                        contentDescription = stringResource(R.string.cd_premium),
                        modifier = Modifier.size(14.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AddedStickerChip(
    sticker: StickerPreset,
    selected: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.size(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selected) StickerUiPrimary.copy(alpha = 0.22f)
                        else StickerUiTertiary.copy(alpha = 0.26f),
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) StickerUiPrimary else StickerUiSecondary.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable(onClick = onSelect),
                contentAlignment = Alignment.Center,
            ) {
                if (sticker.thumbnailUrl != null) {
                    AsyncImage(
                        model = sticker.thumbnailUrl,
                        contentDescription = sticker.name,
                        modifier = Modifier.size(34.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Text(sticker.glyph)
                }
            }
            Surface(
                onClick = onRemove,
                shape = CircleShape,
                color = StickerUiText,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .zIndex(1f),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "×",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Surface(
            onClick = onSelect,
            shape = RoundedCornerShape(16.dp),
            color = StickerUiPrimary.copy(alpha = 0.2f),
        ) {
            Text(
                stringResource(R.string.sticker_position_chip),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = StickerUiText,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
