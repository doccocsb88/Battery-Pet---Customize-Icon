# Spec — App clone (Emoji Battery port)

Tài liệu mô tả **phạm vi**, **kiến trúc**, **nguồn dữ liệu** và **đối chiếu với app gốc** (APK đã decompile trong repo). Chi tiết từng màn hình nằm trong [`docs/screens/`](screens/README.md).

---

## 1. Mục tiêu

- **Clone UI/UX và luồng** gần với app gốc (status bar customize, home store, emoji sticker, gesture, overlay…).
- **Dữ liệu catalog từ Volio** (public API) khi có thể map được với use case trong bản gốc.
- **Fallback local** (`SampleCatalog` trong `AppModels.kt`) khi không có mạng hoặc API lỗi.
- **Overlay** qua `AccessibilityService` + `WindowManager` (không đổi contract cốt lõi so với tài liệu [`BACKEND_DATA_FLOW.md`](BACKEND_DATA_FLOW.md)).

---

## 2. Stack kỹ thuật

| Lớp | Thành phần | Ghi chú |
|-----|------------|---------|
| UI | Jetpack Compose, `EmojiBatteryApp.kt` | NavHost, màn hình theo `AppRoute` |
| State | `EmojiBatteryViewModel`, `AppUiState` | Single source of truth |
| Remote | Retrofit + Gson, `VolioNetwork` | Base URL cố định, endpoint public |
| Ảnh / Lottie | Coil, Lottie Compose | Thumbnail URL, JSON Lottie từ Volio |
| Local prefs | `OverlayConfigStore` | Snapshot overlay (glyph + thumbnail URL…) |
| Billing | Google Play Billing (module billing) | Paywall / unlock (theo state app) |

**Application ID / namespace:** `co.q7labs.co.emoji` — Activity launcher: `dev.hai.emojibattery.MainActivity`.

---

## 3. Điều hướng (routes)

Định nghĩa tại `dev/hai/emojibattery/ui/navigation/AppRoute.kt`.

| Route | Màn hình (mô tả ngắn) |
|-------|------------------------|
| `splash`, `language`, `onboarding`, `tutorial` | Onboarding |
| `home` | Home — category strip + grid item (Volio + local) |
| `customize`, `statusbar_custom`, `legacy_battery` | Customize status bar / battery |
| `gesture` | Gesture trên status bar |
| `achievement` | Achievement |
| `search` | Search |
| `settings`, `feedback`, `paywall`, `legal/{document}` | Settings / feedback / paywall / legal |
| `real_time`, `battery_troll` | Template flows |
| **`emoji_sticker`** | **Emoji Sticker** — Volio sticker scope + overlay |
| **`battery_troll`** | **Battery Troll** — template prank + `trollMessage`; remote Volio nếu set `BATTERY_TROLL_PARENT_ID` |
| `feature/{feature}` | Chi tiết từng mục Customize |

Intent extra (debug): `route` — xem `MainActivity.kt`.

---

## 4. Nguồn dữ liệu từ app gốc → clone

### 4.1 API Volio (public)

- **Base URL** (phải là host **`stores`** số nhiều):  
  `https://stores.volio.vn/stores/api/v5.0/public/`
- **Cách lấy trong app gốc:** decompile (jadx) — tìm chuỗi base URL / `Retrofit` builder; tham chiếu mã: `C2984On.f` (base64), package `hungvv` trong bản decompile.
- **Endpoints dùng trong clone:**
  - `GET categories/all?parent_id={uuid}` — danh mục con (tab / “All”).
  - `GET items?category_id={uuid}&offset=&limit=` — item (thumbnail, photo, `custom_fields.content`, `is_pro`…).

Hằng số trong code: `VolioConstants` (`PARENT_APP_ID` = home emoji battery store, `STICKER_PARENT_ID` = scope sticker).

### 4.2 Dữ liệu không qua API

- **Layout / màu / drawable / font / animation:** lấy từ **apktool** (`res/layout`, `res/values`, `res/font`, `assets/*.json`).
- **Logic nghiệp vụ:** jadx — Fragment / ViewModel / Use case tương ứng (ví dụ `EmojiStickerFragment`, `GT` cho sticker parent id).

Chi tiết quy trình: [`ORIGINAL_APP_DATA_EXTRACTION.md`](ORIGINAL_APP_DATA_EXTRACTION.md).

---

## 5. Mapping repository ↔ màn hình

| Repository / module | Màn hình / luồng |
|---------------------|------------------|
| `VolioHomeRepository` + `HomeCatalogRepository` | Home — tab category + items |
| `VolioStickerRepository` | Emoji Sticker — grid sticker |
| `VolioBatteryTrollRepository` | Battery Troll — khi `BATTERY_TROLL_PARENT_ID` khác rỗng |
| `SampleCatalog` (local) | Fallback + preset battery/emoji/theme/sticker mẫu |
| `OverlayConfigStore` | Sau khi Save overlay (sticker glyph, thumbnail URL…) |

---

## 6. File tham chiếu nhanh (clone)

- ViewModel: `app/src/main/java/dev/hai/emojibattery/app/EmojiBatteryViewModel.kt`
- Model / catalog: `app/src/main/java/dev/hai/emojibattery/model/AppModels.kt`
- Volio: `app/src/main/java/dev/hai/emojibattery/data/volio/`, `VolioHomeRepository.kt`, `VolioStickerRepository.kt`
- UI shell: `app/src/main/java/dev/hai/emojibattery/app/EmojiBatteryApp.kt`
- Overlay: `StatusBarOverlayManager.kt`, `OverlayConfigStore.kt`

---

## 7. Tài liệu liên quan

- [`ORIGINAL_APP_DATA_EXTRACTION.md`](ORIGINAL_APP_DATA_EXTRACTION.md) — cách lấy data từ app gốc.
- [`screens/README.md`](screens/README.md) — index màn hình.
- [`BACKEND_DATA_FLOW.md`](BACKEND_DATA_FLOW.md) — luồng overlay / accessibility (cần cập nhật nhỏ nếu thêm Volio; phần lõi vẫn đúng).
