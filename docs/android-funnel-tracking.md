# Android funnel tracking — Emoji Battery

Spec Firebase Analytics cho clone Android. Instrumented trong `TrackingServices.kt` (27 Aug 2026).

Compose destinations **không** được Firebase auto-log; `screen_view` gửi thủ công theo route.

| Metric | Value |
| --- | --- |
| Events trước | 7 (paywall only) |
| Events sau | 24 (behavior funnel) |
| Aha metric | `apply_success` |
| Primary Ads conversion | `purchase` |

Không copy model APK gốc (`open_screen_26`, `hit_5_1`). Dùng named stages để đọc drop-off.

## Funnel GA4 (Exploration)

Build funnel theo thứ tự dưới. Open rate giữa 2 bước kề nhau = drop-off cần sửa. **Không** import mọi bước vào Google Ads.

| Stage | Event | Drop-off nghĩa là |
| --- | --- | --- |
| 1. Splash | `splash_complete` | Install → first screen |
| 2. Language | `language_selected` | Locale picker friction |
| 3. Onboarding | `onboarding_complete` | Skip vs finish theo page |
| 4. First paywall | `paywall_impression` | Post-onboarding Store paywall |
| 5. Home | `home_reached` | Tới được main product (1 lần / install) |
| 6. Feature | `feature_open` / `content_select` | User thử feature nào trước |
| 7. Permission | `permission_prompt` → `permission_result` | Accessibility = aha gate |
| 8. Apply | `apply_attempt` → `apply_success` | Core activation |
| 9. Paywall | `paywall_*` + `purchase` | Limit / locked / settings store |

## Coverage by bucket

| Bucket | Before | After |
| --- | ---: | ---: |
| Acquisition | 0 | 1 |
| Activation | 0 | 6 |
| Engagement | 0 | 5 |
| Apply / aha | 0 | 5 |
| Monetization | 7 | 7 |
| Ads diagnostic | 0 | 1 |

## Event dictionary — paywall (giữ nguyên)

| Event | When | Key params | Ads |
| --- | --- | --- | --- |
| `paywall_impression` | PaywallScreen visible | `feature_key`, `launch_mode`, `has_weekly` / `has_monthly` / `has_lifetime` | Diagnostic |
| `paywall_item_selected` | Plan card tap | `product_id`, `plan_type`, `has_offer_token` | No |
| `paywall_purchase_started` | Billing flow launched | `product_id`, `plan_type` | Secondary |
| `paywall_purchase_success` | Owned SKU xuất hiện trên paywall | `product_id`, `plan_type` | Secondary |
| `purchase` | Play Billing revenue | `value`, `currency`, `product_id`, `plan_type` | **Primary** |
| `paywall_purchase_error` | Lỗi billing (không phải cancel) | `product_id`, `message` | Diagnostic |
| `paywall_exit` | Close hoặc system back | `reason`, `dwell_ms`, `product_id` | No |

## Event dictionary — activation / apply (mới)

| Event | When | Key params | Ads |
| --- | --- | --- | --- |
| `splash_complete` | Splash handoff | `next_route` | No |
| `language_selected` | Confirm language | `locale_tag`, `locale_changed` | No |
| `onboarding_start` | First onboarding view (once) | — | No |
| `onboarding_step` | Next / back | `page_index`, `page_count`, `action` | No |
| `onboarding_complete` | Get started trang cuối | `page_count` | Secondary |
| `onboarding_skip` | Skip (nếu UI dùng) | `page_index` | No |
| `home_reached` | First home screen (once / install) | — | Secondary |
| `screen_view` | Mỗi Compose destination | `screen_name`, `previous_screen` | No |
| `tab_select` | Đổi bottom tab | `tab_name`, `from_tab` | No |
| `feature_open` | Mở một feature | `feature_key`, `source` | No |
| `content_select` | Mở một item cụ thể | `content_type`, `content_id`, `locked` | No |
| `permission_prompt` | Dialog accessibility | `from_screen` | No |
| `permission_result` | Grant / revoke sau hydrate | `granted` | No |
| `apply_attempt` | Tap Apply / Set / Save | `content_type`, `content_id` | No |
| `apply_success` | Overlay hoặc wallpaper applied | `content_type`, `content_id` | Secondary |
| `apply_fail` | Blocked hoặc error | `reason` | Diagnostic |
| `ad_interstitial` | Interstitial shown / skipped | `result`, `reason`, `placement` | No |
| `tutorial_start` | Mở tutorial | — | No |
| `tutorial_complete` | Xong / skip tutorial | `skipped` | No |

