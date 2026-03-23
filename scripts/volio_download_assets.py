#!/usr/bin/env python3
"""
Scan volio_api_crawl JSON (items pages), extract every http(s) asset URL from each item:
  - thumbnail, photo
  - custom_fields: any value that is an http(s) URL (battery, emoji, content/.json, etc.)

Skips categories_all.json. Saves under:
  <crawl_root>/downloaded_assets/<scope>/<category_id>/<item_id>/<role>__<filename>

Uses stdlib only. Re-run safe: skips existing files with same size (use --force to overwrite).
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
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Any

DEFAULT_HEADERS = {
    "Accept": "*/*",
    "User-Agent": "EmojiBatteryPort/0.1 (volio_download_assets.py)",
}

# Strings in custom_fields that are clearly not URLs (short text tags)
_SLUG_KEYS = frozenset({"slug_search", "slug", "tags"})


def _is_url(s: str) -> bool:
    s = s.strip()
    return s.startswith("http://") or s.startswith("https://")


def collect_asset_urls(item: dict[str, Any]) -> list[tuple[str, str]]:
    """Returns (role, url) pairs. Role is used in the saved filename prefix."""
    out: list[tuple[str, str]] = []
    for top in ("thumbnail", "photo"):
        v = item.get(top)
        if isinstance(v, str) and _is_url(v):
            out.append((top, v.strip()))

    cf = item.get("custom_fields")
    if isinstance(cf, dict):
        for key, v in cf.items():
            if key in _SLUG_KEYS:
                continue
            if isinstance(v, str) and _is_url(v):
                out.append((f"custom_fields.{key}", v.strip()))
            elif isinstance(v, dict):
                for sub_k, sub_v in v.items():
                    if isinstance(sub_v, str) and _is_url(sub_v):
                        out.append((f"custom_fields.{key}.{sub_k}", sub_v.strip()))
    return out


def _safe_filename_part(s: str) -> str:
    s = re.sub(r"[^a-zA-Z0-9._-]+", "_", s)
    return s[:180] if len(s) > 180 else s


def url_to_local_name(role: str, url: str) -> str:
    parsed = urllib.parse.urlparse(url)
    base = Path(parsed.path).name or "asset"
    base = _safe_filename_part(base)
    role_part = _safe_filename_part(role.replace(".", "_"))
    return f"{role_part}__{base}"


def download_one(
    url: str,
    dest: Path,
    timeout: float,
    force: bool,
) -> tuple[bool, str]:
    dest.parent.mkdir(parents=True, exist_ok=True)
    if dest.exists() and dest.stat().st_size > 0 and not force:
        return True, "skip_exists"

    tmp = dest.with_suffix(dest.suffix + ".part")
    try:
        req = urllib.request.Request(url, headers=DEFAULT_HEADERS, method="GET")
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            data = resp.read()
        tmp.write_bytes(data)
        tmp.replace(dest)
        return True, "ok"
    except Exception as e:
        if tmp.exists():
            tmp.unlink(missing_ok=True)
        return False, str(e)


def parse_scope_category(crawl_root: Path, json_path: Path) -> tuple[str, str] | None:
    try:
        rel = json_path.relative_to(crawl_root)
    except ValueError:
        return None
    parts = rel.parts
    if len(parts) < 4:
        return None
    if parts[1] != "items":
        return None
    return parts[0], parts[2]


def iter_item_json_files(crawl_root: Path):
    for p in sorted(crawl_root.rglob("*.json")):
        if p.name == "categories_all.json":
            continue
        if "items" not in p.parts:
            continue
        if parse_scope_category(crawl_root, p) is None:
            continue
        yield p


def load_items(path: Path) -> list[dict[str, Any]]:
    try:
        raw = path.read_text(encoding="utf-8")
        doc = json.loads(raw)
    except (OSError, json.JSONDecodeError):
        return []
    data = doc.get("data")
    if not isinstance(data, list):
        return []
    return [x for x in data if isinstance(x, dict)]


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--crawl-root",
        type=Path,
        default=Path("volio_api_crawl"),
        help="Path to volio_api_crawl directory",
    )
    ap.add_argument(
        "--out-subdir",
        default="downloaded_assets",
        help="Created inside crawl-root",
    )
    ap.add_argument("--timeout", type=float, default=120.0)
    ap.add_argument("--sleep", type=float, default=0.0, help="Delay between downloads (serial mode)")
    ap.add_argument("--workers", type=int, default=6, help="Parallel downloads (0 = serial)")
    ap.add_argument("--force", action="store_true", help="Overwrite existing files")
    args = ap.parse_args()

    crawl_root = args.crawl_root.resolve()
    if not crawl_root.is_dir():
        print(f"Not a directory: {crawl_root}", file=sys.stderr)
        return 1

    out_root = crawl_root / args.out_subdir

    tasks: list[tuple[str, Path]] = []
    seen_url_dest: set[tuple[str, str]] = set()

    for jpath in iter_item_json_files(crawl_root):
        scope_cat = parse_scope_category(crawl_root, jpath)
        if scope_cat is None:
            continue
        scope, category_id = scope_cat
        for item in load_items(jpath):
            item_id = item.get("id")
            if not item_id:
                continue
            item_id = str(item_id)
            for role, url in collect_asset_urls(item):
                fname = url_to_local_name(role, url)
                dest = out_root / scope / category_id / item_id / fname
                key = (url, str(dest))
                if key in seen_url_dest:
                    continue
                seen_url_dest.add(key)
                tasks.append((url, dest))

    print(f"Crawl root: {crawl_root}")
    print(f"Output: {out_root}")
    print(f"Files to fetch: {len(tasks)}")

    ok = 0
    fail = 0

    def run_one(t: tuple[str, Path]) -> tuple[bool, str, str]:
        url, dest = t
        success, msg = download_one(url, dest, args.timeout, args.force)
        return success, url, msg

    if args.workers <= 0:
        for i, t in enumerate(tasks, 1):
            success, url, msg = run_one(t)
            if success:
                ok += 1
            else:
                fail += 1
                print(f"FAIL {url}\n  {msg}", flush=True)
            if args.sleep > 0:
                time.sleep(args.sleep)
            if i % 50 == 0:
                print(f"Progress {i}/{len(tasks)} …", flush=True)
    else:
        with ThreadPoolExecutor(max_workers=args.workers) as ex:
            futs = {ex.submit(run_one, t): t for t in tasks}
            done = 0
            for fut in as_completed(futs):
                done += 1
                success, url, msg = fut.result()
                if success:
                    ok += 1
                else:
                    fail += 1
                    print(f"FAIL {url}\n  {msg}", flush=True)
                if done % 100 == 0 or done == len(tasks):
                    print(f"Progress {done}/{len(tasks)} …", flush=True)

    print(f"Done. ok={ok} fail={fail}")
    return 0 if fail == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
