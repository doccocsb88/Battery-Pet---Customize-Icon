package dev.hai.emojibattery.ui.screen

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.hai.emojibattery.service.AnimationTemplate
import dev.hai.emojibattery.service.AnimationTemplateCatalog
import dev.hai.emojibattery.service.OverlayConfigStore

@Composable
internal fun AnimationScreen(
    onBack: () -> Unit,
    selectedFromList: Int?,
    onConsumeListSelection: () -> Unit,
    onOpenAnimationList: (Int) -> Unit,
    onApply: (enabled: Boolean, sizePercent: Int, templateId: Int) -> Unit,
) {
    val context = LocalContext.current
    val initialPrefs = remember { OverlayConfigStore.readAnimationPrefs(context) }
    val templates = remember { AnimationTemplateCatalog.templates }
    var enabled by remember { mutableStateOf(initialPrefs.enabled) }
    var sizePercent by remember { mutableIntStateOf(initialPrefs.sizePercent) }
    var selectedId by remember { mutableIntStateOf(initialPrefs.templateId) }
    if (selectedFromList != null && selectedFromList >= 0 && selectedFromList != selectedId) {
        selectedId = selectedFromList
        onConsumeListSelection()
    }
    val selected = remember(selectedId) { AnimationTemplateCatalog.resolve(selectedId) }
    val previewItems = templates.take(6)

    Scaffold(
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
                    text = stringResource(R.string.home_animation),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "🍼",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
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
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xFFF2F2F2),
                ),
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
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF8FB6D4),
                                checkedBorderColor = Color(0xFF8FB6D4),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF94A3B8),
                                uncheckedBorderColor = Color(0xFF94A3B8),
                            ),
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
                            .height(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 6.dp),
                    ) {
                        items(previewItems) { template ->
                            val isSelected = template.id == selectedId
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(98.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F2F2))
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
                    if (templates.size > previewItems.size) {
                        OutlinedButton(
                            onClick = { onOpenAnimationList(selectedId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(999.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.view_more),
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onApply(enabled, sizePercent, selectedId)
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
internal fun AnimationPreview(
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
        val context = LocalContext.current
        val request = remember(template.assetPath) {
            ImageRequest.Builder(context)
                .data("file:///android_asset/${template.assetPath}")
                .allowHardware(false)
                .decoderFactory(
                    if (Build.VERSION.SDK_INT >= 28) {
                        ImageDecoderDecoder.Factory()
                    } else {
                        GifDecoder.Factory()
                    },
                )
                .build()
        }
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
    }
}
