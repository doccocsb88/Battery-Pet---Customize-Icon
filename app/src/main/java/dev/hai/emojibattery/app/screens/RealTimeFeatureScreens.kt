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
import androidx.compose.material.icons.rounded.Colorize
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

@Composable
internal fun RealTimeScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onApply: () -> Unit,
) {
    val selected = SampleCatalog.realTimeTemplates.first { it.id == uiState.selectedRealTimeTemplateId }
    ScreenContainer(
        title = stringResource(R.string.realtime_screen_title),
        subtitle = stringResource(R.string.realtime_screen_subtitle),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TemplatePreviewCard(
                title = selected.title,
                summary = selected.summary,
                glyph = selected.accentGlyph,
                tag = selected.tag,
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    if (uiState.premiumUnlocked || uiState.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_PREMIUM_REALTIME_CAT_DIARY)) {
                        "Premium template access is available for this account state."
                    } else {
                        "Templates marked PRO open a paywall unless unlocked by reward or premium access."
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PermissionBanner(enabled = uiState.accessibilityGranted, onToggle = onToggleAccessibility)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SampleCatalog.realTimeTemplates) { template ->
                    ContentTemplateCard(
                        template = template,
                        selected = template.id == uiState.selectedRealTimeTemplateId,
                        onClick = { onSelectTemplate(template.id) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.onboarding_back))
                }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.apply_template))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FeatureDetailScreen(
    entry: CustomizeEntry,
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    if (entry == CustomizeEntry.DateTime) {
        DateTimeFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Ringer) {
        RingerFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Hotspot) {
        HotspotFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Airplane) {
        AirplaneFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Charge) {
        ChargeFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Signal) {
        SignalFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Data) {
        DataFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Wifi) {
        WifiFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Emotion) {
        EmotionFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    val config = uiState.featureConfigs[entry] ?: FeatureConfig(variant = SampleCatalog.featureVariants[entry]?.first().orEmpty())
    val variants = SampleCatalog.featureVariants[entry].orEmpty()
    ScreenContainer(title = entry.title, subtitle = "Isolated editor with local config, variants, and apply/reset actions.") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TemplatePreviewCard(
                title = entry.title,
                summary = entry.subtitle,
                glyph = featureGlyph(entry),
                tag = if (config.enabled) config.variant else stringResource(R.string.feature_disabled),
            )
            SettingToggle(stringResource(R.string.feature_enable_format, entry.title), config.enabled, onToggleEnabled)
            SliderField(stringResource(R.string.feature_intensity_format, entry.title), config.intensity, 0.1f..1f, onSetIntensity)
            Text(stringResource(R.string.common_variants), style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                variants.forEach { variant ->
                    ChoiceChip(
                        label = variant,
                        selected = variant == config.variant,
                        onClick = { onSelectVariant(variant) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.onboarding_back))
                }
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.common_reset))
                }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    }
}

private data class WifiColorOption(
    val id: String,
    val color: Color,
)

private val WifiColorOptions = listOf(
    WifiColorOption("picker", Color.Transparent),
    WifiColorOption("blue", Color(0xFF2952F4)),
    WifiColorOption("green", Color(0xFF2BDF52)),
    WifiColorOption("orange", Color(0xFFF18410)),
    WifiColorOption("black", Color(0xFF11111A)),
    WifiColorOption("yellow", Color(0xFFF1DF1E)),
)

private data class ChargeOption(
    val id: String,
    val glyph: String,
)

private val ChargeOptions = listOf(
    ChargeOption("chg_1", "⚡"),
    ChargeOption("chg_2", "↯"),
    ChargeOption("chg_3", "⌁"),
    ChargeOption("chg_4", "⏻"),
    ChargeOption("chg_5", "🔌"),
    ChargeOption("chg_6", "⏚"),
    ChargeOption("chg_7", "ϟ"),
    ChargeOption("chg_8", "⌬"),
    ChargeOption("chg_9", "⎓"),
    ChargeOption("chg_10", "⟡"),
    ChargeOption("chg_11", "⌇"),
    ChargeOption("chg_12", "⋇"),
)

