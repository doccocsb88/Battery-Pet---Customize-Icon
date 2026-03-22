package dev.hai.emojibattery.app.screens


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatusBarCustomScreen(
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
        containerColor = MaterialTheme.colorScheme.background,
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
                    color = MaterialTheme.colorScheme.onSurface,
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
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    StatusSliderRow("Status bar height", config.statusBarHeight, onSetStatusBarHeight)
                    StatusSliderRow("Status bar left margin", config.leftMargin, onSetLeftMargin)
                    StatusSliderRow("Status bar right margin", config.rightMargin, onSetRightMargin)
                }
            }
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    StatusColorRow("Status bar icon color", Color(config.accentColor))
                    StatusColorRow("Status bar background color", Color(config.backgroundColor))
                }
            }
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, onClick = { onSelectTab(StatusBarTab.Theme) }) {
                StatusChevronRow("More template")
            }
            Text(
                "Customize Icon",
                color = MaterialTheme.colorScheme.onSurface,
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
                            color = MaterialTheme.colorScheme.onSurface,
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
                StatusBarTab.Settings -> Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
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

@Composable
internal fun OriginalStatusTabStrip(
    selected: StatusBarTab,
    onSelect: (StatusBarTab) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(StatusBarTab.entries) { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    tab.title,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
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
            Text("${(value * 100).toInt()}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StatusBarChoiceGrid(
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
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(2.dp, if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
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
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
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
