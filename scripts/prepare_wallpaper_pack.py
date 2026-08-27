#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any

try:
    from PIL import Image
except Exception as exc:  # pragma: no cover
    raise SystemExit(
        "Missing dependency Pillow. Install with: pip3 install Pillow\n"
        f"Original error: {exc}"
    )


SUPPORTED_EXTS = {".png", ".jpg", ".jpeg", ".webp"}


def slugify(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", value.lower()).strip("_")


def title_from_id(category_id: str) -> str:
    clean = re.sub(r"^\d+[_-]?", "", category_id)
    clean = clean.replace("-", " ").replace("_", " ").strip()
    return " ".join(token.capitalize() for token in clean.split()) or category_id


def humanize_stem(stem: str) -> str:
    clean = re.sub(r"^\d+[_-]?", "", stem)
    clean = clean.replace("_thumb", "")
    clean = clean.replace("-", " ").replace("_", " ").strip()
    words = [w for w in clean.split() if w]
    return " ".join(w.capitalize() for w in words) or stem


def process_image(src: Path, dst: Path, crop_bottom: int, target_width: int, quality: int) -> None:
    dst.parent.mkdir(parents=True, exist_ok=True)
    with Image.open(src) as img:
        img = img.convert("RGB")
        width, height = img.size
        cropped_height = max(1, height - max(0, crop_bottom))
        if cropped_height != height:
            img = img.crop((0, 0, width, cropped_height))
            width, height = img.size
        if width != target_width:
            target_height = max(1, round(height * (target_width / width)))
            img = img.resize((target_width, target_height), Image.Resampling.LANCZOS)
        img.save(dst, format="WEBP", quality=quality, method=6)


def upsert_manifest(
    manifest_path: Path,
    category_id: str,
    title: str,
    delivery_pack_name: str,
    thumbnail_asset_path: str,
    items: list[dict[str, str]],
) -> None:
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    data: list[dict[str, Any]]
    if manifest_path.exists():
        data = json.loads(manifest_path.read_text(encoding="utf-8"))
    else:
        data = []

    entry = {
        "id": category_id,
        "pack_name": title,
        "delivery_pack_name": delivery_pack_name,
        "title": title,
        "thumbnail_asset_path": thumbnail_asset_path,
        "items": items,
    }

    replaced = False
    for idx, old in enumerate(data):
        if old.get("id") == category_id:
            data[idx] = entry
            replaced = True
            break
    if not replaced:
        data.append(entry)
    data.sort(key=lambda item: item.get("id", ""))
    manifest_path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def write_build_gradle(module_dir: Path, delivery_pack_name: str) -> None:
    content = (
        "plugins {\n"
        "    id(\"com.android.asset-pack\")\n"
        "}\n\n"
        "assetPack {\n"
        f"    packName.set(\"{delivery_pack_name}\")\n"
        "    dynamicDelivery {\n"
        "        deliveryType.set(\"on-demand\")\n"
        "    }\n"
        "}\n"
    )
    (module_dir / "build.gradle.kts").write_text(content, encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(
        description=(
            "Prepare wallpaper category as PAD asset pack: crop bottom, resize width, "
            "convert WebP, and update manifest/thumbnail."
        )
    )
    parser.add_argument("source_dir", type=Path, help="Source folder containing input images")
    parser.add_argument("--category-id", required=True, help="Category id, e.g. 01_cat")
    parser.add_argument("--title", default="", help="Display title for category")
    parser.add_argument("--crop-bottom", type=int, default=200, help="Pixels to crop from bottom")
    parser.add_argument("--width", type=int, default=1200, help="Target output width")
    parser.add_argument("--quality", type=int, default=75, help="WebP quality (0..100)")
    parser.add_argument(
        "--emoji-root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="Emoji project root",
    )
    args = parser.parse_args()

    if not args.source_dir.is_dir():
        raise SystemExit(f"Source folder not found: {args.source_dir}")
    if args.quality < 0 or args.quality > 100:
        raise SystemExit("--quality must be in range 0..100")

    category_id = args.category_id.strip()
    if not category_id:
        raise SystemExit("--category-id cannot be empty")
    title = args.title.strip() or title_from_id(category_id)

    module_slug = slugify(category_id)
    module_name = f"wallpaper_pack_{module_slug}"
    delivery_pack_name = f"wallpaper_{module_slug}"

    emoji_root = args.emoji_root.resolve()
    module_dir = emoji_root / "app_pack" / module_name
    output_dir = module_dir / "src" / "main" / "assets" / "wallpapers" / category_id
    category_thumbs_dir = emoji_root / "app" / "src" / "main" / "assets" / "wallpapers" / "category_thumbs"
    manifest_path = emoji_root / "app" / "src" / "main" / "assets" / "wallpapers" / "wallpaper_pack_manifest.json"

    output_dir.mkdir(parents=True, exist_ok=True)
    category_thumbs_dir.mkdir(parents=True, exist_ok=True)
    write_build_gradle(module_dir, delivery_pack_name)

    sources = sorted(
        p for p in args.source_dir.iterdir() if p.is_file() and p.suffix.lower() in SUPPORTED_EXTS and not p.name.startswith(".")
    )
    if not sources:
        raise SystemExit(f"No supported image files found in: {args.source_dir}")

    items: list[dict[str, str]] = []
    converted: list[Path] = []
    for src in sources:
        out_name = f"{src.stem}.webp"
        dst = output_dir / out_name
        process_image(src, dst, args.crop_bottom, args.width, args.quality)
        converted.append(dst)
        items.append(
            {
                "id": src.stem,
                "name": humanize_stem(src.stem),
                "file": out_name,
                "path": f"wallpapers/{category_id}/{out_name}",
            }
        )

    first_file = converted[0].name
    thumb_name = f"{slugify(category_id)}__{first_file}"
    thumb_dst = category_thumbs_dir / thumb_name
    thumb_dst.write_bytes(converted[0].read_bytes())

    upsert_manifest(
        manifest_path=manifest_path,
        category_id=category_id,
        title=title,
        delivery_pack_name=delivery_pack_name,
        thumbnail_asset_path=f"wallpapers/category_thumbs/{thumb_name}",
        items=items,
    )

    print(f"Prepared module: {module_name}")
    print(f"Delivery pack: {delivery_pack_name}")
    print(f"Output images: {len(converted)}")
    print(f"Manifest: {manifest_path}")


if __name__ == "__main__":
    main()

