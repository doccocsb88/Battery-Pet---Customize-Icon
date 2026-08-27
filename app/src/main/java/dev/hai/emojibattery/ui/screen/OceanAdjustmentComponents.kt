package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hai.emojibattery.ui.theme.OceanSerenity

@Composable
internal fun OceanAdjustmentPanelSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = OceanSerenity.Surface,
        shadowElevation = 0.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
internal fun OceanAdjustmentHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = OceanSerenity.OnSurface,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = OceanSerenity.OnSurfaceVariant,
        )
    }
}

@Composable
internal fun OceanAdjustmentStage(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = OceanSerenity.PrimaryContainer.copy(alpha = 0.34f),
                shape = RoundedCornerShape(20.dp),
            )
            .border(1.dp, OceanSerenity.Outline.copy(alpha = 0.7f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
internal fun OceanAdjustmentActions(
    dismissText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                OceanSerenity.Outline,
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = OceanSerenity.Surface,
                contentColor = OceanSerenity.OnSurface,
            ),
        ) {
            Text(dismissText, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OceanSerenity.Primary,
                contentColor = OceanSerenity.OnPrimary,
            ),
        ) {
            Text(confirmText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun OceanSectionAccentLabel(
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 14.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(OceanSerenity.Primary, OceanSerenity.Secondary),
                    ),
                    shape = RoundedCornerShape(999.dp),
                ),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = OceanSerenity.OnSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun OceanAdjustmentDashFrame(
    modifier: Modifier = Modifier,
    inset: Dp = 10.dp,
    strokeColor: Color = OceanSerenity.Primary,
) {
    Canvas(modifier = modifier) {
        val insetPx = inset.toPx()
        val dash = 8.dp.toPx()
        val gap = 7.dp.toPx()
        val stroke = 2.dp.toPx()
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)

        drawLine(
            color = strokeColor,
            start = Offset(insetPx, insetPx),
            end = Offset(size.width - insetPx, insetPx),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dashEffect,
        )
        drawLine(
            color = strokeColor,
            start = Offset(insetPx, size.height - insetPx),
            end = Offset(size.width - insetPx, size.height - insetPx),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dashEffect,
        )
        drawLine(
            color = strokeColor,
            start = Offset(insetPx, insetPx),
            end = Offset(insetPx, size.height - insetPx),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dashEffect,
        )
        drawLine(
            color = strokeColor,
            start = Offset(size.width - insetPx, insetPx),
            end = Offset(size.width - insetPx, size.height - insetPx),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dashEffect,
        )
    }
}

@Composable
internal fun OceanAdjustmentResizeHandle(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    onDrag: (Offset) -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color.White, CircleShape)
            .border(1.4.dp, OceanSerenity.Primary, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            },
    )
}
