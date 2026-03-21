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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun EmojiStickerScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onAddSticker: (String) -> Unit,
    onSelectSticker: (String) -> Unit,
    onRemoveSticker: (String) -> Unit,
    onUpdateStickerSize: (Float) -> Unit,
    onUpdateStickerSpeed: (Float) -> Unit,
    onOpenTutorial: () -> Unit,
    onRefreshStickerCatalog: () -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onSave: () -> Unit,
    onTurnOff: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onRefreshStickerCatalog()
    }

    val stickerLibrary = if (uiState.stickerCatalogRemote.isNotEmpty()) {
        uiState.stickerCatalogRemote
    } else {
        SampleCatalog.stickerPresets
    }
    val selectedSticker = uiState.selectedStickerId?.let { uiState.stickerPresetForId(it) }
    val selectedPlacement = uiState.selectedStickerId?.let { id ->
        uiState.stickerPlacements.firstOrNull { it.stickerId == id }
    }
    val stickerScroll = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFFEF5FA),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(stickerScroll)
                .padding(horizontal = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_40_new),
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(40.dp)
                        .clickable(onClick = onBack),
                )
            }
            StickerPreviewCard(
                selectedSticker = selectedSticker,
                selectedPlacement = selectedPlacement,
                overlayEnabled = uiState.stickerOverlayEnabled,
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                Text(
                    if (uiState.premiumUnlocked) {
                        "Premium unlocked. You can add up to ${SampleCatalog.PREMIUM_STICKER_SLOTS} stickers."
                    } else if (uiState.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_EXTRA_STICKER_SLOT)) {
                        "Reward unlocked. You can add up to ${SampleCatalog.REWARD_EXTRA_STICKER_SLOTS} stickers."
                    } else {
                        "Free mode allows ${SampleCatalog.FREE_STICKER_SLOTS} sticker. Premium stickers trigger paywall."
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Color(0xFF5C4B51),
                    style = MaterialTheme.typography.bodyMedium,
                )
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
                            "Add Sticker",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFE5FC),
                            modifier = Modifier.clickable(onClick = onOpenTutorial),
                        ) {
                            Text(
                                "Tutorial",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color(0xFF5C4B51),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                    if (uiState.stickerCatalogLoading && uiState.stickerCatalogRemote.isEmpty()) {
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
                                "Loading…",
                                color = Color(0xFF5C4B51),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stickerLibrary.chunked(4).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    row.forEach { sticker ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            StickerCatalogCard(
                                                sticker = sticker,
                                                selected = uiState.selectedStickerId == sticker.id,
                                                added = uiState.stickerPlacements.any { it.stickerId == sticker.id },
                                                onClick = { onAddSticker(sticker.id) },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "My Sticker",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "${uiState.stickerPlacements.size} added",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (uiState.stickerPlacements.isEmpty()) {
                        Text(
                            "Add a sticker from the library above to start editing.",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(88.dp),
                        ) {
                            items(uiState.stickerPlacements, key = { it.stickerId }) { placement ->
                                val sticker = uiState.stickerPresetForId(placement.stickerId)
                                if (sticker != null) {
                                    AddedStickerChip(
                                        sticker = sticker,
                                        selected = uiState.selectedStickerId == sticker.id,
                                        onSelect = { onSelectSticker(sticker.id) },
                                        onRemove = { onRemoveSticker(sticker.id) },
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.Black)
                    Text(
                        "Selected Sticker Controls",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5C4B51),
                    )
                    if (selectedSticker != null && selectedPlacement != null) {
                        Text(
                            "${selectedSticker.glyph} ${selectedSticker.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF5C4B51),
                        )
                        SliderField("Sticker size", selectedPlacement.size, 0.2f..1f, onUpdateStickerSize)
                        SliderField("Sticker speed", selectedPlacement.speed, 0.2f..1f, onUpdateStickerSpeed)
                    } else {
                        Text(
                            "Select one of your added stickers to edit size and speed.",
                            color = Color(0xFF5C4B51),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFFFFE5FC),
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
                                    "Turn Off",
                                    color = Color(0xFFD47DFE),
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
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFABE5), Color(0xFFD47DFE)),
                                    ),
                                )
                                .clickable(onClick = onSave),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Save",
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
internal fun StickerMediaPreview(
    sticker: StickerPreset,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            sticker.lottieUrl != null -> {
                val composition by rememberLottieComposition(LottieCompositionSpec.Url(sticker.lottieUrl))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            sticker.thumbnailUrl != null -> {
                AsyncImage(
                    model = sticker.thumbnailUrl,
                    contentDescription = sticker.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            else -> {
                Text(
                    sticker.glyph,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        }
    }
}

@Composable
internal fun StickerPreviewCard(
    selectedSticker: StickerPreset?,
    selectedPlacement: StickerPlacement?,
    overlayEnabled: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE5C7D2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Sticker Preview",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5C4B51),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                if (overlayEnabled) "Overlay active" else "Overlay inactive",
                color = Color(0xFF5C4B51),
                style = MaterialTheme.typography.bodySmall,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F8F8)),
            ) {
                if (selectedSticker != null) {
                    StickerMediaPreview(
                        selectedSticker,
                        Modifier
                            .align(Alignment.Center)
                            .padding(top = ((1f - (selectedPlacement?.speed ?: 0.5f)) * 24f).dp)
                            .fillMaxSize()
                            .padding(12.dp),
                    )
                } else {
                    Text(
                        text = "✨",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color(0xFF5C4B51),
                    )
                }
            }
            if (selectedSticker != null && selectedPlacement != null) {
                Text(
                    "${selectedSticker.name}  •  size ${(selectedPlacement.size * 100).toInt()}%  •  speed ${(selectedPlacement.speed * 100).toInt()}%",
                    color = Color(0xFF5C4B51),
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text("Pick a sticker to preview it here.", color = Color(0xFF5C4B51), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
internal fun StickerCatalogCard(
    sticker: StickerPreset,
    selected: Boolean,
    added: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        border = if (selected) {
            BorderStroke(1.dp, Color(0xFFD47DFE))
        } else {
            BorderStroke(0.5.dp, Color(0xFFE0E0E0))
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            StickerMediaPreview(
                sticker,
                Modifier
                    .fillMaxSize()
                    .padding(6.dp),
            )
            if (added) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.08f)),
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (sticker.premium) {
                    Image(
                        painter = painterResource(R.drawable.ic_diamond),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                    )
                }
                if (sticker.animated) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x33FFFFFF),
                    ) {
                        Text(
                            "GIF",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5C4B51),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AddedStickerChip(
    sticker: StickerPreset,
    selected: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFFFE5FC) else Color(0xFFF8F8F8),
        ),
        border = if (selected) BorderStroke(1.dp, Color(0xFFD47DFE)) else null,
        onClick = onSelect,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (sticker.thumbnailUrl != null) {
                AsyncImage(
                    model = sticker.thumbnailUrl,
                    contentDescription = sticker.name,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(sticker.glyph)
            }
            Text(sticker.name, color = Color(0xFF5C4B51), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onRemove) { Text("×", color = Color(0xFF5C4B51)) }
        }
    }
}
