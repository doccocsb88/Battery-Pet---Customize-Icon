package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.service.NotchTemplateCatalog
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore

@Composable
internal fun NotchScreen(
    onBack: () -> Unit,
) {
    val templates = remember { NotchTemplateCatalog.allTemplates() }
    val context = LocalContext.current
    var selectedId by remember { mutableIntStateOf(OverlayConfigStore.read(context).notchTemplateId) }
    var selectedColor by remember { mutableStateOf(OverlayConfigStore.read(context).notchColorVariant) }
    val pickerColorArgb = parsePickerColorVariant(selectedColor)
    val pickerSelected = isPickerColorVariant(selectedColor)
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        val initialColor = pickerColorArgb ?: (WifiColorOptions.firstOrNull { it.id == "blue" }?.color?.value?.toLong() ?: 0xFF2952F4)
        FeatureColorWheelPickerDialog(
            initialArgb = initialColor,
            onDismiss = { showPicker = false },
            onApply = { argb ->
                val variant = encodePickerColorVariant(argb)
                selectedColor = variant
                OverlayConfigStore.saveNotchColorVariant(context, variant)
                OverlayAccessibilityService.requestRefresh(context)
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
                    text = stringResource(R.string.home_notch),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_bullet2),
                            contentDescription = null,
                            modifier = Modifier.size(5.dp, 18.dp),
                        )
                        Text(
                            text = stringResource(R.string.notch_color),
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
                            val selected = if (option.id == "picker") pickerSelected else option.id == selectedColor
                            val swatchColor = when {
                                option.id != "picker" -> option.color
                                pickerColorArgb != null -> Color(pickerColorArgb)
                                else -> Color.White
                            }
                            Surface(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable {
                                        if (option.id == "picker") {
                                            showPicker = true
                                        } else {
                                            selectedColor = option.id
                                            OverlayConfigStore.saveNotchColorVariant(context, option.id)
                                            OverlayAccessibilityService.requestRefresh(context)
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
            ) {
                items(templates) { template ->
                    val selected = template.id == selectedId
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = if (selected) 2.dp else 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedId = template.id
                                OverlayConfigStore.saveNotchTemplateId(context, template.id)
                                OverlayAccessibilityService.requestRefresh(context)
                            },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(10.dp),
                                    )
                                    .padding(vertical = 10.dp),
                            ) {
                                if (template.drawableRes != null) {
                                    Image(
                                        painter = painterResource(template.drawableRes),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .size(width = 100.dp, height = 20.dp),
                                    )
                                } else {
                                    Text(
                                        text = "Hide notch",
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                            Text(
                                text = if (template.id == -1) "Hide notch" else "Notch ${template.id}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}
