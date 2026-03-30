package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    val splashColorText = Color.White

    var showLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Keep parameter for call-site compatibility; splash timing is now fixed.
        @Suppress("UNUSED_VARIABLE")
        val ignoredFastForward = fastForward

        showLoading = false

        withContext(NonCancellable) {
            delay(iconPhaseDurationMs)
            showLoading = true
            delay(loadingPhaseDurationMs)
        }
        onFinish()
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_loading),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            if (showLoading) {
                Text(
                    text = stringResource(R.string.loading_dot),
                    color = splashColorText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(56.dp))
        }
    }
}
