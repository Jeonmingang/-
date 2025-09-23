#!/usr/bin/env bash
set -euo pipefail
mvn -q -DskipTests -U clean package