### `apply_fail.reason`

`no_permission` · `limit` · `paywall_locked` · `overlay_off` · `troll_disabled` · `empty_sticker` · `not_found` · `apply_error` · `unknown`

### `ad_interstitial`

- `result`: `shown` | `skipped`
- `reason` (khi skipped): `consent` · `throttled` · `no_fill` · `empty_unit` · `failed_show`
- `placement`: `tab_switch` · `home_item` · `home` · `customize` · `wallpaper_locked` · …

Premium user skip interstitial **không** log (expected, tránh noise).

## Call sites

| File | Responsibility |
| --- | --- |
| `tracking/TrackingServices.kt` | Facade + event names + user properties |
| `app/EmojiBatteryApp.kt` | `screen_view`, splash, tabs, `feature_open`, `permission_prompt`, theme apply |
| `app/EmojiBatteryViewModel.kt` | language, onboarding, tutorial, `apply_*`, `permission_result`, locked content |
| `paywall/PaywallModule.kt` | impression, item select, success, exit |
| `billing/GooglePlayPurchaseService.kt` | purchase_started, error, `purchase` revenue |
| `ui/screen/WallpaperScreens.kt` | wallpaper apply / fail / `paywall_locked` |
| `ads/GoogleMobileAdsService.kt` | `ad_interstitial` |

## `paywall_impression.feature_key`

Tách lý do user thấy paywall. Không phải event mới.

| `feature_key` | Trigger |
| --- | --- |
| `flow:post_onboarding_home` | First-run sau onboarding (Store mode) |
| `settings:store` | Nút Premium / Settings store |
| `limit:apply_battery` | Quota apply battery free |
| `limit:apply_sticker` | Quota apply sticker free |
| `reward:extra_sticker_slot` | Sticker slot cap |
| `sticker:{id}` | Premium sticker locked |
| `template:{id}` | Premium realtime template locked |

## Google Ads import

**Import**

| Event | Role |
| --- | --- |
| `purchase` | Primary — bid vào event này |
| `paywall_purchase_success` | Backup nếu `purchase` delay |
| `apply_success` | In-app action / engaged user |
| `onboarding_complete` | Early quality, không phải bid target |
| `home_reached` | Install quality proxy only |

**Không import làm conversion**

| Event | Why |
| --- | --- |
| `screen_view` | Fires mọi màn |
| `tab_select` | High volume, low intent |
| `paywall_impression` | Gần như mọi new user đều thấy |
| `ad_interstitial` | Diagnostic |
| `apply_fail` | Failure, không phải value |

## User properties

| Property | Values |
| --- | --- |
| `is_premium` | `true` / `false` |
| `has_accessibility` | `true` / `false` |
| `onboarded` | `true` / `false` |

Segment funnel theo 3 property này. User `has_accessibility=false` + nhiều `apply_fail` / `no_permission` đang kẹt ở aha gate, **không** phải ở paywall.

## Đọc data tuần đầu

- High `onboarding_complete` nhưng low `home_reached` — post-onboarding paywall đang chặn. Xem `paywall_exit.dwell_ms` và `reason`.
- High `feature_open` nhưng `apply_fail reason=no_permission` chiếm đa số — copy / timing accessibility.
- `apply_fail reason=limit` rồi `paywall_impression` mà không có `paywall_purchase_started` — offer / creative paywall quota, không phải acquisition.
- Tab switch có `ad_interstitial result=shown` rồi không có `feature_open` — interstitial đang giết exploration.

## Debug

Debug builds log mọi event ra Logcat tag `TrackingServices`. Confirm Firebase DebugView trước khi mark conversion trên Google Ads.
