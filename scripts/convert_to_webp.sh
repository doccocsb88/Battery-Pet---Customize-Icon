#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  scripts/convert_to_webp.sh <input_path> [output_path] [--quality <0-100>] [--keep-original]

Examples:
  scripts/convert_to_webp.sh app/src/main/res/drawable
  scripts/convert_to_webp.sh ./input.png ./output.webp --quality 75
  scripts/convert_to_webp.sh ./assets ./assets_webp --quality 75 --keep-original

Notes:
  - Default quality is 75.
  - If input_path is a directory and output_path is omitted, files are converted in-place.
  - Supported input formats: png, jpg, jpeg, bmp, tiff.
EOF
}

if [[ $# -lt 1 ]]; then
  usage
  exit 1
fi

if ! command -v cwebp >/dev/null 2>&1; then
  echo "Missing dependency: cwebp (install libwebp)." >&2
  exit 1
fi

INPUT_PATH="$1"
shift

OUTPUT_PATH=""
QUALITY=75
KEEP_ORIGINAL=0

if [[ $# -gt 0 && "$1" != --* ]]; then
  OUTPUT_PATH="$1"
  shift
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --quality)
      QUALITY="${2:-}"
      shift 2
      ;;
    --keep-original)
      KEEP_ORIGINAL=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if ! [[ "$QUALITY" =~ ^[0-9]+$ ]] || (( QUALITY < 0 || QUALITY > 100 )); then
  echo "Invalid --quality value: $QUALITY (expected 0..100)" >&2
  exit 1
fi

SUPPORTED_REGEX='.*\.(png|jpg|jpeg|bmp|tiff)$'

convert_file() {
  local src="$1"
  local dst="$2"
  mkdir -p "$(dirname "$dst")"
  cwebp -quiet -q "$QUALITY" "$src" -o "$dst"
  if [[ "$KEEP_ORIGINAL" -eq 0 && "$src" != "$dst" ]]; then
    rm -f "$src"
  fi
  echo "Converted: $src -> $dst"
}

if [[ -f "$INPUT_PATH" ]]; then
  if [[ ! "$INPUT_PATH" =~ $SUPPORTED_REGEX ]]; then
    echo "Unsupported input file format: $INPUT_PATH" >&2
    exit 1
  fi
  if [[ -z "$OUTPUT_PATH" ]]; then
    OUTPUT_PATH="${INPUT_PATH%.*}.webp"
  fi
  convert_file "$INPUT_PATH" "$OUTPUT_PATH"
  exit 0
fi

if [[ ! -d "$INPUT_PATH" ]]; then
  echo "Input path does not exist: $INPUT_PATH" >&2
  exit 1
fi

if [[ -z "$OUTPUT_PATH" ]]; then
  OUTPUT_PATH="$INPUT_PATH"
fi

while IFS= read -r -d '' src; do
  rel="${src#"$INPUT_PATH"/}"
  if [[ "$OUTPUT_PATH" == "$INPUT_PATH" ]]; then
    dst="${src%.*}.webp"
  else
    dst="$OUTPUT_PATH/${rel%.*}.webp"
  fi
  convert_file "$src" "$dst"
done < <(find "$INPUT_PATH" -type f \( -iname '*.png' -o -iname '*.jpg' -o -iname '*.jpeg' -o -iname '*.bmp' -o -iname '*.tiff' \) -print0)

