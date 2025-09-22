#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"
echo "[*] Building UltimatePixelmonRating v1.6.1 (Java 8 / Spigot 1.16.5)"
mvn -q -DskipTests package
OUT="$DIR/out"
mkdir -p "$OUT"
cp target/UltimatePixelmonRating-1.6.1.jar "$OUT/"
cp src/main/resources/config.yml "$OUT/"
cp src/main/resources/rewards.yml "$OUT/"
cp src/main/resources/arenas.yml "$OUT/"
echo "[*] Artifacts -> $OUT"
