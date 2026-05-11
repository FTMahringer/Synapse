#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────
# SYNAPSE Installer — Linux / macOS
# ──────────────────────────────────────────────────────────────────
# This script:
#   1. Detects your OS and package manager
#   2. Installs Go if not present
#   3. Builds the 'synapse' CLI binary
#   4. Installs it to ~/.local/bin (Linux) or ~/bin (macOS)
#   5. Adds it to your PATH
#   6. Runs the interactive TUI installer
# ──────────────────────────────────────────────────────────────────
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLI_DIR="$ROOT_DIR/packages/cli"

# ── ANSI colors ──────────────────────────────────────────────────
BOLD='\033[1m'
DIM='\033[2m'
BLUE='\033[38;2;123;159;224m'
GREEN='\033[38;2;76;175;135m'
YELLOW='\033[38;2;255;200;50m'
RED='\033[38;2;255;80;80m'
RESET='\033[0m'

info()  { printf "${BOLD}${BLUE}%s${RESET}\n" "$1"; }
ok()    { printf "${GREEN}✓ %s${RESET}\n" "$1"; }
warn()  { printf "${YELLOW}⚠ %s${RESET}\n" "$1"; }
err()   { printf "${RED}✗ %s${RESET}\n" "$1"; }

# ── OS Detection ─────────────────────────────────────────────────
detect_os() {
  case "$(uname -s)" in
    Linux*)  OS="linux" ;;
    Darwin*) OS="darwin" ;;
    *)       OS="unknown" ;;
  esac

  case "$OS" in
    linux)
      if command -v apt &>/dev/null; then PKG_MGR="apt"
      elif command -v dnf &>/dev/null; then PKG_MGR="dnf"
      elif command -v yum &>/dev/null; then PKG_MGR="yum"
      elif command -v pacman &>/dev/null; then PKG_MGR="pacman"
      elif command -v zypper &>/dev/null; then PKG_MGR="zypper"
      elif command -v apk &>/dev/null; then PKG_MGR="apk"
      else PKG_MGR="unknown"; fi
      BIN_DIR="$HOME/.local/bin"
      PROFILE_FILE="$HOME/.bashrc"
      ;;
    darwin)
      if command -v brew &>/dev/null; then PKG_MGR="brew"
      elif command -v port &>/dev/null; then PKG_MGR="macports"
      else PKG_MGR="unknown"; fi
      BIN_DIR="$HOME/bin"
      PROFILE_FILE="$HOME/.zshrc"
      [ -f "$HOME/.bashrc" ] && PROFILE_FILE="$HOME/.bashrc"
      ;;
  esac

  echo "  OS:            $(uname -s)"
  echo "  Package mgr:   ${PKG_MGR:-none detected}"
  echo "  Install dir:   $BIN_DIR"
}

# ── Go Installation ──────────────────────────────────────────────
install_go() {
  info "Installing Go..."

  case "${PKG_MGR:-}" in
    apt)
      sudo apt-get update -qq
      sudo apt-get install -y -qq golang-go
      ;;
    dnf|yum)
      sudo dnf install -y golang
      ;;
    pacman)
      sudo pacman -S --noconfirm go
      ;;
    zypper)
      sudo zypper install -y go
      ;;
    apk)
      sudo apk add go
      ;;
    brew)
      brew install go
      ;;
    macports)
      sudo port install go
      ;;
    *)
      warn "No known package manager. Downloading Go manually..."
      curl -fsSL https://go.dev/dl/go1.26.linux-amd64.tar.gz -o /tmp/go1.26.linux-amd64.tar.gz
      sudo tar -C /usr/local -xzf /tmp/go1.26.linux-amd64.tar.gz
      export PATH=$PATH:/usr/local/go/bin
      echo 'export PATH=$PATH:/usr/local/go/bin' >> "$PROFILE_FILE"
      ;;
  esac

  if command -v go &>/dev/null; then
    ok "Go $(go version | grep -oP 'go\S+' || true) installed"
  else
    err "Go installation failed. Please install manually: https://go.dev/dl/"
    exit 1
  fi
}

# ── Build & Install ──────────────────────────────────────────────
build_and_install() {
  info "Building SYNAPSE CLI..."

  cd "$CLI_DIR"

  # Ensure dependencies
  [ -f go.sum ] || go mod tidy

  # Create bin dir
  mkdir -p "$BIN_DIR"

  # Build to the install directory
  go build -o "$BIN_DIR/synapse" .

  if [ ! -f "$BIN_DIR/synapse" ]; then
    err "Build failed"
    exit 1
  fi

  chmod +x "$BIN_DIR/synapse"
  ok "synapse installed to $BIN_DIR/synapse"

  # Add to PATH if not already there
  if [[ ":$PATH:" != *":$BIN_DIR:"* ]]; then
    export PATH="$PATH:$BIN_DIR"
    echo "export PATH=\"\$PATH:$BIN_DIR\"" >> "$PROFILE_FILE"
    ok "Added $BIN_DIR to PATH in $PROFILE_FILE"
    warn "You may need to restart your terminal or run: source $PROFILE_FILE"
  else
    ok "Already in PATH"
  fi
  echo ""
}

# ══════════════════════════════════════════════════════════════════
# MAIN
# ══════════════════════════════════════════════════════════════════

clear
printf "${BOLD}${BLUE}"
printf "   ╔══════════════════════════════════════════════════╗\n"
printf "   ║                                                  ║\n"
printf "   ║           SYNAPSE INSTALLER                      ║\n"
printf "   ║           Bootstrap Script                       ║\n"
printf "   ║                                                  ║\n"
printf "   ╚══════════════════════════════════════════════════╝\n"
printf "${RESET}\n"

# Step 1: Detect OS
info "Detecting system..."
detect_os
echo ""

# Step 2: Check / Install Go
if command -v go &>/dev/null; then
  GO_VER=$(go version | grep -oP 'go\S+' || true)
  ok "Go ${GO_VER} found"
else
  warn "Go not found — installing..."
  install_go
fi
echo ""

# Step 3: Build and install the synapse CLI
build_and_install

# Step 4: Run the interactive TUI installer
info "Launching interactive installer..."
echo ""
"$BIN_DIR/synapse" install

echo ""
ok "Installation complete!"
ok "You can now use 'synapse' from any terminal."
echo ""
info "Run 'synapse --help' to see available commands."
