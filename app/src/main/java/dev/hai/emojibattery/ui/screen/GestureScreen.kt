package dev.hai.emojibattery.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.GestureAction
import dev.hai.emojibattery.model.GestureTrigger
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.ui.gesture.actionLabelRes
import dev.hai.emojibattery.ui.gesture.rowIconRes
import dev.hai.emojibattery.ui.gesture.triggerLabelRes
import dev.hai.emojibattery.ui.theme.StrawberryCtaGradientBrush

private val GestureIconBlue = Color(0xFF8FB6D4)
private val ToggleCheckedBlue = Color(0xFF8FB6D4)
private val ToggleUncheckedGray = Color(0xFF94A3B8)

/**
 * Order matches [fragment_guesture.xml](decompiled): Single tap → up/down → L→R → R→L → long press.
 */
private val GestureTriggerDisplayOrder = listOf(
    GestureTrigger.SingleTap,
    GestureTrigger.SwipeTopToBottom,
    GestureTrigger.SwipeLeftToRight,
    GestureTrigger.SwipeRightToLeft,
    GestureTrigger.LongPress,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GestureScreen(
    uiState: AppUiState,
    onSetGestureEnabled: (Boolean) -> Unit,
    onSetVibrateFeedback: (Boolean) -> Unit,
    onSetGestureAction: (GestureTrigger, GestureAction) -> Unit,
) {
    var sheetTrigger by remember { mutableStateOf<GestureTrigger?>(null) }
    var pendingAction by remember { mutableStateOf(GestureAction.DoNothing) }

    LaunchedEffect(sheetTrigger) {
        val t = sheetTrigger ?: return@LaunchedEffect
        pendingAction = uiState.gestureActions[t] ?: SampleCatalog.defaultGestureActions[t] ?: GestureAction.DoNothing
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OriginalTopShell(
                title = stringResource(R.string.battery_icon_title),
                onLeftSecondary = {},
                onSearch = {},
                showLeftSecondary = false,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(Modifier.padding(vertical = 6.dp)) {
                    GestureSwitchRow(
                        iconRes = R.drawable.ic_guesture_32,
                        title = stringResource(R.string.gesture),
                        description = stringResource(R.string.use_gestures_in_the_status_bar_for_custom_actions),
                        enabled = uiState.gestureEnabled,
                        onToggle = onSetGestureEnabled,
                    )
                    AnimatedVisibility(uiState.gestureEnabled) {
                        Column {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 15.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                            )
                            GestureTriggerDisplayOrder.forEach { trigger ->
                                val current = uiState.gestureActions[trigger] ?: GestureAction.DoNothing
                                GestureMappingRowWithIcon(
                                    iconRes = trigger.rowIconRes(),
                                    triggerLabel = stringResource(trigger.triggerLabelRes()),
                                    actionLabel = stringResource(current.actionLabelRes()),
                                    onClick = { sheetTrigger = trigger },
                                )
                            }
                        }
                    }
                }
            }
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                GestureSwitchRow(
                    iconRes = R.drawable.ic_vibrate_feedback_32,
                    title = stringResource(R.string.vibrate_feedback),
                    description = stringResource(R.string.vibrate_feedback_when_interacting_with_the_status_bar),
                    enabled = uiState.vibrateFeedback,
                    onToggle = onSetVibrateFeedback,
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    sheetTrigger?.let { trigger ->
        ModalBottomSheet(
            onDismissRequest = { sheetTrigger = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null,
        ) {
            SelectActionSheetContent(
                pendingAction = pendingAction,
                onPendingChange = { pendingAction = it },
                onApply = {
                    onSetGestureAction(trigger, pendingAction)
                    sheetTrigger = null
                },
                onClose = { sheetTrigger = null },
            )
        }
    }
}

@Composable
private fun GestureMappingRowWithIcon(
    iconRes: Int,
    triggerLabel: String,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            colorFilter = ColorFilter.tint(GestureIconBlue),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                triggerLabel,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                actionLabel,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SelectActionSheetContent(
    pendingAction: GestureAction,
    onPendingChange: (GestureAction) -> Unit,
    onApply: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                stringResource(R.string.select_action),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
        ) {
            items(SampleCatalog.gestureActionOptions.toList(), key = { it.name }) { action ->
                SelectActionRadioRow(
                    selected = action == pendingAction,
                    label = stringResource(action.actionLabelRes()),
                    onSelect = { onPendingChange(action) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(StrawberryCtaGradientBrush)
                .clickable(onClick = onApply),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.apply),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SelectActionRadioRow(
    selected: Boolean,
    label: String,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(
                        width = if (selected) 2.5.dp else 1.5.dp,
                        color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    ),
            )
            if (selected) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                )
            }
        }
        Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
internal fun GestureSwitchRow(
    iconRes: Int,
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(GestureIconBlue),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                Text(description, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            modifier = Modifier.graphicsLayer { alpha = 1f },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ToggleCheckedBlue,
                checkedBorderColor = ToggleCheckedBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ToggleUncheckedGray,
                uncheckedBorderColor = ToggleUncheckedGray,
            ),
        )
    }
}
