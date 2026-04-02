package dev.hai.emojibattery.ui.screen


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.locale.AppLanguageConfig
import dev.hai.emojibattery.locale.displayNameForLocaleTag
import java.util.Locale

private val AccentIconBlue = Color(0xFF8FB6D4)

@Composable
internal fun SettingsScreen(
    uiState: AppUiState,
    onOpenLanguage: () -> Unit,
    onOpenStore: () -> Unit,
    onToggleProtection: (Boolean) -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onShareApp: () -> Unit,
    onOpenFeedback: () -> Unit,
    onRateApp: () -> Unit,
    onSelectRating: (Int) -> Unit,
    onCheckUpdate: () -> Unit,
    onToggleAccessibility: (Boolean) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val displayLocale = remember(configuration) {
        configuration.locales.get(0) ?: Locale.getDefault()
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SettingsTopBar()
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (AppLanguageConfig.isLanguagePickerFlowEnabled) {
                val languageSubtitle = displayNameForLocaleTag(uiState.selectedLocaleTag, displayLocale)
                SettingsRow(stringResource(R.string.language), R.drawable.ic_language_settings, languageSubtitle, onOpenLanguage)
            }

            SettingsRow(stringResource(R.string.settings_store), R.drawable.ic_rate_us_setting, null, onOpenStore)
            SettingsRow(stringResource(R.string.feedback_title), R.drawable.ic_feed_back_setting, null, onOpenFeedback)
            SettingsRow(stringResource(R.string.settings_share_app), R.drawable.ic_share_app_settings, null, onShareApp)
            SettingsRow(stringResource(R.string.settings_rate_us), R.drawable.ic_rate_us_setting, if (uiState.ratingSelection > 0) stringResource(R.string.settings_rating_line, uiState.ratingSelection) else null, onRateApp)
            SettingsRow(stringResource(R.string.privacy_policy), R.drawable.ic_privacy_settings, null, onOpenPrivacy)
            SettingsRow(stringResource(R.string.terms_amp_conditions), R.drawable.ic_privacy_settings, null, onOpenTerms)
        }
    }
}

@Composable
internal fun SettingsRow(
    title: String,
    iconRes: Int,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AccentIconBlue),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    if (subtitle != null) {
                        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Image(
                painter = painterResource(R.drawable.ic_end_setting),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
internal fun SettingsTopBar(
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp, shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.size(40.dp))
                Text(stringResource(R.string.settings_screen_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.size(40.dp))
            }
        }
    }
}
