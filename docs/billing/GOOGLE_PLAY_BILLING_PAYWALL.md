# Google Play Billing And Paywall

## Scope

This document covers the new purchase flow added to the app:

- package id migration
- Google Play Billing product model
- paywall screen
- purchase service
- entitlement sync

## App Identifier

App bundle / application id:

- `co.q7labs.co.emoji`

Billing product ids:

- weekly subscription:
  - `co.q7labs.co.emoji.weekly`
- lifetime one-time product:
  - `co.q7labs.co.emoji.lifetime`

## Product Model

### Weekly

Type:

- Google Play subscription

Product id:

- `co.q7labs.co.emoji.weekly`

User message:

- weekly subscription
- auto-renews until canceled

### Lifetime

Type:

- Google Play in-app product
- non-consumable one-time purchase

Product id:

- `co.q7labs.co.emoji.lifetime`

User message:

- one-time purchase
- no recurring charge

## Main Files

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/dev/hai/emojibattery/billing/BillingService.kt`
- `app/src/main/java/dev/hai/emojibattery/app/EmojiBatteryApp.kt`
- `app/src/main/java/dev/hai/emojibattery/app/EmojiBatteryViewModel.kt`
- `app/src/main/java/dev/hai/emojibattery/ui/navigation/AppRoute.kt`

## Paywall Screen

The app now uses a full-screen paywall route:

- route:
  - `paywall`

The screen includes:

- premium headline based on current lock reason
- benefits section
- weekly plan card
- lifetime plan card
- restore purchases button
- manage subscriptions button
- policy text describing:
  - weekly auto-renew behavior
  - lifetime one-time behavior

## Billing Service Design

`BillingService` is a singleton wrapper around `BillingClient`.

Responsibilities:

- start billing connection
- query product details
- cache `ProductDetails`
- launch billing flow
- restore owned purchases
- acknowledge completed purchases
- expose current billing state as `StateFlow`

State model:

- `BillingUiState`

Fields:

- `connected`
- `loading`
- `weeklyPlan`
- `lifetimePlan`
- `ownedProductIds`
- `purchaseInFlight`
- `errorMessage`

## Purchase Flow

### 1. Service startup

When the app starts:

- `BillingService.start(context)` is called

Then:

- connect to Google Play Billing
- query product details for weekly and lifetime
- query owned purchases for:
  - subscriptions
  - in-app products

### 2. Purchase launch

When user taps purchase button:

- weekly:
  - `BillingService.launchPurchase(activity, co.q7labs.co.emoji.weekly)`
- lifetime:
  - `BillingService.launchPurchase(activity, co.q7labs.co.emoji.lifetime)`

### 3. Purchase update

When Google Play returns purchase result:

- `onPurchasesUpdated(...)` runs
- service acknowledges purchase if required
- service refreshes owned purchases

### 4. Entitlement sync

`EmojiBatteryApp` observes `BillingService.uiState`.

If `ownedProductIds` contains:

- `co.q7labs.co.emoji.weekly`
- or `co.q7labs.co.emoji.lifetime`

Then:

- `viewModel.syncPremiumAccess(true)`

Effect:

- premium lock conditions are bypassed

## Lock And Paywall Integration

Current lock points connected to paywall:

- premium stickers
- premium real-time template
- extra sticker slot limit

When a lock hits:

- ViewModel writes `paywallState`
- app navigates to `paywall`
- user can:
  - go back
  - restore purchases
  - manage subscriptions
  - buy weekly
  - buy lifetime

## Google Play Policy Alignment

The current paywall follows the safer direction of Google Play policy by:

- showing clear product separation
- showing recurring vs one-time distinction
- clearly stating weekly auto-renew behavior
- providing restore action
- providing manage subscriptions action
- using Google Play Billing instead of custom payment rails

What is not yet included:

- privacy / terms links directly on paywall
- trial or intro offer handling
- region-specific tax/legal wording
- server-side receipt verification

## Known Limits

- real prices only appear when product ids exist in Google Play Console and the app is installed in a valid billing environment
- on local debug or emulator, price loading may remain unavailable
- lifetime purchase is local entitlement sync through Play ownership query, without backend verification

## Next Steps

If you want this production-ready, the next steps are:

1. add Play Console products with matching ids
2. add privacy policy and terms links directly to paywall
3. add server-side receipt verification if needed
4. add analytics events for paywall open / purchase / restore / cancel
