# CLI Component Framework & Installer Overhaul

> **Status**: Spec / Brainstorming  
> **Target**: `packages/cli/`  
> **Goal**: Reusable TUI component framework + unified `synapse install` command

---

## 1. Motivation

The current installers (`install.sh`, `install.ps1`) are bare-bones linear prompts.  
The CLI has no interactive TUI beyond ANSI output.  

We want:
- A **reusable component framework** (like a mini Bubble Tea kit) so every interactive command looks consistent.
- A **`synapse install`** command that replaces both shell scripts with a polished, cross-platform TUI.
- Components that can be reused by future commands (`synapse config`, `synapse plugins install`, etc.).

---

## 2. Component Framework — Design

### 2.1 Philosophy

Each component is a **self-contained Bubble Tea model** that:
- Has its own `Init()`, `Update()`, `View()`.
- Emits a **result value** when completed (via a shared channel or callback).
- Can be composed into multi-step wizards.

### 2.2 Component Catalog

| Component | Purpose | Used In |
|---|---|---|
| **`Welcome`** | Branded splash screen with version | Installer start |
| **`TextInput`** | Free-text field with validation, masking | Passwords, names, secrets |
| **`SingleSelect`** | Pick one from a list (radio) | Install mode, provider |
| **`MultiSelect`** | Pick N from a list (checkboxes) | Skills, features to enable |
| **`SearchList`** | Filter-as-you-type list | Model providers, plugins |
| **`Toggle`** | On/off switch | ECHO, monitoring |
| **`Progress`** | Spinner / progress bar | Docker compose, downloads |
| **`Summary`** | Read-only key-value review | Pre-install confirmation |
| **`Confirm`** | Yes/no prompt | Destructive actions |
| **`Section`** | Visual section wrapper (borders, headers) | Layout |

### 2.3 Component Contract

```go
// Component is the interface every interactive component satisfies.
type Component[T any] interface {
    Init() tea.Cmd
    Update(tea.Msg) (Component[T], tea.Cmd)
    View() string
    Value() T         // Returns the collected value when done
    Done() bool       // True when component has completed
}
```

### 2.4 Styling System (Lipgloss)

```go
// Theme defines the visual identity for all components.
type Theme struct {
    Background lipgloss.Color
    Surface    lipgloss.Color
    Primary    lipgloss.Color   // Main Agent blue   #7B9FE0
    Secondary  lipgloss.Color   // AI-Firm purple    #B07FE8
    Accent     lipgloss.Color   // Teams orange      #E07B5A
    Echo       lipgloss.Color   // ECHO green        #4CAF87
    Text       lipgloss.Color
    DimText    lipgloss.Color
    Error      lipgloss.Color
    Success    lipgloss.Color
}

// DefaultTheme returns the SYNAPSE brand theme.
func DefaultTheme() Theme { ... }
```

---

## 3. Installer Command — `synapse install`

### 3.1 Command Signature

```
synapse install [flags]

Flags:
  --mode string          Installation mode: quick, dev, production (default "quick")
  --non-interactive      Skip TUI, use defaults / flags
  --config-file string   Path to YAML config for non-interactive install
  --skip-docker          Generate .env only, don't run compose
  --profile string       Config profile to save connection info (default "default")
```

### 3.2 Installation Flow (Interactive)

```
┌─────────────────────────────────────────────┐
│  ╔═══════════════════════════════════════╗  │
│  ║         SYNAPSE INSTALLER v2.x.x     ║  │
│  ╚═══════════════════════════════════════╝  │
│                                             │
│  ┌─ Prerequisites ───────────────────────┐  │
│  │  ✓ Docker found                       │  │
│  │  ✓ Docker Compose found               │  │
│  │  ⚠ Port 8080 in use (will reassign)   │  │
│  └────────────────────────────────────────┘  │
│                                             │
│  ┌─ Installation Mode ────────────────────┐  │
│  │  ○ Quick Start   (defaults, evaluate)  │  │
│  │  ● Development   (debug tools, hot     │  │
│  │                   reload, ECHO on)      │  │
│  │  ○ Production    (hardened, optimized)  │  │
│  └────────────────────────────────────────┘  │
│                                             │
│  ┌─ System Configuration ────────────────┐  │
│  │  System Name     [SYNAPSE           ] │  │
│  │  Public Domain   [localhost         ] │  │
│  │  Installation    [./synapse-data    ] │  │
│  └────────────────────────────────────────┘  │
│                                             │
│  ┌─ Security ────────────────────────────┐  │
│  │  Postgres Password  [•••••••••••••  ] │  │
│  │  JWT Secret         [•••••••••••••  ] │  │
│  │  Encryption Key     [•••••••••••••  ] │  │
│  └────────────────────────────────────────┘  │
│                                             │
│  ┌─ Model Provider ──────────────────────┐  │
│  │  Search: [anthropic                ]  │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │ ● Anthropic (Claude)    API Key │  │  │
│  │  │ ○ OpenAI (GPT)          API Key │  │  │
│  │  │ ○ DeepSeek              API Key │  │  │
│  │  │ ○ Ollama (local)        Self-host│  │  │
│  │  │ ○ Azure OpenAI          Subscription│  │
│  │  │ ○ Google (Gemini)       API Key │  │  │
│  │  └─────────────────────────────────┘  │  │
│  └────────────────────────────────────────┘  │
│                                             │
│  ┌─ Features ────────────────────────────┐  │
│  │  ECHO Debug Agent     [●] Enabled     │  │
│  │  Grafana Monitoring   [○] Disabled    │  │
│  │  Git Integration      [none        ▼] │  │
│  └────────────────────────────────────────┘  │
│                                             │
│  ┌─ Summary ─────────────────────────────┐  │
│  │  Mode:        Development             │  │
│  │  System:      SYNAPSE                 │  │
│  │  Domain:      localhost               │  │
│  │  Provider:    Anthropic (API Key)     │  │
│  │  ECHO:        Enabled                 │  │
│  │  Ports:       8080→8081, 5432, 6379,  │  │
│  │               6333, 3000              │  │
│  │                                        │  │
│  │  [●] I've reviewed the configuration  │  │
│  │                                        │  │
│  │  [ Proceed ]  [ Save Config ]  [ Exit ]│  │
│  └────────────────────────────────────────┘  │
│                                             │
│  ┌─ Installing ──────────────────────────┐  │
│  │  [████████████░░░░░░] 72%             │  │
│  │  Starting PostgreSQL...               │  │
│  │  Running Flyway migrations...         │  │
│  └────────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

### 3.3 Port Allocation Strategy

Default ports (chosen to avoid common conflicts):

| Service | Default Port | Why This Port |
|---|---|---|
| Backend API | **8081** | 8080 is commonly taken by Jenkins, dev servers |
| Dashboard | **3000** | Standard for Node.js, but common — user can change |
| PostgreSQL | **5432** | Standard, but we check first |
| Redis | **6379** | Standard |
| Qdrant | **6333** | Standard for Qdrant |
| Grafana | **3090** | 3000 is dashboard, 3090 is less common |

The installer will:
1. Check if each port is available.
2. If taken, suggest the next available port.
3. Show the final port mapping in the summary.

### 3.4 Model Provider — Subscription Support

Providers can have different auth modes:

| Provider | API Key | Subscription (OAuth/Managed) |
|---|---|---|
| Anthropic | ✅ | ❌ |
| OpenAI | ✅ | ✅ (Azure/AWS Bedrock) |
| DeepSeek | ✅ | ❌ |
| Ollama | ❌ (self-hosted) | ❌ |
| Azure OpenAI | ❌ | ✅ (Azure subscription) |
| Google Gemini | ✅ | ✅ (Vertex AI) |
| AWS Bedrock | ❌ | ✅ (AWS subscription) |

The `SearchList` component will show the provider name and auth type, and when selected, prompt for the appropriate credentials.

### 3.5 Non-Interactive Mode

```yaml
# install-config.yaml
mode: production
systemName: MySynapse
domain: synapse.example.com
dataDir: /opt/synapse/data
security:
  postgresPassword: <secret>
  jwtSecret: <secret>
  encryptionKey: <secret>
