package dev.hai.emojibattery.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.service.NotchTemplateCatalog
import dev.hai.emojibattery.service.OverlayAccessibilityService
import dev.hai.emojibattery.service.OverlayConfigStore

@Composable
internal fun NotchScreen(
    onBack: () -> Unit,
) {
    val templates = remember { (-1..13).map { NotchTemplateCatalog.resolve(it) } }
    val context = LocalContext.current
    var selectedId by remember { mutableIntStateOf(OverlayConfigStore.read(context).notchTemplateId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OriginalTopShell(
                title = stringResource(R.string.home_notch),
                onLeftSecondary = onBack,
                onSearch = onBack,
            )
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
        ) {
            items(templates) { template ->
                val selected = template.id == selectedId
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = if (selected) 2.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedId = template.id
                            OverlayConfigStore.saveNotchTemplateId(context, template.id)
                            OverlayAccessibilityService.requestRefresh(context)
                        },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .padding(vertical = 10.dp),
                        ) {
                            if (template.drawableRes != null) {
                                Image(
                                    painter = painterResource(template.drawableRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .size(width = 100.dp, height = 20.dp),
                                )
                            } else {
                                Text(
                                    text = "Hide notch",
                                    modifier = Modifier
                                        .padding(vertical = 2.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        Text(
                            text = if (template.id == -1) "Hide notch" else "Notch ${template.id}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
