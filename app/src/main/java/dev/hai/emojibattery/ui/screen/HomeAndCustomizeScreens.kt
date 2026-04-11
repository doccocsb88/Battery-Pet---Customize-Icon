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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SignalCellular4Bar
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.HomeCategoryTab
import dev.hai.emojibattery.model.HomeBatteryItem
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.ui.theme.OceanSerenity
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush
import dev.hai.emojibattery.ui.theme.StrawberryMilk
import dev.hai.emojibattery.ui.theme.oceanModuleLabelTextStyle
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun HomeScreen(
    uiState: AppUiState,
    onSelectCategory: (String) -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenStatusBarCustom: (HomeBatteryItem?) -> Unit,
    onOpenLegacyBattery: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSticker: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenPremium: () -> Unit,
    onSetOverlayEnabled: (Boolean) -> Unit,
) {
    if (uiState.padCatalogLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = stringResource(R.string.common_loading_ellipsis),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        return
    }

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
            onOpenPremium = onOpenPremium,
            onSetOverlayEnabled = onSetOverlayEnabled,
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
    onOpenStatusBarCustom: (HomeBatteryItem?) -> Unit,
    onOpenLegacyBattery: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSticker: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenPremium: () -> Unit,
    onSetOverlayEnabled: (Boolean) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            OriginalTopShell(
                title = stringResource(R.string.battery_icon_title),
                onLeftSecondary = onOpenFeedback,
                onSearch = onOpenSearch,
                showLeftSecondary = false,
                trailingContent = {
                    if (!uiState.premiumUnlocked) {
                        PremiumButton(
                            modifier = Modifier
                                .width(104.dp)
                                .aspectRatio(2.2f),
                            onClick = onOpenPremium,
                        )
                    } else {
                        Spacer(Modifier.width(40.dp))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EmojiBatteryOverlayAccessCard(
                accessibilityGranted = uiState.accessibilityGranted,
                enabled = uiState.statusBarOverlayEnabled,
                onToggle = onSetOverlayEnabled,
                onRequestAccessibility = onOpenAccessibility,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = StrawberryMilk.SecondaryContainer.copy(alpha = 0.85f),
                shadowElevation = 0.dp,
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        count = categories.size,
                        key = { index -> categories[index].id },
                    ) { index ->
                        val category = categories[index]
                        val selected = pagerState.settledPage == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .then(
                                    if (selected) {
                                        Modifier.background(StrawberryCtaGradientBrush)
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    },
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        onSelectCategory(category.id)
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (category.id == "hot") "Hot" else category.title,
                                color = if (selected) Color.White else StrawberryMilk.Secondary,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
                            )
                        }
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
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(gridItems.chunked(3)) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    rowItems.forEach { item ->
                                        HomeBatteryGridCard(
                                            item = item,
                                            onClick = {
                                                when {
                                                    item.animated -> onOpenSticker()
                                                    item.title.contains("Troll", ignoreCase = true) -> onOpenBatteryTroll()
                                                    item.title.contains("Search", ignoreCase = true) -> onOpenSearch()
                                                    else -> onOpenStatusBarCustom(item)
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
    uiState: AppUiState,
    onOpenSticker: () -> Unit,
    onOpenFeature: (CustomizeEntry) -> Unit,
    onOpenStatusBarCustom: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotch: () -> Unit,
    onOpenAnimation: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
    onOpenPremium: () -> Unit,
    onSetOverlayEnabled: (Boolean) -> Unit,
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                            Spacer(Modifier.width(40.dp))
                        }
                        Text(stringResource(R.string.battery_icon_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!uiState.premiumUnlocked) {
                                PremiumButton(
                                    modifier = Modifier
                                        .width(104.dp)
                                        .aspectRatio(2.2f),
                                    onClick = onOpenPremium,
                                )
                            } else {
                                Spacer(Modifier.width(40.dp))
                            }
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
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp)
                .padding(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EmojiBatteryOverlayAccessCard(
                accessibilityGranted = uiState.accessibilityGranted,
                enabled = uiState.statusBarOverlayEnabled,
                onToggle = onSetOverlayEnabled,
                onRequestAccessibility = onOpenAccessibility,
            )
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
                .height(164.dp),
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
                    .padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 16.dp)
                    .fillMaxWidth(0.64f),
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
                    maxLines = 2,
                )
                Surface(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .heightIn(min = 42.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF4E86BE),
                    border = BorderStroke(3.dp, Color.White),
                    contentColor = Color.White,
                ) {
                    Text(
                        cta,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                        ),
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
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = OceanSerenity.ModuleShadow,
                spotColor = OceanSerenity.ModuleShadow,
            ),
        shape = RoundedCornerShape(28.dp),
        color = OceanSerenity.ModuleCard,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(OceanSerenity.ModuleIconHalo),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = customizeIconVector(entry),
                    contentDescription = customizeLabel(entry),
                    modifier = Modifier.size(22.dp),
                    tint = OceanSerenity.ModuleIconTint,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                customizeLabel(entry).uppercase(),
                textAlign = TextAlign.Center,
                color = OceanSerenity.ModuleLabel,
                style = oceanModuleLabelTextStyle(),
                maxLines = 2,
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
internal fun OriginalTopShell(
    title: String,
    onLeftSecondary: () -> Unit,
    onSearch: () -> Unit,
    showLeftSecondary: Boolean = true,
    showEnableBanner: Boolean = false,
    onStart: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
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
                    if (showLeftSecondary) {
                        HomeRoundIcon(R.drawable.ic_feeb_back_home, onLeftSecondary)
                    } else {
                        Spacer(Modifier.width(40.dp))
                    }
                }
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (trailingContent != null) {
                        trailingContent()
                    } else {
                        Spacer(Modifier.width(40.dp))
                    }
                }
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
    Surface(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = OceanSerenity.ModuleShadow,
                spotColor = OceanSerenity.ModuleShadow,
            ),
        shape = RoundedCornerShape(28.dp),
        color = OceanSerenity.ModuleCard,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
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
                        .padding(14.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Image(
                    painter = painterResource(item.previewRes),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                )
            }
            if (item.premium) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_diamond),
                        contentDescription = stringResource(R.string.cd_premium),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            if (item.animated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.9f)),
                ) {
                    Text(
                        stringResource(R.string.label_gif),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = OceanSerenity.ModuleLabel,
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

internal fun customizeIconVector(entry: CustomizeEntry): ImageVector = when (entry) {
    CustomizeEntry.Hotspot -> Icons.Rounded.WifiTethering
    CustomizeEntry.Wifi -> Icons.Rounded.Wifi
    CustomizeEntry.Signal -> Icons.Rounded.SignalCellular4Bar
    CustomizeEntry.Data -> Icons.Rounded.DataUsage
    CustomizeEntry.Ringer -> Icons.Rounded.NotificationsActive
    CustomizeEntry.Charge -> Icons.Rounded.Bolt
    CustomizeEntry.Emotion -> Icons.Rounded.Mood
    CustomizeEntry.Airplane -> Icons.Rounded.Flight
    CustomizeEntry.DateTime -> Icons.Rounded.Schedule
    CustomizeEntry.Theme -> Icons.Rounded.Tune
    CustomizeEntry.Settings -> Icons.Rounded.Tune
}
