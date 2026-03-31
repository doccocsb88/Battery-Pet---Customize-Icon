package dev.hai.emojibattery.ui.screen


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
internal fun DataFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[CustomizeEntry.Data]
        ?: FeatureConfig(enabled = false, variant = "5G")
    val colorOptions = WifiColorOptions
    val styleOptions = listOf("2G", "3G", "4G", "5G", "6G")
    val selection = parseDataVariant(config.variant, styleOptions, colorOptions.map { it.id })
    val selectedStyle = selection.style
    val selectedColorId = selection.colorId
    val pickerColorArgb = parsePickerColorVariant(selectedColorId)
    val pickerSelected = isPickerColorVariant(selectedColorId)
    var showPicker by remember { mutableStateOf(false) }
    val sliderDp = (10f + (26f * config.intensity)).coerceIn(10f, 36f)

    if (showPicker) {
        val initialColor = pickerColorArgb ?: (WifiColorOptions.firstOrNull { it.id == "blue" }?.color?.value?.toLong() ?: 0xFF2952F4)
        FeatureColorWheelPickerDialog(
            initialArgb = initialColor,
            onDismiss = { showPicker = false },
            onApply = { argb ->
                onSelectVariant(encodeDataVariant(selectedStyle, encodePickerColorVariant(argb)))
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
                    text = stringResource(R.string.data),
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
                                text = stringResource(R.string.enable_feature, stringResource(R.string.data)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Switch(
                            checked = config.enabled,
                            onCheckedChange = onToggleEnabled,
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
                                text = stringResource(R.string.data_size),
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
                                enabled = config.enabled,
                                onValueChange = { dpValue ->
                                    val intensity = ((dpValue - 10f) / 26f).coerceIn(0.1f, 1f)
                                    onSetIntensity(intensity)
                                },
                                onValueChangeFinished = onApply,
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
                            colorOptions.forEach { option ->
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
                                                onSelectVariant(encodeDataVariant(selectedStyle, option.id))
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
                                color = MaterialTheme.colorScheme.onSurface,
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
                                                onSelectVariant(encodeDataVariant(style, selectedColorId))
                                                onApply()
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            if (selected) 1.dp else 0.5.dp,
                                            if (selected) Color(0xFF8FB6D4) else Color(0xFFD8DDE2),
                                        ),
                                        color = if (selected) Color(0xFFEAF3FA) else Color(0xFFF8F8F8),
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

private data class DataSelection(
    val style: String,
    val colorId: String,
)

private fun parseDataVariant(
    raw: String?,
    styleOptions: List<String>,
    colorIds: List<String>,
): DataSelection {
    val defaultStyle = styleOptions.firstOrNull { it.equals("5G", ignoreCase = true) } ?: styleOptions.first()
    val defaultColor = colorIds.firstOrNull { it == "blue" } ?: colorIds.first()
    val source = raw.orEmpty()
    if ("::" !in source) {
        val style = styleOptions.firstOrNull { it.equals(source, ignoreCase = true) } ?: defaultStyle
        val color = when {
            isPickerColorVariant(source) -> source
            else -> colorIds.firstOrNull { it.equals(source, ignoreCase = true) } ?: defaultColor
        }
        return DataSelection(style = style, colorId = color)
    }
    val parts = source.split("::", limit = 2)
    val style = styleOptions.firstOrNull { it.equals(parts.getOrNull(0).orEmpty(), ignoreCase = true) } ?: defaultStyle
    val rawColor = parts.getOrNull(1).orEmpty()
    val color = when {
        isPickerColorVariant(rawColor) -> rawColor
        else -> colorIds.firstOrNull { it.equals(rawColor, ignoreCase = true) } ?: defaultColor
    }
    return DataSelection(style = style, colorId = color)
}

private fun encodeDataVariant(style: String, colorId: String): String = "$style::$colorId"
