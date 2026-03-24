package dev.hai.emojibattery.app.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.hai.emojibattery.service.AnimationTemplate
import dev.hai.emojibattery.service.AnimationTemplateCatalog
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore

@Composable
internal fun AnimationScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val initialPrefs = remember { OverlayConfigStore.readAnimationPrefs(context) }
    val templates = remember { AnimationTemplateCatalog.templates }
    var enabled by remember { mutableStateOf(initialPrefs.enabled) }
    var sizePercent by remember { mutableIntStateOf(initialPrefs.sizePercent) }
    var selectedId by remember { mutableIntStateOf(initialPrefs.templateId) }
    var expanded by remember { mutableStateOf(false) }

    val selected = remember(selectedId) { AnimationTemplateCatalog.resolve(selectedId) }
    val maxCollapsedCount = 18
    val visibleTemplates = if (expanded) templates else templates.take(maxCollapsedCount)

    Scaffold(
        topBar = {
            OriginalTopShell(
                title = stringResource(R.string.home_animation),
                onLeftSecondary = onBack,
                onSearch = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
            ) {
                AnimationPreview(
                    template = selected,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.enable_animation),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it },
                        )
                    }
                    Text(
                        text = "${stringResource(R.string.animation_size)}: $sizePercent%",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Slider(
                        value = sizePercent.toFloat(),
                        valueRange = 0f..100f,
                        onValueChange = { sizePercent = it.toInt() },
                    )
                    Text(
                        text = stringResource(R.string.animation_style),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (expanded) 500.dp else 360.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 6.dp),
                    ) {
                        items(visibleTemplates) { template ->
                            val isSelected = template.id == selectedId
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(98.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    .clickable { selectedId = template.id },
                            ) {
                                AnimationPreview(
                                    template = template,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                    if (!expanded && templates.size > maxCollapsedCount) {
                        Text(
                            text = stringResource(R.string.view_more),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(999.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
                                .clickable { expanded = true }
                                .padding(vertical = 10.dp),
                        )
                    }
                }
            }

            Button(
                onClick = {
                    OverlayConfigStore.saveAnimationPrefs(
                        context = context,
                        enabled = enabled,
                        sizePercent = sizePercent,
                        templateId = selectedId,
                    )
                    OverlayAccessibilityService.requestRefresh(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            ) {
                Text(text = stringResource(R.string.apply))
            }
        }
    }
}

@Composable
private fun AnimationPreview(
    template: AnimationTemplate,
    modifier: Modifier = Modifier,
) {
    if (template.isLottie) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset(template.assetPath))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = "file:///android_asset/${template.assetPath}",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}
