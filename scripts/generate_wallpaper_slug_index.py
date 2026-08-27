#!/usr/bin/env python3
from __future__ import annotations

import argparse
import datetime as dt
import json
import os
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Optional


SUPPORTED_IMAGE_EXTS = {".png", ".jpg", ".jpeg", ".webp"}
STOP_WORDS = {
    "a",
    "an",
    "and",
    "are",
    "as",
    "at",
    "be",
    "but",
    "by",
    "for",
    "from",
    "if",
    "in",
    "into",
    "is",
    "it",
    "its",
    "no",
    "not",
    "of",
    "on",
    "or",
    "our",
    "so",
    "that",
    "the",
    "their",
    "then",
    "these",
    "this",
    "those",
    "to",
    "up",
    "we",
    "with",
    "without",
    "you",
    "your",
    # common file noise
    "douyin",
    "tiktok",
    "wtf",
    "gemini",
    "generated",
    "image",
    "thumb",
}


def slugify(value: str) -> str:
    # Keep consistent with existing wallpaper pack scripts in this repo.
    return re.sub(r"[^a-z0-9]+", "_", value.lower()).strip("_")


def is_hidden_path(path: Path) -> bool:
    return any(part.startswith(".") for part in path.parts)


def read_text_or_empty(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except Exception:
        return ""


def split_keywords(*values: str) -> list[str]:
    raw = " ".join(v for v in values if v).lower()
    parts = re.split(r"[^a-z0-9]+", raw)
    keywords: set[str] = set()
    for part in parts:
        if not part:
            continue
        # Drop pure numbers; keep alphanum tokens like "3d".
        if part.isdigit():
            continue
        if not re.search(r"[a-z]", part):
            continue
        if len(part) < 2:
            continue
        if part in STOP_WORDS:
            continue
        keywords.add(part)
    return sorted(keywords)


def load_wallpaper_manifest(manifest_path: Path) -> dict[str, dict]:
    # Manifest format is created by scripts/generate_wallpaper_packs.py and includes title/description.
    try:
        data = json.loads(manifest_path.read_text(encoding="utf-8"))
        if isinstance(data, list):
            out: dict[str, dict] = {}
            for item in data:
                if isinstance(item, dict) and isinstance(item.get("id"), str):
                    out[item["id"]] = item
            return out
    except Exception:
        pass
    return {}


def extract_delivery_pack_name(build_gradle_kts: Path) -> Optional[str]:
    # build.gradle.kts typically contains: packName.set("wallpaper_01_cat")
    text = read_text_or_empty(build_gradle_kts)
    match = re.search(r'packName\.set\(\s*"([^"]+)"\s*\)', text)
    return match.group(1) if match else None


@dataclass(frozen=True)
class ImageMeta:
    width: Optional[int]
    height: Optional[int]


def _png_dimensions(header: bytes) -> Optional[ImageMeta]:
    # PNG signature (8) + IHDR length/type (8) + width/height (8) => 24 bytes total needed
    if len(header) < 24:
        return None
    if header[:8] != b"\x89PNG\r\n\x1a\n":
        return None
    # IHDR chunk must start at offset 8, and type at 12.
    if header[12:16] != b"IHDR":
        return None
    width = int.from_bytes(header[16:20], "big", signed=False)
    height = int.from_bytes(header[20:24], "big", signed=False)
    if width <= 0 or height <= 0:
        return None
    return ImageMeta(width=width, height=height)


def _jpeg_dimensions(stream: bytes) -> Optional[ImageMeta]:
    # Minimal JPEG segment scan for SOF0/SOF2 markers.
    if len(stream) < 4 or stream[:2] != b"\xff\xd8":
        return None
    idx = 2
    length = len(stream)
    while idx + 4 <= length:
        if stream[idx] != 0xFF:
            idx += 1
            continue
        # Skip fill bytes 0xFF.
        while idx < length and stream[idx] == 0xFF:
            idx += 1
        if idx >= length:
            break
        marker = stream[idx]
        idx += 1
        # Standalone markers without length.
        if marker in {0xD9, 0xDA}:  # EOI / SOS
            break
        if idx + 2 > length:
            break
        seg_len = int.from_bytes(stream[idx : idx + 2], "big", signed=False)
        if seg_len < 2:
            return None
        seg_start = idx + 2
        seg_end = seg_start + (seg_len - 2)
        if seg_end > length:
            break
        # SOF0..SOF3, SOF5..SOF7, SOF9..SOF11, SOF13..SOF15
        if marker in {
            0xC0,
            0xC1,
            0xC2,
            0xC3,
            0xC5,
            0xC6,
            0xC7,
            0xC9,
            0xCA,
            0xCB,
            0xCD,
            0xCE,
            0xCF,
        }:
            if seg_start + 7 > length:
                break
            # [precision (1)][height (2)][width (2)]...
            height = int.from_bytes(stream[seg_start + 1 : seg_start + 3], "big", signed=False)
            width = int.from_bytes(stream[seg_start + 3 : seg_start + 5], "big", signed=False)
            if width <= 0 or height <= 0:
                return None
            return ImageMeta(width=width, height=height)
        idx = seg_end
    return None


def _jpeg_dimensions_stream(f) -> Optional[ImageMeta]:
    # Stream parser to handle large APP/EXIF segments.
    start = f.read(2)
    if start != b"\xff\xd8":
        return None
    while True:
        # Seek marker prefix 0xFF.
        byte = f.read(1)
        while byte and byte != b"\xff":
            byte = f.read(1)
        if not byte:
            return None
        # Skip fill bytes 0xFF.
        byte = f.read(1)
        while byte == b"\xff":
            byte = f.read(1)
        if not byte:
            return None
        marker = byte[0]
        if marker in {0xD9, 0xDA}:  # EOI / SOS
            return None
        seg_len_raw = f.read(2)
        if len(seg_len_raw) != 2:
            return None
        seg_len = int.from_bytes(seg_len_raw, "big", signed=False)
        if seg_len < 2:
            return None
        payload_len = seg_len - 2

        if marker in {
            0xC0,
            0xC1,
            0xC2,
            0xC3,
            0xC5,
            0xC6,
            0xC7,
            0xC9,
            0xCA,
            0xCB,
            0xCD,
            0xCE,
            0xCF,
        }:
            sof = f.read(5)
            if len(sof) != 5:
                return None
            height = int.from_bytes(sof[1:3], "big", signed=False)
            width = int.from_bytes(sof[3:5], "big", signed=False)
            if width > 0 and height > 0:
                return ImageMeta(width=width, height=height)
            return None

        # Skip segment payload.
        f.seek(payload_len, os.SEEK_CUR)


def _webp_dimensions_stream(f) -> Optional[ImageMeta]:
    header = f.read(12)
    if len(header) != 12 or header[:4] != b"RIFF" or header[8:12] != b"WEBP":
        return None

    while True:
        chunk_header = f.read(8)
        if len(chunk_header) != 8:
            return None
        chunk_type = chunk_header[:4]
        chunk_size = int.from_bytes(chunk_header[4:8], "little", signed=False)

        if chunk_type == b"VP8X" and chunk_size >= 10:
            payload = f.read(10)
            if len(payload) != 10:
                return None
            w = 1 + int.from_bytes(payload[4:7], "little", signed=False)
            h = 1 + int.from_bytes(payload[7:10], "little", signed=False)
            if w > 0 and h > 0:
                return ImageMeta(width=w, height=h)
            return None

        if chunk_type == b"VP8 " and chunk_size >= 10:
            payload = f.read(10)
            if len(payload) != 10:
                return None
            if payload[3:6] == b"\x9d\x01\x2a":
                w_raw = int.from_bytes(payload[6:8], "little", signed=False)
                h_raw = int.from_bytes(payload[8:10], "little", signed=False)
                w = w_raw & 0x3FFF
                h = h_raw & 0x3FFF
                if w > 0 and h > 0:
                    return ImageMeta(width=w, height=h)
            return None

        if chunk_type == b"VP8L" and chunk_size >= 5:
            payload = f.read(5)
            if len(payload) != 5:
                return None
            if payload[0] == 0x2F:
                b1, b2, b3, b4 = payload[1], payload[2], payload[3], payload[4]
                w = 1 + (b1 | ((b2 & 0x3F) << 8))
                h = 1 + (((b2 & 0xC0) >> 6) | (b3 << 2) | ((b4 & 0x0F) << 10))
                if w > 0 and h > 0:
                    return ImageMeta(width=w, height=h)
            return None

        # Skip chunk data (+ pad byte for odd sizes).
        f.seek(chunk_size + (chunk_size % 2), os.SEEK_CUR)


def image_dimensions(path: Path) -> ImageMeta:
    suffix = path.suffix.lower()
    try:
        with path.open("rb") as f:
            if suffix == ".png":
                header = f.read(24)
                meta = _png_dimensions(header)
                return meta or ImageMeta(width=None, height=None)
            if suffix in {".jpg", ".jpeg"}:
                meta = _jpeg_dimensions_stream(f)
                return meta or ImageMeta(width=None, height=None)
            if suffix == ".webp":
                meta = _webp_dimensions_stream(f)
                return meta or ImageMeta(width=None, height=None)
    except Exception:
        pass
    return ImageMeta(width=None, height=None)


def iter_wallpaper_pack_dirs(app_pack_root: Path, prefix: str) -> Iterable[Path]:
    for child in sorted(app_pack_root.iterdir()):
        if not child.is_dir():
            continue
        if child.name.startswith(prefix):
            yield child


def iter_image_files(root: Path) -> Iterable[Path]:
    for dirpath, dirnames, filenames in os.walk(root):
        # skip hidden dirs
        dirnames[:] = [d for d in dirnames if not d.startswith(".")]
        for name in filenames:
            if name.startswith("."):
                continue
            p = Path(dirpath) / name
            if is_hidden_path(p):
                continue
            if p.suffix.lower() in SUPPORTED_IMAGE_EXTS and p.is_file():
                yield p


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate slug index for wallpaper_pack_* asset packs.")
    parser.add_argument(
        "--emoji-root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="Emoji project root (default: scripts/..)",
    )
    parser.add_argument(
        "--pack-prefix",
        default="wallpaper_pack_",
        help="Only scan asset pack modules with this prefix.",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=None,
        help="Output JSON path (default: app/src/main/assets/wallpapers/wallpaper_pack_slug_index.json)",
    )
    args = parser.parse_args()

    emoji_root: Path = args.emoji_root.resolve()
    app_pack_root = emoji_root / "app_pack"
    if not app_pack_root.is_dir():
        raise SystemExit(f"app_pack not found: {app_pack_root}")

    output_path: Path = (
        args.output.resolve()
        if args.output
        else (emoji_root / "app" / "src" / "main" / "assets" / "wallpapers" / "wallpaper_pack_slug_index.json")
    )
    output_path.parent.mkdir(parents=True, exist_ok=True)

    manifest_by_id = load_wallpaper_manifest(
        emoji_root / "app" / "src" / "main" / "assets" / "wallpapers" / "wallpaper_pack_manifest.json"
    )

    generated_at = dt.datetime.now().astimezone().replace(microsecond=0).isoformat()

    packs: list[dict] = []
    total_folders = 0
    total_images = 0

    for pack_dir in iter_wallpaper_pack_dirs(app_pack_root, args.pack_prefix):
        module_name = pack_dir.name
        module_slug = module_name[len(args.pack_prefix) :] if module_name.startswith(args.pack_prefix) else module_name
        delivery_pack = extract_delivery_pack_name(pack_dir / "build.gradle.kts")

        assets_root = pack_dir / "src" / "main" / "assets"
        wallpapers_root = assets_root / "wallpapers"
        if not wallpapers_root.is_dir():
            # Not a wallpaper pack with assets.
            continue

        # categoryId is the first folder under wallpapers_root (typical structure wallpapers/<categoryId>/...).
        category_ids = sorted({p.name for p in wallpapers_root.iterdir() if p.is_dir() and not p.name.startswith(".")})
        category_id = category_ids[0] if category_ids else module_slug
        manifest_entry = manifest_by_id.get(category_id, {})
        category_title = (
            manifest_entry.get("title")
            or manifest_entry.get("pack_name")
            or category_id
        )
        category_description = manifest_entry.get("description") or ""
        thumbnail_asset_path = manifest_entry.get("thumbnail_asset_path") or ""
        pack_keywords = split_keywords(
            category_id,
            module_slug,
            delivery_pack or "",
            str(category_title),
            str(manifest_entry.get("pack_name") or ""),
            str(category_description),
        )

        folder_entries: list[dict] = []
        # Collect folders under wallpapers_root, including nested.
        for dirpath, dirnames, _filenames in os.walk(wallpapers_root):
            dirnames[:] = [d for d in dirnames if not d.startswith(".")]
            p = Path(dirpath)
            if p == wallpapers_root:
                continue
            rel = p.relative_to(assets_root).as_posix()
            rel_under_wallpapers = p.relative_to(wallpapers_root).as_posix()
            parts = rel_under_wallpapers.split("/") if rel_under_wallpapers else []
            category_id = parts[0] if parts else ""
            folder_entries.append(
                {
                    "path": rel,
                    "name": p.name,
                    "slug": slugify(p.name),
                    "fullSlug": slugify(rel_under_wallpapers),
                    "depth": len(parts),
                    "categoryId": category_id,
                    "keywords": pack_keywords,
                }
            )

        folder_entries.sort(key=lambda item: item["path"])

        images: list[dict] = []
        for img_path in iter_image_files(wallpapers_root):
            rel_path = img_path.relative_to(assets_root).as_posix()
            rel_under_wallpapers = img_path.relative_to(wallpapers_root).as_posix()
            parts = rel_under_wallpapers.split("/") if rel_under_wallpapers else []
            category_id = parts[0] if parts else ""
            parent_rel = img_path.parent.relative_to(assets_root).as_posix()

            stem = img_path.stem
            is_thumb = stem.endswith("_thumb")
            base_stem = stem[: -len("_thumb")] if is_thumb else stem
            dims = image_dimensions(img_path)

            images.append(
                {
                    "path": rel_path,
                    "file": img_path.name,
                    "id": stem,
                    "baseId": base_stem,
                    "slug": slugify(base_stem),
                    "keywords": sorted(set(pack_keywords).union(split_keywords(base_stem))),
                    "ext": img_path.suffix.lower().lstrip("."),
                    "bytes": img_path.stat().st_size,
                    "w": dims.width,
                    "h": dims.height,
                    "isThumb": is_thumb,
                    "folderPath": parent_rel,
                    "folderSlug": slugify(img_path.parent.name),
                    "categoryId": category_id,
                }
            )

        images.sort(key=lambda item: item["path"])

        total_folders += len(folder_entries)
        total_images += len(images)
        packs.append(
            {
                "moduleName": module_name,
                "moduleSlug": module_slug,
                "deliveryPackName": delivery_pack,
                "category": {
                    "id": category_id,
                    "title": category_title,
                    "description": category_description,
                    "thumbnailAssetPath": thumbnail_asset_path,
                },
                "keywords": pack_keywords,
                "folders": folder_entries,
                "images": images,
            }
        )

    packs.sort(key=lambda item: item["moduleName"])
    output = {
        "schemaVersion": 2,
        "generatedAt": generated_at,
        "root": "app_pack",
        "packPrefix": args.pack_prefix,
        "stats": {
            "packCount": len(packs),
            "folderCount": total_folders,
            "imageCount": total_images,
        },
        "packs": packs,
    }

    output_path.write_text(json.dumps(output, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Wrote: {output_path}")
    print(f"Packs: {len(packs)} | Folders: {total_folders} | Images: {total_images}")


if __name__ == "__main__":
    main()
