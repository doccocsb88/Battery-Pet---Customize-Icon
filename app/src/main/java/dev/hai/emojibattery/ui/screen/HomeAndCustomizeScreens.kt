package dev.hai.emojibattery.ui.screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.HomeCategoryTab
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun HomeScreen(
    uiState: AppUiState,
    onSelectCategory: (String) -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenStatusBarCustom: () -> Unit,
    onOpenLegacyBattery: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSticker: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
    onOpenFeedback: () -> Unit,
) {
    val categories = uiState.homeTabs.takeIf { it.isNotEmpty() }
        ?: SampleCatalog.homeCategories.map { HomeCategoryTab(it.id, it.title) }
    if (categories.isEmpty()) return

    key(categories.joinToString { it.id }) {
        val startPage = categories.indexOfFirst { it.id == uiState.selectedHomeCategoryId }
            .coerceIn(0, (categories.size - 1).coerceAtLeast(0))
        val pagerState = rememberPagerState(
            initialPage = startPage,
            pageCount = { categories.size },
        )
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(categories.map { it.id }.joinToString(), uiState.selectedHomeCategoryId) {
            val idx = categories.indexOfFirst { it.id == uiState.selectedHomeCategoryId }.coerceAtLeast(0)
            if (pagerState.currentPage != idx) {
                pagerState.scrollToPage(idx)
            }
        }

        LaunchedEffect(pagerState, categories) {
            snapshotFlow { pagerState.settledPage }
                .distinctUntilChanged()
                .collect { page ->
                    val id = categories.getOrNull(page)?.id ?: return@collect
                    if (id != uiState.selectedHomeCategoryId) {
                        onSelectCategory(id)
                    }
                }
        }

        HomeScreenScaffold(
            categories = categories,
            pagerState = pagerState,
            coroutineScope = coroutineScope,
            uiState = uiState,
            onSelectCategory = onSelectCategory,
            onOpenAccessibility = onOpenAccessibility,
            onOpenStatusBarCustom = onOpenStatusBarCustom,
            onOpenLegacyBattery = onOpenLegacyBattery,
            onOpenSearch = onOpenSearch,
            onOpenSticker = onOpenSticker,
            onOpenBatteryTroll = onOpenBatteryTroll,
            onOpenFeedback = onOpenFeedback,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun HomeScreenScaffold(
    categories: List<HomeCategoryTab>,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    uiState: AppUiState,
    onSelectCategory: (String) -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenStatusBarCustom: () -> Unit,
    onOpenLegacyBattery: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSticker: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
    onOpenFeedback: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OriginalTopShell(
                title = stringResource(R.string.battery_icon_title),
                onLeftSecondary = onOpenFeedback,
                onSearch = onOpenSearch,
                showEnableBanner = !uiState.accessibilityGranted,
                onStart = onOpenAccessibility,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    count = categories.size,
                    key = { index -> categories[index].id },
                ) { index ->
                    val category = categories[index]
                    val selected = pagerState.settledPage == index
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (selected) Color(0xFF8FB6D4) else Color(0xFFF2F2F2),
                    ) {
                        Text(
                            text = if (category.id == "hot") "HOT" else category.title.uppercase(),
                            color = if (selected) Color(0xFF3C637E) else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                            ),
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch {
                                        onSelectCategory(category.id)
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState,
                    beyondViewportPageCount = 1,
                ) { page ->
                    val categoryId = categories[page].id
                    val gridItems = uiState.homeItemsByCategoryId[categoryId].orEmpty()
                    val loading = uiState.homeCategoryLoadingId == categoryId && gridItems.isEmpty()

                    if (loading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(gridItems.chunked(3)) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    rowItems.forEach { item ->
                                        HomeBatteryGridCard(
                                            item = item,
                                            onClick = {
                                                when {
                                                    item.animated -> onOpenSticker()
                                                    item.title.contains("Troll", ignoreCase = true) -> onOpenBatteryTroll()
                                                    item.title.contains("Search", ignoreCase = true) -> onOpenSearch()
                                                    else -> onOpenStatusBarCustom()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    repeat(3 - rowItems.size) {
                                        Spacer(Modifier.weight(1f))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CustomizeHubScreen(
    onOpenSticker: () -> Unit,
    onOpenFeature: (CustomizeEntry) -> Unit,
    onOpenStatusBarCustom: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotch: () -> Unit,
    onOpenAnimation: () -> Unit,
    onOpenRealTime: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
) {
    val gridEntries = listOf(
                CustomizeEntry.Hotspot,
        CustomizeEntry.Wifi,
                CustomizeEntry.Signal,
        CustomizeEntry.Data,
        CustomizeEntry.Ringer,
        CustomizeEntry.Charge,
                CustomizeEntry.Emotion,
                        CustomizeEntry.Airplane,

        CustomizeEntry.DateTime,
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
                shadowElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            HomeRoundIcon(R.drawable.ic_feeb_back_home, onOpenRealTime)
                        }
                        Text(stringResource(R.string.battery_icon_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            HomeRoundIcon(R.drawable.ic_home_search, onOpenSearch)
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PromoBannerCard(
                backgroundRes = R.drawable.img_bg_emoji_sticker,
                title = stringResource(R.string.home_promo_stickers_title),
                body = stringResource(R.string.home_promo_stickers_body),
                cta = stringResource(R.string.home_promo_stickers_cta),
                onClick = onOpenSticker,
            )
            PromoBannerCard(
                backgroundRes = R.drawable.image_battery_troll_customize,
                title = stringResource(R.string.home_promo_troll_title),
                body = stringResource(R.string.home_promo_troll_body),
                cta = stringResource(R.string.home_promo_troll_cta),
                leadingIconRes = R.drawable.ic_battery_troll_customize_32,
                onClick = onOpenBatteryTroll,
            )
            Surface(
                onClick = onOpenStatusBarCustom,
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_btn_status_bar_new),
                        contentDescription = stringResource(R.string.home_status_bar_customize),
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        stringResource(R.string.home_status_bar_customize),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallCustomizeCard(
                    title = stringResource(R.string.home_notch),
                    iconRes = R.drawable.ic_item_notch,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenNotch,
                )
                SmallCustomizeCard(
                    title = stringResource(R.string.home_animation),
                    iconRes = R.drawable.ic_item_animation,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAnimation,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                Text(
                    stringResource(R.string.home_customize_icon_row),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                gridEntries.forEach { entry ->
                    CustomizeIconGridItem(
                        entry = entry,
                        modifier = Modifier.fillMaxWidth(0.31f),
                        onClick = { onOpenFeature(entry) },
                    )
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
internal fun PromoBannerCard(
    backgroundRes: Int,
    title: String,
    body: String,
    cta: String,
    onClick: () -> Unit,
    leadingIconRes: Int? = null,
    badge: String? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
        ) {
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (badge != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(bottomStart = 18.dp),
                    color = MaterialTheme.colorScheme.secondary,
                ) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, top = 18.dp, bottom = 16.dp)
                    .fillMaxWidth(0.58f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    if (leadingIconRes != null) {
                        Image(
                            painter = painterResource(leadingIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            colorFilter = ColorFilter.tint(Color(0xFF8FB6D4)),
                        )
                    }
                }
                Text(
                    body,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF8FB6D4))
                        .border(BorderStroke(3.dp, Color.White), RoundedCornerShape(999.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    Text(
                        cta,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SmallCustomizeCard(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = title,
                modifier = Modifier.size(40.dp),
            )
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
internal fun CustomizeIconGridItem(
    entry: CustomizeEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(customizeIconRes(entry)),
                        contentDescription = customizeLabel(entry),
                        modifier = Modifier.size(54.dp),
                    )
                }
            }
            Text(
                customizeLabel(entry),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
internal fun HomeRoundIcon(
    iconRes: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
internal fun EnableBanner(
    onStart: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFD8DDE2)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.home_enable_banner),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF8FB6D4)),
            ) {
                TextButton(onClick = onStart) {
                    Text(
                        stringResource(R.string.home_start),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun OriginalTopShell(
    title: String,
    onLeftSecondary: () -> Unit,
    onSearch: () -> Unit,
    showEnableBanner: Boolean = false,
    onStart: (() -> Unit)? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    HomeRoundIcon(R.drawable.ic_feeb_back_home, onLeftSecondary)
                }
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    HomeRoundIcon(R.drawable.ic_home_search, onSearch)
                    Spacer(Modifier.width(16.dp))
                }
            }
            if (showEnableBanner) {
                EnableBanner(onStart = onStart ?: onSearch)
            }
        }
    }
}

@Composable
internal fun HomeBatteryGridCard(
    item: dev.hai.emojibattery.model.HomeBatteryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp),
                ),
        ) {
            if (!item.thumbnailUrl.isNullOrBlank()) {
                val request = ImageRequest.Builder(context)
                    .data(item.thumbnailUrl)
                    .listener(
                        onError = { _, result ->
                            Log.e(
                                "HomeFeed",
                                "Coil onError id=${item.id} url=${item.thumbnailUrl?.take(120)}",
                                result.throwable,
                            )
                        },
                        onSuccess = { _, _ ->
                            Log.d("HomeFeed", "Coil onSuccess id=${item.id}")
                        },
                    )
                    .build()
                AsyncImage(
                    model = request,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Image(
                    painter = painterResource(item.previewRes),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                )
            }
            if (item.premium) {
                Image(
                    painter = painterResource(R.drawable.ic_diamond),
                    contentDescription = stringResource(R.string.cd_premium),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp),
                )
            }
            if (item.animated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                ) {
                    Text(
                        stringResource(R.string.label_gif),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun FeatureTileCard(
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

internal fun featureGlyph(entry: CustomizeEntry): String = when (entry) {
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

internal fun customizeLabel(entry: CustomizeEntry): String = when (entry) {
    CustomizeEntry.Emotion -> "Emotion"
    CustomizeEntry.Charge -> "Charge"
    else -> entry.title
}

internal fun customizeIconRes(entry: CustomizeEntry): Int = when (entry) {
    CustomizeEntry.Ringer -> R.drawable.ic_item_ringer
    CustomizeEntry.Emotion -> R.drawable.ic_item_emotion
    CustomizeEntry.Wifi -> R.drawable.ic_item_wifi
    CustomizeEntry.Airplane -> R.drawable.ic_item_airplane
    CustomizeEntry.Hotspot -> R.drawable.ic_item_hotspot
    CustomizeEntry.Charge -> R.drawable.ic_item_charge
    CustomizeEntry.DateTime -> R.drawable.ic_item_date_time
    CustomizeEntry.Data -> R.drawable.ic_item_data
    CustomizeEntry.Theme -> R.drawable.img_btn_status_bar_new
    CustomizeEntry.Settings -> R.drawable.ic_item_animation
    CustomizeEntry.Signal -> R.drawable.ic_item_signal
}
