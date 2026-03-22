package dev.hai.emojibattery.locale

/**
 * When [isLanguagePickerFlowEnabled] is false, the first-run flow skips the language screen,
 * Settings hides the language row, and the app uses [fixedAppLocaleTag] only. The
 * [LanguageScreen] composable and related helpers remain in the codebase for reuse.
 */
object AppLanguageConfig {
    const val isLanguagePickerFlowEnabled: Boolean = false

    /** BCP 47 tag applied when the picker flow is disabled. */
    const val fixedAppLocaleTag: String = "en"
}
