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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.SampleCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnboardingScreen(
    uiState: AppUiState,
    onSkip: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val page = SampleCatalog.onboardingPages[uiState.onboardingPage.coerceIn(0, SampleCatalog.onboardingPages.lastIndex)]
    val isLast = uiState.onboardingPage == SampleCatalog.onboardingPages.lastIndex
    val imageRes = when (uiState.onboardingPage) {
        0 -> R.drawable.img_onboard_1
        1 -> R.drawable.img_onboard_2
        else -> R.drawable.img_onboard_3
    }
    val titleColor = colorResource(R.color.onboarding_title)
    val bodyColor = colorResource(R.color.onboarding_body)
    val indicatorActive = colorResource(R.color.onboarding_dot_active)
    val indicatorInactive = colorResource(R.color.onboarding_dot_inactive)
    val ctaGradientStart = colorResource(R.color.onboarding_cta_gradient_start)
    val ctaGradientEnd = colorResource(R.color.onboarding_cta_gradient_end)

    Scaffold(
        containerColor = colorResource(R.color.onboarding_scaffold),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = page.title,
                color = titleColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = page.body,
                    color = bodyColor,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                )
            }
            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SampleCatalog.onboardingPages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (index == uiState.onboardingPage) indicatorActive else indicatorInactive),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            if (isLast) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ctaGradientStart,
                                    ctaGradientEnd,
                                ),
                            ),
                        )
                        .clickable { onNext() }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_get_started),
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.onboarding_next),
                    color = indicatorActive,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNext() },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