@Composable
private fun RingerFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) = SizeColorFeatureScreen(
    title = stringResource(R.string.ringer),
    sizeLabel = stringResource(R.string.ringer_size),
    entry = CustomizeEntry.Ringer,
    uiState = uiState,
    onBack = onBack,
    onToggleEnabled = onToggleEnabled,
    onSetIntensity = onSetIntensity,
    onSelectVariant = onSelectVariant,
    onApply = onApply,
)

@Composable
private fun HotspotFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) = SizeColorFeatureScreen(
    title = stringResource(R.string.hotspot),
    sizeLabel = stringResource(R.string.hotspot_size),
    entry = CustomizeEntry.Hotspot,
    uiState = uiState,
    onBack = onBack,
    onToggleEnabled = onToggleEnabled,
    onSetIntensity = onSetIntensity,
    onSelectVariant = onSelectVariant,
    onApply = onApply,
)

@Composable
private fun AirplaneFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) = SizeColorFeatureScreen(
    title = stringResource(R.string.airplane),
    sizeLabel = stringResource(R.string.airplane_size),
    entry = CustomizeEntry.Airplane,
    uiState = uiState,
    onBack = onBack,
    onToggleEnabled = onToggleEnabled,
    onSetIntensity = onSetIntensity,
    onSelectVariant = onSelectVariant,
    onApply = onApply,
)

@Composable
private fun SizeColorFeatureScreen(
    title: String,
    sizeLabel: String,
    entry: CustomizeEntry,
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[entry]
        ?: FeatureConfig(variant = WifiColorOptions[1].id)
    val selectedColorId = WifiColorOptions.firstOrNull { it.id == config.variant }?.id ?: WifiColorOptions[1].id
    val sliderDp = (10f + (26f * config.intensity)).coerceIn(10f, 36f)

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
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
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A3F46),
                )
                Spacer(Modifier.weight(1f))
                Text(text = "🍼", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFD48DF6)),
                color = Color(0xFFFBE4F5),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.enable_disable_emoji_battery),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A3F46),
                    )
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = onToggleEnabled,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = sizeLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Slider(
                                value = sliderDp,
                                onValueChange = { dpValue ->
                                    val intensity = ((dpValue - 10f) / 26f).coerceIn(0.1f, 1f)
                                    onSetIntensity(intensity)
                                },
                                valueRange = 10f..36f,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${sliderDp.toInt()}dp",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4A3F46),
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.icon_color_short),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WifiColorOptions.forEach { option ->
                                val selected = option.id == selectedColorId
                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable(enabled = config.enabled) {
                                            onSelectVariant(option.id)
                                            onApply()
                                        },
                                    shape = CircleShape,
                                    color = if (option.id == "picker") Color.White else option.color,
                                    border = BorderStroke(
                                        if (selected) 2.dp else 0.8.dp,
                                        if (selected) Color(0xFFD48DF6) else Color(0xFFDEDEE5),
                                    ),
                                ) {
                                    if (option.id == "picker") {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Colorize,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp),
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
    }
}


@Composable
private fun SignalFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[CustomizeEntry.Signal]
        ?: FeatureConfig(variant = WifiColorOptions[1].id)
    val selectedColorId = WifiColorOptions.firstOrNull { it.id == config.variant }?.id ?: WifiColorOptions[1].id
    val sliderDp = (10f + (26f * config.intensity)).coerceIn(10f, 36f)

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
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
                    text = stringResource(R.string.signal),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A3F46),
                )
                Spacer(Modifier.weight(1f))
                Text(text = "🍼", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFD48DF6)),
                color = Color(0xFFFBE4F5),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.enable_disable_emoji_battery),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A3F46),
                    )
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = onToggleEnabled,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.signal_size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Slider(
                                value = sliderDp,
                                onValueChange = { dpValue ->
                                    val intensity = ((dpValue - 10f) / 26f).coerceIn(0.1f, 1f)
                                    onSetIntensity(intensity)
                                },
                                valueRange = 10f..36f,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${sliderDp.toInt()}dp",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4A3F46),
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.icon_color_short),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WifiColorOptions.forEach { option ->
                                val selected = option.id == selectedColorId
                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable(enabled = config.enabled) {
                                            onSelectVariant(option.id)
                                            onApply()
                                        },
                                    shape = CircleShape,
                                    color = if (option.id == "picker") Color.White else option.color,
                                    border = BorderStroke(
                                        if (selected) 2.dp else 0.8.dp,
                                        if (selected) Color(0xFFD48DF6) else Color(0xFFDEDEE5),
                                    ),
                                ) {
                                    if (option.id == "picker") {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Colorize,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp),
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
    }
}

