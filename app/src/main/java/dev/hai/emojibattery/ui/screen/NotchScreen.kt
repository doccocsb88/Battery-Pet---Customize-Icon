package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.service.NotchTemplateCatalog
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore
import dev.hai.emojibattery.ui.theme.OceanSerenity
import kotlin.math.roundToInt

@Composable
internal fun NotchScreen(
    onBack: () -> Unit,
) {
    val templates = remember { NotchTemplateCatalog.allTemplates() }
    val context = LocalContext.current
    val snapshot = OverlayConfigStore.read(context)
    var selectedId by remember { mutableIntStateOf(snapshot.notchTemplateId) }
    var selectedColor by remember { mutableStateOf(snapshot.notchColorVariant) }
    var adjustmentTemplateId by remember { mutableStateOf<Int?>(null) }
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

    adjustmentTemplateId?.let { templateId ->
        val template = templates.firstOrNull { it.id == templateId }
        if (template != null && template.drawableRes != null) {
            NotchAdjustmentDialog(
                drawableRes = template.drawableRes,
                initialScale = snapshot.notchScale,
                initialOffsetX = snapshot.notchOffsetX,
                initialOffsetY = snapshot.notchOffsetY,
                onDismiss = { adjustmentTemplateId = null },
                onDone = { scale, offsetX, offsetY ->
                    selectedId = template.id
                    OverlayConfigStore.saveNotchTemplateId(context, template.id)
                    OverlayConfigStore.saveNotchAdjustment(context, scale, offsetX, offsetY)
                    OverlayAccessibilityService.requestRefresh(context)
                    adjustmentTemplateId = null
                },
            )
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
                    OceanFeatureSectionTitle(text = stringResource(R.string.notch_color))

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
                                if (template.drawableRes == null) {
                                    selectedId = template.id
                                    OverlayConfigStore.saveNotchTemplateId(context, template.id)
                                    OverlayAccessibilityService.requestRefresh(context)
                                } else {
                                    adjustmentTemplateId = template.id
                                }
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

@Composable
private fun NotchAdjustmentDialog(
    drawableRes: Int,
    initialScale: Float,
    initialOffsetX: Float,
    initialOffsetY: Float,
    onDismiss: () -> Unit,
    onDone: (Float, Float, Float) -> Unit,
) {
    val density = LocalDensity.current
    var notchScale by remember(drawableRes) { mutableStateOf(initialScale.coerceIn(0.5f, 2.2f)) }
    var notchOffsetX by remember(drawableRes) { mutableStateOf(initialOffsetX.coerceIn(0f, 1f)) }
    var notchOffsetY by remember(drawableRes) { mutableStateOf(initialOffsetY.coerceIn(0f, 1f)) }
    var containerWidthPx by remember { mutableStateOf(1f) }
    var containerHeightPx by remember { mutableStateOf(1f) }

    fun commitScale(next: Float) {
        notchScale = next.coerceIn(0.5f, 2.2f)
    }

    fun commitOffset(nextX: Float, nextY: Float) {
        notchOffsetX = nextX.coerceIn(0f, 1f)
        notchOffsetY = nextY.coerceIn(0f, 1f)
    }

    fun resetAdjustment() {
        notchScale = 1f
        notchOffsetX = 0.5f
        notchOffsetY = 0f
    }

    Dialog(onDismissRequest = onDismiss) {
        OceanAdjustmentPanelSurface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            OceanAdjustmentHeader(
                title = "Notch Adjustment",
                subtitle = "Drag notch to move. Drag 4 corner handles to resize.",
            )
            OceanAdjustmentStage(
                modifier = Modifier
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .onSizeChanged {
                        containerWidthPx = it.width.toFloat().coerceAtLeast(1f)
                        containerHeightPx = it.height.toFloat().coerceAtLeast(1f)
                    },
            ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        val baseWidthPx = containerWidthPx * 0.42f
                        val notchWidthPx = (baseWidthPx * notchScale).coerceAtLeast(48f)
                        val notchHeightPx = (notchWidthPx * 0.18f).coerceAtLeast(12f)
                        val frameHorizontalPaddingPx = with(density) { 18.dp.toPx() }
                        val frameVerticalPaddingPx = with(density) { 14.dp.toPx() }
                        val minFrameWidthPx = with(density) { 140.dp.toPx() }
                        val minFrameHeightPx = with(density) { 56.dp.toPx() }
                        val frameWidthPx = maxOf(minFrameWidthPx, notchWidthPx + (frameHorizontalPaddingPx * 2f))
                        val frameHeightPx = maxOf(minFrameHeightPx, notchHeightPx + (frameVerticalPaddingPx * 2f))
                        val leftPx = ((containerWidthPx - frameWidthPx) * notchOffsetX).coerceIn(
                            0f,
                            (containerWidthPx - frameWidthPx).coerceAtLeast(0f),
                        )
                        val topPx = ((containerHeightPx - frameHeightPx) * notchOffsetY).coerceIn(
                            0f,
                            (containerHeightPx - frameHeightPx).coerceAtLeast(0f),
                        )
                        val resizeScaleFactor = 280f

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                                .size(
                                    width = with(density) { frameWidthPx.toDp() },
                                    height = with(density) { frameHeightPx.toDp() },
                                ),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(containerWidthPx, containerHeightPx) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            commitOffset(
                                                notchOffsetX + (dragAmount.x / containerWidthPx),
                                                notchOffsetY + (dragAmount.y / containerHeightPx),
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    painter = painterResource(drawableRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        width = with(density) { notchWidthPx.toDp() },
                                        height = with(density) { notchHeightPx.toDp() },
                                    ),
                                )
                            }
                            OceanAdjustmentDashFrame(
                                modifier = Modifier.fillMaxSize(),
                                inset = 9.dp,
                            )
                            NotchResizeHandle(
                                modifier = Modifier.align(Alignment.TopStart),
                                visualOffset = DpOffset((-10).dp, (-10).dp),
                                onDrag = { drag -> commitScale(notchScale + ((-drag.x - drag.y) / resizeScaleFactor)) },
                            )
                            NotchResizeHandle(
                                modifier = Modifier.align(Alignment.TopEnd),
                                visualOffset = DpOffset(10.dp, (-10).dp),
                                onDrag = { drag -> commitScale(notchScale + ((drag.x - drag.y) / resizeScaleFactor)) },
                            )
                            NotchResizeHandle(
                                modifier = Modifier.align(Alignment.BottomStart),
                                visualOffset = DpOffset((-10).dp, 10.dp),
                                onDrag = { drag -> commitScale(notchScale + ((-drag.x + drag.y) / resizeScaleFactor)) },
                            )
                            NotchResizeHandle(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                visualOffset = DpOffset(10.dp, 10.dp),
                                onDrag = { drag -> commitScale(notchScale + ((drag.x + drag.y) / resizeScaleFactor)) },
                            )
                        }
                    }
                }
            OceanAdjustmentActions(
                dismissText = "Reset",
                confirmText = "Done",
                onDismiss = { resetAdjustment() },
                onConfirm = { onDone(notchScale, notchOffsetX, notchOffsetY) },
            )
        }
    }
}

@Composable
private fun NotchResizeHandle(
    modifier: Modifier = Modifier,
    visualOffset: DpOffset = DpOffset.Zero,
    onDrag: (Offset) -> Unit,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .pointerInput(onDrag) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .offset(x = visualOffset.x, y = visualOffset.y)
                .size(18.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.6.dp, OceanSerenity.Primary),
        ) {}
    }
}
