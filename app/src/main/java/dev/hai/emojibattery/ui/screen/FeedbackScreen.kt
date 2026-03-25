package dev.hai.emojibattery.ui.screen


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FeedbackScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectRating: (Int) -> Unit,
    onToggleReason: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val gradient = StrawberryCtaGradientBrush
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.48f))
            .padding(horizontal = 18.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.size(40.dp))
                    Text(stringResource(R.string.feedback_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(stringResource(R.string.multiply_sign), color = MaterialTheme.colorScheme.surface, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val reasons = listOf(
                        stringResource(R.string.feedback_chip_5_stars) to "⭐",
                        stringResource(R.string.feedback_chip_cant_allow_permission) to null,
                        stringResource(R.string.feedback_chip_feature_error) to null,
                        stringResource(R.string.feedback_chip_cant_exit_app) to null,
                        stringResource(R.string.feedback_chip_cant_navigate) to null,
                    )
                    reasons.forEachIndexed { index, (label, icon) ->
                        val selected = if (index == 0) uiState.ratingSelection == 5 else uiState.feedbackReasons.contains(SampleCatalog.feedbackReasons.getOrNull((index - 1).coerceAtLeast(0))?.id)
                        Surface(
                            onClick = {
                                if (index == 0) onSelectRating(5) else SampleCatalog.feedbackReasons.getOrNull((index - 1).coerceAtLeast(0))?.let { onToggleReason(it.id) }
                            },
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent),
                        ) {
                            Text(
                                buildString {
                                    if (icon != null) append("$icon ")
                                    append(label)
                                },
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Text(
                    stringResource(R.string.feedback_prompt_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    OutlinedTextField(
                        value = uiState.feedbackNote,
                        onValueChange = onNoteChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        minLines = 5,
                        placeholder = { Text(stringResource(R.string.feedback_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)) },
                    )
                }
                if (uiState.lastFeedbackSubmitted) {
                    Text(stringResource(R.string.feedback_success), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(gradient),
                ) {
                    TextButton(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.submit), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}
