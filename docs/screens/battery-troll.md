# Màn hình Battery Troll — Spec (clone)

## 1. Mục đích

- Chọn **template prank** (pin ảo / nhãn fake) và **nhãn hiển thị** (`trollMessage`) cho overlay.
- Bật **Auto drop animation** (state trong `AppUiState`).
- **Save** lưu overlay qua `OverlayConfigStore.saveBatteryTroll`; **Turn Off** gọi `clearBatteryTroll`.
- UI đồng bộ **cùng ngôn ngữ visual** với màn Emoji Sticker trong clone: nền `#FEF5FA`, card trắng bo 16dp, chữ `#5C4B51`, chip Tutorial `#FFE5FC`, nút Turn Off / Save gradient, lưới 4 cột, Lottie loading khi fetch remote.

**Lưu ý:** Trong workspace hiện **không** có APK gốc đã apktool; không có `fragment_*troll*.xml` để diff tên file. Layout được **căn chỉnh theo cùng pattern** đã dùng cho `fragment_emoji_sticker` (đã mô tả trong spec Emoji Sticker).

## 2. Route & code

| Mục | Giá trị |
|-----|---------|
| `AppRoute` | `battery_troll` |
| Composable | `BatteryTrollScreen` trong `EmojiBatteryApp.kt` |
| ViewModel | `selectBatteryTrollTemplate`, `setTrollMessage`, `setTrollAutoDrop`, `applyBatteryTroll`, `turnOffBatteryTroll`, `refreshBatteryTrollCatalog` |
| State | `selectedBatteryTrollTemplateId`, `trollMessage`, `trollAutoDrop`, `trollOverlayEnabled`, `batteryTrollCatalogRemote`, `batteryTrollCatalogLoading` |

## 3. Dữ liệu

### Local (mặc định)

- `SampleCatalog.batteryTrollTemplates` — `BatteryTrollTemplate` (id, title, summary, `prankMessage`, `accentGlyph`).
- `SampleCatalog.trollMessageOptions` — danh sách chip nhãn; UI **gộp thêm** `prankMessage` từ template đang load (kể cả remote).

### Remote (tuỳ chọn)

- `VolioBatteryTrollRepository.fetchTemplates()` — cùng DTO `items` như sticker/home.
- **Bật** bằng cách set **`VolioConstants.BATTERY_TROLL_PARENT_ID`** = UUID scope từ app gốc (jadx). Để **rỗng** → không gọi mạng, chỉ dùng sample.
- `AppUiState.batteryTrollTemplateForId(id)` — sample **hoặc** `batteryTrollCatalogRemote`.

## 4. Asset / drawable (clone)

| Asset | Mục đích |
|-------|-----------|
| `ic_back_40_new` | Nút back (cùng màn Sticker) |
| `ic_battery_troll_customize_32` | Icon dòng mô tả đầu trang |
| `ic_turn_off_shimeji` | Nút Turn Off |
| `cute_loading.json` | Lottie khi `batteryTrollCatalogLoading` và remote đang trống (chỉ khi có parent id và đang fetch) |
| Theme | Albert Sans qua `MaterialTheme` (`Theme.kt`) |

## 5. Overlay

- `OverlayConfigStore.saveBatteryTroll` — `troll_message` = `uiState.trollMessage`.
- `StatusBarOverlayManager` — `trollView.text = "Fake ${snapshot.trollMessage}"` (không đổi trong task này).

## 6. Kiểm thử đã chạy (build + launch)

- `./gradlew :app:assembleDebug` — **SUCCESS**
- `./gradlew :app:installDebug` — cài emulator
- Mở màn hình trực tiếp (không crash):

```bash
adb shell am start -n co.q7labs.co.emoji/dev.hai.emojibattery.MainActivity --es route battery_troll
```

- `logcat` không thấy `FATAL` / `AndroidRuntime` liên quan app ngay sau khi mở (kiểm tra thủ công sau bản build này).

## 7. Việc cần làm khi có APK gốc

1. apktool: tìm layout/fragment battery troll, string màu, drawable riêng.
2. jadx: tìm `parent_id` Volio cho troll → gán `BATTERY_TROLL_PARENT_ID`.
3. So khớp text nút (Save / Apply / Turn Off) với resource `strings.xml` gốc nếu cần localization.

## 8. Liên quan

- [`../ORIGINAL_APP_DATA_EXTRACTION.md`](../ORIGINAL_APP_DATA_EXTRACTION.md)
- [`../BACKEND_DATA_FLOW.md`](../BACKEND_DATA_FLOW.md) — luồng apply troll
