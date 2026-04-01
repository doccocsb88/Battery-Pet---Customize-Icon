package dev.hai.emojibattery.app

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.q7labs.co.emoji.R
import dev.hai.emojibattery.data.BundledVolioHomeRepository
import dev.hai.emojibattery.data.HomeCatalogRepository
import dev.hai.emojibattery.data.HomeCategoryPackResolver
import dev.hai.emojibattery.data.HomeStoreLocalImageResolver
import dev.hai.emojibattery.data.PadVolioBatteryTrollRepository
import dev.hai.emojibattery.data.PadVolioHomeRepository
import dev.hai.emojibattery.data.VolioBatteryTrollRepository
import dev.hai.emojibattery.data.VolioStickerRepository
import dev.hai.emojibattery.data.volio.VolioConstants
import dev.hai.emojibattery.model.AppUiState
import dev.hai.emojibattery.model.BatteryTrollTemplate
import dev.hai.emojibattery.model.HomeBatteryItem
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
import dev.hai.emojibattery.model.batteryTrollTemplateForId
import dev.hai.emojibattery.model.stickerPresetForId
import dev.hai.emojibattery.model.StatusBarTab
import dev.hai.emojibattery.model.ThemePreset
import dev.hai.emojibattery.locale.AppFlowPreferences
import dev.hai.emojibattery.locale.AppLanguageConfig
import dev.hai.emojibattery.locale.AppLocalePreferences
import dev.hai.emojibattery.locale.localeForSupportedTag
import dev.hai.emojibattery.locale.normalizeSupportedTag
import dev.hai.emojibattery.locale.resolveDefaultLocaleTag
import dev.hai.emojibattery.service.GestureSettingsStore
import dev.hai.emojibattery.service.OverlayConfigStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmojiBatteryViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private var homeCategoryLoadJob: Job? = null
    private var stickerCatalogLoadJob: Job? = null

    companion object {
        private const val APPLY_SUCCESS_MESSAGE = "Applied successfully."
        private const val TAG = "HomeFeed"
        private const val PENDING_HOME_STATUSBAR_CATEGORY_ID = "pending_home_statusbar_category_id"
        private const val PENDING_HOME_STATUSBAR_SELECTED_ITEM_ID = "pending_home_statusbar_selected_item_id"
        private val HOME_BUNDLED_FALLBACK_CATEGORY_TITLES = setOf("korean", "china", "winter")
    }

    private val initialDefaultStatusBarItems = defaultStatusBarCatalogItems(application)
    private val initialDefaultStatusBarItemId =
        initialDefaultStatusBarItems.firstOrNull()?.id ?: SampleCatalog.defaultConfig.batteryPresetId
    private val initialDefaultConfig = SampleCatalog.defaultConfig.copy(
        batteryPresetId = initialDefaultStatusBarItemId,
        emojiPresetId = initialDefaultStatusBarItemId,
    )

    private val _uiState = MutableStateFlow(
        AppUiState(
            editingConfig = initialDefaultConfig,
            appliedConfig = initialDefaultConfig,
            homeTabs = HomeCatalogRepository.categoryTabs(),
            statusBarCatalogItems = initialDefaultStatusBarItems,
        ),
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        val app = getApplication<Application>()
        val gestureSnapshot = GestureSettingsStore.read(app)
        val overlaySnapshot = OverlayConfigStore.read(app)
        val defaultTag = resolveDefaultLocaleTag(app)
        val englishOnly = !AppLanguageConfig.isLanguagePickerFlowEnabled
        val resolvedLocaleTag = if (englishOnly) {
            AppLanguageConfig.fixedAppLocaleTag
        } else {
            normalizeSupportedTag(
                AppLocalePreferences.getPersistedLocaleTag(app) ?: defaultTag,
            )
        }
        _uiState.update {
            it.copy(
                gestureEnabled = gestureSnapshot.gestureEnabled,
                vibrateFeedback = gestureSnapshot.vibrateFeedback,
                gestureActions = gestureSnapshot.gestureActions,
                splashDone = AppFlowPreferences.isSplashDone(app),
                languageChosen = englishOnly || AppLocalePreferences.isLanguageFlowCompleted(app),
                onboardingCompleted = AppFlowPreferences.isOnboardingDone(app),
                selectedLocaleTag = resolvedLocaleTag,
                padCatalogLoading = true,
                featureConfigs = overlaySnapshot.featureConfigs,
                statusBarOverlayEnabled = overlaySnapshot.statusBarEnabled,
            )
        }
        viewModelScope.launch {
            Log.d(TAG, "init: home tabs — bundled_volio → SampleCatalog (per-category PAD for items)")
            val app = getApplication<Application>()
            val bundledTabs = runCatching { BundledVolioHomeRepository.fetchCategoryTabs(app) }.getOrElse { emptyList() }
            val tabs = if (bundledTabs.isNotEmpty()) {
                Log.d(TAG, "init: tabs from bundled assets count=${bundledTabs.size} firstId=${bundledTabs.first().id}")
                bundledTabs
            } else {
                Log.w(TAG, "init: no bundled catalog — SampleCatalog tabs")
                HomeCatalogRepository.categoryTabs()
            }
            _uiState.update {
                it.copy(
                    homeTabs = tabs,
                    selectedHomeCategoryId = tabs.first().id,
                    homeItemsByCategoryId = emptyMap(),
                    padCatalogLoading = false,
                )
            }
            loadHomeCategoryItems(tabs.first().id)
        }
    }

    fun finishSplash() {
        val app = getApplication<Application>()
        AppFlowPreferences.setSplashDone(app)
        _uiState.update { it.copy(splashDone = true) }
    }

    fun selectLocaleForLanguagePicker(localeTag: String) {
        _uiState.update {
            it.copy(selectedLocaleTag = normalizeSupportedTag(localeTag))
        }
    }

    /**
     * Persists locale using the original app preference keys (`language_setting` / `key_language` /
     * `key_country`), applies AppCompat application locales (see hungvv.C4588dQ0 / C2536It0), which
     * recreates the activity when the locale changes.
     *
     * @return true if the locale value changed and AppCompat was applied (activity will recreate).
     */
    fun confirmLanguageSelection(): Boolean {
        val app = getApplication<Application>()
        val tag = normalizeSupportedTag(_uiState.value.selectedLocaleTag)
        val locale = localeForSupportedTag(tag)
        val old = AppLocalePreferences.getPersistedLocale(app)
        val localeChanged = old == null ||
            old.language != locale.language ||
            old.country.orEmpty() != locale.country.orEmpty()
        AppLocalePreferences.setPersistedLocale(app, locale)
        AppLocalePreferences.setLanguageFlowCompleted(app, true)
        if (localeChanged) {
            AppLocalePreferences.applyAppCompatFromPersistedLocales(app)
        }
        _uiState.update {
            it.copy(
                languageChosen = true,
                onboardingPage = 0,
                selectedLocaleTag = tag,
            )
        }
        return localeChanged
    }

    fun nextOnboardingPage() {
        _uiState.update { state ->
            val lastIndex = SampleCatalog.onboardingPages.lastIndex
            if (state.onboardingPage >= lastIndex) {
                AppFlowPreferences.setOnboardingDone(getApplication())
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
        AppFlowPreferences.setOnboardingDone(getApplication())
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

    fun postInfoMessage(message: String) {
        _uiState.update { it.copy(infoMessage = message) }
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
                val app = getApplication<Application>()
                val uuidCategory = isVolioCategoryId(categoryId)
                Log.d(TAG, "loadHomeCategoryItems: categoryId=$categoryId offlineStore=$uuidCategory")
                when {
                    !uuidCategory -> {
                        Log.d(TAG, "loadHomeCategoryItems: HomeCatalogRepository (sample ids)")
                        runCatching { HomeCatalogRepository.loadItemsForCategory(categoryId) }
                            .getOrElse { emptyList() }
                    }
                    else -> loadOfflineHomeStoreItems(app, categoryId)
                }
            }
            val first = items.firstOrNull()
            Log.d(
                TAG,
                "loadHomeCategoryItems: done categoryId=$categoryId count=${items.size} " +
                    "firstThumbBlank=${first?.thumbnailUrl.isNullOrBlank()} firstTitle=${first?.title}",
            )
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

    /**
     * Active editor tab (original app: `StatusBarCustomFragment` ViewPager page). Does not reload data;
     * Battery and Emoji share [statusBarCatalogItems]; Theme uses the same list for background `photo` URLs.
     */
    fun selectStatusTab(tab: StatusBarTab) {
        _uiState.update { it.copy(activeStatusBarTab = tab) }
    }

    /**
     * Offline store feed for the status-bar editor.
     * Uses PAD first, then bundled assets fallback (no Volio network).
     */
    fun loadStatusBarCatalog() {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val pendingCategoryId = savedStateHandle.get<String>(PENDING_HOME_STATUSBAR_CATEGORY_ID)
            val pendingSelectedId = savedStateHandle.get<String>(PENDING_HOME_STATUSBAR_SELECTED_ITEM_ID)
            val hasPendingHomeSelection = !pendingCategoryId.isNullOrBlank() && !pendingSelectedId.isNullOrBlank()
            val categoryId = pendingCategoryId?.takeIf { it.isNotBlank() }
                ?: _uiState.value.selectedHomeCategoryId.takeIf { it.isNotBlank() }
                ?: _uiState.value.homeTabs.firstOrNull()?.id
                ?: withContext(Dispatchers.IO) {
                    BundledVolioHomeRepository.fetchCategoryTabs(app).firstOrNull()?.id
                }
            if (categoryId == null) {
                Log.w(TAG, "loadStatusBarCatalog: no category id (home tabs empty, no bundled categories)")
                return@launch
            }
            val items = withContext(Dispatchers.IO) {
                if (isVolioCategoryId(categoryId)) {
                    loadOfflineHomeStoreItems(app, categoryId)
                } else {
                    // Status Bar editor fallback should use bundled default battery/icon assets,
                    // not sample text presets from HomeCatalogRepository.
                    emptyList()
                }
            }
            val resolvedItems = if (items.isNotEmpty()) items else defaultStatusBarCatalogItems(app)
            Log.d(
                TAG,
                "loadStatusBarCatalog: categoryId=$categoryId count=${items.size} " +
                    "resolvedCount=${resolvedItems.size} source=${if (items.isEmpty()) "default_assets" else "catalog"}",
            )
            _uiState.update { state ->
                var updated = state.copy(statusBarCatalogItems = resolvedItems)
                val fallbackSelectedId = resolvedItems.firstOrNull()?.id
                if (fallbackSelectedId != null) {
                    val hasBatterySelection = resolvedItems.any { it.id == state.editingConfig.batteryPresetId }
                    val hasEmojiSelection = resolvedItems.any { it.id == state.editingConfig.emojiPresetId }
                    if (!hasBatterySelection || !hasEmojiSelection) {
                        updated = updated.copy(
                            editingConfig = updated.editingConfig.copy(
                                batteryPresetId = if (hasBatterySelection) updated.editingConfig.batteryPresetId else fallbackSelectedId,
                                emojiPresetId = if (hasEmojiSelection) updated.editingConfig.emojiPresetId else fallbackSelectedId,
                            ),
                        )
                    }
                }
                if (hasPendingHomeSelection) {
                    val selected = resolvedItems.firstOrNull { it.id == pendingSelectedId } ?: resolvedItems.firstOrNull()
                    if (selected != null) {
                        updated = updated.copy(
                            editingConfig = updated.editingConfig.copy(
                                batteryPresetId = selected.id,
                                emojiPresetId = selected.id,
                            ),
                        )
                    }
                }
                updated
            }
            if (hasPendingHomeSelection) {
                savedStateHandle[PENDING_HOME_STATUSBAR_CATEGORY_ID] = null
                savedStateHandle[PENDING_HOME_STATUSBAR_SELECTED_ITEM_ID] = null
            }
        }
    }

    private fun defaultStatusBarCatalogItems(app: Application): List<HomeBatteryItem> {
        val packageName = app.packageName
        data class DefaultPair(val id: String, val batteryRes: Int, val emojiRes: Int)
        val pairs = listOf(
            DefaultPair("default_hai_trung_quoc_01_r1", R.drawable.default_battery_hai_trung_quoc_01_r1, R.drawable.default_icon_hai_trung_quoc_01_r1),
            DefaultPair("default_hai_trung_quoc_01_r2", R.drawable.default_battery_hai_trung_quoc_01_r2, R.drawable.default_icon_hai_trung_quoc_01_r2),
            DefaultPair("default_hai_trung_quoc_01_r3", R.drawable.default_battery_hai_trung_quoc_01_r3, R.drawable.default_icon_hai_trung_quoc_01_r3),
            DefaultPair("default_hai_trung_quoc_02_r1", R.drawable.default_battery_hai_trung_quoc_02_r1, R.drawable.default_icon_hai_trung_quoc_02_r1),
            DefaultPair("default_hai_trung_quoc_02_r2", R.drawable.default_battery_hai_trung_quoc_02_r2, R.drawable.default_icon_hai_trung_quoc_02_r2),
            DefaultPair("default_hai_trung_quoc_02_r3", R.drawable.default_battery_hai_trung_quoc_02_r3, R.drawable.default_icon_hai_trung_quoc_02_r3),
        )
        return pairs.mapIndexed { index, pair ->
            HomeBatteryItem(
                id = pair.id,
                categoryId = "default_status_bar",
                title = "Default ${index + 1}",
                previewRes = pair.batteryRes,
                thumbnailUrl = "android.resource://$packageName/drawable/${app.resources.getResourceEntryName(pair.batteryRes)}",
                batteryArtUrl = "android.resource://$packageName/drawable/${app.resources.getResourceEntryName(pair.batteryRes)}",
                emojiArtUrl = "android.resource://$packageName/drawable/${app.resources.getResourceEntryName(pair.emojiRes)}",
                premium = false,
                animated = false,
            )
        }
    }

    fun stageStatusBarSelectionFromHome(categoryId: String, selectedItemId: String) {
        if (categoryId.isBlank() || selectedItemId.isBlank()) return
        savedStateHandle[PENDING_HOME_STATUSBAR_CATEGORY_ID] = categoryId
        savedStateHandle[PENDING_HOME_STATUSBAR_SELECTED_ITEM_ID] = selectedItemId
    }

    fun clearStagedStatusBarSelectionFromHome() {
        savedStateHandle[PENDING_HOME_STATUSBAR_CATEGORY_ID] = null
        savedStateHandle[PENDING_HOME_STATUSBAR_SELECTED_ITEM_ID] = null
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

    /** Theme tab — background color row (original layoutColorBackground). */
    fun setThemeBackgroundColor(color: Long) = updateConfig {
        copy(
            backgroundColor = color,
            backgroundTemplatePhotoUrl = null,
            backgroundTemplateDrawableRes = null,
        )
    }

    /** Theme tab — remote / legacy template image URL (clears local drawable). */
    fun setBackgroundTemplatePhoto(url: String?) = updateConfig {
        copy(
            backgroundTemplatePhotoUrl = url?.takeIf { it.isNotBlank() },
            backgroundTemplateDrawableRes = null,
        )
    }

    /** Theme tab — local template drawable ([R.drawable.theme_bg_template_XX], original ColorTemplate list). */
    fun setBackgroundTemplateDrawable(drawableRes: Int?) = updateConfig {
        copy(
            backgroundTemplateDrawableRes = drawableRes?.takeIf { it != 0 },
            backgroundTemplatePhotoUrl = null,
        )
    }

    /** Percentage / accent color (see decompiled layout_choose_color_config — status bar editor). */
    fun setAccentColor(color: Long) = updateConfig { copy(accentColor = color) }

    fun setStatusBarHeight(value: Float) = updateConfig { copy(statusBarHeight = value) }

    fun setStatusBarLeftMargin(value: Float) = updateConfig { copy(leftMargin = value) }

    fun setStatusBarRightMargin(value: Float) = updateConfig { copy(rightMargin = value) }

    fun setBatteryPercentScale(value: Float) = updateConfig { copy(batteryPercentScale = value) }

    fun setEmojiScale(value: Float) = updateConfig { copy(emojiScale = value) }

    fun setEmojiAdjustmentScale(value: Float) = updateConfig { copy(emojiAdjustmentScale = value) }

    fun setEmojiOffset(offsetX: Float, offsetY: Float) = updateConfig {
        copy(
            emojiOffsetX = offsetX.coerceIn(0f, 1f),
            emojiOffsetY = offsetY.coerceIn(0f, 1f),
        )
    }

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

    /**
     * Apply: commits **editing** → **applied** config in one shot (original status-bar editor: one shared model;
     * the visible tab only changes which section is edited). Accessibility must be on for success; the UI layer
     * persists overlay prefs after this when the bridge is enabled.
     */
    fun applyConfig() {
        var appliedSuccessfully = false
        _uiState.update { state ->
            if (!state.accessibilityGranted) {
                state.copy(infoMessage = "Enable accessibility bridge before applying the status-bar icon.")
            } else {
                appliedSuccessfully = true
                state.copy(
                    appliedConfig = state.editingConfig,
                    infoMessage = APPLY_SUCCESS_MESSAGE,
                )
            }
        }
        if (appliedSuccessfully) {
            advanceAchievement("apply_status_bar")
        }
    }

    fun applyLegacyBatteryConfig() {
        _uiState.update { it.copy(appliedConfig = it.editingConfig, infoMessage = APPLY_SUCCESS_MESSAGE) }
        advanceAchievement("apply_status_bar")
    }

    fun refreshStickerCatalog() {
        stickerCatalogLoadJob?.cancel()
        stickerCatalogLoadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    stickerCatalogLoading = true,
                    stickerCatalogAppending = false,
                    stickerCatalogRemote = emptyList(),
                    stickerCatalogLoadedPageCount = 0,
                    stickerCatalogTotalPageCount = 0,
                )
            }
            val app = getApplication<Application>()
            val pageCount = withContext(Dispatchers.IO) {
                runCatching { VolioStickerRepository.stickerCatalogPageCount(app) }.getOrDefault(0)
            }
            val firstPage = withContext(Dispatchers.IO) {
                runCatching { VolioStickerRepository.fetchStickerPresetsPage(app, 0) }.getOrElse { emptyList() }
            }
            val withThumb = firstPage.count { !it.thumbnailUrl.isNullOrBlank() }
            val withLottie = firstPage.count { !it.lottieUrl.isNullOrBlank() }
            val emptyMedia = firstPage.count { it.thumbnailUrl.isNullOrBlank() && it.lottieUrl.isNullOrBlank() }
            Log.d(
                TAG,
                "refreshStickerCatalog: firstPageCount=${firstPage.size} pageCount=$pageCount withThumb=$withThumb withLottie=$withLottie emptyMedia=$emptyMedia",
            )
            firstPage.take(5).forEachIndexed { index, sticker ->
                Log.d(
                    TAG,
                    "refreshStickerCatalog: sample[$index] id=${sticker.id} name=${sticker.name} thumb=${sticker.thumbnailUrl} lottie=${sticker.lottieUrl}",
                )
            }
            _uiState.update {
                it.copy(
                    stickerCatalogRemote = firstPage,
                    stickerCatalogLoading = false,
                    stickerCatalogLoadedPageCount = if (firstPage.isEmpty()) 0 else 1,
                    stickerCatalogTotalPageCount = pageCount,
                )
            }
            if (pageCount <= 1) return@launch
            for (pageIndex in 1 until pageCount) {
                _uiState.update { it.copy(stickerCatalogAppending = true) }
                val pageItems = withContext(Dispatchers.IO) {
                    runCatching { VolioStickerRepository.fetchStickerPresetsPage(app, pageIndex) }.getOrElse { emptyList() }
                }
                _uiState.update {
                    it.copy(
                        stickerCatalogRemote = it.stickerCatalogRemote + pageItems,
                        stickerCatalogAppending = false,
                        stickerCatalogLoadedPageCount = pageIndex + 1,
                    )
                }
            }
        }
    }

    fun refreshBatteryTrollCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(batteryTrollCatalogLoading = true) }
            val app = getApplication<Application>()
            val result: Pair<List<BatteryTrollTemplate>, String> = withContext(Dispatchers.IO) {
                val fromPad = runCatching { PadVolioBatteryTrollRepository.fetchTemplates(app) }
                    .getOrElse { emptyList() }
                    .takeIf { it.isNotEmpty() }
                if (fromPad != null) {
                    fromPad to "pad"
                } else {
                    emptyList<BatteryTrollTemplate>() to "sample_fallback"
                }
            }
            val (list, source) = result
            Log.d(
                TAG,
                "refreshBatteryTrollCatalog: source=$source count=${list.size} parentIdSet=${VolioConstants.BATTERY_TROLL_PARENT_ID.isNotBlank()}",
            )
            _uiState.update {
                it.copy(
                    batteryTrollCatalogRemote = list,
                    batteryTrollCatalogLoading = false,
                )
            }
        }
    }

    fun addSticker(stickerId: String) {
        val sticker = _uiState.value.stickerPresetForId(stickerId) ?: return
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
                state.copy(
                    selectedStickerId = stickerId,
                    showStickerAdjustmentPanel = true,
                    infoMessage = "Sticker already added. Opened for editing.",
                )
            } else {
                state.copy(
                    stickerPlacements = state.stickerPlacements + StickerPlacement(stickerId = stickerId),
                    selectedStickerId = stickerId,
                    showStickerAdjustmentPanel = true,
                    infoMessage = "Sticker added.",
                )
            }
        }
    }

    fun selectSticker(stickerId: String) {
        _uiState.update { it.copy(selectedStickerId = stickerId, showStickerAdjustmentPanel = true) }
    }

    fun removeSticker(stickerId: String) {
        _uiState.update { state ->
            val updated = state.stickerPlacements.filterNot { it.stickerId == stickerId }
            state.copy(
                stickerPlacements = updated,
                selectedStickerId = updated.lastOrNull()?.stickerId,
                showStickerAdjustmentPanel = updated.isNotEmpty(),
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

    fun updateSelectedStickerRotation(value: Float) {
        updateSelectedSticker { copy(rotation = value.coerceIn(-180f, 180f)) }
    }

    fun nudgeSelectedSticker(deltaX: Float, deltaY: Float) {
        updateSelectedSticker {
            copy(
                offsetX = (offsetX + deltaX).coerceIn(0f, 1f),
                offsetY = (offsetY + deltaY).coerceIn(0f, 1f),
            )
        }
    }

    fun dismissStickerAdjustmentPanel() {
        _uiState.update { it.copy(showStickerAdjustmentPanel = false) }
    }

    fun saveStickerOverlay() {
        _uiState.update { state ->
            when {
                state.stickerPlacements.isEmpty() -> state.copy(infoMessage = "Please select at least one sticker.")
                !state.accessibilityGranted -> state.copy(infoMessage = "Enable accessibility bridge before saving sticker overlay.")
                else -> state.copy(
                    stickerOverlayEnabled = true,
                    showStickerAdjustmentPanel = false,
                infoMessage = APPLY_SUCCESS_MESSAGE,
                )
            }
        }
        advanceAchievement("save_sticker")
    }

    fun turnOffStickerOverlay() {
        _uiState.update {
            it.copy(
                stickerOverlayEnabled = false,
                showStickerAdjustmentPanel = false,
                infoMessage = "Sticker overlay turned off.",
            )
        }
    }

    fun setGestureEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(
                gestureEnabled = enabled,
                infoMessage = if (enabled) "Status-bar gestures enabled." else "Status-bar gestures disabled.",
            )
        }
        persistGestureSettings()
    }

    fun setVibrateFeedback(enabled: Boolean) {
        _uiState.update {
            it.copy(
                vibrateFeedback = enabled,
                infoMessage = if (enabled) "Vibrate feedback enabled." else "Vibrate feedback disabled.",
            )
        }
        persistGestureSettings()
    }

    fun setGestureAction(trigger: GestureTrigger, action: GestureAction) {
        _uiState.update { state ->
            state.copy(
                gestureActions = state.gestureActions + (trigger to action),
                infoMessage = "${trigger.title} mapped to ${action.title}.",
            )
        }
        persistGestureSettings()
        val mappedCount = _uiState.value.gestureActions.values.count { it != GestureAction.DoNothing }
        if (mappedCount >= 3) advanceAchievement("gesture_mapper")
    }

    private fun persistGestureSettings() {
        val state = _uiState.value
        GestureSettingsStore.write(
            getApplication(),
            state.gestureEnabled,
            state.vibrateFeedback,
            state.gestureActions,
        )
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
                infoMessage = APPLY_SUCCESS_MESSAGE,
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
                state.copy(infoMessage = APPLY_SUCCESS_MESSAGE)
            }
        }
        advanceAchievement("template_explorer")
    }

    fun selectBatteryTrollTemplate(templateId: String) {
        val template = _uiState.value.batteryTrollTemplateForId(templateId) ?: return
        val defaultEmoji = template.emojiOptionsUrls.firstOrNull()
            ?: template.emojiThumbnailUrl
            ?: template.thumbnailUrl
        val defaultBattery = template.batteryOptionsUrls.firstOrNull()
            ?: template.batteryThumbnailUrl
            ?: template.thumbnailUrl
        val resolvedMessage = resolveTrollMessage(template.prankMessage)
        _uiState.update {
            it.copy(
                selectedBatteryTrollTemplateId = templateId,
                trollMessage = resolvedMessage,
                trollSelectedEmojiUrl = defaultEmoji,
                trollSelectedBatteryUrl = defaultBattery,
            )
        }
    }

    fun selectBatteryTrollEmoji(url: String) {
        _uiState.update { it.copy(trollSelectedEmojiUrl = url) }
    }

    fun selectBatteryTrollBattery(url: String) {
        _uiState.update { it.copy(trollSelectedBatteryUrl = url) }
    }

    fun setBatteryTrollEnabled(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                trollFeatureEnabled = enabled,
                trollOverlayEnabled = if (enabled) state.trollOverlayEnabled else false,
                infoMessage = if (enabled) "Battery Troll enabled." else "Battery Troll disabled.",
            )
        }
    }

    fun setStatusBarOverlayEnabled(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                statusBarOverlayEnabled = enabled,
                trollOverlayEnabled = if (enabled) state.trollOverlayEnabled else false,
                infoMessage = if (enabled) "Emoji battery overlay enabled." else "Emoji battery overlay disabled.",
            )
        }
    }

    fun setBatteryTrollUseRealBattery(useRealBattery: Boolean) {
        _uiState.update { it.copy(trollUseRealBattery = useRealBattery) }
    }

    fun setTrollMessage(message: String) {
        _uiState.update { it.copy(trollMessage = message) }
    }

    private fun resolveTrollMessage(rawMessage: String?): String {
        val value = rawMessage?.trim().orEmpty()
        if (value.isBlank()) return "999%"
        val normalized = value.lowercase()
            .replace("_", "")
            .replace("-", "")
            .replace(" ", "")
        // Volio templates often have message placeholders like "Battery6", "Battery 6", or "battery6emotion05".
        if (normalized.matches(Regex("^battery\\d+([a-z]+\\d*)*\$"))) return "999%"
        return value
    }

    fun setTrollShowPercentage(enabled: Boolean) {
        _uiState.update { it.copy(trollShowPercentage = enabled) }
    }

    fun setTrollPercentageSizeDp(value: Int) {
        _uiState.update { it.copy(trollPercentageSizeDp = value.coerceIn(5, 40)) }
    }

    fun setTrollEmojiSizeDp(value: Int) {
        _uiState.update { it.copy(trollEmojiSizeDp = value.coerceIn(1, 50)) }
    }

    fun setTrollRandomizedMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                trollRandomizedMode = enabled,
                trollAutoDrop = enabled,
            )
        }
    }

    fun setTrollShowEmoji(enabled: Boolean) {
        _uiState.update { it.copy(trollShowEmoji = enabled) }
    }

    fun setTrollAutoDrop(enabled: Boolean) {
        _uiState.update {
            it.copy(
                trollAutoDrop = enabled,
                trollRandomizedMode = enabled,
            )
        }
    }

    fun applyBatteryTroll() {
        _uiState.update { state ->
            if (!state.accessibilityGranted) {
                state.copy(infoMessage = "Enable accessibility bridge before applying a Battery Troll overlay.")
            } else if (!state.statusBarOverlayEnabled) {
                state.copy(
                    trollOverlayEnabled = false,
                    infoMessage = "Enable emoji battery overlay first.",
                )
            } else if (!state.trollFeatureEnabled) {
                state.copy(
                    trollOverlayEnabled = false,
                    infoMessage = "Battery Troll is disabled.",
                )
            } else {
                state.copy(
                    trollOverlayEnabled = true,
                    infoMessage = APPLY_SUCCESS_MESSAGE,
                )
            }
        }
        advanceAchievement("template_explorer")
    }

    fun turnOffBatteryTroll() {
        _uiState.update {
            it.copy(
                trollFeatureEnabled = false,
                trollOverlayEnabled = false,
                infoMessage = "Battery Troll turned off.",
            )
        }
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

    fun openStore() {
        _uiState.update {
            it.copy(
                paywallState = PaywallState(
                    featureKey = "settings:store",
                    title = "Unlock Premium",
                    message = "Upgrade to premium to unlock all features.",
                ),
            )
        }
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
                backgroundTemplatePhotoUrl = null,
                backgroundTemplateDrawableRes = null,
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

    private suspend fun loadOfflineHomeStoreItems(
        app: Application,
        categoryId: String,
    ): List<HomeBatteryItem> {
        val packName = HomeCategoryPackResolver.packNameFor(categoryId)
        val allowBundledFallback = _uiState.value.homeTabs
            .firstOrNull { it.id == categoryId }
            ?.title
            ?.trim()
            ?.lowercase()
            ?.let { it in HOME_BUNDLED_FALLBACK_CATEGORY_TITLES }
            ?: false
        val padItems = runCatching { PadVolioHomeRepository.fetchItemsForCategory(app, categoryId) }
            .getOrElse { emptyList() }
            .takeIf { it.isNotEmpty() }
        val merged = padItems
            ?: if (allowBundledFallback) {
                runCatching { BundledVolioHomeRepository.fetchItemsForCategory(app, categoryId) }
                    .getOrElse { emptyList() }
                    .takeIf { it.isNotEmpty() }
            } else {
                null
            }
            ?: emptyList()
        when {
            padItems != null -> Log.d(TAG, "offlineStore: items from PAD count=${merged.size} pack=$packName")
            merged.isNotEmpty() -> Log.d(
                TAG,
                "offlineStore: items from bundled assets count=${merged.size} category=$categoryId",
            )
            else -> Log.w(TAG, "offlineStore: no PAD/bundled items for $categoryId")
        }
        return HomeStoreLocalImageResolver.enrichItems(app, merged, packName = packName)
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
