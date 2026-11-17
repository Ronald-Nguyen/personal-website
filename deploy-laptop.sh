#!/usr/bin/env bash
# Copies the current working tree to the Windows laptop and rebuilds the site there.
# Usage: ./deploy-laptop.sh          (host alias "laptop" from ~/.ssh/config)
#        HOST=other ./deploy-laptop.sh
set -euo pipefail
HOST="${HOST:-laptop}"
REMOTE_DIR="C:/Users/ronal/personal-website"
cd "$(dirname "$0")"

echo "==> Kopiere Projekt nach $HOST:$REMOTE_DIR"
tar czf - \
  --exclude=./frontend/node_modules --exclude=./frontend/dist \
  --exclude=./backend/target --exclude=./.git --exclude=./.impeccable --exclude=./.env \
  . | ssh "$HOST" "tar.exe xzf - -C $REMOTE_DIR"

echo "==> Baue und starte die Container"
ssh "$HOST" "powershell -ExecutionPolicy Bypass -File $REMOTE_DIR/site.ps1 up"

echo "==> Fertig: http://192.168.2.130:3000"
