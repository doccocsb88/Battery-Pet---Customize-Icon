# PRD: MoodBar

Date: 2026-03-20
Status: Draft
Owner: Product

## 1. Overview

MoodBar is an iOS personalization app focused on cute battery widgets, matching wallpapers, and charging themes.

The product does not attempt to modify the real iPhone status bar. Instead, it delivers a cohesive visual customization experience within App Store-safe technical boundaries.

Core promise:
- Make your iPhone battery feel cute, expressive, and personal
- Apply matching widget + wallpaper + charging themes in a fast, simple flow

## 2. Problem Statement

Users want to personalize their iPhone, but the current market has three recurring problems:

1. Most customization apps are generic bundles of icons, widgets, and wallpapers with weak differentiation.
2. The setup experience is often slow, confusing, and ad-heavy.
3. Some apps imply system-level customization that iOS does not actually allow, which creates disappointment and weak trust.

There is room for a narrower product that is:
- more focused
- more honest
- faster to apply
- visually more coherent

## 3. Product Vision

Build the best iPhone app for cute battery personalization.

MoodBar should feel like:
- a battery pet
- a mood-based lock screen accessory
- a lightweight aesthetic personalization tool

It should not feel like:
- a generic theme warehouse
- a scammy status-bar changer
- a bloated all-in-one customization app

## 4. Goals

### Primary goals

- Validate demand for a focused cute-battery personalization app on iOS
- Achieve strong install-to-apply conversion
- Generate subscription revenue from premium packs

### Secondary goals

- Build a reusable content-pack system for seasonal drops
- Create a product format suitable for TikTok / Reels / Pinterest distribution

## 5. Non-Goals

- Replacing the real iOS battery icon
- Modifying the real iOS status bar globally
- Building a full icon changer platform in v1
- Building a community marketplace in v1
- Supporting highly advanced widget editing in v1

## 6. Target Users

### Primary audience

- iPhone users aged roughly 13-30
- Users who like cute, aesthetic, kawaii, pastel, or playful phone customization
- Users who already install themes, widgets, lock screen packs, or charging animation apps

### Secondary audience

- Existing personalization app users looking for something more focused
- Users discovering the app via short-form videos and visual before/after transformations

### User motivations

- Make the phone feel more personal
- Show taste or mood through the lock screen and home screen
- Enjoy a cute battery experience
- Get quick visual novelty without much setup work

## 7. Core Value Proposition

MoodBar offers:
- cute battery widgets
- matching wallpapers
- charging animations
- theme packs designed as a coherent set

The value is not just asset quantity. The value is:
- strong visual coherence
- fast application
- clarity about what is and is not possible on iPhone

## 8. Platform Constraints

### iOS-safe product surface

- Home Screen widgets
- Lock Screen widgets
- wallpaper generation and export
- charging animations
- in-app previews and setup guides

### Hard constraints

The app must not claim to:
- change the real iPhone status bar
- replace the real battery icon
- alter Dynamic Island or notch behavior

All product messaging, screenshots, and onboarding must stay consistent with these constraints.

## 9. MVP Scope

### 9.1 Theme Packs

Ship 12 curated theme packs:
- Cat
- Bear
- Bunny
- Matcha
- Pink
- Minimal
- Retro
- Space
- Ghost
- Food
- Kawaii
- Study

Each theme pack includes:
- 1 lock screen wallpaper
- 1 home screen wallpaper
- 1 lock screen battery widget style
- 2 home screen widget styles
- 1 charging animation

### 9.2 Battery Widgets

Widget sizes:
- Lock Screen inline or circular battery widget
- Home Screen small widget
- Home Screen medium widget

Battery states supported:
- full
- normal
- low
- charging

Visual state changes may include:
- face / mood
- color
- small decorative elements

### 9.3 Wallpaper Generator

Requirements:
- choose a theme pack
- generate wallpaper variants matched to supported iPhone screen layouts
- preview before save
- export to Photos
- clear guidance for setting as Lock Screen / Home Screen

### 9.4 Charging Animations

Requirements:
- 8 charging animations at launch
- visually matched to theme packs
- accessible from a dedicated charging section

### 9.5 Apply Flow

The app should support a fast first-use flow:

1. Choose a theme
2. Preview the full setup
3. Save wallpaper
4. Open widget setup guide
5. Activate first personalized screen in under 2 minutes

### 9.6 Content Management

Internal content system should support:
- pack metadata
- theme previews
- free vs premium gating
- weekly content drops without app redesign

## 10. Out of Scope for MVP

- icon shortcut generator
- user-uploaded pack creator
- UGC sharing feed
- daily challenges
- referrals
- full subscription experimentation platform

These can be added after initial retention and monetization validation.

## 11. User Stories

### First-time user

- As a new user, I want to understand quickly what the app changes and what it does not change.
- As a new user, I want to preview a cute battery look before I commit to setup steps.
- As a new user, I want to apply my first setup with minimal friction.

