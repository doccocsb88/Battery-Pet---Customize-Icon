# Paywall And Locking

## Overview

This document describes the newly implemented lock condition and paywall behavior.

This logic is local-only.

There is no real billing provider yet.

Current premium unlock is simulated by:

- showing a paywall dialog
- allowing the user to tap `Unlock Premium`
- setting `premiumUnlocked = true`

## Implemented Lock Surfaces

### 1. Premium sticker packs

Relevant model:

- `StickerPreset.premium`

Current premium stickers:

- `robot_wave`
- `star_orbit`

Lock rule:

- if user taps a premium sticker and `premiumUnlocked == false`
- then ViewModel does not add the sticker
- instead it writes `paywallState`

User response:

- paywall dialog is shown

## 2. Extra sticker slot

Free limit:

- 1 sticker

Reward limit:

- 2 stickers after reward unlock `slot:extra`

Premium limit:

- 4 stickers after premium unlock

Lock rule:

- if user tries to add a new sticker above current allowed slot count
- ViewModel writes `paywallState` with feature key `slot:extra`

## 3. Premium real-time template

Relevant model:

- `ContentTemplate.premium`

Current premium template:

- `cat_diary`

Unlock paths:

1. local premium unlock
2. achievement reward `template_explorer`

Reward feature key:

- `template:cat_diary`

Lock rule:

- if user selects premium template without access
- ViewModel writes `paywallState`

## Entitlement State

Implemented fields in `AppUiState`:

- `premiumUnlocked: Boolean`
- `unlockedFeatureKeys: Set<String>`
- `paywallState: PaywallState?`

## Reward Mapping

### `save_sticker`

Reward:

- `slot:extra`

Effect:

- increases sticker slot capacity from 1 to 2

### `template_explorer`

Reward:

- `template:cat_diary`

Effect:

- unlocks the premium real-time template `cat_diary`

## Paywall State Contract

Type:

```kotlin
data class PaywallState(
    val featureKey: String,
    val title: String,
    val message: String,
)
```

When populated:

- UI renders `PaywallDialog`

When dismissed:

- `paywallState = null`

When premium is unlocked:

- `premiumUnlocked = true`
- `paywallState = null`

## Decision Table

### Sticker tap

| Condition | Result |
|---|---|
| non-premium sticker and slot available | add sticker |
| non-premium sticker and no slot available | show paywall |
| premium sticker without premium access | show paywall |
| premium sticker with premium access | add sticker |

### Real-time template tap

| Condition | Result |
|---|---|
| free template | select template |
| premium template with reward unlock | select template |
| premium template with premium access | select template |
| premium template without access | show paywall |

## Current Limitations

- no real Google Play Billing
- no restore purchase flow
- no persisted premium state across reinstall except through normal app storage retention
- no server-side entitlement verification
- no expiration model

## Next Steps

If needed, the next implementation step should be:

1. introduce a `BillingRepository`
2. persist entitlements in dedicated storage instead of only app state
3. separate reward unlocks from premium unlocks in a typed entitlement model
4. add paywall analytics events
