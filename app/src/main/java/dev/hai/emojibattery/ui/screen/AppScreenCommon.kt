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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
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
import java.util.Locale

@Composable
internal fun PermissionBanner(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.accessibility_bridge_title), fontWeight = FontWeight.SemiBold)
                Text(
                    if (enabled) {
                        stringResource(R.string.accessibility_bridge_active)
                    } else {
                        stringResource(R.string.accessibility_bridge_disabled)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
internal fun TabStrip(
    tabs: List<StatusBarTab>,
    selected: StatusBarTab,
    onSelect: (StatusBarTab) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tabs) { tab ->
            FilterChip(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                label = { Text(tab.title) },
            )
        }
    }
}

@Composable
internal fun SettingToggle(
    label: String,
    value: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label)
            Switch(checked = value, onCheckedChange = onChange)
        }
    }
}

@Composable
internal fun SliderField(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.slider_percent_format, (value * 100).toInt()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(value = value, onValueChange = onChange, valueRange = range)
        }
    }
}

@Composable
internal fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    AssistChip(onClick = onClick, label = { Text(label) }, leadingIcon = if (selected) {
        {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    } else {
        null
    })
}

@Composable
internal fun HeroCard(
    title: String,
    body: String,
    cta: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onClick) { Text(cta) }
        }
    }
}

@Composable
internal fun SmallActionCard(
    title: String,
    subtitle: String,
    glyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(glyph, style = MaterialTheme.typography.headlineSmall)
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScreenContainer(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
internal fun PlaceholderScreen(
    title: String,
    subtitle: String,
) {
    ScreenContainer(title = title, subtitle = subtitle) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = subtitle,
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
internal fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (AppRoute, MainSection) -> Unit,
) {
    val items = listOf(
        Triple(AppRoute.Home, MainSection.Home, Icons.Rounded.Home),
        Triple(AppRoute.Customize, MainSection.Customize, Icons.Rounded.AutoAwesome),
        Triple(AppRoute.Gesture, MainSection.Gesture, Icons.Rounded.TouchApp),
        Triple(AppRoute.Settings, MainSection.Settings, Icons.Rounded.Settings),
    )
    val barShape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = barShape,
                    ambientColor = Color.Black.copy(alpha = 0.06f),
                    spotColor = Color.Black.copy(alpha = 0.06f),
                )
                .clip(barShape)
                .background(Color(0xFFFFFFFF))
                .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
        items.forEach { (route, section, icon) ->
            val selected = currentRoute == route.route
            val itemColor = if (selected) Color(0xFF3C637E) else Color(0xFF94A3B8)
            val iconSize = 24.dp

            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) Color(0xFFAFD6F6) else Color.Transparent)
                    .clickable { onNavigate(route, section) }
                    .widthIn(min = if (selected) 122.dp else 92.dp)
                    .padding(
                        horizontal = if (selected) 22.dp else 10.dp,
                        vertical = 10.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = section.title,
                    modifier = Modifier.size(iconSize),
                    tint = itemColor,
                )
                Text(
                    text = section.title.uppercase(Locale.ROOT),
                    color = itemColor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 1.2.sp,
                    ),
                )
            }
        }
        }
    }
}
