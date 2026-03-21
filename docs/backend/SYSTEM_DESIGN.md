# System Design

## Overview

The app uses a local-only architecture.

There are 4 main layers:

1. UI layer
2. State layer
3. Persistence and entitlement layer
4. Overlay execution layer

There is no remote backend service.

## Main Components

### 1. `MainActivity`

Responsibilities:

- Android entry point
- reads optional route override from intent extra `route`
- mounts Compose app and `EmojiBatteryViewModel`

### 2. `EmojiBatteryApp`

Responsibilities:

- navigation shell
- binds screens to ViewModel actions
- calls accessibility settings bridge
- persists overlay config through `OverlayConfigStore`
- sends refresh request to `OverlayAccessibilityService`
- renders paywall dialog when lock conditions fail

### 3. `EmojiBatteryViewModel`

Responsibilities:

- owns `AppUiState`
- mutates editing state
- validates apply conditions
- enforces lock conditions
- handles local premium unlock
- handles reward-based unlocks
- tracks achievements

### 4. `OverlayConfigStore`

Responsibilities:

- writes overlay-relevant state into `SharedPreferences`
- reads compact `OverlaySnapshot`

### 5. `AccessibilityBridge`

Responsibilities:

- checks if the accessibility service is enabled
- opens system Accessibility Settings

### 6. `OverlayAccessibilityService`

Responsibilities:

- receives refresh broadcasts
- owns the live listener stack
- merges persisted overlay snapshot with live system state
- delegates final drawing to `StatusBarOverlayManager`

### 7. `StatusBarOverlayManager`

Responsibilities:

- mounts overlay view into `WindowManager`
- renders:
  - status bar
  - sticker overlay
  - battery troll overlay
  - real time overlay

## State Ownership

Primary state is in `AppUiState`.

Key groups:

- navigation state
- editor state
- sticker state
- real-time and troll template state
- gesture state
- achievement state
- entitlement state
- paywall UI state

Entitlement fields:

- `premiumUnlocked: Boolean`
- `unlockedFeatureKeys: Set<String>`
- `paywallState: PaywallState?`

## Entitlement Design

The app now supports 2 unlock paths:

1. local premium unlock
2. achievement reward unlock

### Premium unlock

Premium unlock sets:

- `premiumUnlocked = true`

Effect:

- bypasses all paywall gating
- expands sticker slot limit
- unlocks all premium stickers and premium templates

### Reward unlock

Reward unlock uses `unlockedFeatureKeys`.

Current reward-based feature keys:

- `slot:extra`
- `template:cat_diary`

## Lock Conditions

### Premium stickers

Condition:

- `StickerPreset.premium == true`
- and `premiumUnlocked == false`

Response:

- show paywall

### Extra sticker slot

Free limit:

- `1` sticker

Reward limit:

- `2` stickers if `slot:extra` is unlocked

Premium limit:

- `4` stickers if `premiumUnlocked == true`

Condition:

- adding a new sticker when current sticker count is already at allowed limit

Response:

- show paywall

### Premium real-time template

Condition:

- `ContentTemplate.premium == true`
- and feature key `template:<id>` is not unlocked
- and `premiumUnlocked == false`

Response:

- show paywall

## Reward Mapping

Current reward mapping from achievement claim:

- `save_sticker`
  - unlocks `slot:extra`
- `template_explorer`
  - unlocks `template:cat_diary`

Other achievements currently change progress and claim state only.

## Overlay Runtime Design

The overlay runtime merges 2 inputs:

1. persisted overlay snapshot
2. live system status

### Persisted snapshot

Source:

- `OverlayConfigStore.read(context)`

Contains:

- status-bar enabled state
- battery text and colors
- sticker enabled and glyph
- troll enabled and message
- real-time enabled, glyph, title

### Live status

Source:

- `OverlayAccessibilityService`

Contains:

- battery percent
- charging state
- wifi connectivity
- mobile connectivity
- airplane mode
- cellular signal level

### Render output

`StatusBarOverlayManager.render(snapshot, liveStatus)` updates:

- clock and date
- wifi label
- signal bars
- battery content
- sticker overlay
- troll overlay
- real-time overlay

## Failure Model

### Accessibility disabled

Effect:

- apply requests fail logically
- state may still mutate locally, but service-backed overlay is not refreshed

### Unknown ids

Effect:

- invalid sticker/template/theme ids are ignored safely

### Service not running

Effect:

- refresh broadcast may not cause visible overlay update
- persisted data remains available for later refresh
