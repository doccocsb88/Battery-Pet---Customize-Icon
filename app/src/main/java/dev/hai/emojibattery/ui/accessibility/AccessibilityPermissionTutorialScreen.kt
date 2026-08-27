package dev.hai.emojibattery.ui.accessibility

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.tracking.TrackingServices
import dev.hai.emojibattery.ui.theme.OceanSerenity

/**
 * Full-screen how-to after the usage consent dialog, before opening system
 * Accessibility Settings. Mirrors original [DialogTutorialRequestPermission]:
 * numbered OEM steps + Go to Setting.
 */
@Composable
fun AccessibilityPermissionTutorialScreen(
    fromScreen: String?,
    onBack: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    val context = LocalContext.current
    val appName = stringResource(R.string.app_name)
    val oemFamily = remember { detectAccessibilityOemFamily() }
    val steps = accessibilityTutorialSteps(oemFamily, appName)

    LaunchedEffect(Unit) {
        TrackingServices.trackFeatureOpen(
            context = context.applicationContext,
            featureKey = "permission_tutorial",
            source = fromScreen,
        )
    }

    BackHandler(onBack = onBack)

    Surface(
        color = OceanSerenity.Surface,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_40_new),
                    contentDescription = stringResource(R.string.close),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onBack)
                        .padding(4.dp),
                )
                Text(
                    text = stringResource(R.string.how_to_use_title),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = OceanSerenity.OnSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(40.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                steps.forEach { step ->
                    TutorialPermissionStep(
                        step = step,
                        appName = appName,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OceanSerenity.Surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 42.dp)
                    .padding(top = 16.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(OceanSerenity.Primary, OceanSerenity.Secondary),
                            ),
                        )
                        .clickable(onClick = onGoToSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.go_to_setting),
                        color = OceanSerenity.OnPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialPermissionStep(
    step: AccessibilityTutorialStep,
    appName: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "${step.index}.",
                color = OceanSerenity.OnSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = highlightedInstruction(step.instruction, appName),
                color = OceanSerenity.OnSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        TutorialStepImage(step = step)
    }
}

@Composable
private fun highlightedInstruction(instruction: String, appName: String) = buildAnnotatedString {
    val quoted = "“$appName”"
    val start = instruction.indexOf(quoted)
    if (start < 0) {
        append(instruction)
        return@buildAnnotatedString
    }
    append(instruction.substring(0, start))
    withStyle(SpanStyle(color = OceanSerenity.Primary, fontWeight = FontWeight.SemiBold)) {
        append(quoted)
    }
    append(instruction.substring(start + quoted.length))
}

@Composable
private fun TutorialStepImage(step: AccessibilityTutorialStep) {
    val painter = painterResource(step.imageRes)
    val intrinsic = painter.intrinsicSize
    val aspectRatio = if (intrinsic.isSpecified && intrinsic.height > 0f) {
        intrinsic.width / intrinsic.height
    } else {
        16f / 9f
    }
    Image(
        painter = painter,
        contentDescription = step.instruction,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .aspectRatio(aspectRatio),
        contentScale = ContentScale.FillWidth,
    )
}

internal enum class AccessibilityOemFamily {
    Samsung,
    Pixel,
    Xiaomi,
    Oppo,
    Huawei,
}

internal data class AccessibilityTutorialStep(
    val index: Int,
    val instruction: String,
    @DrawableRes val imageRes: Int,
)

internal fun detectAccessibilityOemFamily(): AccessibilityOemFamily {
    val haystack = listOf(Build.MANUFACTURER, Build.BRAND, Build.DEVICE)
        .joinToString(" ")
        .lowercase()
    return when {
        "huawei" in haystack || "honor" in haystack -> AccessibilityOemFamily.Huawei
        "xiaomi" in haystack || "redmi" in haystack || "poco" in haystack -> AccessibilityOemFamily.Xiaomi
        "oppo" in haystack || "realme" in haystack || "oneplus" in haystack -> AccessibilityOemFamily.Oppo
        "google" in haystack || "pixel" in haystack -> AccessibilityOemFamily.Pixel
        else -> AccessibilityOemFamily.Samsung
    }
}

@Composable
private fun accessibilityTutorialSteps(
    oemFamily: AccessibilityOemFamily,
    appName: String,
): List<AccessibilityTutorialStep> {
    val selectApp = stringResource(R.string.select_app_in_quotes, appName)
    val turnOn = stringResource(R.string.turn_on)
    return when (oemFamily) {
        AccessibilityOemFamily.Pixel -> listOf(
            AccessibilityTutorialStep(
                index = 1,
                instruction = stringResource(R.string.select_installed_apps_or_downloaded_app_in_accessibility_setting),
                imageRes = R.drawable.permision_pixel1,
            ),
            AccessibilityTutorialStep(2, selectApp, R.drawable.permision_pixel2),
            AccessibilityTutorialStep(3, turnOn, R.drawable.permision_pixel3),
        )
        AccessibilityOemFamily.Oppo -> listOf(
            AccessibilityTutorialStep(
                index = 1,
                instruction = stringResource(R.string.select_downloaded_app_in_accessibility_setting),
                imageRes = R.drawable.permission_oppo_1,
            ),
            AccessibilityTutorialStep(2, selectApp, R.drawable.permission_oppo_2),
            AccessibilityTutorialStep(3, turnOn, R.drawable.permission_oppo_3),
        )
        AccessibilityOemFamily.Xiaomi -> listOf(
            AccessibilityTutorialStep(
                index = 1,
                instruction = stringResource(R.string.select_downloaded_services_in_accessibility_setting),
                imageRes = R.drawable.permission_xiao_1,
            ),
            AccessibilityTutorialStep(2, selectApp, R.drawable.permission_xiao_2),
            AccessibilityTutorialStep(3, turnOn, R.drawable.permission_xiao_3),
            AccessibilityTutorialStep(
                index = 4,
                instruction = stringResource(R.string.click_allow),
                imageRes = R.drawable.permission_xiao_4,
            ),
        )
        AccessibilityOemFamily.Huawei -> listOf(
            AccessibilityTutorialStep(
                index = 1,
                instruction = stringResource(R.string.select_installed_services_in_accessibility_setting),
                imageRes = R.drawable.permission_hua_1,
            ),
            AccessibilityTutorialStep(2, selectApp, R.drawable.permission_hua_2),
            AccessibilityTutorialStep(3, turnOn, R.drawable.permission_hua_3),
            AccessibilityTutorialStep(
                index = 4,
                instruction = stringResource(R.string.click_grant_access),
                imageRes = R.drawable.permission_hua_4,
            ),
        )
        AccessibilityOemFamily.Samsung -> listOf(
            AccessibilityTutorialStep(
                index = 1,
                instruction = stringResource(R.string.select_installed_apps_in_accessibility_setting),
                imageRes = R.drawable.permission_sam_1,
            ),
            AccessibilityTutorialStep(2, selectApp, R.drawable.permission_sam_2),
            AccessibilityTutorialStep(3, turnOn, R.drawable.permission_sam_3),
        )
    }
}
