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

@Composable
internal fun SettingsScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onOpenLanguage: () -> Unit,
    onReplayTutorial: () -> Unit,
    onToggleProtection: (Boolean) -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onShareApp: () -> Unit,
    onOpenFeedback: () -> Unit,
    onRateApp: () -> Unit,
    onSelectRating: (Int) -> Unit,
    onCheckUpdate: () -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
) {
    Scaffold(
        containerColor = Color(0xFFFEF5FA),
        topBar = {
            SettingsTopBar(
                onBack = onBack,
                onStart = { onToggleAccessibility(true) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SettingsRow("Language", R.drawable.ic_language_settings, uiState.selectedLanguage, onOpenLanguage)
            SettingsRow("Not-Allowed Apps", R.drawable.ic_not_allow, if (uiState.protectFromRecentApps) "Protected" else null) {
                onToggleProtection(!uiState.protectFromRecentApps)
            }
            SettingsRow("Tutorial", R.drawable.ic_tutorials_new, "Permission and gesture guide", onReplayTutorial)
            SettingsRow("Privacy policy", R.drawable.ic_privacy_settings, null, onOpenPrivacy)
            SettingsRow("Terms & Conditions", R.drawable.ic_privacy_settings, null, onOpenTerms)
            SettingsRow("Feedback", R.drawable.ic_feed_back_setting, null, onOpenFeedback)
            SettingsRow("Share app", R.drawable.ic_share_app_settings, null, onShareApp)
            SettingsRow("Rate us", R.drawable.ic_rate_us_setting, if (uiState.ratingSelection > 0) "${uiState.ratingSelection}/5" else null, onRateApp)
            SettingsRow("Check for update", R.drawable.ic_check_update_settings, "Version: 1.2.9", onCheckUpdate)
        }
    }
}

@Composable
internal fun SettingsRow(
    title: String,
    iconRes: Int,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = Color(0xFF5F4B54))
                    if (subtitle != null) {
                        Text(subtitle, color = Color(0xFF8D7680), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Image(
                painter = painterResource(R.drawable.ic_end_setting),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FeedbackScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectRating: (Int) -> Unit,
    onToggleReason: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFFF6A2D8), Color(0xFFB765F5)))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.48f))
            .padding(horizontal = 18.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.size(40.dp))
                    Text("Feedback", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5F4B54))
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF6A5961)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("×", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val reasons = listOf(
                        "5 stars" to "⭐",
                        "Can't allow permission" to null,
                        "Feature error" to null,
                        "I can't close the ads" to null,
                        "I can't exit the app" to null,
                        "I can't navigate to the next screen" to null,
                    )
                    reasons.forEachIndexed { index, (label, icon) ->
                        val selected = if (index == 0) uiState.ratingSelection == 5 else uiState.feedbackReasons.contains(SampleCatalog.feedbackReasons.getOrNull((index - 1).coerceAtLeast(0))?.id)
                        Surface(
                            onClick = {
                                if (index == 0) onSelectRating(5) else SampleCatalog.feedbackReasons.getOrNull((index - 1).coerceAtLeast(0))?.let { onToggleReason(it.id) }
                            },
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFFF8F8F8),
                            border = BorderStroke(1.dp, if (selected) Color(0xFFE9A8EC) else Color.Transparent),
                        ) {
                            Text(
                                buildString {
                                    if (icon != null) append("$icon ")
                                    append(label)
                                },
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                color = Color(0xFF5F4B54),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Text(
                    "Do you have any additional feedback for us?\nWe're Listening.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5F4B54),
                )
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF8F8F8),
                ) {
                    OutlinedTextField(
                        value = uiState.feedbackNote,
                        onValueChange = onNoteChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        minLines = 5,
                        placeholder = { Text("Please describe your issue in detail.", color = Color(0xFFD4CFD5)) },
                    )
                }
                if (uiState.lastFeedbackSubmitted) {
                    Text("Your feedback was successfully submitted", color = Color(0xFF17A398), fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(gradient),
                ) {
                    TextButton(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
                        Text("Submit", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingsTopBar(
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    Surface(color = Color.White, shadowElevation = 4.dp, shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Image(
                        painter = painterResource(R.drawable.ic_back_40_new),
                        contentDescription = "Back",
                        modifier = Modifier.size(40.dp),
                    )
                }
                Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5F4B54))
                Spacer(Modifier.size(40.dp))
            }
            EnableBanner(onStart = onStart)
        }
    }
}
