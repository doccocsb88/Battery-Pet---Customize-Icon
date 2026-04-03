package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.FeatureConfig

@OptIn(ExperimentalFoundationApi::class)
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
    var selectedPageIndex by rememberSaveable { mutableIntStateOf(0) }
    val chargePages = ChargePageCatalog
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { chargePages.size })

    LaunchedEffect(currentVariant, chargePages.size) {
        selectedPageIndex = chargePageIndexForVariant(chargePages, currentVariant)
    }

    LaunchedEffect(selectedPageIndex, chargePages.size) {
        val targetPage = selectedPageIndex.coerceIn(0, (chargePages.size - 1).coerceAtLeast(0))
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
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
                        OceanFeatureSectionTitle(
                            text = stringResource(R.string.enable_feature, stringResource(R.string.charge)),
                            modifier = Modifier.weight(1f),
                        )
                        AppSwitch(checked = config.enabled, onCheckedChange = onToggleEnabled)
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
                    Text(
                        text = chargePages.getOrNull(pagerState.currentPage)?.title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        beyondViewportPageCount = 1,
                        pageSpacing = 0.dp,
                    ) { pageIndex ->
                        val page = chargePages[pageIndex]
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 8.dp),
                        ) {
                            items(page.items, key = { it.id }) { item ->
                                val selected = currentVariant.packId == page.packId && currentVariant.itemId == item.id
                                val enabled = config.enabled
                                val drawableRes = if (page.packId == "built_in") {
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
                                            val nextVariant = if (page.packId == "built_in") {
                                                item.id
                                            } else {
                                                encodeChargeVariant(ChargeVariantState(page.packId, item.id))
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
                                        if (page.packId == "built_in") {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(chargePages.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (pagerState.currentPage == index) 10.dp else 7.dp),
                            ) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = if (pagerState.currentPage == index) Color(0xFF8FB6D4) else Color(0xFFD8DDE2),
                                    modifier = Modifier.fillMaxSize(),
                                ) {}
                            }
                        }
                    }

                    Text(
                        text = "${pagerState.currentPage + 1}/${chargePages.size}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
