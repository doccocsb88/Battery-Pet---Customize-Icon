package dev.hai.emojibattery.ui.screen


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.ImageView
import android.view.Gravity
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.provider.Settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.window.Dialog
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
import dev.hai.emojibattery.model.HomeBatteryItem
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
import dev.hai.emojibattery.model.StatusBarThemeTemplateCatalog
import dev.hai.emojibattery.model.ThemePreset
import dev.hai.emojibattery.billing.BillingUiState
import dev.hai.emojibattery.billing.GooglePlayPurchaseService
import dev.hai.emojibattery.billing.PurchaseService
import dev.hai.emojibattery.data.PadBackgroundTemplateCategory
import dev.hai.emojibattery.data.PadBackgroundTemplateItem
import dev.hai.emojibattery.data.PadBackgroundTemplateRepository
import dev.hai.emojibattery.paywall.LegalWebViewScreen
import dev.hai.emojibattery.paywall.PaywallScreen
import dev.hai.emojibattery.service.AccessibilityBridge
import dev.hai.emojibattery.service.NotchTemplateCatalog
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore
import dev.hai.emojibattery.ui.navigation.AppRoute
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush
import dev.hai.emojibattery.ui.theme.StrawberryMilk
import kotlinx.coroutines.delay

/**
 * Shape / layer-list drawables (e.g. gradient XML) are not supported by [painterResource];
 * use [ImageView] via [AndroidView] instead.
 */
@Composable
private fun ThemeShapeDrawableImage(
    drawableRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { view ->
            view.setImageResource(drawableRes)
            view.contentDescription = contentDescription
        },
        modifier = modifier,
    )
}

/** Preset swatches aligned with decompiled Percentage Color row (SubCustomBattery). */
private val StatusBarPercentageColorPresets = listOf<Long>(
    0xFF2196F3,
    0xFF4CAF50,
    0xFFFF9800,
    0xFF000000,
    0xFFFFEB3B,
)

private data class StatusBarTemplateUiEntry(
    val key: String,
    val assetUrl: String,
    val label: String,
)

/** Volio row for editor selection, or null if still using [SampleCatalog] ids. */
private fun statusBarBatteryItem(uiState: AppUiState, batteryPresetId: String): HomeBatteryItem? =
    uiState.statusBarCatalogItems.firstOrNull { it.id == batteryPresetId }

private fun statusBarEmojiItem(uiState: AppUiState, emojiPresetId: String): HomeBatteryItem? =
    uiState.statusBarCatalogItems.firstOrNull { it.id == emojiPresetId }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatusBarCustomScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectTab: (StatusBarTab) -> Unit,
    onSelectBattery: (String) -> Unit,
    onSelectEmoji: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onViewMoreBatteryChoices: () -> Unit,
    onViewMoreEmojiChoices: () -> Unit,
    onSetThemeBackgroundColor: (Long) -> Unit,
    onSetBackgroundTemplatePhoto: (String?) -> Unit,
    onViewMoreBackgroundTemplates: () -> Unit,
    onSetAccentColor: (Long) -> Unit,
    onSetStatusBarHeight: (Float) -> Unit,
    onSetLeftMargin: (Float) -> Unit,
    onSetRightMargin: (Float) -> Unit,
    onSetBatteryScale: (Float) -> Unit,
    onSetEmojiScale: (Float) -> Unit,
    onSetEmojiAdjustmentScale: (Float) -> Unit,
    onSetEmojiOffset: (Float, Float) -> Unit,
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
    val editorBg = colorResource(R.color.status_bar_editor_scaffold)
    val previewBrush = Brush.verticalGradient(
        listOf(
            colorResource(R.color.status_bar_preview_gradient_start),
            colorResource(R.color.status_bar_preview_gradient_end),
        ),
    )
    val applyBrush = Brush.horizontalGradient(
        listOf(
            colorResource(R.color.status_bar_apply_gradient_start),
            colorResource(R.color.status_bar_apply_gradient_end),
        ),
    )

    /** Matches original ViewPager switching pages: scroll the editor panel back to the top. */
    val editorScrollState = rememberScrollState()
    LaunchedEffect(uiState.activeStatusBarTab) {
        editorScrollState.scrollTo(0)
    }

    val maxPreviewRows = 6
    val maxVolioPreviewItems = StatusBarVolioGridColumns * maxPreviewRows
    var showEmojiAdjustment by remember { mutableStateOf(false) }
    val defaultEmojiAdjustmentScale = SampleCatalog.defaultConfig.emojiAdjustmentScale.coerceIn(0.35f, 2.2f)

    val selectBatteryWithAdjustment: (String) -> Unit = { id ->
        onSelectBattery(id)
        onSetEmojiAdjustmentScale(defaultEmojiAdjustmentScale)
        onSetEmojiOffset(0.5f, 0.5f)
        showEmojiAdjustment = true
    }
    val selectEmojiWithAdjustment: (String) -> Unit = { id ->
        onSelectEmoji(id)
        onSetEmojiAdjustmentScale(defaultEmojiAdjustmentScale)
        onSetEmojiOffset(0.5f, 0.5f)
        showEmojiAdjustment = true
    }
    Scaffold(
        containerColor = editorBg,
        topBar = {
            StatusBarCustomHeader(
                title = stringResource(R.string.status_bar_custom_title),
                onBack = onBack,
                onApply = onApply,
                applyBrush = applyBrush,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(editorScrollState)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PermissionBanner(enabled = uiState.accessibilityGranted, onToggle = onAccessibilityChanged)
            StatusBarLivePreviewCard(
                uiState = uiState,
                previewBrush = previewBrush,
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    OriginalStatusTabStrip(
                        selected = uiState.activeStatusBarTab,
                        onSelect = onSelectTab,
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        when (uiState.activeStatusBarTab) {
                                StatusBarTab.Battery -> {
                                    if (uiState.statusBarCatalogItems.isNotEmpty()) {
                                        val previewItems = uiState.statusBarCatalogItems.take(maxVolioPreviewItems)
                                        StatusBarVolioChoiceGrid(
                                            items = previewItems,
                                            selectedId = config.batteryPresetId,
                                            onSelect = selectBatteryWithAdjustment,
                                            previewImageUrl = { it.batteryArtUrl ?: it.thumbnailUrl },
                                        )
                                        if (uiState.statusBarCatalogItems.size > maxVolioPreviewItems) {
                                            OutlinedButton(
                                                onClick = onViewMoreBatteryChoices,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(999.dp),
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.view_more),
                                                    color = StrawberryMilk.Secondary,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                            }
                                        }
                                    } else {
                                        StatusBarChoiceGrid(
                                            maxItemsInEachRow = 4,
                                            labels = batteryPresets.map { it.name },
                                            selectedLabel = batteryPresets.firstOrNull { it.id == config.batteryPresetId }?.name.orEmpty(),
                                            onClick = { label ->
                                                batteryPresets.firstOrNull { it.name == label }?.let { selectBatteryWithAdjustment(it.id) }
                                            },
                                            icon = { label ->
                                                Text(
                                                    text = batteryPresets.first { it.name == label }.body,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    textAlign = TextAlign.Center,
                                                )
                                            },
                                        )
                                    }
                                    StatusBarPercentageSection(
                                        value = config.batteryPercentScale,
                                        onValueChange = onSetBatteryScale,
                                    )
                                    StatusBarPercentageColorRow(
                                        selectedArgb = config.accentColor,
                                        presets = StatusBarPercentageColorPresets,
                                        onSelect = onSetAccentColor,
                                    )
                                }
                                StatusBarTab.Emoji -> {
                                    if (uiState.statusBarCatalogItems.isNotEmpty()) {
                                        val previewItems = uiState.statusBarCatalogItems.take(maxVolioPreviewItems)
                                        StatusBarVolioChoiceGrid(
                                            items = previewItems,
                                            selectedId = config.emojiPresetId,
                                            onSelect = selectEmojiWithAdjustment,
                                            previewImageUrl = { it.emojiArtUrl ?: it.thumbnailUrl },
                                        )
                                        if (uiState.statusBarCatalogItems.size > maxVolioPreviewItems) {
                                            OutlinedButton(
                                                onClick = onViewMoreEmojiChoices,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(999.dp),
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.view_more),
                                                    color = StrawberryMilk.Secondary,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                            }
                                        }
                                    } else {
                                        StatusBarChoiceGrid(
                                            maxItemsInEachRow = 4,
                                            labels = emojiPresets.map { it.name },
                                            selectedLabel = emojiPresets.firstOrNull { it.id == config.emojiPresetId }?.name.orEmpty(),
                                            onClick = { label ->
                                                emojiPresets.firstOrNull { it.name == label }?.let { selectEmojiWithAdjustment(it.id) }
                                            },
                                            icon = { label ->
                                                Text(
                                                    text = emojiPresets.first { it.name == label }.glyph,
                                                    style = MaterialTheme.typography.headlineMedium,
                                                )
                                            },
                                        )
                                    }
                                }
                                StatusBarTab.Theme -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    StatusBarThemeBackgroundColorRow(
                                        selectedArgb = config.backgroundColor,
                                        onSelect = onSetThemeBackgroundColor,
                                    )
                                    StatusBarBackgroundTemplateSection(
                                        selectedPhotoUrl = config.backgroundTemplatePhotoUrl,
                                        selectedDrawableRes = config.backgroundTemplateDrawableRes,
                                        onSelectPhoto = onSetBackgroundTemplatePhoto,
                                        onViewMore = onViewMoreBackgroundTemplates,
                                    )
                                    StatusBarChoiceGrid(
                                        maxItemsInEachRow = 4,
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
                                }
                                StatusBarTab.Settings -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        StatusSliderRow(stringResource(R.string.status_bar_height), config.statusBarHeight, onSetStatusBarHeight)
                                        StatusSliderRow(stringResource(R.string.status_bar_left_margin), config.leftMargin, onSetLeftMargin)
                                        StatusSliderRow(stringResource(R.string.status_bar_right_margin), config.rightMargin, onSetRightMargin)
                                        StatusColorRow(stringResource(R.string.status_bar_icon_color), Color(config.accentColor))
                                        StatusColorRow(stringResource(R.string.status_bar_background_color), Color(config.backgroundColor))
                                        StatusSwitchRow(stringResource(R.string.show_percentage), config.showPercentage, onTogglePercentage)
                                        StatusSwitchRow(stringResource(R.string.animate_charge), config.animateCharge, onToggleAnimate)
                                        StatusSwitchRow(stringResource(R.string.show_stroke), config.showStroke, onToggleStroke)
                                        StatusSliderRow(stringResource(R.string.battery_text_size), config.batteryPercentScale, onSetBatteryScale)
                                        StatusSliderRow(stringResource(R.string.emoji_size_label), config.emojiScale, onSetEmojiScale)
                                        OutlinedButton(
                                            onClick = onRestore,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                        ) {
                                            Text(stringResource(R.string.restore_applied))
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
    if (showEmojiAdjustment) {
        StatusBarEmojiAdjustmentDialog(
            uiState = uiState,
            onSetEmojiAdjustmentScale = onSetEmojiAdjustmentScale,
            onSetEmojiOffset = onSetEmojiOffset,
            onDismiss = { showEmojiAdjustment = false },
        )
    }
}

@Composable
private fun StatusBarCustomHeader(
    title: String,
    onBack: () -> Unit,
    onApply: () -> Unit,
    applyBrush: Brush,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back_40_new),
                contentDescription = stringResource(R.string.cd_back),
                modifier = Modifier.size(36.dp),
            )
        }
        Text(
            text = title,
            modifier = Modifier
                .padding(start = 4.dp),
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(applyBrush)
                .clickable(onClick = onApply)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.apply),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
            )
        }
    }
}