@Composable
private fun WifiFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[CustomizeEntry.Wifi]
        ?: FeatureConfig(variant = WifiColorOptions[1].id)
    val selectedColorId = WifiColorOptions.firstOrNull { it.id == config.variant }?.id ?: WifiColorOptions[1].id
    val sliderDp = (10f + (26f * config.intensity)).coerceIn(10f, 36f)

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
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
                    text = stringResource(R.string.wifi),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A3F46),
                )
                Spacer(Modifier.weight(1f))
                Text(text = "🍼", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFD48DF6)),
                color = Color(0xFFFBE4F5),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.enable_disable_emoji_battery),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A3F46),
                    )
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = onToggleEnabled,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.wi_fi_size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Slider(
                                value = sliderDp,
                                onValueChange = { dpValue ->
                                    val intensity = ((dpValue - 10f) / 26f).coerceIn(0.1f, 1f)
                                    onSetIntensity(intensity)
                                },
                                valueRange = 10f..36f,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${sliderDp.toInt()}dp",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4A3F46),
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.icon_color_short),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WifiColorOptions.forEach { option ->
                                val selected = option.id == selectedColorId
                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable(enabled = config.enabled) {
                                            onSelectVariant(option.id)
                                            onApply()
                                        },
                                    shape = CircleShape,
                                    color = if (option.id == "picker") Color.White else option.color,
                                    border = BorderStroke(
                                        if (selected) 2.dp else 0.8.dp,
                                        if (selected) Color(0xFFD48DF6) else Color(0xFFDEDEE5),
                                    ),
                                ) {
                                    if (option.id == "picker") {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Colorize,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp),
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
    }
}

@Composable
private fun DataFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[CustomizeEntry.Data]
        ?: FeatureConfig(variant = "5G")
    val colorOptions = WifiColorOptions
    val styleOptions = listOf("2G", "3G", "4G", "5G", "6G")
    val selectedStyle = styleOptions.firstOrNull { it.equals(config.variant, ignoreCase = true) } ?: "5G"
    val selectedColorId = colorOptions.firstOrNull { it.id == config.variant }?.id ?: colorOptions[1].id
    val sliderDp = (10f + (26f * config.intensity)).coerceIn(10f, 36f)

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
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
                    text = stringResource(R.string.data),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A3F46),
                )
                Spacer(Modifier.weight(1f))
                Text(text = "🍼", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFD48DF6)),
                color = Color(0xFFFBE4F5),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.enable_disable_emoji_battery),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A3F46),
                    )
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = onToggleEnabled,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.data_size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Slider(
                                value = sliderDp,
                                onValueChange = { dpValue ->
                                    val intensity = ((dpValue - 10f) / 26f).coerceIn(0.1f, 1f)
                                    onSetIntensity(intensity)
                                },
                                valueRange = 10f..36f,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${sliderDp.toInt()}dp",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4A3F46),
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.icon_color_short),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            colorOptions.forEach { option ->
                                val selected = option.id == selectedColorId
                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable(enabled = config.enabled) {
                                            onSelectVariant(option.id)
                                            onApply()
                                        },
                                    shape = CircleShape,
                                    color = if (option.id == "picker") Color.White else option.color,
                                    border = BorderStroke(
                                        if (selected) 2.dp else 0.8.dp,
                                        if (selected) Color(0xFFD48DF6) else Color(0xFFDEDEE5),
                                    ),
                                ) {
                                    if (option.id == "picker") {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Colorize,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.data_style),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        styleOptions.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                row.forEach { style ->
                                    val selected = style == selectedStyle
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clickable(enabled = config.enabled) {
                                                onSelectVariant(style)
                                                onApply()
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            if (selected) 1.dp else 0.5.dp,
                                            if (selected) Color(0xFFD48DF6) else Color(0xFFE9E4E8),
                                        ),
                                        color = if (selected) Color(0xFFF7E4FA) else Color(0xFFF8F8F8),
                                    ) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = style,
                                                style = MaterialTheme.typography.displaySmall,
                                                color = Color(0xFF181823),
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                }
                                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChargeFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[CustomizeEntry.Charge]
        ?: FeatureConfig(variant = ChargeOptions.first().id)
    val selectedId = ChargeOptions.firstOrNull { it.id == config.variant }?.id ?: ChargeOptions.first().id

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
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
                    text = stringResource(R.string.charge),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A3F46),
                )
                Spacer(Modifier.weight(1f))
                Text(text = "🍼", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFD48DF6)),
                color = Color(0xFFFBE4F5),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.enable_disable_emoji_battery),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A3F46),
                    )
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = onToggleEnabled,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ChargeOptions.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { item ->
                                val selected = item.id == selectedId
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable(enabled = config.enabled) {
                                            onSelectVariant(item.id)
                                            onApply()
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        if (selected) 1.dp else 0.5.dp,
                                        if (selected) Color(0xFFD48DF6) else Color(0xFFE9E4E8),
                                    ),
                                    color = if (selected) Color(0xFFF7E4FA) else Color(0xFFF8F8F8),
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = item.glyph,
                                            style = MaterialTheme.typography.displayMedium,
                                            color = Color(0xFF12122B),
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
}

