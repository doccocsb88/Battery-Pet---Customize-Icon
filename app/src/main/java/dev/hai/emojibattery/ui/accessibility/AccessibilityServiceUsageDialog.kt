package dev.hai.emojibattery.ui.accessibility

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.q7labs.co.emoji.R

/**
 * Port of [com.android.example.baseprojecthd.ui.dialog.DialogRequestAccessibilityService] and
 * [layout/dialog_request_accessibility_service.xml] from decompiled emoji-battery-icon-customize 1.2.8.
 */
@Composable
fun AccessibilityServiceUsageDialog(
    onDismiss: () -> Unit,
    onConfirmOpenSettings: () -> Unit,
    onMissingConsent: () -> Unit,
) {
    var termsAccepted by remember { mutableStateOf(false) }

    val dialogText = colorResource(R.color.accessibility_usage_dialog_text)
    val linkBlue = colorResource(R.color.accessibility_usage_dialog_link)
    val highlight = colorResource(R.color.accessibility_usage_dialog_highlight)
    val gradientStart = colorResource(R.color.accessibility_usage_cta_gradient_start)
    val gradientEnd = colorResource(R.color.accessibility_usage_cta_gradient_end)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colorResource(R.color.accessibility_usage_dialog_surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 17.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(R.string.accessibility_service_usage),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = dialogText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.please_agree_the_terms_of_service),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = dialogText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.this_app_required_the_accessibility_services_permission_for),
                    color = dialogText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                )
                Spacer(Modifier.height(10.dp))
                BulletWithHighlight(
                    displayLabel = stringResource(R.string.display_label),
                    appName = stringResource(R.string.app_name),
                    tail = stringResource(R.string.view_on_mobile_screen_and_to_detect_gestures_on_the_status_bar),
                    dialogText = dialogText,
                    highlight = highlight,
                )
                BulletRow(
                    text = stringResource(R.string.start_accessibility_actions_action_to_home_back_show_recent_screen),
                    dialogText = dialogText,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.the_application_commits_not_to_collect_or_share_any_user_information_about_this_accessibility_right),
                    color = dialogText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { termsAccepted = !termsAccepted },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(
                            if (termsAccepted) {
                                R.drawable.ic_check_box_permission_checked
                            } else {
                                R.drawable.ic_check_box_permission_unchecked
                            },
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp),
                    )
                    Text(
                        text = stringResource(R.string.i_have_read_and_agree_terms_of_service),
                        color = linkBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 22.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center,
                        color = dialogText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                    )
                    val nextAlpha = if (termsAccepted) 1f else 0.4f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .graphicsLayer { alpha = nextAlpha }
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(gradientStart, gradientEnd),
                                ),
                            )
                            .clickable {
                                if (termsAccepted) {
                                    onConfirmOpenSettings()
                                } else {
                                    onMissingConsent()
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.next),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                            modifier = Modifier.padding(horizontal = 22.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletRow(
    text: String,
    dialogText: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "•",
            color = dialogText,
            fontSize = 14.sp,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 4.dp),
            color = dialogText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
        )
    }
}

@Composable
private fun BulletWithHighlight(
    displayLabel: String,
    appName: String,
    tail: String,
    dialogText: Color,
    highlight: Color,
) {
    val annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = dialogText)) {
            append(displayLabel)
            append(" \"")
        }
        withStyle(SpanStyle(color = highlight)) {
            append(appName)
        }
        withStyle(SpanStyle(color = dialogText)) {
            append("\" ")
            append(tail)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "•",
            color = dialogText,
            fontSize = 14.sp,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
        )
        Text(
            text = annotated,
            modifier = Modifier.padding(start = 4.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
        )
    }
}
