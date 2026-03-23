package dev.hai.emojibattery.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.q7labs.co.emoji.R
import coil.compose.AsyncImage
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.HomeBatteryItem
import dev.hai.emojibattery.ui.theme.StrawberryMilk

/** Distinct Volio `photo` URLs from the status-bar store feed (same items as Battery/Emoji tabs). */
internal fun backgroundTemplatePhotoUrlsFromCatalog(items: List<HomeBatteryItem>): List<String> =
    items.mapNotNull { it.backgroundPhotoUrl?.takeIf { u -> u.isNotBlank() } }.distinct()

/**
 * Full-screen list of background templates — Volio `photo` URLs (raster banners), matching the
 * original “View more” flow (single column, wide thumbnails).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackgroundTemplateListScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectPhotoUrl: (String?) -> Unit,
) {
    val urls = backgroundTemplatePhotoUrlsFromCatalog(uiState.statusBarCatalogItems)
    val selected = uiState.editingConfig.backgroundTemplatePhotoUrl
    val titleColor = colorResource(R.color.splash_title)
    val editorBg = colorResource(R.color.status_bar_editor_scaffold)

    Scaffold(
        containerColor = editorBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.background_template),
                        color = titleColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_40_new),
                            contentDescription = stringResource(R.string.cd_back),
                            tint = titleColor,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.apply),
                            tint = StrawberryMilk.Secondary,
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (urls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.background_template_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(urls, key = { it }) { url ->
                    val isSelected = url == selected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) StrawberryMilk.Secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable {
                                onSelectPhotoUrl(if (isSelected) null else url)
                            },
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(112.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(StrawberryMilk.Secondary, RoundedCornerShape(999.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