private data class DateTimeStyleOption(
    val id: String,
    val line1: String,
    val line2: String? = null,
    val line2Bold: Boolean = false,
)

private val DateTimeStyles = listOf(
    DateTimeStyleOption("style_1", "Tue, Mar 24"),
    DateTimeStyleOption("style_2", "Tue, Mar", "24", line2Bold = true),
    DateTimeStyleOption("style_3", "Tue", "24", line2Bold = true),
    DateTimeStyleOption("style_4", "Mar 24"),
    DateTimeStyleOption("style_5", "Tuesday"),
    DateTimeStyleOption("style_6", "Tuesday", "24", line2Bold = true),
)

private data class DateTimeVariantState(
    val styleId: String,
    val colorId: String,
    val showDate: Boolean,
)

private fun parseDateTimeVariant(raw: String?): DateTimeVariantState {
    val fallback = DateTimeVariantState(styleId = "style_4", colorId = "blue", showDate = true)
    if (raw.isNullOrBlank()) return fallback
    val pieces = raw.split(";").mapNotNull {
        val p = it.split("=", limit = 2)
        if (p.size == 2) p[0] to p[1] else null
    }.toMap()
    return DateTimeVariantState(
        styleId = pieces["style"] ?: fallback.styleId,
        colorId = pieces["color"] ?: fallback.colorId,
        showDate = (pieces["show"] ?: "1") == "1",
    )
}

private fun encodeDateTimeVariant(state: DateTimeVariantState): String =
    "style=${state.styleId};color=${state.colorId};show=${if (state.showDate) "1" else "0"}"

