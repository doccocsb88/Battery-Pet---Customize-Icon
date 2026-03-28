package dev.hai.emojibattery.ui.screen


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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import androidx.compose.material3.Slider
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush
import dev.hai.emojibattery.service.AccessibilityBridge
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore
import dev.hai.emojibattery.ui.navigation.AppRoute
import kotlinx.coroutines.delay

private const val STICKER_CATALOG_COLUMNS = 4
private const val STICKER_CATALOG_ROWS = 4
private const val STICKERS_PER_CATALOG_PAGE = STICKER_CATALOG_COLUMNS * STICKER_CATALOG_ROWS
private val StickerUiPrimary = Color(0xFF8FB6D4)
private val StickerUiSecondary = Color(0xFF76916B)
private val StickerUiTertiary = Color(0xFFD9B99B)
private val StickerUiText = Color(0xFF3C3C3C)

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
    onNudgeStickerPosition: (Float, Float) -> Unit,
    onDismissStickerAdjustment: () -> Unit,
    onOpenTutorial: () -> Unit,
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
                        stringResource(R.string.sticker_premium_unlocked_slots, SampleCatalog.PREMIUM_STICKER_SLOTS)
                    } else if (uiState.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_EXTRA_STICKER_SLOT)) {
                        stringResource(R.string.sticker_reward_unlocked_slots, SampleCatalog.REWARD_EXTRA_STICKER_SLOTS)
                    } else {
                        stringResource(R.string.sticker_free_mode_hint, SampleCatalog.FREE_STICKER_SLOTS)
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface,
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
                            stringResource(R.string.sticker_add_sticker),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable(onClick = onOpenTutorial),
                        ) {
                            Text(
                                stringResource(R.string.tutorial),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface,
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
                                stringResource(R.string.common_loading_ellipsis),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    } else {
                        val stickerPages = remember(stickerLibrary) {
                            stickerLibrary.chunked(STICKERS_PER_CATALOG_PAGE)
                        }
                        if (stickerPages.isEmpty()) {
                            Text(
                                stringResource(R.string.sticker_no_stickers),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            val pagerState = rememberPagerState(pageCount = { stickerPages.size })
                            LaunchedEffect(stickerPages.size) {
                                if (pagerState.currentPage >= stickerPages.size) {
                                    pagerState.scrollToPage((stickerPages.size - 1).coerceAtLeast(0))
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxWidth(),
                                    beyondViewportPageCount = 1,
                                    pageSpacing = 0.dp,
                                ) { pageIndex ->
                                    StickerCatalogGridPage(
                                        stickers = stickerPages[pageIndex],
                                        uiState = uiState,
                                        onAddSticker = onAddSticker,
                                    )
                                }
                                Text(
                                    stringResource(
                                        R.string.sticker_page_indicator,
                                        pagerState.currentPage + 1,
                                        stickerPages.size,
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
//                    Text(
//                        stringResource(R.string.sticker_selected_controls),
//                        style = MaterialTheme.typography.titleSmall,
//                        fontWeight = FontWeight.SemiBold,
//                        color = MaterialTheme.colorScheme.onSurface,
//                    )
//                    if (selectedSticker != null && selectedPlacement != null) {
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
//                    } else {
                        Text(
                            stringResource(R.string.sticker_select_to_edit_hint),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
//                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                .background(
                                    StrawberryCtaGradientBrush,
                                )
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
        }
        if (uiState.showStickerAdjustmentPanel && selectedSticker != null && selectedPlacement != null) {
            StickerAdjustmentOverlay(
                sticker = selectedSticker,
                placement = selectedPlacement,
                onDismiss = onDismissStickerAdjustment,
                onUpdateSize = onUpdateStickerSize,
                onUpdateRotation = onUpdateStickerRotation,
                onNudgePosition = onNudgeStickerPosition,
            )
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
    onNudgePosition: (Float, Float) -> Unit,
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
            onNudgePosition = onNudgePosition,
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
    onNudgePosition: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(Modifier.width(28.dp))
                Text(
                    stringResource(R.string.sticker_adjustment_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = StickerUiText,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = StickerUiPrimary,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.multiply_sign),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StickerUiPrimary),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.sticker_size_slider),
                            style = MaterialTheme.typography.titleSmall,
                            color = StickerUiText,
                        )
                    }
                    Slider(
                        value = placement.size.coerceIn(0.2f, 1f),
                        valueRange = 0.2f..1f,
                        onValueChange = onUpdateSize,
                        colors = SliderDefaults.colors(
                            thumbColor = StickerUiSecondary,
                            activeTrackColor = StickerUiSecondary,
                            inactiveTrackColor = StickerUiPrimary.copy(alpha = 0.28f),
                        ),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StickerUiPrimary),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.sticker_rotate_slider),
                            style = MaterialTheme.typography.titleSmall,
                            color = StickerUiText,
                        )
                    }
                    Slider(
                        value = placement.rotation.coerceIn(-180f, 180f),
                        valueRange = -180f..180f,
                        onValueChange = onUpdateRotation,
                        colors = SliderDefaults.colors(
                            thumbColor = StickerUiSecondary,
                            activeTrackColor = StickerUiSecondary,
                            inactiveTrackColor = StickerUiPrimary.copy(alpha = 0.28f),
                        ),
                    )
                }
                Column(
                    modifier = Modifier.width(104.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        onClick = { onNudgePosition(0f, -0.06f) },
                        shape = CircleShape,
                        color = StickerUiPrimary.copy(alpha = 0.18f),
                    ) {
                        Icon(
                            Icons.Rounded.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = StickerUiText,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            onClick = { onNudgePosition(-0.06f, 0f) },
                            shape = CircleShape,
                            color = StickerUiPrimary.copy(alpha = 0.18f),
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowLeft,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = StickerUiText,
                            )
                        }
                        Surface(
                            onClick = { onNudgePosition(0.06f, 0f) },
                            shape = CircleShape,
                            color = StickerUiPrimary.copy(alpha = 0.18f),
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = StickerUiText,
                            )
                        }
                    }
                    Surface(
                        onClick = { onNudgePosition(0f, 0.06f) },
                        shape = CircleShape,
                        color = StickerUiPrimary.copy(alpha = 0.18f),
                    ) {
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = StickerUiText,
                        )
                    }
                }
            }
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
                Image(
                    painter = painterResource(R.drawable.ic_sticker_premium_badge),
                    contentDescription = stringResource(R.string.cd_premium),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp),
                    contentScale = ContentScale.Fit,
                )
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
            modifier = Modifier
                .size(56.dp)
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
            Surface(
                onClick = onRemove,
                shape = CircleShape,
                color = StickerUiText,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .zIndex(1f),
            ) {
                Box(
                    modifier = Modifier.size(18.dp),
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
