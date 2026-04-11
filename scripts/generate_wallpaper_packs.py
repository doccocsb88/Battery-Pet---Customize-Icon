from __future__ import annotations

import json
import re
import shutil
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT.parent / "drawable" / "wallpaper"
APP_ASSETS_ROOT = ROOT / "app" / "src" / "main" / "assets" / "wallpapers"
APP_PACK_ROOT = ROOT / "app_pack"
MANIFEST_PATH = APP_ASSETS_ROOT / "wallpaper_pack_manifest.json"
CATEGORY_THUMBS_ROOT = APP_ASSETS_ROOT / "category_thumbs"

IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".webp"}


def slugify(raw: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", raw.lower()).strip("_")


def title_from_folder(folder_name: str) -> str:
    cleaned = re.sub(r"^\d+[_-]?", "", folder_name)
    cleaned = cleaned.replace("-", " ").replace("_", " ").strip()
    return " ".join(part.capitalize() for part in cleaned.split()) or folder_name


def title_from_file(file_stem: str) -> str:
    cleaned = re.sub(r"^\d+[_-]?", "", file_stem)
    cleaned = cleaned.replace("_thumb", "")
    cleaned = cleaned.replace("-", " ").replace("_", " ").strip()
    return " ".join(part.capitalize() for part in cleaned.split()) or file_stem


def image_files(folder: Path) -> list[Path]:
    return sorted(
        file
        for file in folder.iterdir()
        if file.is_file() and not file.name.startswith(".") and file.suffix.lower() in IMAGE_EXTENSIONS
    )


def write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def main() -> None:
    APP_ASSETS_ROOT.mkdir(parents=True, exist_ok=True)
    CATEGORY_THUMBS_ROOT.mkdir(parents=True, exist_ok=True)

    for old_thumb in CATEGORY_THUMBS_ROOT.iterdir():
        if old_thumb.is_file():
            old_thumb.unlink()

    for old_pack in APP_PACK_ROOT.glob("wallpaper_pack_*"):
        if old_pack.is_dir():
            shutil.rmtree(old_pack)

    categories: list[dict] = []

    for folder in sorted(path for path in SOURCE_ROOT.iterdir() if path.is_dir()):
        files = image_files(folder)
        if not files:
            continue

        folder_slug = slugify(folder.name)
        module_name = f"wallpaper_pack_{folder_slug}"
        delivery_pack_name = f"wallpaper_{folder_slug}"
        pack_dir = APP_PACK_ROOT / module_name
        pack_assets_dir = pack_dir / "src" / "main" / "assets" / "wallpapers" / folder.name
        pack_assets_dir.mkdir(parents=True, exist_ok=True)

        items = []
        for file in files:
            shutil.copy2(file, pack_assets_dir / file.name)
            items.append(
                {
                    "id": file.stem,
                    "name": title_from_file(file.stem),
                    "file": file.name,
                    "path": f"wallpapers/{folder.name}/{file.name}",
                }
            )

        thumb_file = files[0]
        thumb_name = f"{folder_slug}__{thumb_file.name}"
        shutil.copy2(thumb_file, CATEGORY_THUMBS_ROOT / thumb_name)

        write_text(
            pack_dir / "build.gradle.kts",
            "\n".join(
                [
                    "plugins {",
                    '    id("com.android.asset-pack")',
                    "}",
                    "",
                    "assetPack {",
                    f'    packName.set("{delivery_pack_name}")',
                    "    dynamicDelivery {",
                    '        deliveryType.set("on-demand")',
                    "    }",
                    "}",
                    "",
                ]
            ),
        )

        categories.append(
            {
                "id": folder.name,
                "pack_name": title_from_folder(folder.name),
                "delivery_pack_name": delivery_pack_name,
                "title": title_from_folder(folder.name),
                "thumbnail_asset_path": f"wallpapers/category_thumbs/{thumb_name}",
                "items": items,
            }
        )

    write_text(MANIFEST_PATH, json.dumps(categories, indent=2, ensure_ascii=False) + "\n")
    print(f"Generated {len(categories)} wallpaper packs.")


if __name__ == "__main__":
    main()
