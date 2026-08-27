package dev.hai.emojibattery.ui.gesture

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.model.GestureAction
import dev.hai.emojibattery.model.GestureTrigger

/** String ids mirror `decompiled/.../apktool-main/res/values/strings.xml` (GuestureFragment / DialogChooseActionGesture). */
@StringRes
fun GestureTrigger.triggerLabelRes(): Int = when (this) {
    GestureTrigger.SingleTap -> R.string.single_tap_action
    GestureTrigger.SwipeTopToBottom -> R.string.swipe_up_to_down
    GestureTrigger.SwipeLeftToRight -> R.string.swipe_left_to_right_action
    GestureTrigger.SwipeRightToLeft -> R.string.swipe_right_to_left_action
    GestureTrigger.LongPress -> R.string.long_press_action
}

@DrawableRes
fun GestureTrigger.rowIconRes(): Int = when (this) {
    GestureTrigger.SingleTap -> R.drawable.ic_single_tap_button_32
    GestureTrigger.SwipeTopToBottom -> R.drawable.ic_up_to_down_32
    GestureTrigger.SwipeLeftToRight -> R.drawable.ic_left_to_right_32
    GestureTrigger.SwipeRightToLeft -> R.drawable.ic_right_to_left_32
    GestureTrigger.LongPress -> R.drawable.ic_long_press_32
}

@StringRes
fun GestureAction.actionLabelRes(): Int = when (this) {
    GestureAction.OpenApp -> R.string.open_app
    GestureAction.DoNothing -> R.string.do_nothing
    GestureAction.BackAction -> R.string.action_button_back
    GestureAction.HomeAction -> R.string.action_button_home
    GestureAction.RecentAction -> R.string.recent_apps_button
    GestureAction.NotificationCenter -> R.string.action_opens_notification_center
    GestureAction.ControlCenter -> R.string.open_control_center
    GestureAction.PowerSourceOptions -> R.string.power_source_options
    GestureAction.LockScreen -> R.string.screen_lock_button_action
    GestureAction.TakeScreenshot -> R.string.screenshot_action_button
}
