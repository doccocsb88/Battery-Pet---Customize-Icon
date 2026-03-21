# Màn hình Emoji Sticker — Spec (clone)

## 1. Mục đích

- Chọn **sticker** (ảnh / Lottie từ Volio), điều chỉnh **size/speed**, **Save** để bật overlay hoặc **Turn Off** để tắt.
- UI bám **`fragment_emoji_sticker.xml`** (apktool): nền `#fef5fa`, card trắng bo góc, loading `cute_loading.json`, nút Turn Off / Save gradient.

## 2. Route & code

| Mục | Giá trị |
|-----|---------|
| `AppRoute` | `emoji_sticker` |
| Composable | `EmojiStickerScreen` trong `EmojiBatteryApp.kt` |
| ViewModel | `refreshStickerCatalog`, `addSticker`, `selectSticker`, `removeSticker`, `updateSelectedStickerSize/Speed`, `saveStickerOverlay`, `turnOffStickerOverlay` |
| State | `stickerCatalogRemote`, `stickerCatalogLoading`, `stickerPlacements`, `selectedStickerId`, `stickerOverlayEnabled` |

## 3. API & hằng số (app gốc → clone)

| Thành phần | App gốc (cách tìm) | Clone |
|------------|---------------------|-------|
| Sticker store `parent_id` | jadx: class **`hungvv.GT`** (hoặc ViewModel `EmojiStickerViewModel` + use case) | `VolioConstants.STICKER_PARENT_ID` = `9f7b1b47-ee3f-4bf1-b857-09f4c73ffbf0` |
| Category “All” | Response `categories/all` — thường một dòng tên `"All"` | Chọn `name` chứa `All` hoặc category đầu tiên |
| Items | `GET items?category_id=...` | `VolioStickerRepository.fetchStickerPresets()` |

**DTO:** `VolioEmojiBatteryItemDto` — `thumbnail`, `photo`, `custom_fields.content` (URL `.json` → Lottie), `is_pro`.

**Model:** `StickerPreset` — `thumbnailUrl`, `lottieUrl`, `remotePhotoUrl`, `glyph` (fallback overlay chữ).

## 4. Resolve sticker (sample + remote)

- Hàm `AppUiState.stickerPresetForId(id)` — tìm trong `SampleCatalog.stickerPresets` trước, sau đó `stickerCatalogRemote`.

## 5. Overlay sau khi Save

- `OverlayConfigStore.saveStickerOverlay` — lưu `stickerGlyph` + **`stickerThumbnailUrl`** (khi có).
- `StatusBarOverlayManager` — nếu có URL thumbnail → **ImageView** + Coil; không → **TextView** glyph.
- Lottie JSON **full animation** trên overlay: hiện **chưa** chạy Lottie trong service; overlay ưu tiên **thumbnail** PNG từ Volio.

## 6. Asset từ app gốc

| Asset | Nguồn apktool | Trong clone |
|-------|----------------|-------------|
| Loading Lottie | `assets/cute_loading.json` | `app/src/main/assets/cute_loading.json` |
| Icon Turn Off | `res/drawable/ic_turn_off_shimeji.xml` | `res/drawable/ic_turn_off_shimeji.xml` |
| Font Albert Sans | `res/font/*.ttf` | `res/font/` + `Theme.kt` |

## 7. Kiểm thử nhanh

1. Vào Emoji Sticker → chờ load (Lottie) hoặc fallback sample nếu API trống.
2. Chọn sticker → thêm vào “My Sticker”, chỉnh slider.
3. Save → bật accessibility nếu cần → overlay hiển thị thumbnail hoặc glyph.
4. Turn Off → `OverlayConfigStore.clearStickerOverlay`.

## 8. Tài liệu liên quan

- Trích xuất từ APK gốc: [`../ORIGINAL_APP_DATA_EXTRACTION.md`](../ORIGINAL_APP_DATA_EXTRACTION.md)
- Luồng overlay tổng quát: [`../BACKEND_DATA_FLOW.md`](../BACKEND_DATA_FLOW.md)