### Returning user

- As a returning user, I want to browse fresh theme packs so the app keeps feeling new.
- As a returning user, I want to save my favorite packs and switch between them easily.

### Paying user

- As a premium user, I want access to all packs, widgets, and charging themes without clutter or friction.

## 12. UX Principles

- Be honest: never imply the app changes the real iPhone status bar
- Be fast: reduce setup friction wherever possible
- Be cohesive: packs should feel designed, not assembled
- Be delightful: visual states should be expressive and charming
- Be focused: avoid turning the app into a generic customization dump

## 13. Onboarding Requirements

Onboarding must communicate:
- what the app does
- what the app does not do
- how widgets and wallpapers work on iPhone
- what the user gets in the free tier

Recommended onboarding structure:

1. Welcome to cute battery personalization
2. Pick your vibe
3. See a full-screen preview
4. Learn the 2-step apply flow
5. Continue to free content

## 14. Monetization

### Free tier

- 3 free theme packs
- 1 lock screen widget style
- 2 charging animations
- limited wallpaper export
- light watermark on free export

### Premium tier

- all theme packs
- all widgets
- all charging animations
- future weekly pack drops
- no watermark
- optional custom asset support later

### Initial pricing

- Weekly: $2.99 with 3-day free trial
- Yearly: $19.99
- Lifetime: deferred until traction is validated, target $29.99-$34.99

### Paywall trigger

Preferred triggers:
- after first successful export
- when opening a premium pack after the user has seen free value

Avoid:
- immediate hard paywall before first preview

## 15. Success Metrics

### Activation

- Install to first preview rate
- Install to first wallpaper export rate
- Install to first widget setup completion rate

### Retention

- D1 retention
- D7 retention
- % of users applying more than one pack
- weekly pack revisit rate

### Monetization

- Trial start rate
- Trial to paid conversion
- ARPPU
- Paywall view to purchase conversion

### Quality

- App Store rating
- % of reviews mentioning misleading expectations
- support tickets related to setup confusion

## 16. Initial KPI Targets

These are starting targets and should be revised after launch data.

- Install to first preview: 70%+
- Install to first export: 35%+
- Install to first widget setup completion: 20%+
- D1 retention: 28%+
- D7 retention: 10%+
- Trial start from paywall view: 5%+
- App Store rating: 4.4+

## 17. ASO Positioning

Primary search intent:
- battery widget
- cute battery
- emoji battery
- charging animation
- lock screen widget

Suggested app naming directions:
- MoodBar: Cute Battery Widgets
- MoodBar - Battery Themes
- Emoji Battery & Cute Widgets

Example subtitle directions:
- Cute lock screen battery themes
- Battery widget, wallpapers, charge
- Emoji battery, widgets, wallpapers

## 18. Launch Strategy

### Channel fit

Best channels:
- TikTok
- Instagram Reels
- Pinterest
- short YouTube videos

### Creative angle

The product should be marketed visually through:
- before/after lock screen transformations
- low-battery character reactions
- charging animation clips
- themed pack showcases

### Launch content needs

- 20 short-form videos
- 12 pack preview cards
- App Store screenshots showing real apply flow
- one clear screenshot stating that the app uses widgets and wallpapers, not real system bar replacement

## 19. Risks

### Risk 1: expectation mismatch

Users may expect a real status bar changer.

Mitigation:
- transparent App Store copy
- clear onboarding
- screenshot labeling

### Risk 2: low retention

Personalization apps can become one-time-use products.

Mitigation:
- weekly pack drops
- favorites
- mood-based seasonal content

### Risk 3: low differentiation

The app may look like another generic theme app.

Mitigation:
- strong niche focus on cute battery emotion
- avoid icon-changer sprawl in v1

### Risk 4: content burden

The app needs ongoing pack production.

Mitigation:
- reusable pack template pipeline
- limited but high-quality releases

## 20. Open Questions

- Should custom photo-based pack generation be part of v1.1 or later?
- Should we add icon changing at all, or keep the app intentionally narrow?
- Which 3 theme packs should be free for best conversion?
- Is there enough demand for seasonal drops alone, or do we need social sharing earlier?

## 21. Release Plan

### v1

- 12 theme packs
- lock screen and home screen battery widgets
- wallpaper export
- charging animations
- free vs premium gating
- onboarding
- favorites

### v1.1

- more packs
- improved pack recommendations
- better widget previewing
- deeper analytics on apply flow

### v1.2+

- optional custom uploads
- switching themes more quickly
- social sharing assets
- UGC exploration if retention supports it

## 22. Final Decision

Proceed only with a focused product thesis:

MoodBar is a niche iOS personalization app centered on cute battery widgets, matching wallpapers, and charging themes.

Do not broaden the scope into a full generic theme super-app in the initial release.
