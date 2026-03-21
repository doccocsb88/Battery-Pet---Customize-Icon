# Splash & Onboarding — tài nguyên (clone)

## Nguyên tắc

- Màu / gradient / typography bám **theme hồng** (`#FEF5FA`, `#5C4B51`, gradient CTA) giống các màn Home / Emoji Sticker.
- **Không** tích hợp luồng quảng cáo (không banner/interstitial placeholder trên splash hay onboarding).

## File resource

| File | Mục đích |
|------|-----------|
| `res/values/colors.xml` | `splash_*`, `onboarding_*` (gradient, dot, chip, chữ) |
| `res/drawable/bg_splash_screen.xml` | Gradient nền splash + `android:windowBackground` trong theme |
| `res/values/dimens_splash_onboarding.xml` | `splash_logo_box`, `onboarding_hero_height` |
| `res/values/strings.xml` | `splash_tagline` |
| `res/values/themes.xml` | `windowBackground` → `@drawable/bg_splash_screen` (giảm flash trắng khi cold start) |
| `res/font/albert_sans_*.ttf` | Font (đã có; dùng qua `Theme.kt` / `MaterialTheme`) |

## Drawable gốc app (vector) dùng trên splash

- `img_btn_status_bar_new` — icon status bar / pin trong khung logo (từ pack asset clone).

## Compose

- `SplashRoute`: nền `Image(bg_splash_screen)`, logo plate + vector, `stringResource` cho tên app & tagline, progress track trong `colors.xml`.
- `OnboardingScreen`: toàn bộ màu hardcode chuyển sang `colorResource(R.color.onboarding_*)`, CTA gradient từ `onboarding_cta_gradient_*`.

## Khi có APK gốc (apktool)

So khớp thêm: `drawable` splash độc quyền, `layout/activity_splash`, `strings` đúng ngôn ngữ, Lottie splash nếu có — copy vào `res/` và thay tham chiếu trong composable.
