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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush
import dev.hai.emojibattery.ui.theme.StrawberryMilk
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun HomeScreen(
    uiState: AppUiState,
    onSelectCategory: (String) -> Unit,
    onOpenStatusBarCustom: () -> Unit,
    onOpenLegacyBattery: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSticker: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFeedback: () -> Unit,
) {
    val categories = uiState.homeTabs.takeIf { it.isNotEmpty() }
        ?: SampleCatalog.homeCategories.map { HomeCategoryTab(it.id, it.title) }
    if (categories.isEmpty()) return

    val initialPage = remember(uiState.selectedHomeCategoryId, categories) {
        categories.indexOfFirst { it.id == uiState.selectedHomeCategoryId }
            .coerceIn(0, (categories.size - 1).coerceAtLeast(0))
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { categories.size },
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(categories.map { it.id }.joinToString(), uiState.selectedHomeCategoryId) {
        val idx = categories.indexOfFirst { it.id == uiState.selectedHomeCategoryId }.coerceAtLeast(0)
        if (pagerState.currentPage != idx) {
            pagerState.scrollToPage(idx)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                categories.getOrNull(page)?.id?.let(onSelectCategory)
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OriginalTopShell(
                title = "Battery Icon",
                onLeftPrimary = onOpenSettings,
                onLeftSecondary = onOpenFeedback,
                onSearch = onOpenSearch,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp,
            ) {
                Box(Modifier.fillMaxSize()) {
                    // layer-list drawables are not supported by painterResource; draw equivalent wash in Compose
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(alpha = 0.06f)
                            .drawBehind {
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0f to StrawberryMilk.Background,
                                            0.5f to Color(
                                                red = (StrawberryMilk.Background.red + StrawberryMilk.PrimaryContainer.red) / 2f,
                                                green = (StrawberryMilk.Background.green + StrawberryMilk.PrimaryContainer.green) / 2f,
                                                blue = (StrawberryMilk.Background.blue + StrawberryMilk.PrimaryContainer.blue) / 2f,
                                                alpha = 1f,
                                            ),
                                            1f to StrawberryMilk.PrimaryContainer,
                                        ),
                                        start = Offset(size.width, 0f),
                                        end = Offset(0f, size.height),
                                    ),
                                )
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(StrawberryMilk.Primary.copy(alpha = 0.094f), Color.Transparent),
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, 0f),
                                    ),
                                )
                            },
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 14.dp),
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            items(
                                count = categories.size,
                                key = { index -> categories[index].id },
                            ) { index ->
                                val category = categories[index]
                                val selected = pagerState.settledPage == index
                                Text(
                                    text = when {
                                        category.id == "hot" -> "🔥 ${category.title}"
                                        else -> category.title
                                    },
                                    color = if (selected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 8.dp)
                                .size(width = 70.dp, height = 2.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(StrawberryCtaGradientBrush),
                        )
                        HorizontalPager(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
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
                                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 12.dp),
                                ) {
                                    items(gridItems.chunked(3)) { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(0.dp),
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CustomizeHubScreen(
    onOpenSticker: () -> Unit,
    onOpenFeature: (CustomizeEntry) -> Unit,
    onOpenStatusBarCustom: () -> Unit,
    onOpenRealTime: () -> Unit,
    onOpenBatteryTroll: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val gridEntries = listOf(
        CustomizeEntry.Emotion,
        CustomizeEntry.Wifi,
        CustomizeEntry.Data,
        CustomizeEntry.Signal,
        CustomizeEntry.Airplane,
        CustomizeEntry.Hotspot,
        CustomizeEntry.Ringer,
        CustomizeEntry.Charge,
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
                            HomeRoundIcon(R.drawable.ic_settings_new, onOpenSettings)
                            HomeRoundIcon(R.drawable.ic_feeb_back_home, onOpenRealTime)
                        }
                        Text("Battery Icon", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            HomeRoundIcon(R.drawable.ic_home_search, onOpenStatusBarCustom)
                            Image(
                                painter = painterResource(R.drawable.no_ads_on),
                                contentDescription = "Ads on",
                                modifier = Modifier.size(width = 40.dp, height = 36.dp),
                            )
                        }
                    }
                    EnableBanner(onStart = onOpenStatusBarCustom)
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
                title = "Status Bar Stickers",
                body = "Animated stickers for your status bar!",
                cta = "Customize Now",
                onClick = onOpenSticker,
            )
            FakeAdCard()
            PromoBannerCard(
                backgroundRes = R.drawable.image_battery_troll_customize,
                title = "Battery Troll",
                body = "Just for fun, fake your battery % to everyone",
                cta = "Troll Mode",
                leadingIconRes = R.drawable.ic_battery_troll_customize_32,
                badge = "NEW",
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
                        contentDescription = "Status Bar Customize",
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        "Status Bar Customize",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallCustomizeCard(
                    title = "Notch",
                    iconRes = R.drawable.ic_item_notch,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenStatusBarCustom,
                )
                SmallCustomizeCard(
                    title = "Animation",
                    iconRes = R.drawable.ic_item_animation,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenStatusBarCustom,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                Text(
                    "Customize Icon",
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
                        .background(MaterialTheme.colorScheme.primary)
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
internal fun FakeAdCard() {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.secondary) {
                            Text("Ad", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                        }
                        Text("CapCut", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    }
                    Text("Pangle Test Ads - 2", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("VIEW NOW", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
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
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = scheme.primaryContainer,
        border = BorderStroke(2.dp, scheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Enable emoji battery to begin",
                color = scheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(StrawberryCtaGradientBrush),
            ) {
                TextButton(onClick = onStart) {
                    Text("Start", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
internal fun OriginalTopShell(
    title: String,
    onLeftPrimary: () -> Unit,
    onLeftSecondary: () -> Unit,
    onSearch: () -> Unit,
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
                    HomeRoundIcon(R.drawable.ic_settings_new, onLeftPrimary)
                    HomeRoundIcon(R.drawable.ic_feeb_back_home, onLeftSecondary)
                }
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    HomeRoundIcon(R.drawable.ic_home_search, onSearch)
                    Image(
                        painter = painterResource(R.drawable.no_ads_on),
                        contentDescription = "Ads on",
                        modifier = Modifier.size(width = 40.dp, height = 36.dp),
                    )
                }
            }
            EnableBanner(onStart = onSearch)
        }
    }
}

@Composable
internal fun HomeBatteryGridCard(
    item: dev.hai.emojibattery.model.HomeBatteryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 7.dp, vertical = 7.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f), shape = RoundedCornerShape(16.dp))
        ) {
            if (!item.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Image(
                    painter = painterResource(item.previewRes),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                )
            }
            if (item.premium) {
                Image(
                    painter = painterResource(R.drawable.ic_diamond),
                    contentDescription = "Premium",
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
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    Text(
                        "GIF",
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
    CustomizeEntry.Emotion -> R.drawable.ic_item_emotion
    CustomizeEntry.Wifi -> R.drawable.ic_item_wifi
    CustomizeEntry.Data -> R.drawable.ic_item_data
    CustomizeEntry.Signal -> R.drawable.ic_item_signal
    CustomizeEntry.Airplane -> R.drawable.ic_item_airplane
    CustomizeEntry.Hotspot -> R.drawable.ic_item_hotspot
    CustomizeEntry.Ringer -> R.drawable.ic_item_ringer
    CustomizeEntry.Charge -> R.drawable.ic_item_charge
    CustomizeEntry.DateTime -> R.drawable.ic_item_date_time
    CustomizeEntry.Theme -> R.drawable.img_btn_status_bar_new
    CustomizeEntry.Settings -> R.drawable.ic_item_animation
}
