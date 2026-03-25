package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import co.q7labs.co.emoji.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.BatteryTrollTemplate
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.model.batteryTrollTemplateForId
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush

private val TrollCardShape = RoundedCornerShape(20.dp)
private val TrollStrokeColor = Color(0xFF8FB6D4)
private val TrollSoftPink = Color(0xFFF2F2F2)
private val TrollPanelColor = Color.White

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BatteryTrollScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onSetMessage: (String) -> Unit,
    onSetFeatureEnabled: (Boolean) -> Unit,
    onSetUseRealBattery: (Boolean) -> Unit,
    onSetShowPercentage: (Boolean) -> Unit,
    onSetPercentageSize: (Int) -> Unit,
    onSetEmojiSize: (Int) -> Unit,
    onSetRandomizedMode: (Boolean) -> Unit,
    onSetShowEmoji: (Boolean) -> Unit,
    onToggleAutoDrop: (Boolean) -> Unit,
    onOpenTutorial: () -> Unit,
    onRefreshBatteryTrollCatalog: () -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onApply: () -> Unit,
    onTurnOff: () -> Unit,
) {
    LaunchedEffect(Unit) { onRefreshBatteryTrollCatalog() }

    val templateLibrary = if (uiState.batteryTrollCatalogRemote.isNotEmpty()) {
        uiState.batteryTrollCatalogRemote
    } else {
        SampleCatalog.batteryTrollTemplates
    }
    val selected = uiState.batteryTrollTemplateForId(uiState.selectedBatteryTrollTemplateId)
        ?: SampleCatalog.batteryTrollTemplates.first()

    var showEditDialog by remember { mutableStateOf(false) }
    var editText by remember(uiState.trollMessage) { mutableStateOf(uiState.trollMessage) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_40_new),
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.battery_troll_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                Text(text = selected.accentGlyph, style = MaterialTheme.typography.headlineMedium)
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(StrawberryCtaGradientBrush)
                        .clickable(onClick = onApply),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.apply),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = TrollCardShape,
                    border = BorderStroke(1.dp, TrollStrokeColor),
                    color = TrollSoftPink,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.enable_disable_emoji_battery),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Switch(
                            checked = uiState.trollFeatureEnabled,
                            onCheckedChange = onSetFeatureEnabled,
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = TrollCardShape,
                    border = BorderStroke(1.dp, TrollStrokeColor),
                    color = TrollPanelColor,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TrollModeRadio(
                                label = stringResource(R.string.battery_troll_mode),
                                selected = !uiState.trollUseRealBattery,
                                onClick = { onSetUseRealBattery(false) },
                                modifier = Modifier.weight(1f),
                            )
                            TrollModeRadio(
                                label = stringResource(R.string.battery_real),
                                selected = uiState.trollUseRealBattery,
                                onClick = { onSetUseRealBattery(true) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Text(
                            text = stringResource(R.string.show_fake_battery_just_for_fun),
                            color = TrollStrokeColor,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (uiState.trollUseRealBattery) "100%" else uiState.trollMessage,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                )
                                Surface(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clickable(enabled = !uiState.trollUseRealBattery) {
                                            editText = uiState.trollMessage
                                            showEditDialog = true
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Color(0xFFD7CFD4)),
                                    color = Color.White,
                                ) {
                                    Text(
                                        text = stringResource(R.string.edit),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (uiState.trollUseRealBattery) Color(0xFFB8B0B4) else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                            BatteryCellPreview(template = selected)
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = TrollCardShape,
                    color = Color.White,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        TrollSliderRow(
                            title = stringResource(R.string.on_off_percentage),
                            checked = uiState.trollShowPercentage,
                            sliderValue = uiState.trollPercentageSizeDp.toFloat(),
                            sliderRange = 5f..40f,
                            valueSuffix = "dp",
                            onCheckedChange = onSetShowPercentage,
                            onSliderChange = { onSetPercentageSize(it.toInt()) },
                        )
                        TrollSliderRow(
                            title = stringResource(R.string.emoji_size),
                            checked = true,
                            sliderValue = uiState.trollEmojiSizeDp.toFloat(),
                            sliderRange = 20f..80f,
                            valueSuffix = "dp",
                            onCheckedChange = {},
                            onSliderChange = { onSetEmojiSize(it.toInt()) },
                            lockToggle = true,
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = TrollCardShape,
                    color = Color.White,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.funny_emoji_battery),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TrollModeRadio(
                                label = stringResource(R.string.customized),
                                selected = !uiState.trollRandomizedMode,
                                onClick = { onSetRandomizedMode(false) },
                                modifier = Modifier.weight(1f),
                            )
                            TrollModeRadio(
                                label = stringResource(R.string.randomized),
                                selected = uiState.trollRandomizedMode,
                                onClick = { onSetRandomizedMode(true) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Text(
                            text = stringResource(R.string.use_only_one_funny_piece_of_content),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF83777E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = TrollCardShape,
                    color = Color.White,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.show_emoji),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Switch(
                                checked = uiState.trollShowEmoji,
                                onCheckedChange = onSetShowEmoji,
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            templateLibrary.forEach { template ->
                                TrollTemplateChip(
                                    template = template,
                                    selected = template.id == uiState.selectedBatteryTrollTemplateId,
                                    enabled = uiState.trollShowEmoji,
                                    onClick = { onSelectTemplate(template.id) },
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = TrollCardShape,
                    color = Color.White,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.battery_troll_templates),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFEAF3FA),
                                modifier = Modifier.clickable(onClick = onOpenTutorial),
                            ) {
                                Text(
                                    text = stringResource(R.string.tutorial),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                        TemplateGrid(
                            templates = templateLibrary,
                            selectedId = uiState.selectedBatteryTrollTemplateId,
                            onSelect = onSelectTemplate,
                        )
                    }
                }
            }

            item {
                PermissionBanner(
                    enabled = uiState.accessibilityGranted,
                    onToggle = onToggleAccessibility,
                )
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.battery_troll_auto_drop),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Switch(checked = uiState.trollAutoDrop, onCheckedChange = onToggleAutoDrop)
                    }
                }
            }

            item {
                TextButton(
                    onClick = onTurnOff,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text(stringResource(R.string.common_turn_off))
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = stringResource(R.string.edit)) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    singleLine = true,
                    placeholder = { Text(text = "999") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editText.isNotBlank()) {
                            onSetMessage(editText.trim())
                        }
                        showEditDialog = false
                    },
                ) {
                    Text(text = stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(text = stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun BatteryCellPreview(template: BatteryTrollTemplate) {
    Row(
        modifier = Modifier
            .width(130.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFFA72D))
                .border(2.dp, Color(0xFFFFA72D), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                TrollMediaPreview(template, Modifier.size(34.dp))
            }
        }
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFFFA72D)),
        )
    }
}

