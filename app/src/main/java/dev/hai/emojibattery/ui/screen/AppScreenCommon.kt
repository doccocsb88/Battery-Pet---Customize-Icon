package dev.hai.emojibattery.ui.screen


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.MainSection
import dev.hai.emojibattery.model.StatusBarTab
import dev.hai.emojibattery.ui.navigation.AppRoute
import java.util.Locale

@Composable
internal fun PermissionBanner(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.accessibility_bridge_title), fontWeight = FontWeight.SemiBold)
                Text(
                    if (enabled) {
                        stringResource(R.string.accessibility_bridge_active)
                    } else {
                        stringResource(R.string.accessibility_bridge_disabled)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppSwitch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
internal fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // Override M3 defaults to remove the pink-ish disabled tint and keep a neutral, on-brand look.
    val checkedTrack = Color(0xFF8FB6D4) // matches the app's accent blue
    val uncheckedTrack = Color(0xFFE6EEF5) // soft neutral (not pink)
    val uncheckedBorder = Color(0xFFD6E0E8)
    val disabledUncheckedTrack = Color(0xFFF1F5F9)
    val disabledCheckedTrack = Color(0xFFB8C8D6)
    val thumb = Color.White
    val disabledThumb = Color(0xFFF8FAFC)

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = thumb,
            checkedTrackColor = checkedTrack,
            checkedBorderColor = checkedTrack,
            uncheckedThumbColor = thumb,
            uncheckedTrackColor = uncheckedTrack,
            uncheckedBorderColor = uncheckedBorder,
            disabledCheckedThumbColor = disabledThumb,
            disabledCheckedTrackColor = disabledCheckedTrack,
            disabledCheckedBorderColor = disabledCheckedTrack,
            disabledUncheckedThumbColor = disabledThumb,
            disabledUncheckedTrackColor = disabledUncheckedTrack,
            disabledUncheckedBorderColor = uncheckedBorder,
        ),
    )
}

@Composable
internal fun EmojiBatteryOverlayToggleCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keep identical styling to the toggle card used in Battery Troll for consistency across screens.
    val shape = RoundedCornerShape(20.dp)
    val strokeColor = Color(0xFF8FB6D4)
    val background = Color(0xFFF2F2F2)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        border = BorderStroke(1.dp, strokeColor),
        color = background,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.enable_disable_emoji_battery),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppSwitch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
internal fun EmojiBatteryOverlayAccessCard(
    accessibilityGranted: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onRequestAccessibility: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (accessibilityGranted) {
        EmojiBatteryOverlayToggleCard(
            enabled = enabled,
            onToggle = onToggle,
            modifier = modifier,
        )
        return
    }

    val shape = RoundedCornerShape(28.dp)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        border = BorderStroke(1.dp, Color(0xFFD8DDE2)),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_enable_banner),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF8FB6D4)),
            ) {
                Text(
                    text = stringResource(R.string.home_start),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clickable(onClick = onRequestAccessibility)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }
        }
    }
}

@Composable
internal fun TabStrip(
    tabs: List<StatusBarTab>,
    selected: StatusBarTab,
    onSelect: (StatusBarTab) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tabs) { tab ->
            FilterChip(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                label = { Text(tab.title) },
            )
        }
    }
}

@Composable
internal fun SettingToggle(
    label: String,
    value: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label)
            AppSwitch(checked = value, onCheckedChange = onChange)
        }
    }
}

@Composable
internal fun SliderField(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.slider_percent_format, (value * 100).toInt()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppBasicSlider(value = value, onValueChange = onChange, valueRange = range)
        }
    }
}

@Composable
internal fun AppBasicSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    activeColor: Color = Color(0xFF62D3D0),
    inactiveColor: Color = Color(0xFFCBEFED),
    thumbColor: Color = activeColor,
) {
    val coercedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val span = (valueRange.endInclusive - valueRange.start).takeIf { it > 0f } ?: 1f
    val fraction = ((coercedValue - valueRange.start) / span).coerceIn(0f, 1f)
    val thumbSize = 20.dp
    val trackHeight = 6.dp
    val thumbRadius = thumbSize / 2
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier.height(28.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val availableWidth = (maxWidth - thumbSize).coerceAtLeast(0.dp)
        val thumbOffset = availableWidth * fraction
        val activeWidth = (thumbOffset + thumbRadius).coerceAtMost(maxWidth)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(999.dp))
                .background(inactiveColor.copy(alpha = if (enabled) 1f else 0.55f)),
        )
        Box(
            modifier = Modifier
                .width(activeWidth)
                .height(trackHeight)
                .clip(RoundedCornerShape(999.dp))
                .background(activeColor.copy(alpha = if (enabled) 1f else 0.65f)),
        )
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(3.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(thumbColor.copy(alpha = if (enabled) 1f else 0.75f)),
        )
        Slider(
            value = coercedValue,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                disabledThumbColor = Color.Transparent,
                disabledActiveTrackColor = Color.Transparent,
                disabledInactiveTrackColor = Color.Transparent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
        )
    }
}

@Composable
internal fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    AssistChip(onClick = onClick, label = { Text(label) }, leadingIcon = if (selected) {
        {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    } else {
        null
    })
}

@Composable
internal fun HeroCard(
    title: String,
    body: String,
    cta: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onClick) { Text(cta) }
        }
    }
}

@Composable
internal fun SmallActionCard(
    title: String,
    subtitle: String,
    glyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(glyph, style = MaterialTheme.typography.headlineSmall)
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScreenContainer(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
internal fun PlaceholderScreen(
    title: String,
    subtitle: String,
) {
    ScreenContainer(title = title, subtitle = subtitle) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = subtitle,
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
internal fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (AppRoute, MainSection) -> Unit,
) {
    val items = listOf(
        Triple(AppRoute.Home, MainSection.Home, Icons.Rounded.Home),
        Triple(AppRoute.Customize, MainSection.Customize, Icons.Rounded.AutoAwesome),
        Triple(AppRoute.Settings, MainSection.Settings, Icons.Rounded.Settings),
    )
    val barShape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = barShape,
                    ambientColor = Color.Black.copy(alpha = 0.06f),
                    spotColor = Color.Black.copy(alpha = 0.06f),
                )
                .clip(barShape)
                .background(Color(0xFFFFFFFF))
                .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { (route, section, icon) ->
                val selected = currentRoute == route.route
                val itemColor = if (selected) Color(0xFF3C637E) else Color(0xFF94A3B8)

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(if (selected) Color(0xFFAFD6F6) else Color.Transparent)
                            .clickable { onNavigate(route, section) }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = section.title,
                            modifier = Modifier.size(24.dp),
                            tint = itemColor,
                        )
                        Text(
                            text = section.title.uppercase(Locale.ROOT),
                            color = itemColor,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                lineHeight = 12.sp,
                                letterSpacing = 1.2.sp,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PremiumButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Image(
        painter = painterResource(R.drawable.premium_pro_button),
        contentDescription = "Open premium",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
    )
}
