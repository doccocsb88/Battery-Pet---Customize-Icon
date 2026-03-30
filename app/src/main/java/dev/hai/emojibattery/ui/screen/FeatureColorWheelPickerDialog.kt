package dev.hai.emojibattery.ui.screen

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
internal fun FeatureColorWheelPickerDialog(
    initialArgb: Long,
    onDismiss: () -> Unit,
    onApply: (Long) -> Unit,
) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(0f) }
    var value by remember { mutableStateOf(1f) }

    LaunchedEffect(initialArgb) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(initialArgb.toInt(), hsv)
        hue = hsv[0].coerceIn(0f, 360f)
        saturation = hsv[1].coerceIn(0f, 1f)
        value = hsv[2].coerceIn(0f, 1f)
    }

    val pickedColorInt = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
    val pickedColor = Color(pickedColorInt)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_background_color)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(pickedColor, RoundedCornerShape(12.dp)),
                )
                FeatureColorWheel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    hue = hue,
                    saturation = saturation,
                    onChange = { h, s ->
                        hue = h
                        saturation = s
                    },
                )
                Text("Brightness", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = value,
                    onValueChange = { value = it.coerceIn(0f, 1f) },
                    valueRange = 0f..1f,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(pickedColorInt.toLong() and 0xFFFFFFFFL) }) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        containerColor = Color.White,
    )
}

@Composable
private fun FeatureColorWheel(
    modifier: Modifier = Modifier,
    hue: Float,
    saturation: Float,
    onChange: (Float, Float) -> Unit,
) {
    var wheelSizePx by remember { mutableStateOf(0) }
    val wheelBitmap: ImageBitmap? = remember(wheelSizePx) {
        if (wheelSizePx <= 0) null else createFeatureColorWheelBitmap(wheelSizePx)
    }

    fun update(offset: Offset) {
        if (wheelSizePx <= 0) return
        val center = wheelSizePx / 2f
        val dx = offset.x - center
        val dy = offset.y - center
        val radius = center.coerceAtLeast(1f)
        val distance = sqrt((dx * dx) + (dy * dy))
        val sat = (distance / radius).coerceIn(0f, 1f)
        val angle = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 360.0) % 360.0).toFloat()
        onChange(angle, sat)
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { wheelSizePx = minOf(it.width, it.height) }
            .pointerInput(wheelSizePx) {
                detectTapGestures { update(it) }
            }
            .pointerInput(wheelSizePx) {
                detectDragGestures { change, _ ->
                    change.consume()
                    update(change.position)
                }
            },
    ) {
        wheelBitmap?.let { drawImage(it) }

        val radius = size.minDimension / 2f
        val angleRad = Math.toRadians(hue.toDouble())
        val markerDistance = saturation.coerceIn(0f, 1f) * radius
        val markerCenter = Offset(
            x = center.x + (cos(angleRad) * markerDistance).toFloat(),
            y = center.y + (sin(angleRad) * markerDistance).toFloat(),
        )

        drawCircle(
            color = Color.White,
            radius = 9.dp.toPx(),
            center = markerCenter,
            style = Stroke(width = 2.5.dp.toPx()),
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.35f),
            radius = 11.dp.toPx(),
            center = markerCenter,
            style = Stroke(width = 1.5.dp.toPx()),
        )
    }
}

private fun createFeatureColorWheelBitmap(sizePx: Int): ImageBitmap {
    val safeSize = sizePx.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
    val center = safeSize / 2f
    val radius = center.coerceAtLeast(1f)
    val pixels = IntArray(safeSize * safeSize)

    for (y in 0 until safeSize) {
        for (x in 0 until safeSize) {
            val dx = x - center
            val dy = y - center
            val distance = sqrt((dx * dx) + (dy * dy))
            val index = y * safeSize + x
            if (distance <= radius) {
                val sat = (distance / radius).coerceIn(0f, 1f)
                val hue = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 360.0) % 360.0).toFloat()
                pixels[index] = AndroidColor.HSVToColor(floatArrayOf(hue, sat, 1f))
            } else {
                pixels[index] = AndroidColor.TRANSPARENT
            }
        }
    }

    bitmap.setPixels(pixels, 0, safeSize, 0, 0, safeSize, safeSize)
    return bitmap.asImageBitmap()
}
