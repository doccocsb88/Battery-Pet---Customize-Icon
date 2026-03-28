package dev.hai.emojibattery.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
internal fun SplashRoute(
    fastForward: Boolean,
    onFinish: () -> Unit,
) {
    // Always show splash for at least 2s; do not bypass/cancel this flow.
    val minSplashDurationMs = 2_000L
    val iconPhaseDurationMs = 1_000L
    val loadingPhaseDurationMs = (minSplashDurationMs - iconPhaseDurationMs).coerceAtLeast(0L)
    val splashColorPrimary = Color(0xFF8FB6D4)
    val splashColorSecondary = Color(0xFF76916B)
    val splashColorTertiary = Color(0xFFD9B99B)
    val splashColorText = Color(0xFF3C3C3C)

    var launchLogo by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    var progressTarget by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Keep parameter for call-site compatibility; splash timing is now fixed.
        @Suppress("UNUSED_VARIABLE")
        val ignoredFastForward = fastForward

        launchLogo = true
        showLoading = false
        progressTarget = 0f

        withContext(NonCancellable) {
            delay(iconPhaseDurationMs)
            showLoading = true
            progressTarget = 1f
            delay(loadingPhaseDurationMs)
        }
        onFinish()
    }

    val logoScale by animateFloatAsState(
        targetValue = if (launchLogo) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "splash_logo_scale",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (launchLogo) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "splash_logo_alpha",
    )
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
        label = "splash_progress",
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        splashColorPrimary,
                        splashColorSecondary,
                        splashColorTertiary,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.92f),
                tonalElevation = 0.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.splash_logo_box))
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha
                    },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.img_btn_status_bar_new),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = splashColorText,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = splashColorText.copy(alpha = 0.82f),
                textAlign = TextAlign.Center,
            )
            if (showLoading) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.loading_dot),
                    color = splashColorText,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(0.55f),
                    color = splashColorText,
                    trackColor = Color.White.copy(alpha = 0.55f),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
