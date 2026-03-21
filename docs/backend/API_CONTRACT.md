# API And Contract

## Scope

This document defines the local request/response contract between app layers.

There is no HTTP API. All contracts here are in-process contracts.

## 1. Activity Contract

### `MainActivity`

Input:

- intent extra `route: String?`

Output:

- `EmojiBatteryApp(initialRoute = route)`

Behavior:

- if `route == null`, app starts from splash
- if `route != null`, app starts directly at that screen

## 2. Accessibility Contract

### `AccessibilityBridge.isEnabled(context)`

Request:

- `Context`

Response:

- `Boolean`

Semantics:

- `true` if `OverlayAccessibilityService` is present in enabled accessibility services
- `false` otherwise

### `AccessibilityBridge.openSettings(context)`

Request:

- `Context`

Response:

- launches system Accessibility Settings

## 3. ViewModel Contract

The ViewModel accepts imperative requests and returns updated `AppUiState`.

### 3.1 Apply main status-bar config

Request:

- `applyConfig()`

Success response:

- `appliedConfig = editingConfig`
- `infoMessage = "Configuration applied successfully."`
- increments achievement `apply_status_bar`

Failure response:

- if accessibility disabled:
  - `appliedConfig` unchanged
  - `infoMessage = "Enable accessibility bridge before applying the status-bar icon."`

### 3.2 Add sticker

Request:

- `addSticker(stickerId: String)`

Success response:

- sticker appended to `stickerPlacements`
- `selectedStickerId = stickerId`
- `infoMessage = "Sticker added."`

Already exists response:

- does not duplicate sticker
- selects existing sticker

Locked response: premium sticker

- `paywallState = PaywallState(featureKey = "sticker:<id>", ...)`

Locked response: slot limit

- `paywallState = PaywallState(featureKey = "slot:extra", ...)`

### 3.3 Select real-time template

Request:

- `selectRealTimeTemplate(templateId: String)`

Success response:

- `selectedRealTimeTemplateId = templateId`

Locked response:

- if template is premium and not entitled:
  - `paywallState = PaywallState(featureKey = "template:<id>", ...)`

### 3.4 Apply real-time template

Request:

- `applyRealTimeTemplate()`

Success response:

- `infoMessage = "Real Time template '...' prepared and applied."`
- increments achievement `template_explorer`

Failure response:

- if accessibility disabled:
  - `infoMessage = "Enable accessibility bridge before applying a Real Time template."`

### 3.5 Apply battery troll

Request:

- `applyBatteryTroll()`

Success response:

- `trollOverlayEnabled = true`
- info message updated
- increments achievement `template_explorer`

Failure response:

- if accessibility disabled:
  - info message updated

### 3.6 Save sticker overlay

Request:

- `saveStickerOverlay()`

Success response:

- `stickerOverlayEnabled = true`
- increments achievement `save_sticker`

Failure responses:

- if no sticker:
  - info message updated
- if accessibility disabled:
  - info message updated

### 3.7 Claim achievement

Request:

- `claimAchievement(taskId: String)`

Success response:

- target task becomes `claimed = true`
- reward-based entitlements may be added
- info message updated

Reward contract:

- `save_sticker`
  - adds `slot:extra`
- `template_explorer`
  - adds `template:cat_diary`

### 3.8 Paywall actions

#### `dismissPaywall()`

Response:

- `paywallState = null`

#### `unlockPremium()`

Response:

- `premiumUnlocked = true`
- `paywallState = null`
- `infoMessage = "Premium access unlocked locally."`

## 4. Persistence Contract

### 4.1 `OverlayConfigStore.saveStatusBarConfig(context, config)`

Request:

- `Context`
- `BatteryIconConfig`

Transformation:

- resolves battery preset
- resolves emoji preset
- writes:
  - `status_enabled = true`
  - `battery_text = "<body> <glyph>"`
  - `accent`
  - `background`

Response:

- no return value
- persistent storage updated

### 4.2 `OverlayConfigStore.saveStickerOverlay(context, uiState)`

Request:

- `Context`
- `AppUiState`

Transformation:

- chooses `selectedStickerId` or last sticker
- resolves sticker glyph
- writes:
  - `sticker_enabled = true`
  - `sticker_glyph = <glyph>`

### 4.3 `OverlayConfigStore.saveBatteryTroll(context, uiState)`

Writes:

- `troll_enabled = true`
- `troll_message = <message>`

### 4.4 `OverlayConfigStore.saveRealTime(context, templateId)`

Writes:

- `realtime_enabled = true`
- `realtime_glyph = <glyph>`
- `realtime_title = <title>`

### 4.5 `OverlayConfigStore.read(context)`

Response type:

- `OverlaySnapshot`

Defaulting behavior:

- fills missing values from app defaults

## 5. Refresh Contract

### `OverlayAccessibilityService.requestRefresh(context)`

Request:

- `Context`

Transport:

- app-local broadcast

Action:

- `dev.hai.emojibattery.action.REFRESH_OVERLAY`

Response:

- if service is alive, refresh receiver triggers `refreshOverlay()`

## 6. Service Contract

### 6.1 `OverlayAccessibilityService.refreshOverlay()`

Input:

- persisted `OverlaySnapshot`
- live runtime status

Output:

- `StatusBarOverlayManager.render(snapshot, liveStatus)`

### 6.2 Live listeners

Battery input:

- `ACTION_BATTERY_CHANGED`

Connectivity input:

- `CONNECTIVITY_ACTION`
- `ACTION_AIRPLANE_MODE_CHANGED`
- wifi state actions

Signal input:

- `PhoneStateListener.LISTEN_SIGNAL_STRENGTHS`

Time input:

- `TIME_CHANGED`
- `TIMEZONE_CHANGED`
- `DATE_CHANGED`
- `TIME_TICK`

## 7. Entitlement Contract

Entitlement source fields:

- `premiumUnlocked`
- `unlockedFeatureKeys`

Current feature keys:

- `slot:extra`
- `template:cat_diary`
- runtime paywall targets:
  - `sticker:<id>`
  - `template:<id>`

Access rules:

- all premium content is accessible when `premiumUnlocked == true`
- reward unlock can grant access to specific features without global premium

## 8. UI Contract

UI must follow this rule:

- if a ViewModel action can produce a paywall, it should always call the ViewModel first
- do not bypass ViewModel lock checks and mutate premium state directly in UI
