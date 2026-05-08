#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_DIR="$ROOT_DIR/installer/compose"

ask() {
  local prompt="$1"
  local default_value="$2"
  local value
  read -r -p "$prompt [$default_value]: " value
  printf '%s' "${value:-$default_value}"
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

write_env() {
  local env_file="$ROOT_DIR/.env"
  cat > "$env_file" <<EOF
SYSTEM_NAME=$SYSTEM_NAME
INSTALL_MODE=$INSTALL_MODE
PUBLIC_DOMAIN=$PUBLIC_DOMAIN
PRIMARY_MODEL_PROVIDER=$PRIMARY_MODEL_PROVIDER
ECHO_ENABLED=$ECHO_ENABLED
GIT_PROVIDER=$GIT_PROVIDER
POSTGRES_DB=synapse
POSTGRES_USER=synapse
POSTGRES_PASSWORD=synapse_dev_password
REDIS_URL=redis://redis:6379
QDRANT_URL=http://qdrant:6333
EOF
  echo "Wrote $env_file"
}

run_compose() {
  local compose_file="$COMPOSE_DIR/docker-compose.yml"
  if [ "$INSTALL_MODE" = "production" ]; then
    compose_file="$COMPOSE_DIR/docker-compose.prod.yml"
  fi

  docker compose -f "$compose_file" --env-file "$ROOT_DIR/.env" up -d
}

main() {
  echo "SYNAPSE installer"
  require_command docker

  SYSTEM_NAME="$(ask "System name" "SYNAPSE")"
  INSTALL_MODE="$(ask "Install mode: quick, dev, or production" "quick")"
  PUBLIC_DOMAIN="$(ask "Public domain or localhost" "localhost")"
  PRIMARY_MODEL_PROVIDER="$(ask "Primary model provider" "anthropic")"
  ECHO_ENABLED="$(ask "Enable ECHO debug agent: true or false" "true")"
  GIT_PROVIDER="$(ask "Git provider: none, github, gitlab, forgejo, or gitea" "none")"

  case "$INSTALL_MODE" in
    quick|dev|production) ;;
    *) echo "Invalid install mode: $INSTALL_MODE" >&2; exit 1 ;;
  esac

  write_env
  run_compose

  echo "Install complete."
  echo "Dashboard: http://$PUBLIC_DOMAIN:3000"
  echo "Backend: http://$PUBLIC_DOMAIN:8080"
}

main "$@"
