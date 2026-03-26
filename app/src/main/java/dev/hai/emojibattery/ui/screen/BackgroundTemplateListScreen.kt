package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import coil.compose.AsyncImage
import dev.hai.emojibattery.data.PadBackgroundTemplateCategory
import dev.hai.emojibattery.data.PadBackgroundTemplateItem
import dev.hai.emojibattery.data.PadBackgroundTemplateRepository
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.StatusBarThemeTemplateCatalog
import dev.hai.emojibattery.ui.theme.StrawberryMilk
import android.util.Log

private data class BackgroundTemplateUiEntry(
    val key: String,
    val assetUrl: String,
)

@Composable
internal fun BackgroundTemplateListScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectPhotoUrl: (String?) -> Unit,
) {
    val tag = "BgTemplatePAD"
    val context = LocalContext.current.applicationContext
    val padCategories = remember { mutableStateListOf<PadBackgroundTemplateCategory>() }
    val loadedItemsByPack = remember { mutableStateMapOf<String, List<PadBackgroundTemplateItem>>() }
    val attemptedPacks = remember { mutableStateMapOf<String, Boolean>() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var loadingPack by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        padCategories.clear()
        padCategories.addAll(PadBackgroundTemplateRepository.loadCategories(context))
        Log.d(tag, "UI load categories: ${padCategories.size}")
    }

    val selectedCategory = padCategories.getOrNull((selectedTabIndex - 1).coerceAtLeast(0))
        ?.takeIf { selectedTabIndex > 0 }

    LaunchedEffect(selectedCategory?.deliveryPackName) {
        val category = selectedCategory ?: return@LaunchedEffect
        val cached = loadedItemsByPack[category.deliveryPackName]
        if (cached != null && cached.isNotEmpty()) return@LaunchedEffect
        loadingPack = category.deliveryPackName
        attemptedPacks[category.deliveryPackName] = true
        val loaded = PadBackgroundTemplateRepository.loadItemsForCategory(context, category)
        if (loaded.isNotEmpty()) {
            loadedItemsByPack[category.deliveryPackName] = loaded
        } else {
            loadedItemsByPack.remove(category.deliveryPackName)
        }
        Log.d(tag, "UI load items pack=${category.deliveryPackName} count=${loaded.size}")
        loadingPack = null
    }

    val selectedAssetUrl = uiState.editingConfig.backgroundTemplatePhotoUrl
        ?: StatusBarThemeTemplateCatalog.entryForPreviewDrawable(uiState.editingConfig.backgroundTemplateDrawableRes)
            ?.let { StatusBarThemeTemplateCatalog.assetUri(it.assetRelativePath) }

    val tabs = buildList {
        add("Built-in")
        addAll(padCategories.map { it.title?.takeIf { name -> name.isNotBlank() } ?: it.packName })
    }

    val entries: List<BackgroundTemplateUiEntry> = if (selectedTabIndex == 0) {
        StatusBarThemeTemplateCatalog.entries.asReversed().map { entry ->
            BackgroundTemplateUiEntry(
                key = "builtin_${entry.index}",
                assetUrl = StatusBarThemeTemplateCatalog.assetUri(entry.assetRelativePath),
            )
        }
    } else {
        val category = selectedCategory
        if (category == null) {
            emptyList()
        } else {
            loadedItemsByPack[category.deliveryPackName].orEmpty().map { item ->
                BackgroundTemplateUiEntry(
                    key = "${category.deliveryPackName}_${item.id}",
                    assetUrl = item.assetUrl,
                )
            }
        }
    }
    val selectedDeliveryPack = selectedCategory?.deliveryPackName
    val isSelectedCategoryLoading = selectedTabIndex > 0 && (
        loadingPack == selectedDeliveryPack ||
            (selectedDeliveryPack != null &&
                attemptedPacks[selectedDeliveryPack] != true &&
                !loadedItemsByPack.containsKey(selectedDeliveryPack))
        )
    val shouldShowEmpty = selectedTabIndex > 0 &&
        !isSelectedCategoryLoading &&
        selectedDeliveryPack != null &&
        attemptedPacks[selectedDeliveryPack] == true &&
        entries.isEmpty()

    val editorBg = colorResource(R.color.status_bar_editor_scaffold)
    Scaffold(
        containerColor = editorBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_back_40_new),
                        contentDescription = stringResource(R.string.cd_back),
                        modifier = Modifier.size(36.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.background_template),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = stringResource(R.string.apply),
                        tint = StrawberryMilk.Secondary,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex.coerceAtMost((tabs.size - 1).coerceAtLeast(0)),
                    edgePadding = 0.dp,
                    divider = {},
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                )
                            },
                        )
                    }
                }
            }

            if (isSelectedCategoryLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (shouldShowEmpty) {
                item {
                    Text(
                        text = "No templates available in this category.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                items(entries, key = { it.key }) { entry ->
                    val isSelected = entry.assetUrl == selectedAssetUrl
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) {
                                    StrawberryMilk.Secondary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                },
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable {
                                onSelectPhotoUrl(if (isSelected) null else entry.assetUrl)
                            },
                    ) {
                        AsyncImage(
                            model = entry.assetUrl,
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
