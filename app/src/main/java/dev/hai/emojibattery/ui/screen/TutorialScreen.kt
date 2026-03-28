package dev.hai.emojibattery.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.SampleCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TutorialScreen(
    uiState: AppUiState,
    onClose: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenAccessibility: () -> Unit,
) {
    val page = SampleCatalog.tutorialPages[uiState.tutorialPage.coerceIn(0, SampleCatalog.tutorialPages.lastIndex)]
    val isFirst = uiState.tutorialPage == 0
    val isLast = uiState.tutorialPage == SampleCatalog.tutorialPages.lastIndex
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.how_to_use_title)) },
                navigationIcon = { TextButton(onClick = onClose) { Text(stringResource(R.string.close)) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TemplatePreviewCard(
                title = page.title,
                summary = page.body,
                glyph = page.accentGlyph,
                tag = stringResource(R.string.tutorial_step_format, uiState.tutorialPage + 1, SampleCatalog.tutorialPages.size),
            )
            if (uiState.tutorialPage == 0) {
                PermissionBanner(enabled = uiState.accessibilityGranted, onToggle = { onOpenAccessibility() })
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onPrevious, enabled = !isFirst, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.previous))
                }
                Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                    Text(if (isLast) stringResource(R.string.got_it) else stringResource(R.string.onboarding_next))
                }
            }
        }
    }
}