@Composable
private fun StatusBarEmojiAdjustmentDialog(
    uiState: AppUiState,
    onSetEmojiAdjustmentScale: (Float) -> Unit,
    onSetEmojiOffset: (Float, Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val config = uiState.editingConfig
    val batteryVolio = statusBarBatteryItem(uiState, config.batteryPresetId)
    val emojiVolio = statusBarEmojiItem(uiState, config.emojiPresetId)
    val emojiGlyph = SampleCatalog.emojiPresets.firstOrNull { it.id == config.emojiPresetId }?.glyph ?: "●"
    val density = androidx.compose.ui.platform.LocalDensity.current
    var containerWidthPx by remember { mutableStateOf(1f) }
    var containerHeightPx by remember { mutableStateOf(1f) }
    var emojiScale by remember(config.emojiPresetId, config.batteryPresetId) {
        mutableStateOf(config.emojiAdjustmentScale.coerceIn(0.35f, 2.2f))
    }
    var emojiOffsetX by remember(config.emojiPresetId, config.batteryPresetId) {
        mutableStateOf(0.5f)
    }
    var emojiOffsetY by remember(config.emojiPresetId, config.batteryPresetId) {
        mutableStateOf(0.5f)
    }
    val batteryArtUrl = batteryVolio?.batteryArtUrl?.takeIf { it.isNotBlank() }
        ?: batteryVolio?.thumbnailUrl?.takeIf { it.isNotBlank() }
    val batteryArtDrawableRes = batteryVolio?.previewRes?.takeIf { it != 0 }
    val emojiArtUrl = emojiVolio?.emojiArtUrl?.takeIf { it.isNotBlank() }
    val emojiArtDrawableRes = emojiVolio?.previewRes?.takeIf { it != 0 }

    fun commitScale(next: Float) {
        val value = next.coerceIn(0.35f, 2.2f)
        emojiScale = value
        onSetEmojiAdjustmentScale(value)
    }

    fun commitOffset(nextX: Float, nextY: Float) {
        val clampedX = nextX.coerceIn(0f, 1f)
        val clampedY = nextY.coerceIn(0f, 1f)
        emojiOffsetX = clampedX
        emojiOffsetY = clampedY
        onSetEmojiOffset(clampedX, clampedY)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Emoji Adjustment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Drag emoji to move. Drag 4 corner handles to resize.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(config.backgroundColor))
                        .onSizeChanged {
                            containerWidthPx = it.width.toFloat().coerceAtLeast(1f)
                            containerHeightPx = it.height.toFloat().coerceAtLeast(1f)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val batterySizePx = (minOf(containerWidthPx, containerHeightPx) * 0.62f).coerceAtLeast(120f)
                    val batterySizeDp = with(density) { batterySizePx.toDp() }
                    val emojiSizePx = (batterySizePx * emojiScale.coerceIn(0.35f, 2.2f)).coerceAtLeast(20f)
                    val emojiSizeDp = with(density) { emojiSizePx.toDp() }
                    val emojiCenterX = containerWidthPx * emojiOffsetX
                    val emojiCenterY = containerHeightPx * emojiOffsetY
                    val emojiLeftPx = (emojiCenterX - (emojiSizePx / 2f)).coerceIn(-emojiSizePx, containerWidthPx)
                    val emojiTopPx = (emojiCenterY - (emojiSizePx / 2f)).coerceIn(-emojiSizePx, containerHeightPx)
                    val emojiLeftInt = emojiLeftPx.roundToInt()
                    val emojiTopInt = emojiTopPx.roundToInt()
                    val resizeScaleFactor = 280f

                    when {
                        batteryArtUrl != null -> {
                            AsyncImage(
                                model = batteryArtUrl,
                                contentDescription = batteryVolio?.title,
                                modifier = Modifier.size(batterySizeDp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                        batteryArtDrawableRes != null -> {
                            Image(
                                painter = painterResource(batteryArtDrawableRes),
                                contentDescription = batteryVolio?.title,
                                modifier = Modifier.size(batterySizeDp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset { IntOffset(emojiLeftInt, emojiTopInt) }
                            .size(emojiSizeDp),
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(containerWidthPx, containerHeightPx) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val nextX = emojiOffsetX + (dragAmount.x / containerWidthPx)
                                        val nextY = emojiOffsetY + (dragAmount.y / containerHeightPx)
                                        commitOffset(nextX, nextY)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                emojiArtUrl != null -> {
                                    AsyncImage(
                                        model = emojiArtUrl,
                                        contentDescription = emojiVolio?.title,
                                        modifier = Modifier.matchParentSize(),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                                emojiArtDrawableRes != null -> {
                                    Image(
                                        painter = painterResource(emojiArtDrawableRes),
                                        contentDescription = emojiVolio?.title,
                                        modifier = Modifier.matchParentSize(),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                                else -> {
                                    Text(
                                        text = emojiGlyph,
                                        fontSize = with(density) { (emojiSizePx * 0.52f).toSp() },
                                    )
                                }
                            }
                        }

                        EmojiDashFrame(
                            modifier = Modifier.matchParentSize(),
                        )
                        EmojiResizeHandle(
                            modifier = Modifier.align(Alignment.TopStart),
                            onDrag = { drag ->
                                commitScale(emojiScale + ((-drag.x - drag.y) / resizeScaleFactor))
                            },
                        )
                        EmojiResizeHandle(
                            modifier = Modifier.align(Alignment.TopEnd),
                            onDrag = { drag ->
                                commitScale(emojiScale + ((drag.x - drag.y) / resizeScaleFactor))
                            },
                        )
                        EmojiResizeHandle(
                            modifier = Modifier.align(Alignment.BottomStart),
                            onDrag = { drag ->
                                commitScale(emojiScale + ((-drag.x + drag.y) / resizeScaleFactor))
                            },
                        )
                        EmojiResizeHandle(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            onDrag = { drag ->
                                commitScale(emojiScale + ((drag.x + drag.y) / resizeScaleFactor))
                            },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.common_cancel))
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiDashFrame(
    modifier: Modifier = Modifier,
) {
    val handleInset = 10.dp
    Canvas(
        modifier = modifier,
    ) {
        val inset = handleInset.toPx()
        val dash = 8.dp.toPx()
        val gap = 7.dp.toPx()
        val stroke = 2.dp.toPx()
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)
        val color = StrawberryMilk.Secondary

        drawLine(
            color = color,
            start = Offset(inset, inset),
            end = Offset(size.width - inset, inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dashEffect,
        )
        drawLine(
            color = color,
            start = Offset(inset, size.height - inset),
            end = Offset(size.width - inset, size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dashEffect,
        )
        drawLine(
            color = color,
            start = Offset(inset, inset),
            end = Offset(inset, size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dashEffect,
        )
        drawLine(
            color = color,
            start = Offset(size.width - inset, inset),
            end = Offset(size.width - inset, size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dashEffect,
        )
    }
}

@Composable
private fun EmojiResizeHandle(
    modifier: Modifier = Modifier,
    onDrag: (androidx.compose.ui.geometry.Offset) -> Unit,
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, StrawberryMilk.Secondary, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            },
    )
}

@Composable
private fun StatusBarLivePreviewCard(
    uiState: AppUiState,
    previewBrush: Brush,
) {
    val context = LocalContext.current
    val config = uiState.editingConfig
    val notchTemplate = remember(context) {
        NotchTemplateCatalog.resolve(OverlayConfigStore.read(context).notchTemplateId)
    }
    val dateTimeConfig = uiState.featureConfigs[CustomizeEntry.DateTime]
        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.DateTime]
        ?: FeatureConfig(variant = encodeDateTimeVariant(parseDateTimeVariant(null)))
    val chargeConfig = uiState.featureConfigs[CustomizeEntry.Charge]
        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Charge]
        ?: FeatureConfig(enabled = false, variant = "chg_1")
    val wifiConfig = uiState.featureConfigs[CustomizeEntry.Wifi]
        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Wifi]
        ?: FeatureConfig(enabled = false, variant = "blue")
    val signalConfig = uiState.featureConfigs[CustomizeEntry.Signal]
        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Signal]
        ?: FeatureConfig(enabled = false, variant = "blue")
    val dataConfig = uiState.featureConfigs[CustomizeEntry.Data]
        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Data]
        ?: FeatureConfig(enabled = false, variant = "5G::blue")
    val hotspotConfig = uiState.featureConfigs[CustomizeEntry.Hotspot]
        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Hotspot]
        ?: FeatureConfig(enabled = false, variant = "blue")
    val airplaneConfig = uiState.featureConfigs[CustomizeEntry.Airplane]
        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Airplane]
        ?: FeatureConfig(enabled = false, variant = "blue")
    val ringerConfig = uiState.featureConfigs[CustomizeEntry.Ringer]
        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Ringer]
        ?: FeatureConfig(enabled = false, variant = "style=bell;color=blue")
    val parsedRinger = parseRingerVariant(ringerConfig.variant)
    val parsedDateTime = parseDateTimeVariant(dateTimeConfig.variant)
    val datePreview = previewDateStyle(parsedDateTime.styleId)
    val batteryVolio = statusBarBatteryItem(uiState, config.batteryPresetId)
    val emojiVolio = statusBarEmojiItem(uiState, config.emojiPresetId)
    val batteryBody = SampleCatalog.batteryPresets.firstOrNull { it.id == config.batteryPresetId }?.body
        ?: if (batteryVolio != null) "▰▰▰▱" else "▰▰▰▱"
    val emojiGlyph = SampleCatalog.emojiPresets.firstOrNull { it.id == config.emojiPresetId }?.glyph
        ?: if (emojiVolio != null) "●" else "●"
    val batteryText = batteryBody
    val demoPercent = 56
    val percentageText = if (config.showPercentage) " $demoPercent%" else ""
    val chargeVariant = parseChargeVariant(chargeConfig.variant)
    val chargeDrawableName = chargeVariantDrawableName(chargeVariant)
    val chargeDrawableRes = chargeDrawableName?.takeIf { it.isNotBlank() }?.let {
        context.resources.getIdentifier(it, "drawable", context.packageName)
    }?.takeIf { it != 0 }
    val chargeText = if (chargeConfig.enabled) previewChargeGlyph(chargeConfig.variant) else ""
    val airplaneVisible = airplaneConfig.enabled && previewAirplaneModeOn(context)
    val hotspotVisible = hotspotConfig.enabled && previewHotspotOn(context)
    val wifiVisible = wifiConfig.enabled
    val dataVisible = !wifiVisible && dataConfig.enabled
    val signalVisible = signalConfig.enabled && !airplaneVisible
    val dataStyleId = previewDataStyleVariant(dataConfig.variant)
    val dataColorId = previewDataColorVariant(dataConfig.variant)
    val previewRingerMode = previewRingerMode(context)
    val ringerVisible = ringerConfig.enabled && previewRingerMode != AudioManager.RINGER_MODE_NORMAL
    val ringerRes = previewRingerIconRes(context, previewRingerMode, parsedRinger.styleId)
    val rightText = "$batteryText$percentageText".trim()
    val batteryArtUrl = batteryVolio?.batteryArtUrl?.takeIf { it.isNotBlank() }
        ?: batteryVolio?.thumbnailUrl?.takeIf { it.isNotBlank() }
    val batteryArtDrawableRes = batteryVolio?.previewRes?.takeIf { it != 0 }
    val emojiArtUrl = emojiVolio?.emojiArtUrl?.takeIf { it.isNotBlank() }
    val emojiArtDrawableRes = emojiVolio?.previewRes?.takeIf { it != 0 }
    val defaultCommonScale = SampleCatalog.defaultConfig.emojiScale.coerceAtLeast(0.01f)
    val commonScaleFactor = (config.emojiScale.coerceIn(0f, 1f) / defaultCommonScale).coerceIn(0.35f, 2.2f)
    val horizontalStart = (8f + config.leftMargin.coerceIn(0f, 1f) * 88f).dp
    val horizontalEnd = (8f + config.rightMargin.coerceIn(0f, 1f) * 88f).dp
    val verticalPad = (4f + config.statusBarHeight.coerceIn(0f, 1f) * 12f).dp
    val previewHeight = (62f + config.statusBarHeight.coerceIn(0f, 1f) * 34f).dp
    val rowBgColor = Color.Transparent
    val batteryFontSize = (11f + (config.batteryPercentScale.coerceIn(0f, 1f) * 11f)).sp
    val batteryPreviewSize = (18f * commonScaleFactor).dp
    val emojiPreviewSize = (batteryPreviewSize * config.emojiAdjustmentScale.coerceIn(0.35f, 2.2f))
    val emojiPreviewTextSize = (10f + config.emojiAdjustmentScale.coerceIn(0.35f, 2.2f) * 10f).sp
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.status_bar_editor_scaffold)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            val templateRes = config.backgroundTemplateDrawableRes?.takeIf { it != 0 }
            val templateUrl = config.backgroundTemplatePhotoUrl?.takeIf { !it.isNullOrBlank() }
            when {
                templateUrl != null -> {
                    AsyncImage(
                        model = templateUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                templateRes != null -> {
                    ThemeShapeDrawableImage(
                        drawableRes = templateRes,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> Box(Modifier.fillMaxSize().background(Color(config.backgroundColor)))
            }
            notchTemplate.drawableRes?.let { notchDrawable ->
                Image(
                    painter = painterResource(notchDrawable),
                    contentDescription = null,
                    modifier = Modifier
                        .align(notchPreviewAlignment(notchTemplate.gravity))
                        .padding(top = 2.dp)
                        .size(width = 102.dp, height = 14.dp),
                    contentScale = ContentScale.FillBounds,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = horizontalStart, end = horizontalEnd),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = rowBgColor,
                    shape = RoundedCornerShape(14.dp),
                    border = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = verticalPad, bottom = verticalPad),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            Text(
                                stringResource(R.string.demo_time),
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                            )
                            if (parsedDateTime.showDate) {
                                Text(
                                    datePreview.line1,
                                    color = Color(0xFF555555),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                                )
                                datePreview.line2?.let { line2 ->
                                    Text(
                                        line2,
                                        color = Color(0xFF555555),
                                        fontSize = 10.sp,
                                        fontWeight = if (datePreview.line2Bold) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                                    )
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (ringerVisible) {
                                Image(
                                    painter = painterResource(ringerRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(parsedRinger.colorId, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (airplaneVisible) {
                                Image(
                                    painter = painterResource(R.drawable.galaxy_airplane),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(airplaneConfig.variant, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (hotspotVisible) {
                                Image(
                                    painter = painterResource(R.drawable.ic_item_hotspot),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(hotspotConfig.variant, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (hotspotVisible) {
                                Image(
                                    painter = painterResource(R.drawable.ic_item_hotspot),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(hotspotConfig.variant, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (wifiVisible) {
                                Image(
                                    painter = painterResource(R.drawable.galaxy_wifi_4s),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(wifiConfig.variant, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (dataVisible) {
                                Image(
                                    painter = painterResource(previewDataIconRes(dataStyleId)),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(dataColorId, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (signalVisible) {
                                Image(
                                    painter = painterResource(previewSignalIconRes()),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(signalConfig.variant, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            Text(
                                if (batteryArtUrl != null || batteryArtDrawableRes != null) {
                                    percentageText.trim()
                                } else {
                                    rightText
                                },
                                color = Color(config.accentColor),
                                fontSize = batteryFontSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
                            )
                            StatusBarBatteryEmojiCompositePreview(
                                batteryArtUrl = batteryArtUrl,
                                batteryArtDrawableRes = batteryArtDrawableRes,
                                batteryContentDescription = batteryVolio?.title,
                                emojiArtUrl = emojiArtUrl,
                                emojiArtDrawableRes = emojiArtDrawableRes,
                                emojiContentDescription = emojiVolio?.title,
                                emojiFallbackGlyph = emojiGlyph,
                                emojiFallbackColor = Color(0xFF333333),
                                batterySize = batteryPreviewSize,
                                emojiSize = emojiPreviewSize,
                                emojiTextSize = emojiPreviewTextSize,
                                emojiOffsetX = config.emojiOffsetX,
                                emojiOffsetY = config.emojiOffsetY,
                            )
                            when {
                                chargeConfig.enabled && chargeDrawableRes != null -> {
                                    Image(
                                        painter = painterResource(chargeDrawableRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                                chargeText.isNotBlank() -> {
                                    Text(
                                        chargeText,
                                        color = Color(config.accentColor),
                                        fontSize = batteryFontSize,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun notchPreviewAlignment(gravity: Int): Alignment {
    val isTop = (gravity and Gravity.TOP) == Gravity.TOP
    return if (isTop) Alignment.TopCenter else Alignment.Center
}

private fun previewChargeGlyph(variant: String): String = when (variant.lowercase()) {
    "chg_2" -> "↯"
    "chg_3" -> "⌁"
    "chg_4" -> "⏻"
    "chg_5" -> "🔌"
    "chg_6" -> "⏚"
    "chg_7" -> "ϟ"
    "chg_8" -> "⌬"
    "chg_9" -> "⎓"
    "chg_10" -> "⟡"
    "chg_11" -> "⌇"
    "chg_12" -> "⋇"
    else -> "⚡"
}

private fun previewAirplaneModeOn(context: Context): Boolean =
    Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1

private fun previewHotspotOn(context: Context): Boolean {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return false
    return runCatching {
        val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
        method.isAccessible = true
        (method.invoke(wifiManager) as? Boolean) == true
    }.getOrDefault(false)
}

private fun previewRingerMode(context: Context): Int =
    context.getSystemService(AudioManager::class.java)?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL

private fun previewRingerIconRes(context: Context, ringerMode: Int, fallbackStyleId: String): Int {
    val customName = ringerDrawableName(
        when (ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> fallbackStyleId
            AudioManager.RINGER_MODE_VIBRATE -> fallbackStyleId
            else -> fallbackStyleId
        },
        ringerMode,
    )
    val customRes = customName?.let { context.resources.getIdentifier(it, "drawable", context.packageName) }?.takeIf { it != 0 }
    if (customRes != null) return customRes
    return when (ringerMode) {
        AudioManager.RINGER_MODE_SILENT -> android.R.drawable.ic_lock_silent_mode
        AudioManager.RINGER_MODE_VIBRATE -> R.drawable.ic_vibrate_feedback_32
        else -> when (fallbackStyleId.lowercase()) {
            "mute" -> android.R.drawable.ic_lock_silent_mode
            "wave" -> R.drawable.ic_vibrate_feedback_32
            else -> android.R.drawable.ic_lock_silent_mode_off
        }
    }
}

private fun previewResolveColorFromVariant(variant: String?, fallback: Int): Int {
    val raw = variant.orEmpty()
    if (raw.startsWith("picker#", ignoreCase = true)) {
        return raw.removePrefix("picker#").toLongOrNull(16)?.toInt() ?: fallback
    }
    return when (raw.lowercase()) {
        "blue" -> 0xFF2952F4.toInt()
        "green" -> 0xFF2BDF52.toInt()
        "orange" -> 0xFFF18410.toInt()
        "black" -> 0xFF11111A.toInt()
        "yellow" -> 0xFFF1DF1E.toInt()
        else -> fallback
    }
}

private fun previewSignalIconRes(level: Int = 3): Int = when (level.coerceIn(0, 4)) {
    0 -> R.drawable.galaxy_signal_0
    1 -> R.drawable.galaxy_signal_1
    2 -> R.drawable.galaxy_signal_2
    3 -> R.drawable.galaxy_signal_3
    else -> R.drawable.galaxy_signal_4
}

private fun previewDataIconRes(styleId: String): Int = when (styleId.lowercase()) {
    "2g" -> R.drawable.galaxy_data_2g
    "3g" -> R.drawable.galaxy_data_3g
    "4g" -> R.drawable.galaxy_data_4g
    "6g" -> R.drawable.galaxy_data_6g
    else -> R.drawable.galaxy_data_5g
}

private fun previewDataStyleVariant(raw: String): String = raw.substringBefore("::").ifBlank { "5G" }

private fun previewDataColorVariant(raw: String): String {
    val colorId = raw.substringAfter("::", "")
    return if (colorId.isBlank()) "blue" else colorId
}

@Composable
private fun StatusBarBatteryEmojiCompositePreview(
    batteryArtUrl: String?,
    batteryArtDrawableRes: Int?,
    batteryContentDescription: String?,
    emojiArtUrl: String?,
    emojiArtDrawableRes: Int?,
    emojiContentDescription: String?,
    emojiFallbackGlyph: String,
    emojiFallbackColor: Color,
    batterySize: Dp,
    emojiSize: Dp,
    emojiTextSize: TextUnit,
    emojiOffsetX: Float = 0.5f,
    emojiOffsetY: Float = 0.5f,
) {
    val hasBatteryArt = batteryArtUrl != null || batteryArtDrawableRes != null
    val hasEmojiArt = emojiArtUrl != null || emojiArtDrawableRes != null
    val hasEmojiFallback = !hasEmojiArt && emojiFallbackGlyph.isNotBlank()
    if (!hasBatteryArt && !hasEmojiArt && !hasEmojiFallback) return
    val containerSize = if (emojiSize.value > batterySize.value) emojiSize else batterySize
    val offsetX = emojiOffsetX.coerceIn(0f, 1f)
    val offsetY = emojiOffsetY.coerceIn(0f, 1f)
    val emojiTranslationFactor = containerSize * 0.55f
    val emojiX = (offsetX - 0.5f) * 2f * emojiTranslationFactor.value
    val emojiY = (offsetY - 0.5f) * 2f * emojiTranslationFactor.value
    val emojiOffsetModifier = Modifier.offset(emojiX.dp, emojiY.dp)
    Box(
        modifier = Modifier.size(containerSize),
        contentAlignment = Alignment.Center,
    ) {
        when {
            batteryArtUrl != null -> {
                AsyncImage(
                    model = batteryArtUrl,
                    contentDescription = batteryContentDescription,
                    modifier = Modifier
                        .size(batterySize)
                        .clip(RoundedCornerShape(5.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            batteryArtDrawableRes != null -> {
                Image(
                    painter = painterResource(batteryArtDrawableRes),
                    contentDescription = batteryContentDescription,
                    modifier = Modifier.size(batterySize),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        when {
            emojiArtUrl != null -> {
                AsyncImage(
                    model = emojiArtUrl,
                    contentDescription = emojiContentDescription,
                    modifier = Modifier
                        .then(emojiOffsetModifier)
                        .size(emojiSize)
                        .clip(RoundedCornerShape(5.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            emojiArtDrawableRes != null -> {
                Image(
                    painter = painterResource(emojiArtDrawableRes),
                    contentDescription = emojiContentDescription,
                    modifier = Modifier
                        .then(emojiOffsetModifier)
                        .size(emojiSize),
                    contentScale = ContentScale.Fit,
                )
            }
            hasEmojiFallback -> {
                Text(
                    text = emojiFallbackGlyph,
                    color = emojiFallbackColor,
                    fontSize = emojiTextSize,
                    modifier = emojiOffsetModifier,
                )
            }
        }
    }
}

@Composable
private fun StatusBarPercentageSection(
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val labelColor = colorResource(R.color.splash_title)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 6.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_headline_section),
                contentDescription = null,
                modifier = Modifier.height(12.dp),
            )
            Text(
                stringResource(R.string.percentage),
                color = labelColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0.3f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = StrawberryMilk.Secondary,
                    activeTrackColor = StrawberryMilk.Secondary,
                ),
            )
            Text(
                stringResource(
                    R.string.status_bar_value_dp,
                    (value * 100).toInt(),
                ),
                color = labelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
            )
        }
    }
}

@Composable
private fun StatusBarPercentageColorRow(
    selectedArgb: Long,
    presets: List<Long>,
    onSelect: (Long) -> Unit,
) {
    val labelColor = colorResource(R.color.splash_title)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 6.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_headline_section),
                contentDescription = null,
                modifier = Modifier.height(12.dp),
            )
            Text(
                stringResource(R.string.color_percentage),
                color = labelColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            items(
                items = presets,
                key = { it },
            ) { c ->
                val selected = selectedArgb == c
                val yellowSwatch = StatusBarPercentageColorPresets[4]
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) StrawberryMilk.Secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape,
                        )
                        .background(Color(c))
                        .clickable { onSelect(c) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (c == yellowSwatch) Color.Black.copy(alpha = 0.6f) else Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBarThemeBackgroundColorRow(
    selectedArgb: Long,
    onSelect: (Long) -> Unit,
) {
    val labelColor = colorResource(R.color.splash_title)
    var showColorWheelPicker by remember { mutableStateOf(false) }
    var pickHue by remember { mutableStateOf(0f) }
    var pickSaturation by remember { mutableStateOf(0f) }
    var pickValue by remember { mutableStateOf(1f) }
    LaunchedEffect(showColorWheelPicker, selectedArgb) {
        if (showColorWheelPicker) {
            val hsv = FloatArray(3)
            AndroidColor.colorToHSV(selectedArgb.toInt(), hsv)
            pickHue = hsv[0].coerceIn(0f, 360f)
            pickSaturation = hsv[1].coerceIn(0f, 1f)
            pickValue = hsv[2].coerceIn(0f, 1f)
        }
    }

    if (showColorWheelPicker) {
        val dialogTextColor = colorResource(R.color.splash_title)
        val pickedColorInt = AndroidColor.HSVToColor(floatArrayOf(pickHue, pickSaturation, pickValue))
        val pickedColor = Color(pickedColorInt)
        AlertDialog(
            onDismissRequest = { showColorWheelPicker = false },
            title = { Text(stringResource(R.string.theme_background_color)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(pickedColor),
                    )
                    StatusBarColorWheel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp)),
                        hue = pickHue,
                        saturation = pickSaturation,
                        onChange = { h, s ->
                            pickHue = h
                            pickSaturation = s
                        },
                    )
                    Text("Brightness", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = pickValue,
                        onValueChange = { pickValue = it.coerceIn(0f, 1f) },
                        valueRange = 0f..1f,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val argb = pickedColorInt.toLong() and 0xFFFFFFFFL
                        onSelect(argb)
                        showColorWheelPicker = false
                    },
                ) {
                    Text(
                        text = stringResource(R.string.apply),
                        color = StrawberryMilk.Secondary,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showColorWheelPicker = false }) {
                    Text(
                        text = stringResource(android.R.string.cancel),
                        color = StrawberryMilk.Secondary,
                    )
                }
            },
            containerColor = Color.White,
            tonalElevation = 0.dp,
            titleContentColor = dialogTextColor,
            textContentColor = dialogTextColor,
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 6.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_headline_section),
                contentDescription = null,
                modifier = Modifier.height(12.dp),
            )
            Text(
                stringResource(R.string.theme_background_color),
                color = labelColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
            )
        }
        val colorSwatchScroll = rememberScrollState()
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(colorSwatchScroll)
                .padding(top = 4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = CircleShape,
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { showColorWheelPicker = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Palette,
                    contentDescription = stringResource(R.string.theme_background_color),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            StatusBarPercentageColorPresets.forEach { c ->
                val selected = selectedArgb == c
                val yellowSwatch = StatusBarPercentageColorPresets[4]
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) StrawberryMilk.Secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape,
                        )
                        .background(Color(c))
                        .clickable { onSelect(c) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (c == yellowSwatch) Color.Black.copy(alpha = 0.6f) else Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBarColorWheel(
    modifier: Modifier = Modifier,
    hue: Float,
    saturation: Float,
    onChange: (Float, Float) -> Unit,
) {
    var wheelSizePx by remember { mutableStateOf(0) }
    val wheelBitmap: ImageBitmap? = remember(wheelSizePx) {
        if (wheelSizePx <= 0) {
            null
        } else {
            createStatusBarColorWheelBitmap(wheelSizePx)
        }
    }

    fun updateFromOffset(offset: Offset) {
        if (wheelSizePx <= 0) return
        val center = wheelSizePx / 2f
        val dx = offset.x - center
        val dy = offset.y - center
        val radius = center.coerceAtLeast(1f)
        val distance = sqrt((dx * dx) + (dy * dy))
        val sat = (distance / radius).coerceIn(0f, 1f)
        val angleDeg = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 360.0) % 360.0).toFloat()
        onChange(angleDeg, sat)
    }

    Canvas(
        modifier = modifier
            .onSizeChanged {
                wheelSizePx = minOf(it.width, it.height)
            }
            .pointerInput(wheelSizePx) {
                detectTapGestures { offset ->
                    updateFromOffset(offset)
                }
            }
            .pointerInput(wheelSizePx) {
                detectDragGestures { change, _ ->
                    change.consume()
                    updateFromOffset(change.position)
                }
            },
    ) {
        val bitmap = wheelBitmap
        if (bitmap != null) {
            drawImage(bitmap)
        }
        val radius = size.minDimension / 2f
        val angleRad = Math.toRadians(hue.toDouble())
        val markerDistance = saturation.coerceIn(0f, 1f) * radius
        val markerCenter = Offset(
            x = center.x + (cos(angleRad) * markerDistance).toFloat(),
            y = center.y + (sin(angleRad) * markerDistance).toFloat(),
        )
        drawCircle(
            color = Color.White,
            radius = 9.dp.toPx(),
            center = markerCenter,
            style = Stroke(width = 2.5.dp.toPx()),
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.35f),
            radius = 11.dp.toPx(),
            center = markerCenter,
            style = Stroke(width = 1.5.dp.toPx()),
        )
    }
}

private fun createStatusBarColorWheelBitmap(sizePx: Int): ImageBitmap {
    val safeSize = sizePx.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
    val center = safeSize / 2f
    val radius = center.coerceAtLeast(1f)
    val pixels = IntArray(safeSize * safeSize)

    for (y in 0 until safeSize) {
        for (x in 0 until safeSize) {
            val dx = x - center
            val dy = y - center
            val distance = sqrt((dx * dx) + (dy * dy))
            val index = y * safeSize + x
            if (distance <= radius) {
                val sat = (distance / radius).coerceIn(0f, 1f)
                val hue = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 360.0) % 360.0).toFloat()
                pixels[index] = AndroidColor.HSVToColor(floatArrayOf(hue, sat, 1f))
            } else {
                pixels[index] = AndroidColor.TRANSPARENT
            }
        }
    }
    bitmap.setPixels(pixels, 0, safeSize, 0, 0, safeSize, safeSize)
    return bitmap.asImageBitmap()
}

/**
 * Background templates in original decompiled flow are local (`C5914jz0.a.c()`), not remote API.
 * Clone maps that local list to bundled drawable previews ([StatusBarThemeTemplateCatalog]).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusBarBackgroundTemplateSection(
    selectedPhotoUrl: String?,
    selectedDrawableRes: Int?,
    onSelectPhoto: (String?) -> Unit,
    onViewMore: () -> Unit,
) {
    val labelColor = colorResource(R.color.splash_title)
    val context = LocalContext.current.applicationContext
    val maxPreviewRows = 6
    val maxPreviewItems = 3 * maxPreviewRows
    val padCategories = remember { mutableStateListOf<PadBackgroundTemplateCategory>() }
    val loadedItemsByPack = remember { mutableStateMapOf<String, List<PadBackgroundTemplateItem>>() }
    val attemptedPacks = remember { mutableStateMapOf<String, Boolean>() }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var loadingPack by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        padCategories.clear()
        padCategories.addAll(PadBackgroundTemplateRepository.loadCategories(context))
    }

    val selectedCategory = padCategories.getOrNull((selectedTabIndex - 1).coerceAtLeast(0))
        ?.takeIf { selectedTabIndex > 0 }

    LaunchedEffect(selectedCategory?.deliveryPackName) {
        val category = selectedCategory ?: return@LaunchedEffect
        val cached = loadedItemsByPack[category.deliveryPackName]
        if (cached != null && cached.isNotEmpty()) return@LaunchedEffect
        attemptedPacks[category.deliveryPackName] = true
        loadingPack = category.deliveryPackName
        val loaded = PadBackgroundTemplateRepository.loadItemsForCategory(context, category)
        if (loaded.isNotEmpty()) {
            loadedItemsByPack[category.deliveryPackName] = loaded
        } else {
            loadedItemsByPack.remove(category.deliveryPackName)
        }
        loadingPack = null
    }

    val selectedAssetUrl = selectedPhotoUrl
        ?: StatusBarThemeTemplateCatalog.entryForPreviewDrawable(selectedDrawableRes)
            ?.let { StatusBarThemeTemplateCatalog.assetUri(it.assetRelativePath) }

    val tabs = buildList {
        add("Built-in")
        addAll(padCategories.map { it.title?.takeIf { name -> name.isNotBlank() } ?: it.packName })
    }

    val entries: List<StatusBarTemplateUiEntry> = if (selectedTabIndex == 0) {
        StatusBarThemeTemplateCatalog.entries.take(maxPreviewItems).map { entry ->
            StatusBarTemplateUiEntry(
                key = "builtin_${entry.index}",
                assetUrl = StatusBarThemeTemplateCatalog.assetUri(entry.assetRelativePath),
                label = itemIndexLabel(entry.index),
            )
        }
    } else {
        val category = selectedCategory
        if (category == null) {
            emptyList()
        } else {
            loadedItemsByPack[category.deliveryPackName]
                .orEmpty()
                .take(maxPreviewItems)
                .map { item ->
                    StatusBarTemplateUiEntry(
                        key = "${category.deliveryPackName}_${item.id}",
                        assetUrl = item.assetUrl,
                        label = item.name,
                    )
                }
        }
    }

    val selectedDeliveryPack = selectedCategory?.deliveryPackName
    val isSelectedCategoryLoading = selectedTabIndex > 0 && (
        loadingPack == selectedDeliveryPack ||
            (selectedDeliveryPack != null &&
                attemptedPacks[selectedDeliveryPack] != true &&
                !loadedItemsByPack.containsKey(selectedDeliveryPack))
        )
    val shouldShowEmpty = selectedTabIndex > 0 &&
        !isSelectedCategoryLoading &&
        selectedDeliveryPack != null &&
        attemptedPacks[selectedDeliveryPack] == true &&
        entries.isEmpty()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 6.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_headline_section),
                contentDescription = null,
                modifier = Modifier.height(12.dp),
            )
            Text(
                stringResource(R.string.background_template),
                color = labelColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
            )
        }
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex.coerceAtMost((tabs.size - 1).coerceAtLeast(0)),
            edgePadding = 0.dp,
            divider = {},
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            maxLines = 1,
                        )
                    },
                )
            }
        }
        when {
            isSelectedCategoryLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    StatusBarTemplatePlaceholderGrid(count = 12)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            shouldShowEmpty -> {
                Text(
                    text = "No templates available in this category.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            else -> {
                StatusBarTemplateGrid(
                    entries = entries,
                    selectedAssetUrl = selectedAssetUrl,
                    onSelectPhoto = onSelectPhoto,
                )
            }
        }
        if (selectedTabIndex == 0 && StatusBarThemeTemplateCatalog.entries.size > maxPreviewItems) {
            OutlinedButton(
                onClick = onViewMore,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = stringResource(R.string.view_more),
                    color = StrawberryMilk.Secondary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusBarTemplatePlaceholderGrid(count: Int) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        maxItemsInEachRow = 3,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(count) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.31f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .aspectRatio(2f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusBarTemplateGrid(
    entries: List<StatusBarTemplateUiEntry>,
    selectedAssetUrl: String?,
    onSelectPhoto: (String?) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        maxItemsInEachRow = 3,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        entries.forEach { entry ->
            val selected = entry.assetUrl == selectedAssetUrl
            Surface(
                onClick = {
                    if (selected) {
                        onSelectPhoto(null)
                    } else {
                        onSelectPhoto(entry.assetUrl)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.31f),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    1.5.dp,
                    if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = entry.assetUrl,
                        contentDescription = entry.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(StrawberryMilk.Secondary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun itemIndexLabel(index: Int): String = "template ${index + 1}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LegacyBatteryScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectBattery: (String) -> Unit,
    onSelectEmoji: (String) -> Unit,
    onSetBatteryScale: (Float) -> Unit,
    onSetEmojiScale: (Float) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.editingConfig
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
                    text = stringResource(R.string.legacy_battery_flow_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BatteryPreviewCard(uiState = uiState)
            Text(stringResource(R.string.label_battery_body), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SampleCatalog.batteryPresets) { preset ->
                    ChoiceChip(
                        label = preset.name,
                        selected = config.batteryPresetId == preset.id,
                        onClick = { onSelectBattery(preset.id) },
                    )
                }
            }
            Text(stringResource(R.string.label_emoji), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SampleCatalog.emojiPresets) { preset ->
                    ChoiceChip(
                        label = "${preset.glyph} ${preset.name}",
                        selected = config.emojiPresetId == preset.id,
                        onClick = { onSelectEmoji(preset.id) },
                    )
                }
            }
            SliderField(stringResource(R.string.slider_battery_percentage_size), config.batteryPercentScale, 0.3f..1f, onSetBatteryScale)
            SliderField(stringResource(R.string.slider_emoji_size), config.emojiScale, 0.3f..1f, onSetEmojiScale)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.common_cancel)) }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.apply)) }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
internal fun OriginalStatusTabStrip(
    selected: StatusBarTab,
    onSelect: (StatusBarTab) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = StrawberryMilk.SecondaryContainer.copy(alpha = 0.85f),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBarTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(StrawberryCtaGradientBrush)
                            } else {
                                Modifier.background(Color.Transparent)
                            },
                        )
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        tab.title,
                        color = if (isSelected) Color.White else StrawberryMilk.Secondary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
                    )
                }
            }
        }
    }
}

@Composable
internal fun StatusSliderRow(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.slider_value_integer, (value * 100).toInt()),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
internal fun StatusColorRow(
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
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
    }
}

@Composable
internal fun StatusChevronRow(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
        Image(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

private const val StatusBarVolioGridColumns = 4

/**
 * Grid of Volio [HomeBatteryItem] rows — same combined list the original app binds to Battery + Emoji tabs
 * ([EmojiBatteryModel] list from [hungvv.OS.d]). Four columns, icon-only.
 *
 * Cell width is derived from the row width minus [horizontalSpacing] gaps so **four** tiles always fit
 * (using [Modifier.fillMaxWidth] fraction alone breaks on ~360dp screens: only three items per row).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusBarVolioChoiceGrid(
    items: List<HomeBatteryItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    previewImageUrl: (HomeBatteryItem) -> String? = { it.thumbnailUrl },
) {
    val horizontalSpacing = 12.dp
    val columns = StatusBarVolioGridColumns

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellWidth = (maxWidth - horizontalSpacing * (columns - 1)) / columns
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = columns,
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                verticalArrangement = Arrangement.spacedBy(horizontalSpacing),
            ) {
                items.forEach { item ->
                    val selected = item.id == selectedId
                    Surface(
                        onClick = { onSelect(item.id) },
                        modifier = Modifier.width(cellWidth),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) {
                            StrawberryMilk.PrimaryContainer.copy(alpha = 0.95f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = BorderStroke(
                            1.dp,
                            if (selected) {
                                StrawberryMilk.Secondary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            },
                        ),
                        shadowElevation = 0.dp,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            val thumb = previewImageUrl(item)?.takeIf { it.isNotBlank() }
                            if (thumb != null) {
                                AsyncImage(
                                    model = thumb,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Image(
                                    painter = painterResource(item.previewRes),
                                    contentDescription = item.title,
                                    modifier = Modifier.size(44.dp),
                                )
                            }
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(StrawberryMilk.Secondary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = Color.White,
                                    )
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
internal fun StatusBarChoiceGrid(
    labels: List<String>,
    selectedLabel: String,
    onClick: (String) -> Unit,
    icon: @Composable (String) -> Unit,
    maxItemsInEachRow: Int = 4,
) {
    val gap = 12.dp
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellWidth = (maxWidth - gap * (maxItemsInEachRow - 1)) / maxItemsInEachRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = maxItemsInEachRow,
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            labels.forEach { label ->
                val selected = selectedLabel.equals(label, ignoreCase = true)
                Surface(
                    onClick = { onClick(label) },
                    modifier = Modifier.width(cellWidth),
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) {
                        StrawberryMilk.PrimaryContainer.copy(alpha = 0.95f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        1.dp,
                        if (selected) StrawberryMilk.Secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    ),
                    shadowElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        icon(label)
                        Text(
                            label,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = MaterialTheme.typography.labelLarge.fontFamily,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun StatusSwitchRow(
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
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
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
internal fun BatteryPreviewCard(
    uiState: AppUiState,
) {
    val context = LocalContext.current
    val config = uiState.editingConfig
    val dateTimeConfig = uiState.featureConfigs[CustomizeEntry.DateTime]
        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.DateTime]
        ?: FeatureConfig(variant = encodeDateTimeVariant(parseDateTimeVariant(null)))
    val parsedDateTime = parseDateTimeVariant(dateTimeConfig.variant)
    val datePreview = previewDateStyle(parsedDateTime.styleId)
    val batteryVolio = statusBarBatteryItem(uiState, config.batteryPresetId)
    val emojiVolio = statusBarEmojiItem(uiState, config.emojiPresetId)
    val batteryBody = SampleCatalog.batteryPresets.firstOrNull { it.id == config.batteryPresetId }?.body ?: "▰▰▰▱"
    val emojiGlyph = SampleCatalog.emojiPresets.firstOrNull { it.id == config.emojiPresetId }?.glyph ?: "✨"

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
            val templateBannerRes = config.backgroundTemplateDrawableRes?.takeIf { it != 0 }
            val templateBannerUrl = config.backgroundTemplatePhotoUrl?.takeIf { !it.isNullOrBlank() }
            when {
                templateBannerUrl != null -> {
                    AsyncImage(
                        model = templateBannerUrl,
                        contentDescription = stringResource(R.string.background_template),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
                templateBannerRes != null -> {
                    ThemeShapeDrawableImage(
                        drawableRes = templateBannerRes,
                        contentDescription = stringResource(R.string.background_template),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                }
            }
            Text(stringResource(R.string.live_preview), fontWeight = FontWeight.SemiBold, color = Color(config.accentColor))
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val batUrl = batteryVolio?.batteryArtUrl?.takeIf { !it.isNullOrBlank() }
                        ?: batteryVolio?.thumbnailUrl?.takeIf { !it.isNullOrBlank() }
                    val batDrawable = batteryVolio?.previewRes?.takeIf { it != 0 }
                    val emUrl = emojiVolio?.emojiArtUrl?.takeIf { !it.isNullOrBlank() }
                        ?: emojiVolio?.thumbnailUrl?.takeIf { !it.isNullOrBlank() }
                    val emDrawable = emojiVolio?.previewRes?.takeIf { it != 0 }
                    val chargeConfig = uiState.featureConfigs[CustomizeEntry.Charge]
                        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Charge]
                        ?: FeatureConfig(enabled = false, variant = "chg_1")
                    val wifiConfig = uiState.featureConfigs[CustomizeEntry.Wifi]
                        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Wifi]
                        ?: FeatureConfig(enabled = false, variant = "blue")
                    val signalConfig = uiState.featureConfigs[CustomizeEntry.Signal]
                        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Signal]
                        ?: FeatureConfig(enabled = false, variant = "blue")
                    val dataConfig = uiState.featureConfigs[CustomizeEntry.Data]
                        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Data]
                        ?: FeatureConfig(enabled = false, variant = "5G::blue")
                    val hotspotConfig = uiState.featureConfigs[CustomizeEntry.Hotspot]
                        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Hotspot]
                        ?: FeatureConfig(enabled = false, variant = "blue")
                    val airplaneConfig = uiState.featureConfigs[CustomizeEntry.Airplane]
                        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Airplane]
                        ?: FeatureConfig(enabled = false, variant = "blue")
                    val ringerConfig = uiState.featureConfigs[CustomizeEntry.Ringer]
                        ?: SampleCatalog.defaultFeatureConfigs[CustomizeEntry.Ringer]
                        ?: FeatureConfig(enabled = false, variant = "style=bell;color=blue")
                    val parsedRinger = parseRingerVariant(ringerConfig.variant)
                    val defaultCommonScale = SampleCatalog.defaultConfig.emojiScale.coerceAtLeast(0.01f)
                    val commonScaleFactor = (config.emojiScale.coerceIn(0f, 1f) / defaultCommonScale).coerceIn(0.35f, 2.2f)
                    val emojiAdjustmentScale = config.emojiAdjustmentScale.coerceIn(0.35f, 2.2f)
                    val batterySizeSmall = (28.dp * commonScaleFactor)
                    val batterySizeLarge = (36.dp * commonScaleFactor)
                    val chargeVariant = parseChargeVariant(chargeConfig.variant)
                    val chargeDrawableName = chargeVariantDrawableName(chargeVariant)
                    val chargeDrawableRes = chargeDrawableName?.takeIf { it.isNotBlank() }?.let {
                        context.resources.getIdentifier(it, "drawable", context.packageName)
                    }?.takeIf { it != 0 }
                    val chargeText = if (chargeConfig.enabled) previewChargeGlyph(chargeConfig.variant) else ""
                    val airplaneVisible = airplaneConfig.enabled && previewAirplaneModeOn(context)
                    val hotspotVisible = hotspotConfig.enabled && previewHotspotOn(context)
                    val wifiVisible = wifiConfig.enabled
                    val dataVisible = !wifiVisible && dataConfig.enabled
                    val signalVisible = signalConfig.enabled && !airplaneVisible
                    val dataStyleId = previewDataStyleVariant(dataConfig.variant)
                    val dataColorId = previewDataColorVariant(dataConfig.variant)
                    val previewRingerMode = previewRingerMode(context)
                    val ringerVisible = ringerConfig.enabled && previewRingerMode != AudioManager.RINGER_MODE_NORMAL
                    val ringerRes = previewRingerIconRes(context, previewRingerMode, parsedRinger.styleId)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(stringResource(R.string.demo_time), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (parsedDateTime.showDate) {
                                Text(
                                    text = datePreview.line1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                datePreview.line2?.let { line2 ->
                                    Text(
                                        text = line2,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (datePreview.line2Bold) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (ringerVisible) {
                                Image(
                                    painter = painterResource(ringerRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(parsedRinger.colorId, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (airplaneVisible) {
                                Image(
                                    painter = painterResource(R.drawable.galaxy_airplane),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(airplaneConfig.variant, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (wifiVisible) {
                                Image(
                                    painter = painterResource(R.drawable.galaxy_wifi_4s),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(wifiConfig.variant, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (dataVisible) {
                                Image(
                                    painter = painterResource(previewDataIconRes(dataStyleId)),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(dataColorId, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            if (signalVisible) {
                                Image(
                                    painter = painterResource(previewSignalIconRes()),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(
                                        Color(previewResolveColorFromVariant(signalConfig.variant, AndroidColor.parseColor("#333333"))),
                                    ),
                                )
                            }
                            Text(
                                if (batUrl != null || batDrawable != null) {
                                    if (config.showPercentage) "56%" else ""
                                } else {
                                    "$batteryBody ${if (config.showPercentage) "56%" else ""}".trim()
                                },
                                color = Color(config.accentColor),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            )
                            StatusBarBatteryEmojiCompositePreview(
                                batteryArtUrl = batUrl,
                                batteryArtDrawableRes = batDrawable,
                                batteryContentDescription = batteryVolio?.title,
                                emojiArtUrl = emUrl,
                                emojiArtDrawableRes = emDrawable,
                                emojiContentDescription = emojiVolio?.title,
                                emojiFallbackGlyph = emojiGlyph,
                                emojiFallbackColor = MaterialTheme.colorScheme.onSurface,
                                batterySize = batterySizeSmall,
                                emojiSize = batterySizeSmall * emojiAdjustmentScale,
                                emojiTextSize = MaterialTheme.typography.titleMedium.fontSize,
                                emojiOffsetX = config.emojiOffsetX,
                                emojiOffsetY = config.emojiOffsetY,
                            )
                            when {
                                chargeConfig.enabled && chargeDrawableRes != null -> {
                                    Image(
                                        painter = painterResource(chargeDrawableRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                                chargeText.isNotBlank() -> {
                                    Text(
                                        chargeText,
                                        color = Color(config.accentColor),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (uiState.accessibilityGranted) {
                                stringResource(R.string.accessibility_bridge_active_short)
                            } else {
                                stringResource(R.string.accessibility_bridge_required_short)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        StatusBarBatteryEmojiCompositePreview(
                            batteryArtUrl = batUrl,
                            batteryArtDrawableRes = batDrawable,
                            batteryContentDescription = batteryVolio?.title,
                            emojiArtUrl = emUrl,
                            emojiArtDrawableRes = emDrawable,
                            emojiContentDescription = emojiVolio?.title,
                            emojiFallbackGlyph = emojiGlyph,
                            emojiFallbackColor = MaterialTheme.colorScheme.onSurface,
                            batterySize = batterySizeLarge,
                            emojiSize = batterySizeLarge * emojiAdjustmentScale,
                            emojiTextSize = MaterialTheme.typography.headlineMedium.fontSize,
                            emojiOffsetX = config.emojiOffsetX,
                            emojiOffsetY = config.emojiOffsetY,
                        )
                    }
                }
            }
        }
    }
}

private data class PreviewDateStyle(
    val line1: String,
    val line2: String? = null,
    val line2Bold: Boolean = false,
)

private fun previewDateStyle(styleId: String): PreviewDateStyle = when (styleId) {
    "style_1" -> PreviewDateStyle("Tue, Mar 24")
    "style_2" -> PreviewDateStyle("Tue, Mar", "24", line2Bold = true)
    "style_3" -> PreviewDateStyle("Tue", "24", line2Bold = true)
    "style_4" -> PreviewDateStyle("Mar 24")
    "style_5" -> PreviewDateStyle("Tuesday")
    "style_6" -> PreviewDateStyle("Tuesday", "24", line2Bold = true)
    else -> PreviewDateStyle("Mar 24")
}
