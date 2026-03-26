package dev.hai.emojibattery.ui.screen


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.ImageView

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
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
    Scaffold(
        containerColor = editorBg,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            StatusBarCustomHeader(
                title = stringResource(R.string.status_bar_custom_title),
                onBack = onBack,
                onApply = onApply,
                applyBrush = applyBrush,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
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
                                            onSelect = onSelectBattery,
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
                                                batteryPresets.firstOrNull { it.name == label }?.let { onSelectBattery(it.id) }
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
                                            onSelect = onSelectEmoji,
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
                                                emojiPresets.firstOrNull { it.name == label }?.let { onSelectEmoji(it.id) }
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
private fun StatusBarLivePreviewCard(
    uiState: AppUiState,
    previewBrush: Brush,
) {
    val config = uiState.editingConfig
    val batteryVolio = statusBarBatteryItem(uiState, config.batteryPresetId)
    val emojiVolio = statusBarEmojiItem(uiState, config.emojiPresetId)
    val batteryText = batteryVolio?.title?.take(10)
        ?: SampleCatalog.batteryPresets.firstOrNull { it.id == config.batteryPresetId }?.body
        ?: "▰"
    val emojiGlyph = SampleCatalog.emojiPresets.firstOrNull { it.id == config.emojiPresetId }?.glyph ?: "✨"
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
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
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color(config.backgroundColor).copy(alpha = 0.45f)),
                    )
                }
                templateRes != null -> {
                    ThemeShapeDrawableImage(
                        drawableRes = templateRes,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color(config.backgroundColor).copy(alpha = 0.45f)),
                    )
                }
                else -> Box(Modifier.fillMaxSize().background(previewBrush))
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.demo_time),
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                    )
                    val emojiUrl = emojiVolio?.emojiArtUrl?.takeIf { !it.isNullOrBlank() }
                        ?: emojiVolio?.thumbnailUrl?.takeIf { !it.isNullOrBlank() }
                    if (emojiUrl != null) {
                        AsyncImage(
                            model = emojiUrl,
                            contentDescription = emojiVolio?.title,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            emojiGlyph,
                            fontSize = 14.sp,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "▯▯▯",
                        color = Color.Black.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                    )
                    val batUrl = batteryVolio?.batteryArtUrl?.takeIf { !it.isNullOrBlank() }
                        ?: batteryVolio?.thumbnailUrl?.takeIf { !it.isNullOrBlank() }
                    if (batUrl != null) {
                        AsyncImage(
                            model = batUrl,
                            contentDescription = batteryVolio?.title,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Text(
                        text = buildString {
                            if (batUrl == null) append(batteryText)
                            if (config.showPercentage) {
                                if (batUrl == null) append(" ")
                                append((config.batteryPercentScale * 100).toInt())
                                append("%")
                            }
                        },
                        color = Color(config.accentColor),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
                    )
                }
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
    var showRgbPicker by remember { mutableStateOf(false) }
    var pickR by remember { mutableStateOf(0) }
    var pickG by remember { mutableStateOf(0) }
    var pickB by remember { mutableStateOf(0) }
    LaunchedEffect(showRgbPicker, selectedArgb) {
        if (showRgbPicker) {
            val c = Color(selectedArgb)
            pickR = (c.red * 255f).roundToInt().coerceIn(0, 255)
            pickG = (c.green * 255f).roundToInt().coerceIn(0, 255)
            pickB = (c.blue * 255f).roundToInt().coerceIn(0, 255)
        }
    }

    if (showRgbPicker) {
        AlertDialog(
            onDismissRequest = { showRgbPicker = false },
            title = { Text(stringResource(R.string.theme_background_color)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("R: $pickR", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = pickR.toFloat(),
                        onValueChange = { pickR = it.roundToInt().coerceIn(0, 255) },
                        valueRange = 0f..255f,
                    )
                    Text("G: $pickG", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = pickG.toFloat(),
                        onValueChange = { pickG = it.roundToInt().coerceIn(0, 255) },
                        valueRange = 0f..255f,
                    )
                    Text("B: $pickB", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = pickB.toFloat(),
                        onValueChange = { pickB = it.roundToInt().coerceIn(0, 255) },
                        valueRange = 0f..255f,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val argb = (0xFF000000L) or (pickR.toLong() shl 16) or (pickG.toLong() shl 8) or pickB.toLong()
                        onSelect(argb)
                        showRgbPicker = false
                    },
                ) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRgbPicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
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
                    .clickable { showRgbPicker = true },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
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
                Text(
                    text = "🍼",
                    style = MaterialTheme.typography.headlineMedium,
                )
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
    val config = uiState.editingConfig
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(stringResource(R.string.demo_time), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.demo_date),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.demo_wifi), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("▰▰▰▱", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val batUrl = batteryVolio?.batteryArtUrl?.takeIf { !it.isNullOrBlank() }
                                ?: batteryVolio?.thumbnailUrl?.takeIf { !it.isNullOrBlank() }
                            if (batUrl != null) {
                                AsyncImage(
                                    model = batUrl,
                                    contentDescription = batteryVolio?.title,
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            Text(
                                "${if (batUrl == null) batteryBody else ""} ${if (config.showPercentage) "56%" else ""}".trim(),
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
                            if (uiState.accessibilityGranted) {
                                stringResource(R.string.accessibility_bridge_active_short)
                            } else {
                                stringResource(R.string.accessibility_bridge_required_short)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        val emUrl = emojiVolio?.emojiArtUrl?.takeIf { !it.isNullOrBlank() }
                            ?: emojiVolio?.thumbnailUrl?.takeIf { !it.isNullOrBlank() }
                        if (emUrl != null) {
                            AsyncImage(
                                model = emUrl,
                                contentDescription = emojiVolio?.title,
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                emojiGlyph,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }
        }
    }
}
