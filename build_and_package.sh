#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"
echo "[*] Building UltimatePixelmonRating v1.1.3 (Java 8 / Spigot 1.16.5)"
mvn -q -DskipTests package
OUT_DIR="$DIR/out"
mkdir -p "$OUT_DIR"
cp "target/UltimatePixelmonRating-1.1.3.jar" "$OUT_DIR/" || true
cp "src/main/resources/config.yml" "$OUT_DIR/"
cp "src/main/resources/rewards.yml" "$OUT_DIR/"
echo "[*] Artifacts copied to $OUT_DIR"