@Composable
private fun DateTimeFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[CustomizeEntry.DateTime]
        ?: FeatureConfig(variant = encodeDateTimeVariant(parseDateTimeVariant(null)))
    val parsed = parseDateTimeVariant(config.variant)
    val sliderDp = (10f + (26f * config.intensity)).coerceIn(10f, 36f)

    fun updateVariant(newState: DateTimeVariantState) {
        onSelectVariant(encodeDateTimeVariant(newState))
        onApply()
    }

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
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
                    text = stringResource(R.string.date_time_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A3F46),
                )
                Spacer(Modifier.weight(1f))
                Text(text = "🍼", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFD48DF6)),
                color = Color(0xFFFBE4F5),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.enable_disable_emoji_battery),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A3F46),
                    )
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = onToggleEnabled,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.show_date),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Switch(
                            checked = parsed.showDate,
                            onCheckedChange = { checked -> updateVariant(parsed.copy(showDate = checked)) },
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.date_size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Slider(
                                value = sliderDp,
                                onValueChange = { dpValue ->
                                    val intensity = ((dpValue - 10f) / 26f).coerceIn(0.1f, 1f)
                                    onSetIntensity(intensity)
                                },
                                valueRange = 10f..36f,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${sliderDp.toInt()}dp",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4A3F46),
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.date_color),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WifiColorOptions.forEach { option ->
                                val selected = option.id == parsed.colorId
                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable(enabled = config.enabled) {
                                            updateVariant(parsed.copy(colorId = option.id))
                                        },
                                    shape = CircleShape,
                                    color = if (option.id == "picker") Color.White else option.color,
                                    border = BorderStroke(
                                        if (selected) 2.dp else 0.8.dp,
                                        if (selected) Color(0xFFD48DF6) else Color(0xFFDEDEE5),
                                    ),
                                ) {
                                    if (option.id == "picker") {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Colorize,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.date_style),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        DateTimeStyles.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                row.forEach { style ->
                                    val selected = style.id == parsed.styleId
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clickable(enabled = config.enabled) {
                                                updateVariant(parsed.copy(styleId = style.id))
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            if (selected) 1.dp else 0.5.dp,
                                            if (selected) Color(0xFFD48DF6) else Color(0xFFE9E4E8),
                                        ),
                                        color = if (selected) Color(0xFFF7E4FA) else Color(0xFFF8F8F8),
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Text(
                                                text = style.line1,
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = Color(0xFF181823),
                                                fontWeight = FontWeight.Medium,
                                            )
                                            style.line2?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = Color(0xFF181823),
                                                    fontWeight = if (style.line2Bold) FontWeight.Bold else FontWeight.Medium,
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
        }
    }
}

private data class EmotionOption(
    val id: String,
    val glyph: String,
)

private val EmotionOptions = listOf(
    EmotionOption("emo_cute", "🥺"),
    EmotionOption("emo_laugh", "😆"),
    EmotionOption("emo_dizzy", "😵"),
    EmotionOption("emo_shock", "😮"),
    EmotionOption("emo_heart", "😍"),
    EmotionOption("emo_kiss", "😙"),
    EmotionOption("emo_plead", "😟"),
    EmotionOption("emo_smile", "😊"),
    EmotionOption("emo_winkkiss", "😘"),
    EmotionOption("emo_sleepy", "🥱"),
    EmotionOption("emo_cool", "😎"),
    EmotionOption("emo_sleep", "😪"),
    EmotionOption("emo_relief", "🙂"),
    EmotionOption("emo_crylaugh", "😅"),
    EmotionOption("emo_scared", "😨"),
)

@Composable
private fun EmotionFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[CustomizeEntry.Emotion]
        ?: FeatureConfig(variant = EmotionOptions.first().id)
    val selectedId = EmotionOptions.firstOrNull { it.id == config.variant }?.id ?: EmotionOptions.first().id

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
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
                    text = stringResource(R.string.emotion),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A3F46),
                )
                Spacer(Modifier.weight(1f))
                Text(text = "🍼", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFD48DF6)),
                color = Color(0xFFFBE4F5),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.enable_disable_emoji_battery),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A3F46),
                    )
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = onToggleEnabled,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_bullet2),
                                contentDescription = null,
                                modifier = Modifier.size(5.dp, 18.dp),
                            )
                            Text(
                                text = stringResource(R.string.enable_emotion),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A3F46),
                            )
                        }
                        Switch(
                            checked = config.enabled,
                            onCheckedChange = onToggleEnabled,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_bullet2),
                            contentDescription = null,
                            modifier = Modifier.size(5.dp, 18.dp),
                        )
                        Text(
                            text = stringResource(R.string.emotion_list),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A3F46),
                        )
                    }

                    EmotionOptions.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { item ->
                                val selected = item.id == selectedId
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable(enabled = config.enabled) {
                                            onSelectVariant(item.id)
                                            onApply()
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        if (selected) 1.dp else 0.5.dp,
                                        if (selected) Color(0xFFD48DF6) else Color(0xFFE9E4E8),
                                    ),
                                    color = if (selected) Color(0xFFF7E4FA) else Color(0xFFF8F8F8),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(text = item.glyph, style = MaterialTheme.typography.displaySmall)
                                    }
                                }
                            }
                            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}
