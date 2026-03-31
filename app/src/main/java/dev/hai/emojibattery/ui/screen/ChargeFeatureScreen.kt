package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.FeatureConfig

@Composable
internal fun ChargeFeatureScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectVariant: (String) -> Unit,
    onApply: () -> Unit,
) {
    val config = uiState.featureConfigs[CustomizeEntry.Charge]
        ?: FeatureConfig(enabled = false, variant = ChargeOptions.first().id)
    val currentVariant = parseChargeVariant(config.variant)
    var selectedPackIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedPack = ChargePackCatalog.getOrNull(selectedPackIndex) ?: ChargePackCatalog.first()
    val context = LocalContext.current

    LaunchedEffect(currentVariant.packId) {
        selectedPackIndex = ChargePackCatalog.indexOfFirst { it.id == currentVariant.packId }
            .takeIf { it >= 0 } ?: 0
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
                    text = stringResource(R.string.charge),
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
                                text = stringResource(R.string.enable_feature, stringResource(R.string.charge)),
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
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedPackIndex.coerceAtMost((ChargePackCatalog.size - 1).coerceAtLeast(0)),
                        edgePadding = 0.dp,
                        divider = {},
                    ) {
                        ChargePackCatalog.forEachIndexed { index, pack ->
                            Tab(
                                selected = selectedPackIndex == index,
                                onClick = { selectedPackIndex = index },
                                text = {
                                    Text(
                                        text = pack.title,
                                        maxLines = 1,
                                    )
                                },
                            )
                        }
                    }

                    Text(
                        text = selectedPack.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        items(selectedPack.items, key = { it.id }) { item ->
                            val selected = when (selectedPack.id) {
                                "built_in" -> currentVariant.packId == "built_in" && currentVariant.itemId == item.id
                                else -> currentVariant.packId == selectedPack.id && currentVariant.itemId == item.id
                            }
                            val enabled = config.enabled
                            val drawableRes = if (selectedPack.id == "built_in") {
                                0
                            } else {
                                context.resources.getIdentifier(
                                    item.drawableName.orEmpty(),
                                    "drawable",
                                    context.packageName,
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable(enabled = enabled) {
                                        val nextVariant = if (selectedPack.id == "built_in") {
                                            item.id
                                        } else {
                                            encodeChargeVariant(ChargeVariantState(selectedPack.id, item.id))
                                        }
                                        onSelectVariant(nextVariant)
                                        onApply()
                                    },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    if (selected) 1.dp else 0.5.dp,
                                    if (selected) Color(0xFF8FB6D4) else Color(0xFFD8DDE2),
                                ),
                                color = if (selected) Color(0xFFEAF3FA) else Color(0xFFF8F8F8),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (selectedPack.id == "built_in") {
                                            Text(
                                                text = item.glyph.orEmpty(),
                                                style = MaterialTheme.typography.displayMedium,
                                                color = Color(0xFF12122B),
                                            )
                                        } else if (drawableRes != 0) {
                                            Image(
                                                painter = painterResource(drawableRes),
                                                contentDescription = item.id,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit,
                                            )
                                        } else {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
