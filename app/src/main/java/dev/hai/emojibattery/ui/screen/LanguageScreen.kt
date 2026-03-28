package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.locale.SUPPORTED_APP_LANGUAGES
import dev.hai.emojibattery.locale.displayNameForLocaleTag
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush
import java.util.Locale

@Composable
internal fun LanguageScreen(
    selectedLocaleTag: String,
    onSelectLocaleTag: (String) -> Unit,
    onNext: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val displayLocale = remember(configuration) {
        configuration.locales.get(0) ?: Locale.getDefault()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.select_your_language),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(SUPPORTED_APP_LANGUAGES, key = { it.localeTag }) { option ->
                    val active = option.localeTag == selectedLocaleTag
                    val label = displayNameForLocaleTag(option.localeTag, displayLocale)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSelectLocaleTag(option.localeTag) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Image(
                                painter = painterResource(option.flagResId),
                                contentDescription = label,
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Crop,
                            )
                            Spacer(Modifier.size(width = 16.dp, height = 0.dp))
                            Text(
                                label,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            if (option.localeTag == "en") {
                                Text(
                                    stringResource(R.string.language_default),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(end = 12.dp),
                                )
                            }
                            Image(
                                painter = painterResource(
                                    if (active) R.drawable.ic_checked_language_enable
                                    else R.drawable.ic_checked_language_disable,
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White),
                        ),
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(StrawberryCtaGradientBrush)
                        .clickable { onNext() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.next).uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
