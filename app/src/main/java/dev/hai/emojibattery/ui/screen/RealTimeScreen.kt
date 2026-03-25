package dev.hai.emojibattery.ui.screen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.SampleCatalog

@Composable
internal fun RealTimeScreen(
    uiState: AppUiState,
    onBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
    onApply: () -> Unit,
) {
    val selected = SampleCatalog.realTimeTemplates.first { it.id == uiState.selectedRealTimeTemplateId }
    ScreenContainer(
        title = stringResource(R.string.realtime_screen_title),
        subtitle = stringResource(R.string.realtime_screen_subtitle),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TemplatePreviewCard(
                title = selected.title,
                summary = selected.summary,
                glyph = selected.accentGlyph,
                tag = selected.tag,
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    if (uiState.premiumUnlocked || uiState.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_PREMIUM_REALTIME_CAT_DIARY)) {
                        "Premium template access is available for this account state."
                    } else {
                        "Templates marked PRO open a paywall unless unlocked by reward or premium access."
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PermissionBanner(enabled = uiState.accessibilityGranted, onToggle = onToggleAccessibility)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SampleCatalog.realTimeTemplates) { template ->
                    ContentTemplateCard(
                        template = template,
                        selected = template.id == uiState.selectedRealTimeTemplateId,
                        onClick = { onSelectTemplate(template.id) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.onboarding_back))
                }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.apply_template))
                }
            }
        }
    }
}
