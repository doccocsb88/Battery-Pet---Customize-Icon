package dev.hai.emojibattery.ui.screen

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import co.q7labs.co.emoji.R
import coil.compose.AsyncImage
import dev.hai.emojibattery.model.ThemeDefinition
import dev.hai.emojibattery.model.ThemeClockSecondDigitsNormalized
import dev.hai.emojibattery.model.ThemeClockStyleDefinition
import dev.hai.emojibattery.model.ThemeClockWidgetConfig
import dev.hai.emojibattery.model.ThemeOptionCatalog
import dev.hai.emojibattery.model.ThemeOptionItem
import dev.hai.emojibattery.model.batteryPreviewAsset
import dev.hai.emojibattery.model.wallpaperPreviewAsset

private const val TAB_CLOCK = "clock"
private const val TAB_BATTERY = "battery"

@Composable
internal fun ThemeListScreen(
    onBack: () -> Unit,
    onOpenThemeDetail: (String) -> Unit,
) {
    val context = LocalContext.current
    val themes = remember(context) { ThemeOptionCatalog.loadFromAssets(context) }

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
                    text = stringResource(R.string.theme_list_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.theme_list_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            if (themes.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.theme_option_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            } else {
                items(themes) { theme ->
                    ThemeListItem(
                        theme = theme,
                        onClick = { onOpenThemeDetail(theme.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeListItem(
    theme: ThemeDefinition,
    onClick: () -> Unit,
) {
    val cover = theme.options.firstOrNull()?.previewImage.orEmpty()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ThemePreviewImage(
                assetPath = cover,
                contentDescription = theme.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.theme_list_item_count, theme.options.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun ThemeDetailScreen(
    themeId: String,
    onBack: () -> Unit,
    onApplyTheme: (String) -> Unit,
) {
    val context = LocalContext.current
    val themes = remember(context) { ThemeOptionCatalog.loadFromAssets(context) }
    val fallbackTheme = themes.firstOrNull()
    val theme = remember(themes, themeId) { themes.firstOrNull { it.id == themeId } ?: fallbackTheme }
    var selectedOptionId by remember(theme?.id) {
        mutableStateOf(theme?.options?.firstOrNull()?.id.orEmpty())
    }

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
                    text = stringResource(R.string.theme_detail_title, theme?.name ?: "Theme"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick = { onApplyTheme(selectedOptionId) },
                    enabled = selectedOptionId.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                ) {
                    Text(
                        text = stringResource(R.string.apply_theme),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.theme_detail_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            if (theme == null || theme.options.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.theme_option_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            } else {
                items(theme.options) { option ->
                    ThemeOptionPreviewCard(
                        option = option,
                        selected = option.id == selectedOptionId,
                        onClick = { selectedOptionId = option.id },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionPreviewCard(
    option: ThemeOptionItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val fallbackBackground = option.components.wallpaperPreviewAsset().ifBlank { option.previewImage }
    val wallpaperAssets = remember(option.id, fallbackBackground) {
        option.components.wallpaper.variants
            .filter { it.isNotBlank() }
            .ifEmpty { listOf(fallbackBackground).filter { it.isNotBlank() } }
    }
    var wallpaperIndex by remember(option.id) { mutableIntStateOf(0) }
    var totalDragX by remember(option.id) { mutableFloatStateOf(0f) }
    val swipeThresholdPx = 40f
    val hasMultipleWallpapers = wallpaperAssets.size > 1
    val currentBackground = wallpaperAssets.getOrNull(wallpaperIndex).orEmpty().ifBlank { fallbackBackground }

    val clockStyleDefinitions = remember(option.id) {
        option.components.clockWidget?.styleCatalog13?.styles.orEmpty()
    }
    val clockStyleOptions = remember(option.id, clockStyleDefinitions) {
        if (clockStyleDefinitions.isNotEmpty()) {
            clockStyleDefinitions.map { it.id }
        } else {
        val styleVariable = option.components.clockWidget?.styleVariables?.secondColorStyle
        val fromStyleVariable = if (styleVariable != null && styleVariable.max >= styleVariable.min) {
            (styleVariable.min..styleVariable.max).map { it.toString() }
        } else {
            emptyList()
        }
        val fromNormalized = option.components.clockWidget?.assets?.secondDigitsNormalized
            ?.glyphVariants
            .orEmpty()
            .map { it.toString() }
        val fallbackLegacy = option.components.clockWidget?.assets?.timeDigits
            .orEmpty()
            .indices
            .map { it.toString() }
        (fromStyleVariable + fromNormalized + fallbackLegacy).distinct()
        }
    }
    val clockLayoutOptions = remember(option.id) {
        listOfNotNull(
            option.components.clockWidget?.size?.takeIf { it.isNotBlank() },
            option.components.clockWidget?.module?.takeIf { it.isNotBlank() },
        ).distinct()
    }
    var selectedClockStyle by remember(option.id) {
        mutableStateOf(clockStyleOptions.firstOrNull().orEmpty())
    }
    var selectedClockLayout by remember(option.id) {
        mutableStateOf(clockLayoutOptions.firstOrNull().orEmpty())
    }

    val batteryStyleOptions = remember(option.id) {
        val stateMapKeys = option.components.batteryWidget?.stateMap?.keys.orEmpty().toList()
        if (stateMapKeys.isNotEmpty()) {
            stateMapKeys
        } else {
            option.components.battery?.modes?.keys.orEmpty().toList()
        }
    }
    val batteryLayoutOptions = remember(option.id) {
        listOfNotNull(
            option.components.batteryWidget?.layout?.takeIf { it.isNotBlank() }?.substringAfterLast('/'),
            option.components.battery?.layout?.takeIf { it.isNotBlank() },
        ).distinct()
    }
    var selectedBatteryStyle by remember(option.id) {
        mutableStateOf(batteryStyleOptions.firstOrNull().orEmpty())
    }
    var selectedBatteryLayout by remember(option.id) {
        mutableStateOf(batteryLayoutOptions.firstOrNull().orEmpty())
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
            },
        ),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ThemePreviewCard(
                    option = option,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(option.id, wallpaperAssets.size, wallpaperIndex) {
                            if (!hasMultipleWallpapers) return@pointerInput
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    totalDragX += dragAmount
                                },
                                onDragCancel = { totalDragX = 0f },
                                onDragEnd = {
                                    val size = wallpaperAssets.size
                                    when {
                                        totalDragX <= -swipeThresholdPx ->
                                            wallpaperIndex = (wallpaperIndex + 1) % size
                                        totalDragX >= swipeThresholdPx ->
                                            wallpaperIndex = (wallpaperIndex - 1 + size) % size
                                    }
                                    totalDragX = 0f
                                },
                            )
                        }
                        .clip(RoundedCornerShape(12.dp)),
                    backgroundOverride = currentBackground,
                    wallpaperPosition = wallpaperIndex + 1,
                    wallpaperTotal = wallpaperAssets.size,
                    clockStyleOption = selectedClockStyle,
                    clockLayoutOption = selectedClockLayout,
                    batteryStyleOption = selectedBatteryStyle,
                    batteryLayoutOption = selectedBatteryLayout,
                    clockStyleDefinitions = clockStyleDefinitions,
                    clockStyleOptions = clockStyleOptions,
                    clockLayoutOptions = clockLayoutOptions,
                    batteryStyleOptions = batteryStyleOptions,
                    batteryLayoutOptions = batteryLayoutOptions,
                    onClockStyleSelected = { selectedClockStyle = it },
                    onClockLayoutSelected = { selectedClockLayout = it },
                    onBatteryStyleSelected = { selectedBatteryStyle = it },
                    onBatteryLayoutSelected = { selectedBatteryLayout = it },
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = stringResource(R.string.theme_option_selected),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                    )
                }
            }
            Text(
                text = option.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    option: ThemeOptionItem,
    modifier: Modifier = Modifier,
    backgroundOverride: String = "",
    wallpaperPosition: Int = 1,
    wallpaperTotal: Int = 1,
    clockStyleOption: String = "",
    clockLayoutOption: String = "",
    batteryStyleOption: String = "",
    batteryLayoutOption: String = "",
    clockStyleDefinitions: List<ThemeClockStyleDefinition> = emptyList(),
    clockStyleOptions: List<String> = emptyList(),
    clockLayoutOptions: List<String> = emptyList(),
    batteryStyleOptions: List<String> = emptyList(),
    batteryLayoutOptions: List<String> = emptyList(),
    onClockStyleSelected: (String) -> Unit = {},
    onClockLayoutSelected: (String) -> Unit = {},
    onBatteryStyleSelected: (String) -> Unit = {},
    onBatteryLayoutSelected: (String) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val deviceAspectRatio = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        val width = configuration.screenWidthDp.toFloat().coerceAtLeast(1f)
        val height = configuration.screenHeightDp.toFloat().coerceAtLeast(1f)
        (width / height).coerceIn(0.35f, 0.7f)
    }

    val background = backgroundOverride.ifBlank {
        option.components.wallpaperPreviewAsset().ifBlank { option.previewImage }
    }
    val lockScreen = option.components.lockScreen

    val wifi = option.components.wifi.lastOrNull().orEmpty()
    val battery = option.components.batteryPreviewAsset()
    val data = option.components.data.lastOrNull().orEmpty()
    val bluetooth = option.components.bluetooth.lastOrNull().orEmpty()
    val signal = option.components.signal
    val hotspot = option.components.hotspot
    val airplane = option.components.airplane
    val charge = option.components.charge

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .aspectRatio(deviceAspectRatio)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                ) {
                    ThemePreviewImage(
                        assetPath = background,
                        contentDescription = "${option.name} background",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxSize(),
                    )
                    if (lockScreen.isNotBlank()) {
                        AsyncImage(
                            model = ThemeOptionCatalog.assetUri(lockScreen),
                            contentDescription = "${option.name} lockscreen",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.26f,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color.Black.copy(alpha = 0.32f)),
                    )
                    ThemeStatusBarRow(
                        timeText = "9:41",
                        hotspot = hotspot,
                        wifi = wifi,
                        signal = signal,
                        data = data,
                        bluetooth = bluetooth,
                        airplane = airplane,
                        charge = charge,
                        battery = battery,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                    )
                    ThemeWidgetPreviewOverlay(
                        option = option,
                        clockStyleOption = clockStyleOption,
                        clockLayoutOption = clockLayoutOption,
                        batteryStyleOption = batteryStyleOption,
                        batteryLayoutOption = batteryLayoutOption,
                        clockStyleDefinitions = clockStyleDefinitions,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 34.dp, start = 8.dp, end = 8.dp),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "${wallpaperPosition.coerceAtLeast(1)}/${wallpaperTotal.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (wallpaperTotal > 1) {
                Text(
                    text = stringResource(R.string.theme_preview_swipe_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (option.components.clockWidget != null || option.components.batteryWidget != null) {
                val defaultLabel = stringResource(R.string.theme_preview_default)
                val hasClockTab = option.components.clockWidget != null
                val hasBatteryTab = option.components.batteryWidget != null
                var selectedTab by rememberSaveable(option.id) {
                    mutableStateOf(
                        when {
                            hasClockTab -> TAB_CLOCK
                            hasBatteryTab -> TAB_BATTERY
                            else -> TAB_CLOCK
                        },
                    )
                }
                if (selectedTab == TAB_CLOCK && !hasClockTab && hasBatteryTab) {
                    selectedTab = TAB_BATTERY
                } else if (selectedTab == TAB_BATTERY && !hasBatteryTab && hasClockTab) {
                    selectedTab = TAB_CLOCK
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ThemeWidgetTabs(
                        hasClockTab = hasClockTab,
                        hasBatteryTab = hasBatteryTab,
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it },
                    )
                    if (selectedTab == TAB_CLOCK && option.components.clockWidget != null) {
                        ThemeClockStyleControlRow(
                            label = stringResource(R.string.theme_preview_style),
                            options = clockStyleOptions,
                            clockWidget = option.components.clockWidget,
                            clockStyleDefinitions = clockStyleDefinitions,
                            selectedValue = clockStyleOption,
                            onSelected = onClockStyleSelected,
                        )
                        ThemeVariantControlRow(
                            label = stringResource(R.string.theme_preview_layout),
                            options = clockLayoutOptions,
                            selectedValue = clockLayoutOption,
                            valueTransform = { it.ifBlank { defaultLabel } },
                            onSelected = onClockLayoutSelected,
                        )
                    }
                    if (selectedTab == TAB_BATTERY && option.components.batteryWidget != null) {
                        ThemeVariantControlRow(
                            label = stringResource(R.string.theme_preview_style),
                            options = batteryStyleOptions,
                            selectedValue = batteryStyleOption,
                            valueTransform = { it.ifBlank { defaultLabel } },
                            onSelected = onBatteryStyleSelected,
                        )
                        ThemeVariantControlRow(
                            label = stringResource(R.string.theme_preview_layout),
                            options = batteryLayoutOptions,
                            selectedValue = batteryLayoutOption,
                            valueTransform = { it.ifBlank { defaultLabel } },
                            onSelected = onBatteryLayoutSelected,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeWidgetTabs(
    hasClockTab: Boolean,
    hasBatteryTab: Boolean,
    selectedTab: String,
    onSelectTab: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasClockTab) {
            ThemeSelectionChip(
                label = stringResource(R.string.theme_preview_clock_widget),
                selected = selectedTab == TAB_CLOCK,
                onClick = { onSelectTab(TAB_CLOCK) },
            )
        }
        if (hasBatteryTab) {
            ThemeSelectionChip(
                label = stringResource(R.string.theme_preview_battery_widget),
                selected = selectedTab == TAB_BATTERY,
                onClick = { onSelectTab(TAB_BATTERY) },
            )
        }
    }
}

@Composable
private fun ThemeVariantControlRow(
    label: String,
    options: List<String>,
    selectedValue: String,
    valueTransform: (String) -> String = { it },
    onSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (options.isEmpty()) {
                ThemeSelectionChip(
                    label = stringResource(R.string.theme_preview_default),
                    selected = true,
                    onClick = {},
                )
            } else {
                options.forEach { option ->
                    val selected = option == selectedValue
                    ThemeSelectionChip(
                        label = valueTransform(option),
                        selected = selected,
                        onClick = { onSelected(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeClockStyleControlRow(
    label: String,
    options: List<String>,
    clockWidget: ThemeClockWidgetConfig,
    clockStyleDefinitions: List<ThemeClockStyleDefinition>,
    selectedValue: String,
    onSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (options.isEmpty()) {
                ThemeSelectionChip(
                    label = stringResource(R.string.theme_preview_default),
                    selected = true,
                    onClick = {},
                )
            } else {
                options.forEach { styleId ->
                    val selected = styleId == selectedValue
                    ThemeClockStylePreviewChip(
                        styleId = styleId,
                        clockWidget = clockWidget,
                        clockStyleDefinitions = clockStyleDefinitions,
                        selected = selected,
                        onClick = { onSelected(styleId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeClockStylePreviewChip(
    styleId: String,
    clockWidget: ThemeClockWidgetConfig,
    clockStyleDefinitions: List<ThemeClockStyleDefinition>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val selectedStyleFromCatalog = clockStyleDefinitions.firstOrNull { it.id == styleId }
    val timePrefixes = clockWidget.assets.timeDigits.filter { it.isNotBlank() }
    val selectedTimePrefix = selectedStyleFromCatalog?.timePrefix?.takeIf { it.isNotBlank() }
        ?: run {
            val fallbackIndex = styleId.toIntOrNull()
            when {
                timePrefixes.isEmpty() -> ""
                fallbackIndex != null && fallbackIndex in timePrefixes.indices -> timePrefixes[fallbackIndex]
                else -> timePrefixes.first()
            }
        }
    val selectedStyleVariant = selectedStyleFromCatalog?.secondVariant
        ?: styleId.toIntOrNull()
        ?: clockWidget.styleVariables?.secondColorStyle?.default
        ?: clockWidget.assets.secondDigitsNormalized?.glyphVariants?.firstOrNull()
        ?: 0
    val useNormalizedDigitsForPrimaryClock = selectedStyleFromCatalog
        ?.id
        ?.contains("_TIME", ignoreCase = true)
        ?.not() == true

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ThemeClockPrimaryDigitAsset(
                stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                normalized = clockWidget.assets.secondDigitsNormalized,
                styleVariant = selectedStyleVariant,
                digit = 0,
            )
            ThemeClockPrimaryDigitAsset(
                stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                normalized = clockWidget.assets.secondDigitsNormalized,
                styleVariant = selectedStyleVariant,
                digit = 9,
            )
            Text(
                text = ":",
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            ThemeClockPrimaryDigitAsset(
                stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                normalized = clockWidget.assets.secondDigitsNormalized,
                styleVariant = selectedStyleVariant,
                digit = 4,
            )
            ThemeClockPrimaryDigitAsset(
                stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                normalized = clockWidget.assets.secondDigitsNormalized,
                styleVariant = selectedStyleVariant,
                digit = 1,
            )
        }
    }
}

@Composable
private fun ThemeSelectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun ThemeWidgetPreviewOverlay(
    option: ThemeOptionItem,
    clockStyleOption: String,
    clockLayoutOption: String,
    batteryStyleOption: String,
    batteryLayoutOption: String,
    clockStyleDefinitions: List<ThemeClockStyleDefinition> = emptyList(),
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(0.72f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (option.components.clockWidget != null) {
            ClockWidgetPreview(
                option = option,
                clockStyleOption = clockStyleOption,
                clockLayoutOption = clockLayoutOption,
                clockStyleDefinitions = clockStyleDefinitions,
            )
        }
        if (option.components.batteryWidget != null) {
            BatteryWidgetPreview(
                option = option,
                batteryStyleOption = batteryStyleOption,
                batteryLayoutOption = batteryLayoutOption,
            )
        }
    }
}

@Composable
private fun ClockWidgetPreview(
    option: ThemeOptionItem,
    clockStyleOption: String,
    clockLayoutOption: String,
    clockStyleDefinitions: List<ThemeClockStyleDefinition> = emptyList(),
) {
    val clockWidget = option.components.clockWidget ?: return
    val selectedStyleFromCatalog = clockStyleDefinitions.firstOrNull { it.id == clockStyleOption }
    val timePrefixes = clockWidget.assets.timeDigits.filter { it.isNotBlank() }
    val selectedTimePrefix = selectedStyleFromCatalog?.timePrefix?.takeIf { it.isNotBlank() }
        ?: run {
            val fallbackIndex = clockStyleOption.toIntOrNull()
            when {
                timePrefixes.isEmpty() -> ""
                fallbackIndex != null && fallbackIndex in timePrefixes.indices -> timePrefixes[fallbackIndex]
                else -> timePrefixes.first()
            }
        }
    val selectedStyleVariant = selectedStyleFromCatalog?.secondVariant
        ?: clockStyleOption.toIntOrNull()
        ?: clockWidget.styleVariables?.secondColorStyle?.default
        ?: clockWidget.assets.secondDigitsNormalized?.glyphVariants?.firstOrNull()
        ?: 0
    val useNormalizedDigitsForPrimaryClock = selectedStyleFromCatalog
        ?.id
        ?.contains("_TIME", ignoreCase = true)
        ?.not() == true
    val isWideLayout = clockLayoutOption.contains("2x4", ignoreCase = true)
        || clockLayoutOption.contains("clock", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.42f),
    ) {
        if (isWideLayout) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    ThemeClockPrimaryDigitAsset(
                        stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                        normalized = clockWidget.assets.secondDigitsNormalized,
                        styleVariant = selectedStyleVariant,
                        digit = 0,
                    )
                    ThemeClockPrimaryDigitAsset(
                        stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                        normalized = clockWidget.assets.secondDigitsNormalized,
                        styleVariant = selectedStyleVariant,
                        digit = 9,
                    )
                    Text(
                        text = ":",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    ThemeClockPrimaryDigitAsset(
                        stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                        normalized = clockWidget.assets.secondDigitsNormalized,
                        styleVariant = selectedStyleVariant,
                        digit = 4,
                    )
                    ThemeClockPrimaryDigitAsset(
                        stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                        normalized = clockWidget.assets.secondDigitsNormalized,
                        styleVariant = selectedStyleVariant,
                        digit = 1,
                    )
                }
                Text(
                    text = "Tue, Apr 16",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    ThemeClockPrimaryDigitAsset(
                        stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                        normalized = clockWidget.assets.secondDigitsNormalized,
                        styleVariant = selectedStyleVariant,
                        digit = 0,
                    )
                    ThemeClockPrimaryDigitAsset(
                        stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                        normalized = clockWidget.assets.secondDigitsNormalized,
                        styleVariant = selectedStyleVariant,
                        digit = 9,
                    )
                    Text(
                        text = ":",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    ThemeClockPrimaryDigitAsset(
                        stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                        normalized = clockWidget.assets.secondDigitsNormalized,
                        styleVariant = selectedStyleVariant,
                        digit = 4,
                    )
                    ThemeClockPrimaryDigitAsset(
                        stylePrefix = selectedTimePrefix.takeUnless { useNormalizedDigitsForPrimaryClock },
                        normalized = clockWidget.assets.secondDigitsNormalized,
                        styleVariant = selectedStyleVariant,
                        digit = 1,
                    )
                }
                Text(
                    text = "Apr 16",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun ThemeClockPrimaryDigitAsset(
    stylePrefix: String?,
    normalized: ThemeClockSecondDigitsNormalized?,
    styleVariant: Int,
    digit: Int,
) {
    val context = LocalContext.current
    val assetPath = remember(stylePrefix, normalized, digit, styleVariant) {
        val fromPrimaryStyle = stylePrefix?.takeIf { it.isNotBlank() }?.let {
            resolveClockDigitAssetPath(context, it, digit)
        }.orEmpty()
        if (fromPrimaryStyle.isNotBlank()) {
            fromPrimaryStyle
        } else {
            resolveClockSecondDigitAssetPath(
                context = context,
                normalized = normalized,
                digit = digit,
                styleVariant = styleVariant,
            )
        }
    }
    if (assetPath.isBlank()) {
        Text(
            text = digit.toString(),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        return
    }
    AsyncImage(
        model = ThemeOptionCatalog.assetUri(assetPath),
        contentDescription = "clock_digit_$digit",
        modifier = Modifier.size(width = 10.dp, height = 14.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun ThemeClockDigitAsset(
    stylePrefix: String,
    digit: Int,
) {
    val context = LocalContext.current
    val assetPath = remember(stylePrefix, digit) {
        resolveClockDigitAssetPath(context, stylePrefix, digit)
    }
    if (assetPath.isBlank()) {
        Text(
            text = digit.toString(),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        return
    }
    AsyncImage(
        model = ThemeOptionCatalog.assetUri(assetPath),
        contentDescription = "clock_digit_$digit",
        modifier = Modifier.size(width = 10.dp, height = 14.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun BatteryWidgetPreview(
    option: ThemeOptionItem,
    batteryStyleOption: String,
    batteryLayoutOption: String,
) {
    val batteryWidget = option.components.batteryWidget ?: return
    val selectedState = batteryStyleOption.ifBlank {
        batteryWidget.stateMap.keys.firstOrNull().orEmpty()
    }
    val stateItems = batteryWidget.stateMap[selectedState].orEmpty()
    val currentLevel = 72
    val widgetAsset = stateItems.firstOrNull { currentLevel <= it.max }?.asset
        ?: stateItems.lastOrNull()?.asset.orEmpty()
    val isVertical = batteryLayoutOption.contains("vertical", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.42f),
    ) {
        if (isVertical) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                ThemeStatusAssetIcon(
                    assetPath = widgetAsset,
                    contentDescription = "battery_widget",
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "$currentLevel%",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ThemeStatusAssetIcon(
                    assetPath = widgetAsset,
                    contentDescription = "battery_widget",
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "$currentLevel%",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun resolveClockDigitAssetPath(
    context: Context,
    stylePrefix: String,
    digit: Int,
): String {
    if (stylePrefix.isBlank()) return ""
    val normalized = stylePrefix.trim()
    val base = "${normalized}_$digit"
    val candidates = if (base.contains('.')) {
        listOf(base)
    } else {
        listOf("$base.png", "$base.jpg", "$base.jpeg", "$base.webp", base)
    }
    return candidates.firstOrNull { assetPath ->
        runCatching { context.assets.open(assetPath).use { } }.isSuccess
    }.orEmpty()
}

private fun resolveClockSecondDigitAssetPath(
    context: Context,
    normalized: ThemeClockSecondDigitsNormalized?,
    digit: Int,
    styleVariant: Int,
): String {
    if (normalized == null) return ""
    val glyph = normalized.digitToGlyph[digit.toString()].orEmpty()
    if (glyph.isBlank()) return ""

    val pickedVariant = normalized.glyphVariants.firstOrNull { it == styleVariant }
        ?: normalized.glyphVariants.firstOrNull()
        ?: styleVariant
    val fileName = normalized.filePattern
        .replace("{glyph}", glyph)
        .replace("{variant}", pickedVariant.toString())
    val assetPath = "${normalized.folder}/$fileName".replace("//", "/")
    return if (runCatching { context.assets.open(assetPath).use { } }.isSuccess) assetPath else ""
}

@Composable
private fun ThemeStatusBarRow(
    timeText: String,
    hotspot: String,
    wifi: String,
    signal: String,
    data: String,
    bluetooth: String,
    airplane: String,
    charge: String,
    battery: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = timeText,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
            ThemeStatusAssetIcon(hotspot, "hotspot")
            ThemeStatusAssetIcon(wifi, "wifi")
            ThemeStatusAssetIcon(signal, "signal")
            ThemeStatusAssetIcon(data, "data")
            ThemeStatusAssetIcon(bluetooth, "bluetooth")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            ThemeStatusAssetIcon(airplane, "airplane")
            ThemeStatusAssetIcon(charge, "charge")
            ThemeStatusAssetIcon(battery, "battery")
        }
    }
}

@Composable
private fun ThemeStatusAssetIcon(
    assetPath: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (assetPath.isBlank()) return
    AsyncImage(
        model = ThemeOptionCatalog.assetUri(assetPath),
        contentDescription = contentDescription,
        modifier = modifier.size(11.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun ThemePreviewImage(
    assetPath: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (assetPath.isBlank()) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
        ) {}
        return
    }

    AsyncImage(
        model = ThemeOptionCatalog.assetUri(assetPath),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}
