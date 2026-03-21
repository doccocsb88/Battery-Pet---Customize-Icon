# Backend And Data Flow Document

## 1. Scope

This document describes the current backend of the Kotlin port in this repository.

The word "backend" here means the non-UI execution stack inside the Android app:

- app state management
- persistence
- accessibility-service integration
- overlay rendering
- live-update listeners
- request/response contracts between layers

This is not a server backend. There is no remote API in the current implementation.

Primary source files:

- `app/src/main/java/dev/hai/emojibattery/MainActivity.kt`
- `app/src/main/java/dev/hai/emojibattery/app/EmojiBatteryViewModel.kt`
- `app/src/main/java/dev/hai/emojibattery/model/AppModels.kt`
- `app/src/main/java/dev/hai/emojibattery/service/AccessibilityBridge.kt`
- `app/src/main/java/dev/hai/emojibattery/service/OverlayConfigStore.kt`
- `app/src/main/java/dev/hai/emojibattery/service/OverlayAccessibilityService.kt`
- `app/src/main/java/dev/hai/emojibattery/service/StatusBarOverlayManager.kt`
- `app/src/main/AndroidManifest.xml`

## 2. High-level Architecture

The runtime stack is:

1. `MainActivity`
2. `EmojiBatteryApp` composable shell
3. `EmojiBatteryViewModel`
4. local state in `AppUiState`
5. persistence through `OverlayConfigStore`
6. refresh signal through `OverlayAccessibilityService.ACTION_REFRESH`
7. `OverlayAccessibilityService`
8. `StatusBarOverlayManager`
9. Android `WindowManager` overlay

In short:

`UI event -> ViewModel mutation -> optional persistence -> refresh broadcast -> AccessibilityService -> live state collection -> render overlay`

## 3. Main Runtime Components

### 3.1 `MainActivity`

Responsibility:

- app entry point
- reads optional route override from intent extra `route`
- mounts Compose app and ViewModel

Input:

- Android launch intent
- optional extra:
  - `route: String?`

Output:

- starts the composable app tree with `initialRoute`

Used for:

- normal app launch
- screenshot automation
- direct route testing

### 3.2 `EmojiBatteryApp`

Responsibility:

- binds UI routes to `EmojiBatteryViewModel`
- decides when to call service/persistence actions
- syncs accessibility status on resume

Important side-effect points:

- on apply status-bar config:
  - `viewModel.applyConfig()`
  - `OverlayConfigStore.saveStatusBarConfig(...)`
  - `OverlayAccessibilityService.requestRefresh(...)`
- on save sticker:
  - `viewModel.saveStickerOverlay()`
  - `OverlayConfigStore.saveStickerOverlay(...)`
  - `OverlayAccessibilityService.requestRefresh(...)`
- on apply battery troll:
  - `viewModel.applyBatteryTroll()`
  - `OverlayConfigStore.saveBatteryTroll(...)`
  - `OverlayAccessibilityService.requestRefresh(...)`
- on apply real time:
  - `viewModel.applyRealTimeTemplate()`
  - `OverlayConfigStore.saveRealTime(...)`
  - `OverlayAccessibilityService.requestRefresh(...)`

### 3.3 `EmojiBatteryViewModel`

Responsibility:

- owns in-memory app state
- exposes feature actions to UI
- validates preconditions
- updates achievements
- emits user-facing info messages

Important constraint:

- it does not directly talk to `WindowManager`
- it does not directly mount overlays
- it primarily returns "response" as state changes in `AppUiState`

### 3.4 `OverlayConfigStore`

Responsibility:

- persists overlay-related data to `SharedPreferences`
- converts app models to a compact overlay snapshot

Storage backend:

- `SharedPreferences`
- file name:
  - `overlay_config`

### 3.5 `AccessibilityBridge`

Responsibility:

- checks whether the accessibility service is enabled
- opens system Accessibility Settings

### 3.6 `OverlayAccessibilityService`

Responsibility:

- runs as Android `AccessibilityService`
- owns the live-update listener stack
- reads persisted overlay snapshot
- merges persisted snapshot with live system status
- delegates final rendering to `StatusBarOverlayManager`

### 3.7 `StatusBarOverlayManager`

Responsibility:

- mounts a real overlay view into `WindowManager`
- maintains overlay view instances
- renders:
  - status bar
  - sticker overlay
  - battery troll overlay
  - real time overlay

## 4. Core Data Models

### 4.1 `BatteryIconConfig`

