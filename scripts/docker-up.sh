#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "[1/3] Building backend jar..."
(cd "$ROOT_DIR/backend" && mvn -B -DskipTests package)

echo "[2/3] Building frontend dist..."
(cd "$ROOT_DIR/frontend" && npm run build)

echo "[3/3] Starting Docker Compose services..."
(cd "$ROOT_DIR" && docker compose up -d --build)

echo "WorkMate AI Docker services are starting."
