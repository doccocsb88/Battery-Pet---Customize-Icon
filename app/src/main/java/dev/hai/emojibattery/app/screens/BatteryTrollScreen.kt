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
import androidx.compose.material3.TopAppBar
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
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush
import dev.hai.emojibattery.service.AccessibilityBridge
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore
import dev.hai.emojibattery.ui.navigation.AppRoute
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun BatteryTrollScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onSetMessage: (String) -> Unit,
    onToggleAutoDrop: (Boolean) -> Unit,
    onOpenTutorial: () -> Unit,
    onRefreshBatteryTrollCatalog: () -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onApply: () -> Unit,
    onTurnOff: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onRefreshBatteryTrollCatalog()
    }

    val templateLibrary = if (uiState.batteryTrollCatalogRemote.isNotEmpty()) {
        uiState.batteryTrollCatalogRemote
    } else {
        SampleCatalog.batteryTrollTemplates
    }
    val selected = uiState.batteryTrollTemplateForId(uiState.selectedBatteryTrollTemplateId)
        ?: SampleCatalog.batteryTrollTemplates.first()
    val chipMessages = buildList {
        addAll(SampleCatalog.trollMessageOptions)
        templateLibrary.forEach { t ->
            if (t.prankMessage.isNotBlank() && t.prankMessage !in this) add(t.prankMessage)
        }
        if (uiState.trollMessage.isNotBlank() && uiState.trollMessage !in this) add(uiState.trollMessage)
    }
    val trollScroll = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.battery_troll_title),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Image(
                            painter = painterResource(R.drawable.ic_back_40_new),
                            contentDescription = stringResource(R.string.cd_back),
                            modifier = Modifier.size(40.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(trollScroll)
                .padding(horizontal = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BatteryTrollPreviewCard(
                template = selected,
                trollMessage = uiState.trollMessage,
                overlayEnabled = uiState.trollOverlayEnabled,
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_battery_troll_customize_32),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        stringResource(R.string.battery_troll_intro),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            PermissionBanner(
                enabled = uiState.accessibilityGranted,
                onToggle = onToggleAccessibility,
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.battery_troll_templates),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable(onClick = onOpenTutorial),
                        ) {
                            Text(
                                stringResource(R.string.tutorial),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                    if (uiState.batteryTrollCatalogLoading && uiState.batteryTrollCatalogRemote.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val loadingComposition by rememberLottieComposition(
                                LottieCompositionSpec.Asset("cute_loading.json"),
                            )
                            LottieAnimation(
                                composition = loadingComposition,
                                iterations = LottieConstants.IterateForever,
                                speed = 2f,
                                modifier = Modifier.size(120.dp),
                            )
                            Text(
                                stringResource(R.string.common_loading_ellipsis),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            templateLibrary.chunked(4).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    row.forEach { template ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            TrollTemplateCard(
                                                template = template,
                                                selected = template.id == uiState.selectedBatteryTrollTemplateId,
                                                onClick = { onSelectTemplate(template.id) },
                                            )
                                        }
                                    }
                                    repeat(4 - row.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        stringResource(R.string.common_customize),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        chipMessages.forEach { option ->
                            ChoiceChip(
                                label = option,
                                selected = option == uiState.trollMessage,
                                onClick = { onSetMessage(option) },
                            )
                        }
                    }
                    SettingToggle("Auto drop animation", uiState.trollAutoDrop, onToggleAutoDrop)
                    HorizontalDivider(thickness = 1.dp, color = Color.Black)
                    Text(
                        stringResource(R.string.battery_troll_fake_preview_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        stringResource(R.string.battery_troll_fake_message_format, uiState.trollMessage),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable(onClick = onTurnOff),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_turn_off_shimeji),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.common_turn_off),
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    StrawberryCtaGradientBrush,
                                )
                                .clickable(onClick = onApply),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(R.string.common_save),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TrollMediaPreview(
    template: BatteryTrollTemplate,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            template.lottieUrl != null -> {
                val composition by rememberLottieComposition(LottieCompositionSpec.Url(template.lottieUrl))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            template.thumbnailUrl != null -> {
                AsyncImage(
                    model = template.thumbnailUrl,
                    contentDescription = template.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            else -> {
                Text(
                    template.accentGlyph,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        }
    }
}

@Composable
internal fun BatteryTrollPreviewCard(
    template: BatteryTrollTemplate,
    trollMessage: String,
    overlayEnabled: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.battery_troll_preview_card_title),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                if (overlayEnabled) {
                    stringResource(R.string.overlay_active)
                } else {
                    stringResource(R.string.overlay_inactive)
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TrollMediaPreview(
                        template,
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    )
                    Text(
                        text = stringResource(R.string.battery_troll_fake_message_format, trollMessage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Text(
                "${template.title} • ${template.prankMessage}",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun TrollTemplateCard(
    template: BatteryTrollTemplate,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = if (selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        } else {
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TrollMediaPreview(template, Modifier.fillMaxSize())
                }
                Text(
                    template.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (template.premium) {
                    Image(
                        painter = painterResource(R.drawable.ic_diamond),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}
