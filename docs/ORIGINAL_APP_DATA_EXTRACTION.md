# Cách lấy data từ app gốc (phục vụ clone)

Tài liệu này mô tả **quy trình thực tế** để trích xuất URL API, UUID scope, layout, asset và đối chiếu với code clone trong repo.

---

## 1. Chuẩn bị

- File **APK** của app gốc (cùng phiên bản bạn muốn so khớp).
- Công cụ (một trong các bộ):
  - **apktool** — giải nén `res/`, `AndroidManifest.xml`, `assets/`.
  - **jadx** hoặc **jadx-gui** — đọc mã Java/Kotlin đã decompile.
  - (Tuỳ chọn) **mitmproxy** / **Charles** — bắt traffic HTTPS nếu app không pin certificate (chỉ khi pháp lý cho phép).

Trong repo clone có thư mục ví dụ (nếu đã commit):  
`decompiled/emoji-battery-icon-customize-1.2.8/apktool-main/` — dùng làm **mẫu** cấu trúc output apktool.

---

## 2. Bước A — Apktool: resource & asset

```bash
apktool d app-original.apk -o apktool-out
```

**Đọc:**

| Đường dẫn | Mục đích |
|-----------|----------|
| `res/layout/*.xml` | Cấu trúc màn hình, id view, màu (`#fef5fa`…), dimen |
| `res/values/colors.xml`, `styles.xml`, `themes.xml` | Theme, text style (vd. Albert Sans) |
| `res/drawable*`, `res/font` | Icon, shape, font `.ttf` |
| `assets/*.json` | Lottie (vd. `cute_loading.json`) |
| `AndroidManifest.xml` | Activity/Fragment name, deep link |

**Áp vào clone:** copy drawable/font/asset cần thiết vào `app/src/main/res/` hoặc `assets/`, rồi map sang Compose (`Color`, `RoundedCornerShape`, `painterResource`, Lottie `Asset`).

---

## 3. Bước B — Jadx: API base URL và tham số

```bash
jadx -d jadx-out app-original.apk
```

**Tìm kiếm có chủ đích (grep / search trong IDE):**

1. **`stores.volio.vn`** hoặc **`volio`** — xác định chuỗi base URL.  
   - Lưu ý: host đúng thường là **`https://stores.volio.vn/...`** (chữ **stores**), không phải `store.volio.vn`.
2. **`categories/all`**, **`items`**, **`parent_id`** — chỗ gọi Retrofit/OkHttp.
3. **UUID dạng** `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` — gán cho `parent_id` (home store, sticker store…).

**Tham chiếu đã dùng trong clone (ví dụ từ decompile):**

| Ý nghĩa | UUID / hằng số | File clone |
|---------|----------------|------------|
| Home / emoji battery store parent | `26bf9d75-7fd5-438d-81b0-b901f5ba2cd5` | `VolioConstants.PARENT_APP_ID` |
| Emoji sticker store parent | `9f7b1b47-ee3f-4bf1-b857-09f4c73ffbf0` | `VolioConstants.STICKER_PARENT_ID` |
| Use case sticker (tên class gốc) | `hungvv.GT` | comment trong `VolioStickerRepository` |

Khi đổi phiên bản APK gốc, **luôn verify lại UUID** bằng jadx; không giả định UUID giữ nguyên mãi.

---

## 4. Bước C — Xác minh API bằng curl (không cần app)

Sau khi có `parent_id` và (tuỳ chọn) `category_id` từ response categories:

```bash
# Danh mục con (tabs / All)
curl -sS "https://stores.volio.vn/stores/api/v5.0/public/categories/all?parent_id=PARENT_UUID"

# Item trong một category
curl -sS "https://stores.volio.vn/stores/api/v5.0/public/items?category_id=CATEGORY_UUID&offset=0&limit=20"
```

**Trường JSON hữu ích** (item):

- `id`, `name`, `thumbnail`, `photo`
- `is_pro`
- `custom_fields.content` — thường là URL file **Lottie `.json`** hoặc nội dung khác tuỳ loại item.

Clone map các trường này trong `VolioEmojiBatteryItemDto` và repository tương ứng.

---

## 5. Bước D — Map màn hình gốc → Fragment/Activity → clone

1. Trong **jadx**, mở layout XML (vd. `fragment_emoji_sticker.xml` trong apktool) → xem `@layout/...` và tên **Fragment** trong code (`R.layout.fragment_emoji_sticker`).
2. Tìm class **Fragment** / **Activity** gắn layout đó.
3. Tìm **ViewModel** hoặc **use case** inject API (search theo `parent_id` hoặc `Volio`).
4. Trong clone, route tương ứng nằm ở `AppRoute` và composable trong `EmojiBatteryApp.kt`.

Bảng màn hình chi tiết: [`screens/README.md`](screens/README.md).

---

## 6. Lưu ý pháp lý & an toàn

- Chỉ decompile / MITM APK **bạn có quyền phân tích** (app của bạn, license cho phép, hoặc mục đích nghiên cứu được phép).
- **Không** nhúng khóa API bí mật của bên thứ ba nếu không được cấp phép; API **public** Volio ở đây là endpoint công khai qua app store client.

---

## 7. Checklist nhanh trước khi merge feature mới

- [ ] UUID `parent_id` / `category_id` đã verify lại trên bản APK mục tiêu.
- [ ] Base URL host (`stores` vs `store`) đã đúng.
- [ ] Layout/màu đã đối chiếu file XML apktool (hoặc screenshot gốc).
- [ ] Asset Lottie/font đã có license phù hợp và được copy vào `assets/` / `res/font`.