Purpose:

- editable and applied configuration for the main status-bar customization flow

Fields:

- `batteryPresetId: String`
- `emojiPresetId: String`
- `themePresetId: String`
- `batteryPercentScale: Float`
- `emojiScale: Float`
- `showPercentage: Boolean`
- `animateCharge: Boolean`
- `showStroke: Boolean`
- `accentColor: Long`
- `backgroundColor: Long`

### 4.2 `AppUiState`

Purpose:

- single top-level state container used by the app

Major groups:

- boot and navigation:
  - `splashDone`
  - `languageChosen`
  - `selectedLanguage`
  - `activeMainSection`
  - `activeStatusBarTab`
- permission:
  - `accessibilityGranted`
- main editor:
  - `editingConfig`
  - `appliedConfig`
- isolated feature editors:
  - `featureConfigs`
- sticker flow:
  - `stickerPlacements`
  - `selectedStickerId`
  - `stickerOverlayEnabled`
- gesture flow:
  - `gestureEnabled`
  - `vibrateFeedback`
  - `gestureActions`
- search:
  - `searchQuery`
- content overlays:
  - `selectedRealTimeTemplateId`
  - `selectedBatteryTrollTemplateId`
  - `trollMessage`
  - `trollAutoDrop`
  - `trollOverlayEnabled`
- settings and achievements:
  - `tutorialCompleted`
  - `protectFromRecentApps`
  - `achievements`
  - `infoMessage`

### 4.3 `OverlaySnapshot`

Purpose:

- compact persisted representation of overlay state
- loaded by the service at render time

Fields:

- `statusBarEnabled: Boolean`
- `batteryText: String`
- `accentColor: Long`
- `backgroundColor: Long`
- `stickerEnabled: Boolean`
- `stickerGlyph: String`
- `trollEnabled: Boolean`
- `trollMessage: String`
- `realTimeEnabled: Boolean`
- `realTimeGlyph: String`
- `realTimeTitle: String`

### 4.4 `StatusBarOverlayManager.LiveStatus`

Purpose:

- volatile runtime system state
- not persisted
- recomputed from Android callbacks and listeners

Fields:

- `batteryPercent: Int`
- `charging: Boolean`
- `wifiEnabled: Boolean`
- `mobileConnected: Boolean`
- `airplaneMode: Boolean`
- `signalLevel: Int`

## 5. Persistent Storage Contract

Storage location:

- `SharedPreferences("overlay_config")`

Keys:

- `status_enabled`
- `battery_text`
- `accent`
- `background`
- `sticker_enabled`
- `sticker_glyph`
- `troll_enabled`
- `troll_message`
- `realtime_enabled`
- `realtime_glyph`
- `realtime_title`

Write rules:

- status-bar apply writes:
  - `status_enabled`
  - `battery_text`
  - `accent`
  - `background`
- sticker save writes:
  - `sticker_enabled`
  - `sticker_glyph`
- sticker turn-off writes:
  - `sticker_enabled = false`
- battery troll apply writes:
  - `troll_enabled`
  - `troll_message`
- battery troll turn-off writes:
  - `troll_enabled = false`
- real time apply writes:
  - `realtime_enabled`
  - `realtime_glyph`
  - `realtime_title`

Important note:

- there is currently no explicit "clear status bar config" method
- persisted status-bar state stays active until overwritten

## 6. Android Manifest Contract

Declared permissions:

- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.ACCESS_WIFI_STATE`
- `android.permission.READ_PHONE_STATE`

Declared service:

- `.service.OverlayAccessibilityService`

Service requirements:

- `android.permission.BIND_ACCESSIBILITY_SERVICE`
- accessibility metadata:
  - `@xml/accessibility_service_config`

Meaning:

- the app depends on the user enabling the accessibility service from system settings
- apply operations are gated by this requirement

## 7. Inter-layer Request/Response Contracts

This section describes "request" and "response" in local app terms.

### 7.1 UI -> ViewModel

The UI sends function calls.
The ViewModel returns responses as updated `AppUiState`.

#### Request: choose language

Request:

- `chooseLanguage(language: String)`

Response:

- `languageChosen = true`
- `selectedLanguage = language`
- `infoMessage = "Language set to ..."`

#### Request: apply status-bar config

Request:

- `applyConfig()`

Response when accessibility disabled:

- `appliedConfig` unchanged
- `infoMessage = "Enable accessibility bridge before applying the status-bar icon."`

Response when accessibility enabled:

- `appliedConfig = editingConfig`
- `infoMessage = "Configuration applied successfully."`
- achievement `apply_status_bar` incremented

#### Request: save sticker overlay

Request:

- `saveStickerOverlay()`

Response when no sticker selected:

- `infoMessage = "Please select at least one sticker."`

Response when accessibility disabled:

- `infoMessage = "Enable accessibility bridge before saving sticker overlay."`

Response when valid:

- `stickerOverlayEnabled = true`
- `infoMessage = "Sticker overlay saved."`
- achievement `save_sticker` incremented

#### Request: apply real time template

Request:

- `applyRealTimeTemplate()`

Response when accessibility disabled:

- `infoMessage = "Enable accessibility bridge before applying a Real Time template."`

Response when valid:

- `infoMessage = "Real Time template '...' prepared and applied."`
- achievement `template_explorer` incremented

#### Request: apply battery troll

Request:

- `applyBatteryTroll()`

Response when accessibility disabled:

- `infoMessage = "Enable accessibility bridge before applying a Battery Troll overlay."`

Response when valid:

- `trollOverlayEnabled = true`
- `infoMessage = "Battery Troll overlay applied with message '...'."`
- achievement `template_explorer` incremented

#### Request: set gesture action

Request:

- `setGestureAction(trigger, action)`

Response:

- updates `gestureActions[trigger]`
- emits info message
- may advance achievement `gesture_mapper`

### 7.2 UI -> AccessibilityBridge

#### Request: check accessibility enabled

Request:

- `AccessibilityBridge.isEnabled(context)`

Input:

- current app `Context`

Internal logic:

- reads `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`
- compares registered components against:
  - flattened component name
  - fully qualified class name

Response:

- `Boolean`

#### Request: open accessibility settings

Request:

- `AccessibilityBridge.openSettings(context)`

Response:

- launches `Settings.ACTION_ACCESSIBILITY_SETTINGS`

### 7.3 UI -> OverlayConfigStore

These are persistence requests.

#### Request: save main status-bar config

Request:

- `saveStatusBarConfig(context, config: BatteryIconConfig)`

Transformation:

- resolves `batteryPresetId` -> `BatteryPreset`
- resolves `emojiPresetId` -> `EmojiPreset`
- stores compact `batteryText = "<battery.body> <emoji.glyph>"`
- stores accent and background colors

Response:

- persisted `SharedPreferences` values updated
- no return value

#### Request: save sticker overlay

Request:

- `saveStickerOverlay(context, uiState: AppUiState)`

Transformation:

- chooses target sticker from:
  - `selectedStickerId`
  - fallback: last sticker in `stickerPlacements`
- resolves to `StickerPreset`
- stores only the glyph and enabled flag

Response:

- persisted sticker overlay state updated

#### Request: save battery troll

Request:

- `saveBatteryTroll(context, uiState: AppUiState)`

Response:

- persisted prank message and enabled flag updated

#### Request: save real time

Request:

- `saveRealTime(context, templateId: String)`

Transformation:

- resolves template from `SampleCatalog.realTimeTemplates`
- stores:
  - `accentGlyph`
  - `title`
  - enabled flag

Response:

- persisted real-time overlay state updated

#### Request: read overlay snapshot

Request:

- `read(context)`

Response:

- returns `OverlaySnapshot`
- merges defaults where no persisted value exists

### 7.4 UI -> OverlayAccessibilityService

This happens indirectly through broadcast.

#### Request: refresh overlay

Request:

- `OverlayAccessibilityService.requestRefresh(context)`

Implementation:

- sends broadcast:
  - action:
    - `dev.hai.emojibattery.action.REFRESH_OVERLAY`
  - package constrained to current app

Response:

- if service is running, `refreshReceiver` receives broadcast
- service recomputes render using:
  - persisted `OverlaySnapshot`
  - live `LiveStatus`

### 7.5 OverlayAccessibilityService -> StatusBarOverlayManager

#### Request: render overlay

Request:

- `render(snapshot: OverlaySnapshot, liveStatus: LiveStatus)`

Response:

- updates mounted overlay views
- or hides root alpha when nothing is enabled

## 8. End-to-end Feature Flows

### 8.1 Main status-bar apply flow

1. User edits battery/emoji/theme/settings in UI.
2. `EmojiBatteryViewModel.editingConfig` changes in memory.
3. User taps `Apply`.
4. App checks `AccessibilityBridge.isEnabled(context)`.
5. `viewModel.applyConfig()` validates state.
6. If enabled:
   - `OverlayConfigStore.saveStatusBarConfig(...)`
   - `OverlayAccessibilityService.requestRefresh(...)`
7. Service receives refresh broadcast.
8. Service reads persisted `OverlaySnapshot`.
9. Service merges live runtime status:
   - battery
   - charging
   - wifi
   - mobile
   - airplane
   - signal
10. `StatusBarOverlayManager.render(...)` updates overlay UI.

### 8.2 Sticker overlay flow

1. User adds stickers into `stickerPlacements`.
2. User selects a sticker and tweaks size/speed.
3. User taps `Save`.
4. ViewModel validates:
   - at least one sticker exists
   - accessibility enabled
5. If valid:
   - `OverlayConfigStore.saveStickerOverlay(...)`
   - `OverlayAccessibilityService.requestRefresh(...)`
6. Service re-renders overlay.
7. `stickerView` becomes visible if `stickerEnabled = true`.

### 8.3 Battery troll flow

1. User picks template and message.
2. User taps `Apply`.
3. ViewModel validates accessibility.
4. `OverlayConfigStore.saveBatteryTroll(...)`
5. Broadcast refresh sent.
6. Service reads snapshot.
7. `trollView` becomes visible with:
   - `Fake <message>`

### 8.4 Real time flow

1. User picks template from `realTimeTemplates`.
2. User taps `Apply Template`.
3. ViewModel validates accessibility.
4. `OverlayConfigStore.saveRealTime(...)`
5. Broadcast refresh sent.
6. Service reads snapshot.
7. `realtimeView` becomes visible with:
   - `<glyph> <title>`

### 8.5 Accessibility sync flow

1. App resumes.
2. `EmojiBatteryApp` runs `AccessibilityBridge.isEnabled(context)`.
3. Result is pushed into `viewModel.syncAccessibilityGranted(...)`.
4. UI permission banner reflects current service state.

### 8.6 Screenshot/test route flow

1. External caller starts `MainActivity` with extra:
   - `route`
2. `MainActivity` passes `initialRoute` into `EmojiBatteryApp`.
3. App launches directly into a target screen.
4. Used by screenshot automation and route validation.

## 9. Live-update Listener Stack

The service maintains a split between persisted overlay data and live system data.

### 9.1 Battery receiver

Input event:

- `Intent.ACTION_BATTERY_CHANGED`

Request payload from Android:

- `BatteryManager.EXTRA_LEVEL`
- `BatteryManager.EXTRA_SCALE`
- `BatteryManager.EXTRA_STATUS`

Derived response:

- `batteryPercent`
- `charging`

### 9.2 Connectivity receiver

Input events:

- `ConnectivityManager.CONNECTIVITY_ACTION`
- `Intent.ACTION_AIRPLANE_MODE_CHANGED`
- `"android.net.wifi.WIFI_AP_STATE_CHANGED"`
- `"android.net.wifi.WIFI_STATE_CHANGED"`

Derived response:

- re-evaluates:
  - wifi transport
  - mobile transport
  - airplane mode

### 9.3 Signal listener

Input event:

- `PhoneStateListener.onSignalStrengthsChanged(...)`

Request payload from Android:

- `SignalStrength`

Derived response:

- `signalLevel = signalStrength.level`

### 9.4 Time receiver

Input events:

- `Intent.ACTION_TIME_CHANGED`
- `Intent.ACTION_TIMEZONE_CHANGED`
- `Intent.ACTION_DATE_CHANGED`
- `Intent.ACTION_TIME_TICK`

Derived response:

- forces overlay refresh
- `TextClock` and date `TextClock` keep visible clock/date aligned with system time

## 10. Overlay Rendering Contract

### 10.1 Mounting

Overlay type:

- `TYPE_ACCESSIBILITY_OVERLAY` on Android O+
- fallback:
  - `TYPE_SYSTEM_ALERT`

Window flags:

- `FLAG_NOT_FOCUSABLE`
- `FLAG_NOT_TOUCHABLE`
- `FLAG_LAYOUT_IN_SCREEN`

Meaning:

- overlay is display-only
- overlay does not capture user input

### 10.2 Render decision tree

If all are disabled:

- `statusBarEnabled = false`
- `stickerEnabled = false`
- `trollEnabled = false`
- `realTimeEnabled = false`

Response:

- `root.alpha = 0f`

Otherwise:

- `root.alpha = 1f`
- status row and auxiliary overlay views are updated

### 10.3 Status row response shape

Rendered parts:

- left cluster:
  - `clockView`
  - `dateView`
- right cluster:
  - `wifiView`
  - `signalView`
  - `batteryView`

Text rules:

- `batteryView`:
  - `"<batteryText> <batteryPercent>%<charging marker>"`
- `wifiView`:
  - `AIR` when airplane mode is on
  - `WIFI` when wifi transport is active
  - `LTE` when cellular transport is active
  - `OFF` otherwise
- `signalView`:
  - bar glyph derived from `signalLevel`
  - hidden when airplane mode is on

Auxiliary overlay views:

- `stickerView`
- `trollView`
- `realtimeView`

## 11. Request/Response Examples

### 11.1 Example request: apply status-bar editor

Request from UI:

```kotlin
BatteryIconConfig(
    batteryPresetId = "pill",
    emojiPresetId = "spark",
    themePresetId = "blush",
    batteryPercentScale = 0.56f,
    emojiScale = 0.64f,
    showPercentage = true,
    animateCharge = true,
    showStroke = true,
    accentColor = 0xFFEA6A9AL,
    backgroundColor = 0xFFFFF3F8L,
)
```

Persisted response:

```kotlin
OverlaySnapshot(
    statusBarEnabled = true,
    batteryText = "▰▰▰▱ ✨",
    accentColor = 0xFFEA6A9AL,
    backgroundColor = 0xFFFFF3F8L,
    stickerEnabled = false,
    stickerGlyph = "✨",
    trollEnabled = false,
    trollMessage = "999",
    realTimeEnabled = false,
    realTimeGlyph = "⚡",
    realTimeTitle = "Real Time",
)
```

Live response at render time:

```kotlin
StatusBarOverlayManager.LiveStatus(
    batteryPercent = 78,
    charging = true,
    wifiEnabled = true,
    mobileConnected = false,
    airplaneMode = false,
    signalLevel = 3,
)
```

Final visual response:

- time/date shown on left
- `WIFI`
- signal bars `▰▰▰▱`
- battery text `▰▰▰▱ ✨ 78% +`

### 11.2 Example request: save sticker overlay

Request:

```kotlin
uiState.selectedStickerId = "sparkle_cat"
```

Persisted response:

```kotlin
stickerEnabled = true
stickerGlyph = "🐱"
```

Render response:

- `stickerView.text = "🐱"`
- `stickerView.visibility = VISIBLE`

### 11.3 Example request: apply battery troll

Request:

```kotlin
uiState.trollMessage = "1%"
```

Persisted response:

```kotlin
trollEnabled = true
trollMessage = "1%"
```

Render response:

- `trollView.text = "Fake 1%"`
- `trollView.visibility = VISIBLE`

## 12. Failure And Guard Conditions

### 12.1 Accessibility disabled

Impact:

- status-bar apply is blocked
- sticker save is blocked
- real time apply is blocked
- battery troll apply is blocked

User-facing response:

- ViewModel writes a descriptive `infoMessage`

### 12.2 Empty sticker selection

Impact:

- sticker overlay save is blocked

User-facing response:

- `infoMessage = "Please select at least one sticker."`

### 12.3 Unknown template id

Impact:

- store write is skipped
- method returns without changes

Applies to:

- `saveRealTime`
- sticker/template lookups
- theme selection

## 13. Known Gaps

Current implementation gaps relative to the decompiled original:

- no remote backend
- no full clone of original `service/a.java`
- no full asset/layout matrix for every icon type
- no complete receiver stack for all original status-bar subfeatures
- no persisted gesture backend execution layer yet
- some UI state and service state sync during cold route launches can still be imperfect

What is already strong enough for current architecture work:

- state ownership is centralized
- persistence contract is clear
- refresh broadcast contract is explicit
- live system merge path is implemented
- overlay rendering path is deterministic

## 14. Suggested Next Backend Extensions

If this app continues to evolve, the next backend steps should be:

1. split overlay snapshot into feature-specific snapshots instead of one flat structure
2. persist gesture mappings in storage, not memory only
3. add explicit clear/reset methods for status bar and real time
4. replace deprecated connectivity and phone APIs where needed
5. introduce repository classes so `ViewModel` no longer directly depends on static catalog/store objects
6. add structured logging for:
   - apply requests
   - persistence writes
   - service refresh cycles
   - render decisions
