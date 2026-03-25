package dev.hai.emojibattery.ui.screen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.FeatureConfig
import dev.hai.emojibattery.model.SampleCatalog

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FeatureDetailScreen(
    entry: CustomizeEntry,
    uiState: AppUiState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectVariant: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    if (entry == CustomizeEntry.DateTime) {
        DateTimeFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Ringer) {
        RingerFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Hotspot) {
        HotspotFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Airplane) {
        AirplaneFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Charge) {
        ChargeFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Signal) {
        SignalFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Data) {
        DataFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Wifi) {
        WifiFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSetIntensity = onSetIntensity,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    if (entry == CustomizeEntry.Emotion) {
        EmotionFeatureScreen(
            uiState = uiState,
            onBack = onBack,
            onToggleEnabled = onToggleEnabled,
            onSelectVariant = onSelectVariant,
            onApply = onApply,
        )
        return
    }

    val config = uiState.featureConfigs[entry] ?: FeatureConfig(variant = SampleCatalog.featureVariants[entry]?.first().orEmpty())
    val variants = SampleCatalog.featureVariants[entry].orEmpty()
    ScreenContainer(title = entry.title, subtitle = "Isolated editor with local config, variants, and apply/reset actions.") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TemplatePreviewCard(
                title = entry.title,
                summary = entry.subtitle,
                glyph = featureGlyph(entry),
                tag = if (config.enabled) config.variant else stringResource(R.string.feature_disabled),
            )
            SettingToggle(stringResource(R.string.feature_enable_format, entry.title), config.enabled, onToggleEnabled)
            SliderField(stringResource(R.string.feature_intensity_format, entry.title), config.intensity, 0.1f..1f, onSetIntensity)
            Text(stringResource(R.string.common_variants), style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                variants.forEach { variant ->
                    ChoiceChip(
                        label = variant,
                        selected = variant == config.variant,
                        onClick = { onSelectVariant(variant) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.onboarding_back))
                }
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.common_reset))
                }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    }
}
