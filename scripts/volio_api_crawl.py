#!/usr/bin/env python3
"""
Crawl public Volio store JSON API (same as Android VolioStoreApi / VolioConstants).

Endpoints (distinct URLs):
  - GET {base}categories/all?parent_id=<uuid>
  - GET {base}items?category_id=<uuid>&offset=<n>&limit=<page_size>

Pagination: items use offset/limit (not /page/1). The script follows pages until
a response has an empty `data` list or fewer than `limit` items.

Output layout (under --output-dir, default ./volio_api_crawl):
  home/ | sticker/ | battery_troll/
    categories_all.json
    items/<category_id>/page_0000_offset_0.json
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any, Callable, Iterator

# Mirrors app/src/.../VolioConstants.kt
DEFAULT_BASE = "https://stores.volio.vn/stores/api/v5.0/public/"
PARENT_APP_ID = "26bf9d75-7fd5-438d-81b0-b901f5ba2cd5"
STICKER_PARENT_ID = "9f7b1b47-ee3f-4bf1-b857-09f4c73ffbf0"
ITEM_PAGE_SIZE = 500

DEFAULT_HEADERS = {
    "Accept": "application/json",
    "User-Agent": "EmojiBatteryPort/0.1 (volio_api_crawl.py)",
}


def _safe_dir_name(category_id: str) -> str:
    return re.sub(r"[^a-zA-Z0-9._-]", "_", category_id)


def fetch_json(url: str, timeout: float = 60.0) -> dict[str, Any]:
    req = urllib.request.Request(url, headers=DEFAULT_HEADERS, method="GET")
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        raw = resp.read().decode("utf-8")
    return json.loads(raw)


def iter_item_pages(
    base: str,
    category_id: str,
    page_size: int,
    sleep_s: float,
    log: Callable[[str], None],
) -> Iterator[tuple[int, int, dict[str, Any]]]:
    """Yields (page_index, offset, full_json_response). Stops when no more data."""
    offset = 0
    page_index = 0
    while True:
        q = urllib.parse.urlencode(
            {
                "category_id": category_id,
                "offset": str(offset),
                "limit": str(page_size),
            }
        )
        url = urllib.parse.urljoin(base, "items?" + q)
        log(f"GET items offset={offset} limit={page_size} category_id={category_id}")
        if sleep_s > 0:
            time.sleep(sleep_s)
        body = fetch_json(url)
        yield page_index, offset, body
        data = body.get("data") or []
        if not isinstance(data, list):
            log("Warning: response `data` is not a list; stopping pagination.")
            break
        if len(data) == 0:
            break
        if len(data) < page_size:
            break
        offset += page_size
        page_index += 1


def crawl_scope(
    base: str,
    output_root: Path,
    folder_name: str,
    parent_id: str,
    page_size: int,
    sleep_s: float,
    log: Callable[[str], None],
) -> None:
    scope_dir = output_root / folder_name
    scope_dir.mkdir(parents=True, exist_ok=True)

    cat_url = urllib.parse.urljoin(
        base, "categories/all?" + urllib.parse.urlencode({"parent_id": parent_id})
    )
    log(f"[{folder_name}] GET categories/all parent_id={parent_id}")
    if sleep_s > 0:
        time.sleep(sleep_s)
    categories_body = fetch_json(cat_url)
    (scope_dir / "categories_all.json").write_text(
        json.dumps(categories_body, ensure_ascii=False, indent=2), encoding="utf-8"
    )

    rows = categories_body.get("data") or []
    if not isinstance(rows, list):
        log(f"[{folder_name}] categories response has no list data; skipping items.")
        return

    categories = [c for c in rows if isinstance(c, dict) and c.get("status") is not False]
    log(f"[{folder_name}] categories count={len(categories)}")

    for cat in categories:
        cid = cat.get("id")
        if not cid:
            continue
        safe = _safe_dir_name(str(cid))
        items_root = scope_dir / "items" / safe
        items_root.mkdir(parents=True, exist_ok=True)
        for page_index, offset, items_body in iter_item_pages(
            base, str(cid), page_size, sleep_s, log
        ):
            name = f"page_{page_index:04d}_offset_{offset}.json"
            (items_root / name).write_text(
                json.dumps(items_body, ensure_ascii=False, indent=2),
                encoding="utf-8",
            )


def main() -> int:
    parser = argparse.ArgumentParser(description="Crawl Volio public store API to JSON files.")
    parser.add_argument(
        "--base-url",
        default=DEFAULT_BASE,
        help=f"API base with trailing slash (default: {DEFAULT_BASE!r})",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path("volio_api_crawl"),
        help="Root output directory",
    )
    parser.add_argument(
        "--page-size",
        type=int,
        default=ITEM_PAGE_SIZE,
        help="items limit per request",
    )
    parser.add_argument(
        "--sleep",
        type=float,
        default=0.0,
        help="Seconds to sleep between HTTP requests (polite crawl)",
    )
    parser.add_argument(
        "--battery-troll-parent-id",
        default="",
        help="Optional UUID; if set, also crawls battery_troll/ (app default is empty)",
    )
    args = parser.parse_args()
    base = args.base_url
    if not base.endswith("/"):
        base += "/"

    def log(msg: str) -> None:
        print(msg, flush=True)

    out = args.output_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)
    log(f"Output: {out}")

    scopes: list[tuple[str, str]] = [
        ("home", PARENT_APP_ID),
        ("sticker", STICKER_PARENT_ID),
    ]
    bt = (args.battery_troll_parent_id or "").strip()
    if bt:
        scopes.append(("battery_troll", bt))
    else:
        log("Skipping battery_troll (no --battery-troll-parent-id).")

    try:
        for folder_name, parent_id in scopes:
            crawl_scope(
                base=base,
                output_root=out,
                folder_name=folder_name,
                parent_id=parent_id,
                page_size=args.page_size,
                sleep_s=args.sleep,
                log=log,
            )
    except urllib.error.HTTPError as e:
        log(f"HTTP error: {e.code} {e.reason}")
        if e.fp:
            try:
                log(e.fp.read().decode("utf-8", errors="replace")[:2000])
            except Exception:
                pass
        return 1
    except urllib.error.URLError as e:
        log(f"URL error: {e.reason}")
        return 1
    except OSError as e:
        log(f"IO error: {e}")
        return 1

    log("Done.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
