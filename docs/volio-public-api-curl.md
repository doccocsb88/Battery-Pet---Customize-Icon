# Volio public API — curl reference

Base URL (same as `VolioConstants.PUBLIC_API_BASE` in the app):

```text
https://stores.volio.vn/stores/api/v5.0/public/
```

Shared headers (match `VolioNetwork` / Python script):

```bash
-H 'Accept: application/json' \
-H 'User-Agent: EmojiBatteryPort/0.1 (volio_api_crawl.py)'
```

---

## 1. Categories — distinct URL

Lists categories for a store scope (`parent_id` is a UUID).

**home (battery / main store)**

```bash
curl -sS -G 'https://stores.volio.vn/stores/api/v5.0/public/categories/all' \
  --data-urlencode 'parent_id=26bf9d75-7fd5-438d-81b0-b901f5ba2cd5' \
  -H 'Accept: application/json' \
  -H 'User-Agent: EmojiBatteryPort/0.1 (manual-curl)'
```

**sticker**

```bash
curl -sS -G 'https://stores.volio.vn/stores/api/v5.0/public/categories/all' \
  --data-urlencode 'parent_id=9f7b1b47-ee3f-4bf1-b857-09f4c73ffbf0' \
  -H 'Accept: application/json' \
  -H 'User-Agent: EmojiBatteryPort/0.1 (manual-curl)'
```

---

## 2. Items — same path, pagination via query (offset / limit)

This API does **not** use path segments like `/page/1` or `/page/2`. Pagination is:

- `offset`: starting index (0, then `offset + limit`, …)
- `limit`: page size (app uses **500**)

Repeat until `data` is missing, empty, or `len(data) < limit`.

**Example (replace `CATEGORY_ID` with an `id` from `categories_all` response):**

```bash
CATEGORY_ID='YOUR-CATEGORY-UUID'

curl -sS -G 'https://stores.volio.vn/stores/api/v5.0/public/items' \
  --data-urlencode "category_id=${CATEGORY_ID}" \
  --data-urlencode 'offset=0' \
  --data-urlencode 'limit=500' \
  -H 'Accept: application/json' \
  -H 'User-Agent: EmojiBatteryPort/0.1 (manual-curl)'
```

**Next page**

```bash
curl -sS -G 'https://stores.volio.vn/stores/api/v5.0/public/items' \
  --data-urlencode "category_id=${CATEGORY_ID}" \
  --data-urlencode 'offset=500' \
  --data-urlencode 'limit=500' \
  -H 'Accept: application/json' \
  -H 'User-Agent: EmojiBatteryPort/0.1 (manual-curl)'
```

Stop when the JSON has `"data": []` or fewer than `limit` objects in `data`.

---

## 3. Optional: battery troll scope

The shipped app leaves `BATTERY_TROLL_PARENT_ID` empty. If you obtain a UUID from the original app / jadx, pass it to the Python script:

```bash
python3 scripts/volio_api_crawl.py --battery-troll-parent-id 'YOUR-UUID-HERE'
```

Then mirror the same **categories/all** and **items** curl pattern with that `parent_id` for categories and each category’s `id` for items.

---

## 4. Run the automated crawl

From the repo root:

```bash
python3 scripts/volio_api_crawl.py --output-dir ./volio_api_crawl --sleep 0.2
```

Output:

- `home/categories_all.json`, `home/items/<category_id>/page_*_offset_*.json`
- `sticker/...`
- `battery_troll/...` only if `--battery-troll-parent-id` is set.

---

## 5. If another API uses `/page/N` in the path

Some backends use:

```text
GET .../resource?page=1
GET .../resource?page=2
```

or:

```text
GET .../resource/page/1
GET .../resource/page/2
```

For those, increment `page` (or the path segment) until the response is empty or unchanged. The Volio v5 public API used here uses **offset/limit** only; the Python script implements that pattern.
