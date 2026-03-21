#!/usr/bin/env python3
"""One-off splitter: extract EmojiBatteryApp.kt screen composables into app/screens/."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app/src/main/java/dev/hai/emojibattery/app/EmojiBatteryApp.kt"
OUT_DIR = ROOT / "app/src/main/java/dev/hai/emojibattery/app/screens"

SECTIONS: list[tuple[str, list[tuple[int, int]]]] = [
    (
        "AppScreenCommon.kt",
        [
            (4053, 4080),
            (4082, 4097),
            (4099, 4117),
            (4119, 4133),
            (4135, 4148),
            (4150, 4167),
            (4169, 4189),
            (4191, 4219),
            (3407, 3424),
            (4288, 4292),
            (4294, 4315),
        ],
    ),
    ("TemplateCards.kt", [(3817, 3855), (3857, 3893)]),
    ("SplashLanguageOnboarding.kt", [(1013, 1405)]),
    ("GestureScreen.kt", [(602, 673), (973, 1011), (3323, 3404)]),
    ("EmojiStickerScreen.kt", [(675, 971), (3616, 3815)]),
    ("HomeAndCustomizeScreens.kt", [(1407, 1946), (2812, 2925), (4222, 4286)]),
    ("StatusBarScreens.kt", [(1948, 2144), (2927, 3084), (3543, 3614)]),
    ("SearchAchievement.kt", [(2146, 2390)]),
    ("SettingsFeedback.kt", [(2392, 2433), (3086, 3130), (3133, 3250), (3294, 3321)]),
    ("RealTimeFeatureScreens.kt", [(2435, 2486), (2760, 2810)]),
    ("BatteryTrollScreen.kt", [(2488, 2758), (3895, 4051)]),
    ("LegacyHomeTabs.kt", [(3426, 3541)]),
]


def main() -> None:
    lines = SRC.read_text(encoding="utf-8").splitlines(keepends=True)
    import_block = "".join(lines[1:149])

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    for filename, ranges in SECTIONS:
        chunks: list[str] = []
        for start, end in ranges:
            chunks.append("".join(lines[start - 1 : end]))
        body = "\n".join(chunks)
        body = body.replace("private fun ", "internal fun ")
        out = (
            "package dev.hai.emojibattery.app.screens\n\n"
            + import_block
            + "\n"
            + body
        )
        (OUT_DIR / filename).write_text(out, encoding="utf-8")
        print(f"Wrote {filename}")

    head = "".join(lines[:600])
    if "import dev.hai.emojibattery.app.screens" not in head:
        parts = head.split("\n", 2)
        # parts[0] = package line, parts[1] may be empty line
        nl = head.find("\n") + 1
        head = head[:nl] + "import dev.hai.emojibattery.app.screens.*\n" + head[nl:]
    SRC.write_text(head, encoding="utf-8")
    print(f"Updated {SRC} (600 lines + screens import)")


if __name__ == "__main__":
    main()
