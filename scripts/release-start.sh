#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"
PORT="${SERVER_PORT:-8085}"
echo "Starting SAP ERP 适配 on http://127.0.0.1:$PORT"
exec java ${JAVA_OPTS:-} -jar sap-backend-1.0.0.jar --server.port="$PORT" --spring.profiles.active="${SPRING_PROFILES_ACTIVE:-h2}"
