package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hai.emojibattery.ui.theme.OceanSerenity

@Composable
internal fun OceanFeatureSectionMarker(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(5.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        OceanSerenity.Primary.copy(alpha = 0.45f),
                        OceanSerenity.Secondary.copy(alpha = 0.95f),
                    ),
                ),
            ),
    )
}

@Composable
internal fun OceanFeatureSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OceanFeatureSectionMarker()
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