@Composable
private fun TrollModeRadio(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = if (selected) TrollStrokeColor else Color(0xFFD8DDE2),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) TrollStrokeColor else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TrollSliderRow(
    title: String,
    checked: Boolean,
    sliderValue: Float,
    sliderRange: ClosedFloatingPointRange<Float>,
    valueSuffix: String,
    onCheckedChange: (Boolean) -> Unit,
    onSliderChange: (Float) -> Unit,
    lockToggle: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            if (!lockToggle) {
                Switch(checked = checked, onCheckedChange = onCheckedChange)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Slider(
                value = sliderValue,
                onValueChange = onSliderChange,
                valueRange = sliderRange,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${sliderValue.toInt()}$valueSuffix",
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun TrollTemplateChip(
    template: BatteryTrollTemplate,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(74.dp)
            .height(74.dp),
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        border = BorderStroke(
            if (selected) 1.5.dp else 0.5.dp,
            if (selected) TrollStrokeColor else Color(0xFFD8DDE2),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TrollMediaPreview(template, Modifier.size(44.dp))
        }
    }
}

@Composable
private fun TemplateGrid(
    templates: List<BatteryTrollTemplate>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        templates.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { template ->
                    Card(
                        onClick = { onSelect(template.id) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
                        border = BorderStroke(
                            if (template.id == selectedId) 1.5.dp else 0.5.dp,
                            if (template.id == selectedId) TrollStrokeColor else Color(0xFFD8DDE2),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            TrollMediaPreview(template, Modifier.weight(1f))
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                            )
                        }
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TrollMediaPreview(
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
                Text(template.accentGlyph, style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
