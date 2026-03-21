package dev.hai.emojibattery.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hai.emojibattery.data.HomeCatalogRepository
import dev.hai.emojibattery.data.VolioHomeRepository
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.AchievementTask
import dev.hai.emojibattery.model.BatteryIconConfig
import dev.hai.emojibattery.model.CustomizeEntry
import dev.hai.emojibattery.model.FeatureConfig
import dev.hai.emojibattery.model.GestureAction
import dev.hai.emojibattery.model.GestureTrigger
import dev.hai.emojibattery.model.MainSection
import dev.hai.emojibattery.model.PaywallState
import dev.hai.emojibattery.model.SampleCatalog
import dev.hai.emojibattery.model.StickerPlacement
import dev.hai.emojibattery.model.StatusBarTab
import dev.hai.emojibattery.model.ThemePreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmojiBatteryViewModel : ViewModel() {
    private var homeCategoryLoadJob: Job? = null

    private val _uiState = MutableStateFlow(
        AppUiState(
            editingConfig = SampleCatalog.defaultConfig,
            appliedConfig = SampleCatalog.defaultConfig,
            homeTabs = HomeCatalogRepository.categoryTabs(),
        ),
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { VolioHomeRepository.fetchCategoryTabs() }
                .onSuccess { remoteTabs ->
                    if (remoteTabs.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                homeTabs = remoteTabs,
                                selectedHomeCategoryId = remoteTabs.first().id,
                                homeItemsByCategoryId = emptyMap(),
                            )
                        }
                        loadHomeCategoryItems(remoteTabs.first().id)
                    }
                }
        }
    }

    fun finishSplash() {
        _uiState.update { it.copy(splashDone = true) }
    }

    fun chooseLanguage(language: String) {
        _uiState.update {
            it.copy(
                languageChosen = true,
                selectedLanguage = language,
                onboardingPage = 0,
                infoMessage = "Language set to $language",
            )
        }
    }

    fun nextOnboardingPage() {
        _uiState.update { state ->
            val lastIndex = SampleCatalog.onboardingPages.lastIndex
            if (state.onboardingPage >= lastIndex) {
                state.copy(
                    onboardingCompleted = true,
                    infoMessage = "Onboarding completed.",
                )
            } else {
                state.copy(onboardingPage = state.onboardingPage + 1)
            }
        }
    }

    fun previousOnboardingPage() {
        _uiState.update { state ->
            state.copy(onboardingPage = (state.onboardingPage - 1).coerceAtLeast(0))
        }
    }

    fun skipOnboarding() {
        _uiState.update {
            it.copy(
                onboardingCompleted = true,
                infoMessage = "Onboarding skipped.",
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun selectMainSection(section: MainSection) {
        _uiState.update { it.copy(activeMainSection = section) }
    }

    /**
     * Called when the home category strip or [androidx.compose.foundation.pager.HorizontalPager]
     * settles on a category (mirrors original ViewPager2 page + SubHome load).
     */
    fun selectHomeCategory(categoryId: String) {
        _uiState.update { it.copy(selectedHomeCategoryId = categoryId) }
        loadHomeCategoryItems(categoryId)
    }

    private fun loadHomeCategoryItems(categoryId: String) {
        homeCategoryLoadJob?.cancel()
        homeCategoryLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(homeCategoryLoadingId = categoryId) }
            val items = withContext(Dispatchers.IO) {
                runCatching {
                    if (isVolioCategoryId(categoryId)) {
                        VolioHomeRepository.fetchItemsForCategory(categoryId)
                    } else {
                        HomeCatalogRepository.loadItemsForCategory(categoryId)
                    }
                }.getOrElse {
                    if (isVolioCategoryId(categoryId)) {
                        emptyList()
                    } else {
                        HomeCatalogRepository.loadItemsForCategory(categoryId)
                    }
                }
            }
            _uiState.update { state ->
                state.copy(
                    homeItemsByCategoryId = state.homeItemsByCategoryId + (categoryId to items),
                    homeCategoryLoadingId = null,
                )
            }
        }
    }

    private fun isVolioCategoryId(categoryId: String): Boolean =
        categoryId.length >= 32 && categoryId.count { it == '-' } >= 4

    fun selectStatusTab(tab: StatusBarTab) {
        _uiState.update { it.copy(activeStatusBarTab = tab) }
    }

    fun setAccessibilityGranted(granted: Boolean) {
        _uiState.update {
            it.copy(
                accessibilityGranted = granted,
                infoMessage = if (granted) "Accessibility bridge enabled for preview/apply." else "Accessibility bridge disabled.",
            )
        }
    }

    fun syncAccessibilityGranted(granted: Boolean) {
        _uiState.update { it.copy(accessibilityGranted = granted) }
    }

    fun selectBatteryPreset(id: String) = updateConfig { copy(batteryPresetId = id) }

    fun selectEmojiPreset(id: String) = updateConfig { copy(emojiPresetId = id) }

    fun selectTheme(themeId: String) {
        val theme = SampleCatalog.themePresets.firstOrNull { it.id == themeId } ?: return
        updateTheme(theme)
    }

    fun setStatusBarHeight(value: Float) = updateConfig { copy(statusBarHeight = value) }

    fun setStatusBarLeftMargin(value: Float) = updateConfig { copy(leftMargin = value) }

    fun setStatusBarRightMargin(value: Float) = updateConfig { copy(rightMargin = value) }

    fun setBatteryPercentScale(value: Float) = updateConfig { copy(batteryPercentScale = value) }

    fun setEmojiScale(value: Float) = updateConfig { copy(emojiScale = value) }

    fun setShowPercentage(value: Boolean) = updateConfig { copy(showPercentage = value) }

    fun setAnimateCharge(value: Boolean) = updateConfig { copy(animateCharge = value) }

    fun setShowStroke(value: Boolean) = updateConfig { copy(showStroke = value) }

    fun restoreApplied() {
        _uiState.update {
            it.copy(
                editingConfig = it.appliedConfig,
                infoMessage = "Unsaved changes discarded.",
            )
        }
    }

    fun applyConfig() {
        _uiState.update { state ->
            if (!state.accessibilityGranted) {
                state.copy(infoMessage = "Enable accessibility bridge before applying the status-bar icon.")
            } else {
                state.copy(
                    appliedConfig = state.editingConfig,
                    infoMessage = "Configuration applied successfully.",
                )
            }
        }
        advanceAchievement("apply_status_bar")
    }

    fun applyLegacyBatteryConfig() {
        _uiState.update { it.copy(appliedConfig = it.editingConfig, infoMessage = "Legacy battery icon flow applied.") }
        advanceAchievement("apply_status_bar")
    }

    fun addSticker(stickerId: String) {
        val sticker = SampleCatalog.stickerPresets.firstOrNull { it.id == stickerId } ?: return
        _uiState.update { state ->
            if (sticker.premium && !hasFeatureAccess(state, "sticker:$stickerId")) {
                state.copy(
                    paywallState = PaywallState(
                        featureKey = "sticker:$stickerId",
                        title = "Unlock Premium Sticker",
                        message = "${sticker.name} is a premium sticker. Upgrade to unlock premium sticker packs.",
                    ),
                )
            } else if (state.stickerPlacements.none { it.stickerId == stickerId } && state.stickerPlacements.size >= maxStickerSlots(state)) {
                state.copy(
                    paywallState = PaywallState(
                        featureKey = SampleCatalog.FEATURE_EXTRA_STICKER_SLOT,
                        title = "Unlock More Sticker Slots",
                        message = "Free mode allows ${SampleCatalog.FREE_STICKER_SLOTS} sticker. Claim rewards or unlock premium to add more.",
                    ),
                )
            } else if (state.stickerPlacements.any { it.stickerId == stickerId }) {
                state.copy(selectedStickerId = stickerId, infoMessage = "Sticker already added. Opened for editing.")
            } else {
                state.copy(
                    stickerPlacements = state.stickerPlacements + StickerPlacement(stickerId = stickerId),
                    selectedStickerId = stickerId,
                    infoMessage = "Sticker added.",
                )
            }
        }
    }

    fun selectSticker(stickerId: String) {
        _uiState.update { it.copy(selectedStickerId = stickerId) }
    }

    fun removeSticker(stickerId: String) {
        _uiState.update { state ->
            val updated = state.stickerPlacements.filterNot { it.stickerId == stickerId }
            state.copy(
                stickerPlacements = updated,
                selectedStickerId = updated.lastOrNull()?.stickerId,
                infoMessage = "Sticker removed.",
            )
        }
    }

    fun updateSelectedStickerSize(value: Float) {
        updateSelectedSticker { copy(size = value) }
    }

    fun updateSelectedStickerSpeed(value: Float) {
        updateSelectedSticker { copy(speed = value) }
    }

    fun saveStickerOverlay() {
        _uiState.update { state ->
            when {
                state.stickerPlacements.isEmpty() -> state.copy(infoMessage = "Please select at least one sticker.")
                !state.accessibilityGranted -> state.copy(infoMessage = "Enable accessibility bridge before saving sticker overlay.")
                else -> state.copy(stickerOverlayEnabled = true, infoMessage = "Sticker overlay saved.")
            }
        }
        advanceAchievement("save_sticker")
    }

    fun turnOffStickerOverlay() {
        _uiState.update { it.copy(stickerOverlayEnabled = false, infoMessage = "Sticker overlay turned off.") }
    }

    fun setGestureEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(
                gestureEnabled = enabled,
                infoMessage = if (enabled) "Status-bar gestures enabled." else "Status-bar gestures disabled.",
            )
        }
    }

    fun setVibrateFeedback(enabled: Boolean) {
        _uiState.update {
            it.copy(
                vibrateFeedback = enabled,
                infoMessage = if (enabled) "Vibrate feedback enabled." else "Vibrate feedback disabled.",
            )
        }
    }

    fun setGestureAction(trigger: GestureTrigger, action: GestureAction) {
        _uiState.update { state ->
            state.copy(
                gestureActions = state.gestureActions + (trigger to action),
                infoMessage = "${trigger.title} mapped to ${action.title}.",
            )
        }
        val mappedCount = _uiState.value.gestureActions.values.count { it != GestureAction.None }
        if (mappedCount >= 3) advanceAchievement("gesture_mapper")
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateFeatureEnabled(entry: CustomizeEntry, enabled: Boolean) {
        updateFeature(entry) { copy(enabled = enabled) }
    }

    fun updateFeatureIntensity(entry: CustomizeEntry, intensity: Float) {
        updateFeature(entry) { copy(intensity = intensity) }
    }

    fun updateFeatureVariant(entry: CustomizeEntry, variant: String) {
        updateFeature(entry) { copy(variant = variant) }
    }

    fun resetFeature(entry: CustomizeEntry) {
        val default = SampleCatalog.defaultFeatureConfigs[entry] ?: return
        _uiState.update { state ->
            state.copy(
                featureConfigs = state.featureConfigs + (entry to default),
                infoMessage = "${entry.title} reset to default.",
            )
        }
    }

    fun applyFeature(entry: CustomizeEntry) {
        val config = _uiState.value.featureConfigs[entry] ?: return
        _uiState.update {
            it.copy(
                infoMessage = "${entry.title} applied with ${config.variant.lowercase()} style at ${(config.intensity * 100).toInt()}%.",
            )
        }
    }

    fun selectRealTimeTemplate(templateId: String) {
        val template = SampleCatalog.realTimeTemplates.firstOrNull { it.id == templateId } ?: return
        _uiState.update { state ->
            if (template.premium && !hasFeatureAccess(state, "template:$templateId")) {
                state.copy(
                    paywallState = PaywallState(
                        featureKey = "template:$templateId",
                        title = "Unlock Premium Template",
                        message = "${template.title} is premium. Claim the template reward or unlock premium access.",
                    ),
                )
            } else {
                state.copy(selectedRealTimeTemplateId = templateId)
            }
        }
    }

    fun applyRealTimeTemplate() {
        val selected = SampleCatalog.realTimeTemplates.firstOrNull { it.id == _uiState.value.selectedRealTimeTemplateId } ?: return
        _uiState.update { state ->
            if (!state.accessibilityGranted) {
                state.copy(infoMessage = "Enable accessibility bridge before applying a Real Time template.")
            } else {
                state.copy(infoMessage = "Real Time template '${selected.title}' prepared and applied.")
            }
        }
        advanceAchievement("template_explorer")
    }

    fun selectBatteryTrollTemplate(templateId: String) {
        val template = SampleCatalog.batteryTrollTemplates.firstOrNull { it.id == templateId } ?: return
        _uiState.update {
            it.copy(
                selectedBatteryTrollTemplateId = templateId,
                trollMessage = template.prankMessage,
            )
        }
    }

    fun setTrollMessage(message: String) {
        _uiState.update { it.copy(trollMessage = message) }
    }

    fun setTrollAutoDrop(enabled: Boolean) {
        _uiState.update { it.copy(trollAutoDrop = enabled) }
    }

    fun applyBatteryTroll() {
        _uiState.update { state ->
            if (!state.accessibilityGranted) {
                state.copy(infoMessage = "Enable accessibility bridge before applying a Battery Troll overlay.")
            } else {
                state.copy(
                    trollOverlayEnabled = true,
                    infoMessage = "Battery Troll overlay applied with message '${state.trollMessage}'.",
                )
            }
        }
        advanceAchievement("template_explorer")
    }

    fun turnOffBatteryTroll() {
        _uiState.update { it.copy(trollOverlayEnabled = false, infoMessage = "Battery Troll overlay turned off.") }
    }

    fun replayTutorial() {
        _uiState.update {
            it.copy(
                tutorialCompleted = false,
                tutorialPage = 0,
                infoMessage = "Tutorial replay started.",
            )
        }
    }

    fun nextTutorialPage() {
        _uiState.update { state ->
            val lastIndex = SampleCatalog.tutorialPages.lastIndex
            if (state.tutorialPage >= lastIndex) {
                state.copy(
                    tutorialCompleted = true,
                    infoMessage = "Tutorial completed.",
                )
            } else {
                state.copy(tutorialPage = state.tutorialPage + 1)
            }
        }
    }

    fun previousTutorialPage() {
        _uiState.update { state ->
            state.copy(tutorialPage = (state.tutorialPage - 1).coerceAtLeast(0))
        }
    }

    fun skipTutorial() {
        _uiState.update {
            it.copy(
                tutorialCompleted = true,
                infoMessage = "Tutorial skipped.",
            )
        }
    }

    fun setProtectFromRecentApps(enabled: Boolean) {
        _uiState.update {
            it.copy(
                protectFromRecentApps = enabled,
                infoMessage = if (enabled) "App marked as protected from recents cleaning." else "Recent-app protection disabled.",
            )
        }
    }

    fun openPrivacyPolicy() {
        _uiState.update { it.copy(infoMessage = "Privacy policy action triggered.") }
    }

    fun openTermsOfUse() {
        _uiState.update { it.copy(infoMessage = "Terms of use action triggered.") }
    }

    fun shareApp() {
        _uiState.update { it.copy(infoMessage = "Share app action triggered.") }
    }

    fun setRatingSelection(value: Int) {
        _uiState.update { it.copy(ratingSelection = value.coerceIn(0, 5)) }
    }

    fun toggleFeedbackReason(reasonId: String) {
        _uiState.update { state ->
            val updated = state.feedbackReasons.toMutableSet().apply {
                if (!add(reasonId)) remove(reasonId)
            }
            state.copy(feedbackReasons = updated)
        }
    }

    fun setFeedbackNote(value: String) {
        _uiState.update { it.copy(feedbackNote = value, lastFeedbackSubmitted = false) }
    }

    fun submitFeedback() {
        _uiState.update { state ->
            if (state.feedbackReasons.isEmpty() && state.feedbackNote.isBlank()) {
                state.copy(infoMessage = "Please select at least one option or enter your own feedback.")
            } else {
                state.copy(
                    lastFeedbackSubmitted = true,
                    infoMessage = "Your feedback was successfully submitted.",
                )
            }
        }
    }

    fun rateApp() {
        _uiState.update { state ->
            val message = when {
                state.ratingSelection >= 4 -> "Thanks. This should deep-link to Google Play review in production."
                state.ratingSelection in 1..3 -> "Thanks. We captured the low rating path so users can leave feedback."
                else -> "Pick a star rating first."
            }
            state.copy(infoMessage = message)
        }
    }

    fun checkForUpdates() {
        _uiState.update { it.copy(infoMessage = "App is already on the latest local build.") }
    }

    fun dismissPaywall() {
        _uiState.update { it.copy(paywallState = null) }
    }

    fun syncPremiumAccess(hasPremium: Boolean) {
        _uiState.update {
            it.copy(
                premiumUnlocked = hasPremium,
                paywallState = if (hasPremium) null else it.paywallState,
            )
        }
    }

    fun claimAchievement(taskId: String) {
        _uiState.update { state ->
            val tasks = state.achievements.map { task ->
                if (task.id == taskId && task.progress >= task.target && !task.claimed) {
                    task.copy(claimed = true)
                } else {
                    task
                }
            }
            val claimed = tasks.firstOrNull { it.id == taskId && it.claimed }
            val unlockedFeatureKeys = when (taskId) {
                "save_sticker" -> state.unlockedFeatureKeys + SampleCatalog.FEATURE_EXTRA_STICKER_SLOT
                "template_explorer" -> state.unlockedFeatureKeys + SampleCatalog.FEATURE_PREMIUM_REALTIME_CAT_DIARY
                else -> state.unlockedFeatureKeys
            }
            state.copy(
                achievements = tasks,
                unlockedFeatureKeys = unlockedFeatureKeys,
                infoMessage = claimed?.let { "Reward claimed: ${it.reward}." } ?: "Achievement is not ready to claim yet.",
            )
        }
    }

    private fun updateTheme(theme: ThemePreset) {
        updateConfig {
            copy(
                themePresetId = theme.id,
                accentColor = theme.accent,
                backgroundColor = theme.background,
            )
        }
    }

    private fun updateConfig(transform: BatteryIconConfig.() -> BatteryIconConfig) {
        _uiState.update { state ->
            state.copy(editingConfig = state.editingConfig.transform())
        }
    }

    private fun updateSelectedSticker(transform: StickerPlacement.() -> StickerPlacement) {
        _uiState.update { state ->
            val targetId = state.selectedStickerId ?: return@update state
            state.copy(
                stickerPlacements = state.stickerPlacements.map { placement ->
                    if (placement.stickerId == targetId) placement.transform() else placement
                },
            )
        }
    }

    private fun updateFeature(entry: CustomizeEntry, transform: FeatureConfig.() -> FeatureConfig) {
        _uiState.update { state ->
            val current = state.featureConfigs[entry] ?: return@update state
            state.copy(featureConfigs = state.featureConfigs + (entry to current.transform()))
        }
    }

    private fun advanceAchievement(taskId: String, amount: Int = 1) {
        _uiState.update { state ->
            state.copy(
                achievements = state.achievements.map { task ->
                    if (task.id == taskId && !task.claimed) {
                        task.copy(progress = (task.progress + amount).coerceAtMost(task.target))
                    } else {
                        task
                    }
                },
            )
        }
    }

    private fun hasFeatureAccess(state: AppUiState, featureKey: String): Boolean {
        if (state.premiumUnlocked) return true
        if (featureKey == "template:cat_diary" && state.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_PREMIUM_REALTIME_CAT_DIARY)) {
            return true
        }
        return state.unlockedFeatureKeys.contains(featureKey)
    }

    private fun maxStickerSlots(state: AppUiState): Int = when {
        state.premiumUnlocked -> SampleCatalog.PREMIUM_STICKER_SLOTS
        state.unlockedFeatureKeys.contains(SampleCatalog.FEATURE_EXTRA_STICKER_SLOT) -> SampleCatalog.REWARD_EXTRA_STICKER_SLOTS
        else -> SampleCatalog.FREE_STICKER_SLOTS
    }
}