provider:
  primary: anthropic
  credentials:
    type: api-key
    value: sk-ant-...
features:
  echo: false
  grafana: true
  git: github
ports:
  backend: 8081
  dashboard: 3000
  postgres: 5432
  redis: 6379
  qdrant: 6333
  grafana: 3090
```

---

## 4. File Structure

```
packages/cli/
├── cmd/
│   ├── install.go              # New: `synapse install` command
│   └── ... (existing)
├── internal/
│   ├── components/             # NEW: Reusable TUI components
│   │   ├── welcome.go          # Branded splash
│   │   ├── textinput.go        # Validated text input
│   │   ├── singleselect.go     # Radio list
│   │   ├── multiselect.go      # Checkbox list
│   │   ├── searchlist.go       # Filter-as-you-type
│   │   ├── toggle.go           # On/off switch
│   │   ├── progress.go         # Spinner + progress bar
│   │   ├── summary.go          # Key-value review
│   │   ├── confirm.go          # Yes/no
│   │   └── section.go          # Border wrapper
│   ├── theme/
│   │   └── theme.go            # Lipgloss theme (SYNAPSE brand colors)
│   ├── installer/              # NEW: Installer-specific logic
│   │   ├── model.go            # Main wizard model
│   │   ├── steps.go            # Step definitions
│   │   ├── config.go           # .env generation
│   │   ├── docker.go           # Docker compose operations
│   │   ├── prereqs.go          # Prerequisite checks
│   │   └── ports.go            # Port availability + allocation
│   └── ... (existing)
```

---

## 5. Implementation Phases

### Phase 1 — Foundation (this session)
1. Create `internal/theme/` with SYNAPSE brand colors.
2. Create `internal/components/` with core components:
   - `Section` (border wrapper)
   - `TextInput` (validated, masked)
   - `SingleSelect` (radio list)
   - `Confirm` (yes/no)
3. Create `cmd/install.go` skeleton.
4. Create PS1 proof-of-concept transitional script.

### Phase 2 — Installer Logic
5. Port `.env` generation from existing scripts.
6. Add Docker compose execution.
7. Add prerequisite checks (Docker, ports).
8. Add model provider selection with subscription support.

### Phase 3 — Advanced Components
9. `SearchList` with filtering.
10. `MultiSelect` for skills/features.
11. `Progress` bar for long operations.
12. `Summary` review screen.

### Phase 4 — Polish
13. Non-interactive mode with YAML config.
14. Config profile saving.
15. Error handling and rollback.
16. Cross-platform testing.

---

## 6. PS1 Proof-of-Concept

See [`installer/install.tui.ps1`](../../installer/install.tui.ps1) for a transitional PowerShell script that demonstrates the visual concept using ANSI escape codes — no external dependencies.

This script is **not the final product** but a visual prototype to validate the UX before building the Go TUI.

---

## 7. Open Questions

1. Should we keep the old `install.sh` and `install.ps1` as fallbacks, or replace them entirely?
   - **Proposal**: Keep as `install.ps1` → rename to `install.legacy.ps1`, new one becomes `install.ps1`.
2. Docker Go SDK vs shelling out?
   - **Proposal**: Start with `os/exec` shell-out (simpler, matches existing scripts), add SDK later if needed.
3. Should the component framework be published as a separate Go module?
   - **Proposal**: Keep internal for now, extract if another project needs it.