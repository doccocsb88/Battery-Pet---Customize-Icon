package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.data.PadWallpaperCategory
import dev.hai.emojibattery.data.PadWallpaperItem
import dev.hai.emojibattery.data.PadWallpaperRepository
import dev.hai.emojibattery.service.WallpaperSetter
import dev.hai.emojibattery.ui.theme.OceanSerenity
import dev.hai.emojibattery.ui.theme.StrawberryMilk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun WallpaperScreen(
    onOpenCategory: (String) -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val categories = remember { mutableStateListOf<PadWallpaperCategory>() }

    LaunchedEffect(Unit) {
        categories.clear()
        categories.addAll(PadWallpaperRepository.loadCategories(context))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            OriginalTopShell(
                title = "Wallpaper",
                onLeftSecondary = {},
                showLeftSecondary = false,
                onSearch = {},
                trailingContent = { Spacer(Modifier.size(40.dp)) },
            )
        },
    ) { padding ->
        if (categories.isEmpty()) {
            WallpaperLoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { WallpaperHeroCard() }
                items(categories.size, key = { index -> categories[index].id }) { index ->
                    val category = categories[index]
                    WallpaperCategoryCard(
                        title = categoryDisplayTitle(category.title ?: category.packName),
                        description = category.description,
                        itemCount = category.items.size,
                        thumbnailUrl = PadWallpaperRepository.thumbnailAssetUrl(category),
                        onClick = { onOpenCategory(category.id) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun WallpaperCategoryScreen(
    categoryId: String,
    isPremiumUser: Boolean,
    onBack: () -> Unit,
    onOpenPreview: (String, String, Boolean) -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val cachedCategories = remember { PadWallpaperRepository.peekCachedCategories().orEmpty() }
    val cachedCategory = remember(categoryId, cachedCategories) {
        cachedCategories.firstOrNull { it.id == categoryId }
    }
    val cachedItems = remember(categoryId) { PadWallpaperRepository.peekCachedItems(categoryId).orEmpty() }
    var category by remember(categoryId) { mutableStateOf(cachedCategory) }
    var items by remember(categoryId) { mutableStateOf(cachedItems) }
    var loading by remember(categoryId) { mutableStateOf(cachedCategory == null || cachedItems.isEmpty()) }

    LaunchedEffect(categoryId) {
        if (category != null && items.isNotEmpty()) {
            loading = false
            return@LaunchedEffect
        }
        loading = true
        val categories = PadWallpaperRepository.loadCategories(context)
        val resolved = categories.firstOrNull { it.id == categoryId }
        category = resolved
        if (resolved != null) {
            repeat(3) { attempt ->
                val loadedItems = PadWallpaperRepository.loadItemsForCategory(context, resolved)
                if (loadedItems.isNotEmpty()) {
                    items = loadedItems
                    return@repeat
                }
                if (attempt < 2) {
                    delay(900)
                }
            }
        }
        loading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            WallpaperTopBar(
                title = categoryDisplayTitle(category?.title ?: "Wallpaper"),
                subtitle = if (loading) "Preparing wallpapers..." else "${items.size} wallpapers",
                onBack = onBack,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                loading -> WallpaperCategoryLoadingState(modifier = Modifier.padding(padding))
                category == null -> WallpaperEmptyState(
                    message = "Wallpaper category not found.",
                    modifier = Modifier.padding(padding),
                )
                items.isEmpty() -> WallpaperEmptyState(
                    message = "No wallpapers available in this category yet.",
                    modifier = Modifier.padding(padding),
                )
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(items, key = { _, item -> item.id }) { itemIndex, item ->
                            val isLocked = !isPremiumUser && itemIndex >= FREE_WALLPAPER_COUNT_PER_CATEGORY
                            WallpaperGridCard(
                                item = item,
                                locked = isLocked,
                                onClick = { onOpenPreview(categoryId, item.id, isLocked) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WallpaperPreviewScreen(
    categoryId: String,
    wallpaperId: String,
    isPremiumUser: Boolean,
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    onSetBackgroundDone: (String) -> Unit,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var category by remember(categoryId) { mutableStateOf<PadWallpaperCategory?>(null) }
    val itemsByCategory = remember { mutableStateMapOf<String, List<PadWallpaperItem>>() }
    var loading by remember(categoryId, wallpaperId) { mutableStateOf(true) }
    var settingWallpaper by remember { mutableStateOf(false) }
    var selectedTarget by remember { mutableStateOf(WallpaperSetter.Target.BOTH) }
    var showSetTargetSheet by remember { mutableStateOf(false) }
    val setTargetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(categoryId) {
        loading = true
        val categories = PadWallpaperRepository.loadCategories(appContext)
        val resolved = categories.firstOrNull { it.id == categoryId }
        category = resolved
        if (resolved != null) {
            itemsByCategory[categoryId] = PadWallpaperRepository.loadItemsForCategory(appContext, resolved)
        } else {
            itemsByCategory.remove(categoryId)
        }
        loading = false
    }

    val categoryItems = itemsByCategory[categoryId].orEmpty()
    val item = categoryItems.firstOrNull { it.id == wallpaperId }
    val itemIndex = categoryItems.indexOfFirst { it.id == wallpaperId }
    val isLocked = item != null && !isPremiumUser && itemIndex >= FREE_WALLPAPER_COUNT_PER_CATEGORY

    fun applyWallpaper(target: WallpaperSetter.Target, imageUrl: String) {
        scope.launch {
            selectedTarget = target
            settingWallpaper = true
            val result = WallpaperSetter.setWallpaper(
                context = appContext,
                imageUrl = imageUrl,
                fallbackResId = R.drawable.img_bg_emoji_sticker,
                target = target,
            )
            settingWallpaper = false
            onSetBackgroundDone(
                if (result.isSuccess) {
                    wallpaperSetSuccessMessage(target)
                } else {
                    result.exceptionOrNull()?.message ?: wallpaperSetErrorMessage(target)
                },
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            WallpaperTopBar(
                title = categoryDisplayTitle(category?.title ?: "Wallpaper Preview"),
                subtitle = "Preview and apply to your phone",
                onBack = onBack,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                loading -> WallpaperLoadingState(modifier = Modifier.padding(padding))
                item == null -> WallpaperEmptyState(
                    message = "Wallpaper not found.",
                    modifier = Modifier.padding(padding),
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(32.dp),
                                color = Color.White,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                border = BorderStroke(1.dp, OceanSerenity.Outline.copy(alpha = 0.24f)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.58f),
                                ) {
                                    WallpaperArtwork(
                                        imageUrl = item.assetUrl,
                                        contentDescription = item.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.FillBounds,
                                    )
//                                    if (isLocked) {
//                                        PremiumBadge(
//                                            modifier = Modifier
//                                                .align(Alignment.TopEnd)
//                                                .padding(14.dp),
//                                        )
//                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.54f)),
                                                ),
                                            )
                                            .padding(18.dp),
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "Tap Set Wallpaper and choose Home, Lock, or Both.",
                                                color = Color.White.copy(alpha = 0.82f),
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = {
                                    if (settingWallpaper) return@Button
                                    if (isLocked) {
                                        onOpenPaywall()
                                        return@Button
                                    }
                                    showSetTargetSheet = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !settingWallpaper,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OceanSerenity.Primary,
                                    contentColor = OceanSerenity.OnPrimary,
                                ),
                            ) {
                                if (settingWallpaper) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = OceanSerenity.OnPrimary,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Wallpaper,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    text = when {
                                        settingWallpaper -> "Setting ${wallpaperTargetLabel(selectedTarget).lowercase()}..."
                                        isLocked -> "Unlock Premium"
                                        else -> "Set Wallpaper"
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(
                                modifier = Modifier.height(
                                    with(density) {
                                        WindowInsets.navigationBars.getBottom(this).toDp()
                                    } + 12.dp,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    val previewItem = item
    if (showSetTargetSheet && !isLocked && previewItem != null && !settingWallpaper) {
        ModalBottomSheet(
            onDismissRequest = { showSetTargetSheet = false },
            sheetState = setTargetSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            WallpaperSetTargetSheet(
                onDismiss = { showSetTargetSheet = false },
                onSelect = { target ->
                    showSetTargetSheet = false
                    applyWallpaper(target, previewItem.assetUrl)
                },
            )
        }
    }
}

private fun wallpaperTargetLabel(target: WallpaperSetter.Target): String =
    when (target) {
        WallpaperSetter.Target.HOME -> "Home Screen"
        WallpaperSetter.Target.LOCK -> "Lock Screen"
        WallpaperSetter.Target.BOTH -> "Both Screens"
    }

private fun wallpaperSetSuccessMessage(target: WallpaperSetter.Target): String =
    when (target) {
        WallpaperSetter.Target.HOME -> "Home screen wallpaper set successfully."
        WallpaperSetter.Target.LOCK -> "Lock screen wallpaper set successfully."
        WallpaperSetter.Target.BOTH -> "Wallpaper set on home and lock screens."
    }

private fun wallpaperSetErrorMessage(target: WallpaperSetter.Target): String =
    when (target) {
        WallpaperSetter.Target.HOME -> "Unable to set home screen wallpaper."
        WallpaperSetter.Target.LOCK -> "Unable to set lock screen wallpaper on this device."
        WallpaperSetter.Target.BOTH -> "Unable to set wallpaper on both screens."
    }

@Composable
private fun WallpaperSetTargetSheet(
    onDismiss: () -> Unit,
    onSelect: (WallpaperSetter.Target) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
    ) {
        Text(
            text = "Set wallpaper to",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Choose where to apply this wallpaper",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        WallpaperSetTargetRow(
            icon = Icons.Rounded.Home,
            title = "Home screen",
            subtitle = "Apply to your home screen only",
            onClick = { onSelect(WallpaperSetter.Target.HOME) },
        )
        HorizontalDivider(color = OceanSerenity.Outline.copy(alpha = 0.22f))
        WallpaperSetTargetRow(
            icon = Icons.Rounded.Lock,
            title = "Lock screen",
            subtitle = "Apply to your lock screen only",
            onClick = { onSelect(WallpaperSetter.Target.LOCK) },
        )
        HorizontalDivider(color = OceanSerenity.Outline.copy(alpha = 0.22f))
        WallpaperSetTargetRow(
            icon = Icons.Rounded.Wallpaper,
            title = "Both screens",
            subtitle = "Set home and lock screens together",
            onClick = { onSelect(WallpaperSetter.Target.BOTH) },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cancel",
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDismiss)
                .padding(vertical = 14.dp),
            textAlign = TextAlign.Center,
            color = OceanSerenity.Primary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun WallpaperSetTargetRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OceanSerenity.Primary.copy(alpha = 0.12f),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OceanSerenity.Primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = OceanSerenity.Primary.copy(alpha = 0.92f),
        )
    }
}

@Composable
private fun WallpaperHeroCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            OceanSerenity.Primary.copy(alpha = 0.96f),
                            StrawberryMilk.Secondary.copy(alpha = 0.9f),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Image,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                    Text(
                        text = "Curated wallpaper collections",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "Each category is delivered as its own asset pack. Open a collection, preview any wallpaper, then set it as your phone background.",
                    color = Color.White.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun WallpaperCategoryCard(
    title: String,
    description: String?,
    itemCount: Int,
    thumbnailUrl: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OceanSerenity.Outline.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 94.dp, height = 126.dp)
                    .clip(RoundedCornerShape(18.dp)),
            ) {
                WallpaperArtwork(
                    imageUrl = thumbnailUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OceanSerenity.OnSurface,
                )
                Text(
                    text = "$itemCount wallpapers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OceanSerenity.OnSurfaceVariant,
                )
                Text(
                    text = description?.takeIf { it.isNotBlank() } ?: categoryCoverCaption(title),
                    style = MaterialTheme.typography.bodySmall,
                    color = OceanSerenity.OnSurfaceVariant.copy(alpha = 0.92f),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = OceanSerenity.Primary,
            )
        }
    }
}

private fun categoryCoverCaption(title: String): String =
    when {
        title.contains("cat", ignoreCase = true) ->
            "Adorable cats in charming scenes, perfect for a warm and relaxing look."
        title.contains("dog", ignoreCase = true) ->
            "Cute dogs in fun and heartwarming styles to brighten your day."
        title.contains("abstract", ignoreCase = true) || title.contains("ab tract", ignoreCase = true) ->
            "Creative abstract wallpapers with modern colors and unique styles."
        title.contains("cute", ignoreCase = true) ->
            "Adorable and charming wallpapers to brighten your day."
        title.contains("vapor", ignoreCase = true) ->
            "Neon retro vibes and dreamy gradients."
        title.contains("dark", ignoreCase = true) || title.contains("amoled", ignoreCase = true) ->
            "Deep shadows and bold contrast for night mode."
        title.contains("nature", ignoreCase = true) ->
            "Fresh landscapes and calming natural tones."
        title.contains("love", ignoreCase = true) ->
            "Soft romantic themes with warm emotion."
        title.contains("geometric", ignoreCase = true) || title.contains("pattern", ignoreCase = true) ->
            "Clean shapes and stylish repeating forms."
        title.contains("draw", ignoreCase = true) || title.contains("abstract", ignoreCase = true) ->
            "Art-inspired visuals with expressive color."
        title.contains("japan", ignoreCase = true) || title.contains("japani", ignoreCase = true) ->
            "Scenic Japanese moments with peaceful mood."
        title.contains("blue", ignoreCase = true) ->
            "Cool blue palettes with serene atmosphere."
        title.contains("photo", ignoreCase = true) ->
            "Real-world captures with crisp detail."
        title.contains("motivat", ignoreCase = true) ->
            "Positive energy and uplifting visual quotes."
        else ->
            "Handpicked wallpapers tailored to this style."
    }

private fun categoryDisplayTitle(raw: String): String =
    if (raw.equals("Ab Tract", ignoreCase = true)) "Abstract" else raw

@Composable
private fun WallpaperGridCard(
    item: PadWallpaperItem,
    locked: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f),
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OceanSerenity.Outline.copy(alpha = 0.16f)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperArtwork(
                imageUrl = item.assetUrl,
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
            )
            if (locked) {
                PremiumBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.48f)),
                        ),
                    )
                    .height(20.dp),
            )
        }
    }
}

@Composable
private fun PremiumBadge(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = StrawberryMilk.Primary,
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = "Premium",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun WallpaperArtwork(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = painterResource(R.drawable.img_bg_emoji_sticker),
        error = painterResource(R.drawable.img_bg_emoji_sticker),
    )
}

@Composable
private fun WallpaperTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Surface(
        color = Color.White,
        contentColor = OceanSerenity.OnSurface,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_back_40_new),
                        contentDescription = "Back",
                        modifier = Modifier.size(36.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanSerenity.OnSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = OceanSerenity.OnSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = OceanSerenity.Outline.copy(alpha = 0.45f),
            )
        }
    }
}

@Composable
private fun WallpaperLoadingState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = OceanSerenity.Primary)
    }
}

@Composable
private fun WallpaperCategoryLoadingState(
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(6) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (index % 2 == 0) {
                        OceanSerenity.PrimaryContainer.copy(alpha = 0.32f)
                    } else {
                        StrawberryMilk.PrimaryContainer.copy(alpha = 0.45f)
                    },
                ),
                border = BorderStroke(1.dp, OceanSerenity.Outline.copy(alpha = 0.12f)),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.2.dp,
                        color = OceanSerenity.Primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperEmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private const val FREE_WALLPAPER_COUNT_PER_CATEGORY = 2
