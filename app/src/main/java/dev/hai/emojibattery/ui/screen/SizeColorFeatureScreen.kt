package dev.hai.emojibattery.ui.screen


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.FeatureConfig

@Composable
internal fun RingerFeatureScreen(
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
internal fun HotspotFeatureScreen(
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
internal fun AirplaneFeatureScreen(
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
    val selectedColorId = WifiColorOptions.firstOrNull { it.id == config.variant }?.id ?: "picker"
    val pickerColorArgb = parsePickerColorVariant(config.variant)
    val pickerSelected = isPickerColorVariant(config.variant)
    var showPicker by remember { mutableStateOf(false) }
    val sliderDp = (10f + (26f * config.intensity)).coerceIn(10f, 36f)

    if (showPicker) {
        val initialColor = pickerColorArgb ?: (WifiColorOptions.firstOrNull { it.id == "blue" }?.color?.value?.toLong() ?: 0xFF2952F4)
        FeatureColorWheelPickerDialog(
            initialArgb = initialColor,
            onDismiss = { showPicker = false },
            onApply = { argb ->
                onSelectVariant(encodePickerColorVariant(argb))
                onApply()
                showPicker = false
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
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
                border = BorderStroke(1.dp, Color(0xFF8FB6D4)),
                color = Color(0xFFF2F2F2),
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
                                color = MaterialTheme.colorScheme.onSurface,
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
                                color = MaterialTheme.colorScheme.onSurface,
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
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WifiColorOptions.forEach { option ->
                                val selected = if (option.id == "picker") pickerSelected else option.id == selectedColorId
                                val swatchColor = when {
                                    option.id != "picker" -> option.color
                                    pickerColorArgb != null -> Color(pickerColorArgb)
                                    else -> Color.White
                                }
                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable(enabled = config.enabled) {
                                            if (option.id == "picker") {
                                                showPicker = true
                                            } else {
                                                onSelectVariant(option.id)
                                                onApply()
                                            }
                                        },
                                    shape = CircleShape,
                                    color = swatchColor,
                                    border = BorderStroke(
                                        if (selected) 2.dp else 0.8.dp,
                                        if (selected) Color(0xFF8FB6D4) else Color(0xFFD8DDE2),
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
