# Sequence Diagram

## 1. Main Status-Bar Apply

```mermaid
sequenceDiagram
    participant User
    participant UI as "EmojiBatteryApp / Screen"
    participant VM as "EmojiBatteryViewModel"
    participant Bridge as "AccessibilityBridge"
    participant Store as "OverlayConfigStore"
    participant Service as "OverlayAccessibilityService"
    participant Manager as "StatusBarOverlayManager"

    User->>UI: Edit battery / emoji / theme
    UI->>VM: update config actions
    User->>UI: Tap Apply
    UI->>Bridge: isEnabled(context)
    Bridge-->>UI: true/false
    UI->>VM: syncAccessibilityGranted(result)
    UI->>VM: applyConfig()
    alt Accessibility disabled
        VM-->>UI: infoMessage error
    else Accessibility enabled
        VM-->>UI: appliedConfig updated
        UI->>Store: saveStatusBarConfig(context, editingConfig)
        UI->>Service: requestRefresh(context)
        Service->>Store: read(context)
        Store-->>Service: OverlaySnapshot
        Service->>Service: merge live battery/wifi/signal/time state
        Service->>Manager: render(snapshot, liveStatus)
        Manager-->>User: Overlay updated
    end
```

## 2. Sticker Add With Lock Condition

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant VM as "EmojiBatteryViewModel"

    User->>UI: Tap sticker
    UI->>VM: addSticker(stickerId)
    alt Premium sticker and not entitled
        VM-->>UI: paywallState = premium sticker
        UI-->>User: Show paywall dialog
    else Slot limit reached
        VM-->>UI: paywallState = extra slot
        UI-->>User: Show paywall dialog
    else Allowed
        VM-->>UI: stickerPlacements updated
        UI-->>User: Sticker added
    end
```

## 3. Real-Time Template Select With Reward Unlock

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant VM as "EmojiBatteryViewModel"

    User->>UI: Tap template
    UI->>VM: selectRealTimeTemplate(templateId)
    alt Premium template without access
        VM-->>UI: paywallState = template lock
        UI-->>User: Show paywall dialog
    else Reward unlocked or premium unlocked
        VM-->>UI: selectedRealTimeTemplateId updated
    end
```

## 4. Claim Achievement Unlock Path

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant VM as "EmojiBatteryViewModel"

    User->>UI: Tap Claim
    UI->>VM: claimAchievement(taskId)
    alt Task not complete
        VM-->>UI: infoMessage = not ready
    else Task complete
        VM->>VM: mark claimed
        VM->>VM: map reward to unlockedFeatureKeys
        VM-->>UI: achievements + entitlement updated
    end
```

## 5. Premium Purchase Path

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant VM as "EmojiBatteryViewModel"

    User->>UI: Tap locked premium item
    UI->>VM: action(request)
    VM-->>UI: paywallState
    UI-->>User: Render paywall dialog
    User->>UI: Tap Unlock Premium
    UI->>VM: unlockPremium()
    VM-->>UI: premiumUnlocked = true
    UI-->>User: Locked content becomes available
```
