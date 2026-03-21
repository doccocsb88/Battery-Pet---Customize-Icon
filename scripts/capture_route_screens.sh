#!/bin/zsh
set -euo pipefail

ADB="${HOME}/Library/Android/sdk/platform-tools/adb"
DEVICE="${1:-emulator-5554}"
OUT_DIR="${2:-artifacts/screenshots}"
PACKAGE="dev.hai.emojibattery"
ACTIVITY="${PACKAGE}/.MainActivity"

mkdir -p "${OUT_DIR}"

typeset -a ROUTES=(
  "language|language"
  "home|home"
  "customize|customize"
  "gesture|gesture"
  "achievement|achievement"
  "statusbar_custom|statusbar_custom"
  "legacy_battery|legacy_battery"
  "search|search"
  "settings|settings"
  "real_time|real_time"
  "battery_troll|battery_troll"
  "emoji_sticker|emoji_sticker"
  "feature_wifi|feature/Wi-Fi"
  "feature_data|feature/Data"
  "feature_signal|feature/Signal"
  "feature_airplane|feature/Airplane"
  "feature_hotspot|feature/Hotspot"
  "feature_ringer|feature/Ringer"
  "feature_battery|feature/Battery"
  "feature_emoji|feature/Emoji"
  "feature_theme|feature/Theme"
  "feature_settings|feature/Settings"
)

for entry in "${ROUTES[@]}"; do
  label="${entry%%|*}"
  route="${entry#*|}"
  "${ADB}" -s "${DEVICE}" shell am force-stop "${PACKAGE}" >/dev/null
  "${ADB}" -s "${DEVICE}" shell am start -n "${ACTIVITY}" --es route "${route}" >/dev/null
  sleep 3
  "${ADB}" -s "${DEVICE}" exec-out screencap -p > "${OUT_DIR}/${label}.png"
done
