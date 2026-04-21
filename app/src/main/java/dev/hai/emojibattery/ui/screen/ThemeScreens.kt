package dev.hai.emojibattery.ui.screen

import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import coil.compose.AsyncImage
import dev.hai.emojibattery.model.ThemeBatteryMode
import dev.hai.emojibattery.model.ThemeBatterySpriteSheet
import dev.hai.emojibattery.model.ThemeDefinition
import dev.hai.emojibattery.model.ThemeOptionCatalog
import dev.hai.emojibattery.model.ThemeOptionItem
import dev.hai.emojibattery.model.batteryPreviewAsset
import dev.hai.emojibattery.model.wallpaperPreviewAsset

@Composable
internal fun ThemeListScreen(
    onBack: () -> Unit,
    onOpenThemeDetail: (String) -> Unit,
) {
    val context = LocalContext.current
    val themes = remember(context) { ThemeOptionCatalog.loadFromAssets(context) }
    val configuration = LocalConfiguration.current
    val deviceAspectRatio = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        val width = configuration.screenWidthDp.toFloat().coerceAtLeast(1f)
        val height = configuration.screenHeightDp.toFloat().coerceAtLeast(1f)
        width / height
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
                    text = stringResource(R.string.theme_list_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.theme_list_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            if (themes.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.theme_option_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            } else {
                items(items = themes, key = { it.id }) { theme ->
                    ThemeListItem(
                        theme = theme,
                        deviceAspectRatio = deviceAspectRatio,
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
    deviceAspectRatio: Float,
    onClick: () -> Unit,
) {
    val cover = theme.options.firstOrNull()?.previewImage.orEmpty()
    val previewAspectRatio = (deviceAspectRatio * 1.08f).coerceIn(0.5f, 0.75f)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(1.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ThemePreviewImage(
                assetPath = cover,
                contentDescription = theme.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(previewAspectRatio)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.BottomEnd,
            )
            Text(
                text = theme.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            ) {
                Button(
                    onClick = { onApplyTheme(selectedOptionId) },
                    enabled = selectedOptionId.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(12.dp)),
    ) {
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
                },
            backgroundOverride = currentBackground,
        )
        if (hasMultipleWallpapers) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                wallpaperAssets.forEachIndexed { index, _ ->
                    val isSelected = index == wallpaperIndex
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 7.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f)
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    option: ThemeOptionItem,
    modifier: Modifier = Modifier,
    backgroundOverride: String = "",
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
    val context = LocalContext.current
    val wifi = remember(context, option.id, option.components.wifi) {
        pickStatusPreviewAsset(context, option.components.wifi)
    }
    val batteryAsset = option.components.batteryPreviewAsset()
    val batteryMode = remember(option.id, batteryAsset) {
        option.components.battery.findModeByAsset(batteryAsset)
    }
    val data = remember(context, option.id, option.components.data) {
        pickStatusPreviewAsset(context, option.components.data)
    }
    val bluetooth = remember(context, option.id, option.components.bluetooth) {
        pickStatusPreviewAsset(context, option.components.bluetooth)
    }
    val signal = remember(context, option.id, option.components.signal) {
        pickStatusPreviewAsset(context, option.components.signal)
    }
    val hotspot = remember(context, option.id, option.components.hotspot) {
        pickStatusPreviewAsset(context, option.components.hotspot)
    }
    val airplane = option.components.airplane
    val charge = option.components.charge

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width((configuration.screenWidthDp * 0.8f).dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.Black)
                .padding(horizontal = 6.dp, vertical = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .aspectRatio(deviceAspectRatio),
            ) {
                ThemePreviewImage(
                    assetPath = background,
                    contentDescription = "${option.name} background",
                    modifier = Modifier.fillMaxSize(),
                )
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
                    batteryAsset = batteryAsset,
                    batterySprite = option.components.battery,
                    batteryMode = batteryMode,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }
    }
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
    batteryAsset: String,
    batterySprite: ThemeBatterySpriteSheet?,
    batteryMode: ThemeBatteryMode?,
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
            ThemeBatteryPreviewAsset(
                assetPath = batteryAsset,
                spriteSheet = batterySprite,
                spriteModeFrames = batteryMode?.frames ?: 0,
                level = 72,
                contentDescription = "battery",
                modifier = Modifier.size(11.dp),
            )
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
private fun ThemeBatteryPreviewAsset(
    assetPath: String,
    spriteSheet: ThemeBatterySpriteSheet?,
    spriteModeFrames: Int,
    level: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val spriteBitmap = remember(assetPath, spriteSheet, spriteModeFrames, level) {
        decodeBatteryPreviewFrame(
            context = context,
            assetPath = assetPath,
            spriteSheet = spriteSheet,
            frames = spriteModeFrames,
            level = level,
        )
    }
    if (spriteBitmap != null) {
        Image(
            bitmap = spriteBitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    } else {
        ThemeStatusAssetIcon(
            assetPath = assetPath,
            contentDescription = contentDescription,
            modifier = modifier,
        )
    }
}

private fun decodeBatteryPreviewFrame(
    context: android.content.Context,
    assetPath: String,
    spriteSheet: ThemeBatterySpriteSheet?,
    frames: Int,
    level: Int,
) = runCatching {
    if (assetPath.isBlank() || spriteSheet == null || frames <= 1) return@runCatching null
    val sourceBitmap = context.assets.open(assetPath).use(BitmapFactory::decodeStream) ?: return@runCatching null
    val frameWidth = deriveBatteryFrameWidth(
        bitmapWidth = sourceBitmap.width,
        bitmapHeight = sourceBitmap.height,
        configuredWidth = spriteSheet.frame.width,
        configuredHeight = spriteSheet.frame.height,
        frames = frames,
        indexing = spriteSheet.indexing,
    )
    val frameHeight = deriveBatteryFrameHeight(
        bitmapWidth = sourceBitmap.width,
        bitmapHeight = sourceBitmap.height,
        configuredWidth = spriteSheet.frame.width,
        configuredHeight = spriteSheet.frame.height,
        frames = frames,
        indexing = spriteSheet.indexing,
    )
    if (frameWidth <= 0 || frameHeight <= 0) return@runCatching null
    val clampedLevel = level.coerceIn(0, 100)
    val frameIndex = when {
        clampedLevel <= 0 -> 0
        else -> (((clampedLevel - 1) * frames) / 100).coerceIn(0, frames - 1)
    }
    val x = 0
    val y = when (spriteSheet.indexing.lowercase()) {
        "top_to_bottom" -> frameIndex * frameHeight
        else -> frameIndex * frameHeight
    }
    if (sourceBitmap.width < frameWidth || sourceBitmap.height < y + frameHeight) {
        return@runCatching null
    }
    android.graphics.Bitmap.createBitmap(sourceBitmap, x, y, frameWidth, frameHeight)
}.getOrNull()

private fun deriveBatteryFrameWidth(
    bitmapWidth: Int,
    bitmapHeight: Int,
    configuredWidth: Int,
    configuredHeight: Int,
    frames: Int,
    indexing: String,
): Int {
    if (indexing.equals("top_to_bottom", ignoreCase = true)) {
        return configuredWidth.takeIf { it in 1..bitmapWidth } ?: bitmapWidth
    }
    if (indexing.equals("left_to_right", ignoreCase = true)) {
        val inferred = if (frames > 0) bitmapWidth / frames else bitmapWidth
        return configuredWidth.takeIf { it in 1..bitmapWidth } ?: inferred
    }
    return configuredWidth.takeIf { it in 1..bitmapWidth } ?: bitmapWidth
}

private fun deriveBatteryFrameHeight(
    bitmapWidth: Int,
    bitmapHeight: Int,
    configuredWidth: Int,
    configuredHeight: Int,
    frames: Int,
    indexing: String,
): Int {
    if (indexing.equals("top_to_bottom", ignoreCase = true)) {
        val inferred = if (frames > 0) bitmapHeight / frames else bitmapHeight
        return configuredHeight.takeIf { it > 0 && it * frames <= bitmapHeight } ?: inferred
    }
    if (indexing.equals("left_to_right", ignoreCase = true)) {
        return configuredHeight.takeIf { it in 1..bitmapHeight } ?: bitmapHeight
    }
    val configuredValid = configuredHeight.takeIf { it in 1..bitmapHeight }
    if (configuredValid != null) return configuredValid
    return if (configuredWidth > 0 && frames > 0 && bitmapHeight >= configuredHeight) {
        bitmapHeight / frames
    } else {
        bitmapHeight
    }
}

private fun ThemeBatterySpriteSheet?.findModeByAsset(assetPath: String): ThemeBatteryMode? {
    if (this == null || assetPath.isBlank()) return null
    return modes.values.firstOrNull { it.asset == assetPath }
}

private fun pickStatusPreviewAsset(
    context: android.content.Context,
    candidates: List<String>,
): String {
    return candidates
        .asReversed()
        .firstOrNull { isRenderableStatusAsset(context, it) }
        .orEmpty()
}

private fun isRenderableStatusAsset(
    context: android.content.Context,
    assetPath: String,
): Boolean = runCatching {
    if (assetPath.isBlank()) return@runCatching false
    val bitmap = context.assets.open(assetPath).use(BitmapFactory::decodeStream) ?: return@runCatching false
    if (bitmap.width <= 0 || bitmap.height <= 0) return@runCatching false
    for (x in 0 until bitmap.width) {
        for (y in 0 until bitmap.height) {
            if (AndroidColor.alpha(bitmap.getPixel(x, y)) != 0) {
                return@runCatching true
            }
        }
    }
    false
}.getOrDefault(false)

@Composable
private fun ThemePreviewImage(
    assetPath: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
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
        contentScale = contentScale,
        alignment = alignment,
    )
}
