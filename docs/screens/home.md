# Màn hình Home — Spec (clone)

## 1. Mục đích

- Hiển thị **danh mục ngang** (tabs) và **lưới item** (battery / emoji content) giống luồng SubHome + ViewPager trong app gốc.
- Ưu tiên **dữ liệu Volio** khi `category_id` là UUID từ API; fallback **local** qua `HomeCatalogRepository` / `SampleCatalog`.

## 2. Route & code

| Mục | Giá trị |
|-----|---------|
| `AppRoute` | `home` |
| Composable chính | `HomeScreen` trong `EmojiBatteryApp.kt` |
| ViewModel | `EmojiBatteryViewModel` — `selectHomeCategory`, `loadHomeCategoryItems` |
| State | `AppUiState.homeTabs`, `selectedHomeCategoryId`, `homeItemsByCategoryId`, `homeCategoryLoadingId` |

## 3. API & hằng số (app gốc → clone)

| Thành phần | App gốc (cách tìm) | Clone |
|------------|---------------------|-------|
| Base URL Volio | jadx: `stores.volio.vn`, Retrofit module ~`C2984On` / `Interface...Va` | `VolioConstants.PUBLIC_API_BASE` |
| Parent app / home store | use case OS / category loader gắn `parent_id` | `VolioConstants.PARENT_APP_ID` = `26bf9d75-7fd5-438d-81b0-b901f5ba2cd5` |
| `categories/all` | Retrofit `GET` với `parent_id` | `VolioStoreApi.categoriesAll` |
| `items` | `category_id`, `offset`, `limit` | `VolioStoreApi.items` |

**Repository:** `VolioHomeRepository.fetchCategoryTabs()`, `fetchItemsForCategory(categoryId)`.

## 4. Model UI

- `HomeCategoryTab` — id + title (từ API `name`).
- `HomeBatteryItem` — `thumbnailUrl` (Coil `AsyncImage`), `premium`, `animated`, v.v.

## 5. Đối chiếu resource gốc

- Màu nền / card: đồng bộ với theme home trong `Theme.kt` (`background` #FEF5FA, v.v.).
- Icon placeholder: `previewRes` khi không có thumbnail.

## 6. Hành vi lỗi & fallback

- Gọi Volio lỗi → có thể trả về danh sách rỗng hoặc fallback theo `HomeCatalogRepository` (xem logic trong `EmojiBatteryViewModel.loadHomeCategoryItems`).
- Category id dài + dạng UUID → coi là Volio id (`isVolioCategoryId`).

## 7. Kiểm thử nhanh

1. Mở app → Home load tabs từ API.
2. Đổi tab → grid load/shuffle theo category.
3. Tắt mạng → xác nhận fallback local (nếu đã implement cho category đó).
