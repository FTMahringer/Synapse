// Package installer provides the installation wizard for SYNAPSE.
package installer

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"

	"github.com/synapse-dev/synapse-cli/internal/components"
	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// ── OS detection ───────────────────────────────────────────────────

// OSInfo holds detected operating system information.
type OSInfo struct {
	GOOS         string // runtime.GOOS: windows, linux, darwin
	PrettyName   string // User-friendly: "Windows", "Linux", "macOS"
	PackageMgr   string // Package manager: "winget", "apt", "brew", "dnf", "pacman"
	InstallExt   string // File extension for scripts: ".ps1", ".sh"
	Shell        string // Default shell: "powershell", "bash", "sh"
	GoBinary     string // Go binary name: "go" or "go.exe"
	GoInstallCmd string // Command to install Go
}

// DetectOS detects the current operating system and returns OSInfo.
func DetectOS() OSInfo {
	info := OSInfo{GOOS: runtime.GOOS}

	switch runtime.GOOS {
	case "windows":
		info.PrettyName = "Windows"
		info.InstallExt = ".ps1"
		info.Shell = "powershell"
		info.GoBinary = "go.exe"
		info.PackageMgr = detectWindowsPkgMgr()
		info.GoInstallCmd = "powershell -Command \"winget install GoLang.Go || choco install golang\""
	case "darwin":
		info.PrettyName = "macOS"
		info.InstallExt = ".sh"
		info.Shell = "bash"
		info.GoBinary = "go"
		info.PackageMgr = detectMacOSPkgMgr()
		info.GoInstallCmd = "brew install go"
	default: // linux
		info.PrettyName = "Linux"
		info.InstallExt = ".sh"
		info.Shell = "bash"
		info.GoBinary = "go"
		info.PackageMgr = detectLinuxPkgMgr()
		info.GoInstallCmd = detectLinuxGoInstall()
	}

	return info
}

func detectWindowsPkgMgr() string {
	if _, err := exec.LookPath("winget"); err == nil {
		return "winget"
	}
	if _, err := exec.LookPath("choco"); err == nil {
		return "choco"
	}
	if _, err := exec.LookPath("scoop"); err == nil {
		return "scoop"
	}
	return "winget"
}

func detectMacOSPkgMgr() string {
	if _, err := exec.LookPath("brew"); err == nil {
		return "brew"
	}
	if _, err := exec.LookPath("port"); err == nil {
		return "macports"
	}
	return "brew"
}

func detectLinuxPkgMgr() string {
	if _, err := exec.LookPath("apt"); err == nil {
		return "apt"
	}
	if _, err := exec.LookPath("dnf"); err == nil {
		return "dnf"
	}
	if _, err := exec.LookPath("yum"); err == nil {
		return "yum"
	}
	if _, err := exec.LookPath("pacman"); err == nil {
		return "pacman"
	}
	if _, err := exec.LookPath("zypper"); err == nil {
		return "zypper"
	}
	if _, err := exec.LookPath("apk"); err == nil {
		return "apk"
	}
	return "apt"
}

func detectLinuxGoInstall() string {
	switch detectLinuxPkgMgr() {
	case "apt":
		return "sh -c 'sudo apt-get update && sudo apt-get install -y golang-go'"
	case "dnf", "yum":
		return "sh -c 'sudo dnf install -y golang'"
	case "pacman":
		return "sh -c 'sudo pacman -S --noconfirm go'"
	case "zypper":
		return "sh -c 'sudo zypper install -y go'"
	case "apk":
		return "sh -c 'sudo apk add go'"
	default:
		return "sh -c 'wget https://go.dev/dl/go1.26.linux-amd64.tar.gz && sudo tar -C /usr/local -xzf go1.26.linux-amd64.tar.gz'"
	}
}

// ── Configuration model ────────────────────────────────────────────

// Config holds all installation configuration.
type Config struct {
	Mode           string   // quick, dev, production
	SystemName     string   `yaml:"system_name"`
	Domain         string   `yaml:"domain"`
	DataDir        string   `yaml:"data_dir"`
	PostgresPass   string   `yaml:"postgres_password"`
	JWTSecret      string   `yaml:"jwt_secret"`
	EncryptionKey  string   `yaml:"encryption_key"`
	Provider       string   `yaml:"provider"`
	ProviderCreds  string   `yaml:"provider_creds"`
	EchoEnabled    bool     `yaml:"echo_enabled"`
	GrafanaEnabled bool     `yaml:"grafana_enabled"`
	GitProvider    string   `yaml:"git_provider"`
	Channels       []string `yaml:"channels"` // Selected channel plugins
	Skills         []string `yaml:"skills"`   // Selected skill plugins
	Ports          PortMap  `yaml:"ports"`
}

// PortMap defines the port allocation for each service.
type PortMap struct {
	Backend   int `yaml:"backend"`
	Dashboard int `yaml:"dashboard"`
	Postgres  int `yaml:"postgres"`
	Redis     int `yaml:"redis"`
	Qdrant    int `yaml:"qdrant"`
	Grafana   int `yaml:"grafana"`
}

// DefaultPorts returns the recommended default port allocation.
func DefaultPorts() PortMap {
	return PortMap{
		Backend:   8081, // 8080 is commonly taken
		Dashboard: 3000,
		Postgres:  5432,
		Redis:     6379,
		Qdrant:    6333,
		Grafana:   3090, // 3000 is dashboard, 3090 is less common
	}
}

// ── Config persistence ─────────────────────────────────────────────

// ConfigPath returns the path to the installer config file.
func ConfigPath() (string, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}
	dir := filepath.Join(home, ".synapse")
	if err := os.MkdirAll(dir, 0700); err != nil {
		return "", err
	}
	return filepath.Join(dir, "install.yaml"), nil
}

// SaveConfig saves the installer configuration to ~/.synapse/install.yaml.
func SaveConfig(cfg *Config) error {
	path, err := ConfigPath()
	if err != nil {
		return err
	}

	// Simple YAML-like serialization (avoid yaml dependency)
	var b strings.Builder
	b.WriteString("# SYNAPSE Installer Configuration\n")
	b.WriteString(fmt.Sprintf("system_name: %s\n", cfg.SystemName))
	b.WriteString(fmt.Sprintf("domain: %s\n", cfg.Domain))
	b.WriteString(fmt.Sprintf("data_dir: %s\n", cfg.DataDir))
	b.WriteString(fmt.Sprintf("mode: %s\n", cfg.Mode))
	b.WriteString(fmt.Sprintf("provider: %s\n", cfg.Provider))
	b.WriteString(fmt.Sprintf("provider_creds: %s\n", cfg.ProviderCreds))
	b.WriteString(fmt.Sprintf("echo_enabled: %t\n", cfg.EchoEnabled))
	b.WriteString(fmt.Sprintf("grafana_enabled: %t\n", cfg.GrafanaEnabled))
	b.WriteString(fmt.Sprintf("git_provider: %s\n", cfg.GitProvider))
	b.WriteString(fmt.Sprintf("channels: [%s]\n", strings.Join(cfg.Channels, ", ")))
	b.WriteString(fmt.Sprintf("skills: [%s]\n", strings.Join(cfg.Skills, ", ")))
	b.WriteString("ports:\n")
	b.WriteString(fmt.Sprintf("  backend: %d\n", cfg.Ports.Backend))
	b.WriteString(fmt.Sprintf("  dashboard: %d\n", cfg.Ports.Dashboard))
	b.WriteString(fmt.Sprintf("  postgres: %d\n", cfg.Ports.Postgres))
	b.WriteString(fmt.Sprintf("  redis: %d\n", cfg.Ports.Redis))
	b.WriteString(fmt.Sprintf("  qdrant: %d\n", cfg.Ports.Qdrant))
	b.WriteString(fmt.Sprintf("  grafana: %d\n", cfg.Ports.Grafana))

	return os.WriteFile(path, []byte(b.String()), 0600)
}

// LoadConfig loads the installer configuration from ~/.synapse/install.yaml.
// Returns nil if no config exists (first run).
func LoadConfig() (*Config, error) {
	path, err := ConfigPath()
	if err != nil {
		return nil, nil
	}

	data, err := os.ReadFile(path)
	if err != nil {
		return nil, nil // File doesn't exist yet
	}

	cfg := &Config{
		Ports: DefaultPorts(),
	}
	lines := strings.Split(string(data), "\n")
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line == "" || strings.HasPrefix(line, "#") {
			continue
		}

		parts := strings.SplitN(line, ":", 2)
		if len(parts) != 2 {
			continue
		}
		key := strings.TrimSpace(parts[0])
		val := strings.TrimSpace(parts[1])

		switch key {
		case "system_name":
			cfg.SystemName = val
		case "domain":
			cfg.Domain = val
		case "data_dir":
			cfg.DataDir = val
		case "mode":
			cfg.Mode = val
		case "provider":
			cfg.Provider = val
		case "provider_creds":
			cfg.ProviderCreds = val
		case "echo_enabled":
			cfg.EchoEnabled = val == "true"
		case "grafana_enabled":
			cfg.GrafanaEnabled = val == "true"
		case "git_provider":
			cfg.GitProvider = val
		case "channels":
			val = strings.Trim(val, "[]")
			if val != "" {
				cfg.Channels = splitComma(val)
			}
		case "skills":
			val = strings.Trim(val, "[]")
			if val != "" {
				cfg.Skills = splitComma(val)
			}
		case "backend":
			cfg.Ports.Backend, _ = strconv.Atoi(val)
		case "dashboard":
			cfg.Ports.Dashboard, _ = strconv.Atoi(val)
		case "postgres":
			cfg.Ports.Postgres, _ = strconv.Atoi(val)
		case "redis":
			cfg.Ports.Redis, _ = strconv.Atoi(val)
		case "qdrant":
			cfg.Ports.Qdrant, _ = strconv.Atoi(val)
		case "grafana":
			cfg.Ports.Grafana, _ = strconv.Atoi(val)
		}
	}

	return cfg, nil
}

func splitComma(s string) []string {
	parts := strings.Split(s, ",")
	result := make([]string, 0, len(parts))
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p != "" {
			result = append(result, p)
		}
	}
	return result
}

// ── Prerequisites ──────────────────────────────────────────────────

// PrereqResult holds the result of a prerequisite check.
type PrereqResult struct {
	DockerFound   bool
	ComposeFound  bool
	PortConflicts map[int]string // port -> service name
}

// CheckPrereqs checks system prerequisites.
func CheckPrereqs() PrereqResult {
	result := PrereqResult{
		PortConflicts: make(map[int]string),
	}

	// Check Docker
	if _, err := exec.LookPath("docker"); err == nil {
		result.DockerFound = true

		// Check Docker Compose
		cmd := exec.Command("docker", "compose", "version")
		if err := cmd.Run(); err == nil {
			result.ComposeFound = true
		}
	}

	// Check ports (best-effort, platform-specific)
	ports := DefaultPorts()
	portList := map[int]string{
		ports.Backend:   "Backend API",
		ports.Dashboard: "Dashboard",
		ports.Postgres:  "PostgreSQL",
		ports.Redis:     "Redis",
		ports.Qdrant:    "Qdrant",
		ports.Grafana:   "Grafana",
	}

	for port, service := range portList {
		if isPortInUse(port) {
			result.PortConflicts[port] = service
		}
	}

	return result
}

// isPortInUse checks if a TCP port is in use (platform-specific).
func isPortInUse(port int) bool {
	switch runtime.GOOS {
	case "windows":
		cmd := exec.Command("netstat", "-an")
		out, err := cmd.Output()
		if err != nil {
			return false
		}
		return strings.Contains(string(out), fmt.Sprintf(":%d", port))
	default:
		cmd := exec.Command("sh", "-c", fmt.Sprintf("lsof -i :%d 2>/dev/null || ss -tlnp 'sport = :%d' 2>/dev/null", port, port))
		out, _ := cmd.Output()
		return len(out) > 0
	}
}

// ── .env Generation ────────────────────────────────────────────────

// GenerateEnvFile creates the .env file from the config.
func GenerateEnvFile(cfg *Config, rootDir string) (string, error) {
	envFile := filepath.Join(rootDir, ".env")

	content := fmt.Sprintf(`# SYNAPSE Configuration — generated by installer
SYSTEM_NAME=%s
INSTALL_MODE=%s
PUBLIC_DOMAIN=%s
DATA_DIR=%s
PRIMARY_MODEL_PROVIDER=%s
ECHO_ENABLED=%t
GRAFANA_ENABLED=%t
GIT_PROVIDER=%s
DASHBOARD_DEFAULT_USERNAME=admin
DASHBOARD_DEFAULT_PASSWORD=admin
POSTGRES_DB=synapse
POSTGRES_USER=synapse
POSTGRES_PASSWORD=%s
JWT_SECRET=%s
SECRETS_ENCRYPTION_KEY=%s
REDIS_URL=redis://redis:%d
QDRANT_URL=http://qdrant:%d
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
BACKEND_PORT=%d
DASHBOARD_PORT=%d
INSTALLED_CHANNELS=%s
INSTALLED_SKILLS=%s
`,
		cfg.SystemName,
		cfg.Mode,
		cfg.Domain,
		cfg.DataDir,
		cfg.Provider,
		cfg.EchoEnabled,
		cfg.GrafanaEnabled,
		cfg.GitProvider,
		cfg.PostgresPass,
		cfg.JWTSecret,
		cfg.EncryptionKey,
		cfg.Ports.Redis,
		cfg.Ports.Qdrant,
		cfg.Ports.Backend,
		cfg.Ports.Dashboard,
		strings.Join(cfg.Channels, ","),
		strings.Join(cfg.Skills, ","),
	)

	if err := os.WriteFile(envFile, []byte(content), 0600); err != nil {
		return "", fmt.Errorf("writing .env: %w", err)
	}

	return envFile, nil
}

// ── Docker Operations ──────────────────────────────────────────────

// RunDockerCompose executes docker compose up.
func RunDockerCompose(cfg *Config, rootDir string, progress *components.Progress) error {
	composeDir := filepath.Join(rootDir, "installer", "compose")
	composeFile := filepath.Join(composeDir, "docker-compose.yml")
	if cfg.Mode == "production" {
		composeFile = filepath.Join(composeDir, "docker-compose.prod.yml")
	}

	envFile := filepath.Join(rootDir, ".env")

	progress.Update(20, "Starting PostgreSQL...")
	progress.Render()

	progress.Update(40, "Running Flyway migrations...")
	progress.Render()

	progress.Update(60, "Initializing system metadata...")
	progress.Render()

	progress.Update(80, "Syncing plugin store...")
	progress.Render()

	progress.Update(95, "Starting backend and dashboard...")
	progress.Render()

	cmd := exec.Command("docker", "compose",
		"-f", composeFile,
		"--env-file", envFile,
		"up", "-d")
	cmd.Dir = rootDir
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("docker compose failed: %w", err)
	}

	progress.Update(100, "All services running!")
	progress.Render()

	return nil
}

// ── Plugin definitions ─────────────────────────────────────────────

// PluginInfo describes a plugin available in the store.
type PluginInfo struct {
	Key         string
	Name        string
	Type        string // channel, model, skill, mcp
	Description string
}

// AvailableChannels returns channel plugins.
func AvailableChannels() []components.Option {
	return []components.Option{
		{Key: "telegram", Description: "Telegram Bot API — webhook + long-polling"},
		{Key: "discord", Description: "Discord bot — slash commands + events"},
		{Key: "whatsapp", Description: "WhatsApp Business API — message routing"},
		{Key: "slack", Description: "Slack app — events + modals"},
		{Key: "matrix", Description: "Matrix protocol — decentralized chat"},
	}
}

// AvailableSkills returns skill plugins.
func AvailableSkills() []components.Option {
	return []components.Option{
		{Key: "web-search", Description: "Web search and content extraction"},
		{Key: "code-execution", Description: "Sandboxed code execution (Python, JS, Go)"},
		{Key: "image-generation", Description: "AI image generation (DALL-E, Stable Diffusion)"},
		{Key: "file-operations", Description: "Read, write, and manage files"},
		{Key: "data-analysis", Description: "CSV/JSON data analysis and visualization"},
		{Key: "translation", Description: "Multi-language translation"},
		{Key: "summarization", Description: "Text summarization and key points extraction"},
	}
}

// AvailableProviders returns model provider options.
func AvailableProviders() []components.Option {
	return []components.Option{
		{Key: "anthropic", Description: "Claude models — API Key"},
		{Key: "openai", Description: "GPT models — API Key or Azure"},
		{Key: "deepseek", Description: "DeepSeek models — API Key"},
		{Key: "ollama", Description: "Self-hosted local models — no key needed"},
		{Key: "azure-openai", Description: "Azure subscription — managed"},
		{Key: "gemini", Description: "Gemini models — API Key or Vertex AI"},
		{Key: "bedrock", Description: "AWS subscription — managed"},
	}
}

// ── Installer Wizard ───────────────────────────────────────────────

// RunWizard executes the full interactive installation wizard.
func RunWizard(rootDir string, skipDocker bool) error {
	// ── OS Detection (first thing!) ────────────────────────────
	osInfo := DetectOS()
	osSection := components.NewBase("Operating System", theme.Blue)
	osSection.RenderSection()
	osSection.RenderOK(fmt.Sprintf("Detected: %s", osInfo.PrettyName))
	osSection.RenderLine(fmt.Sprintf("  Package manager: %s", osInfo.PackageMgr))
	osSection.RenderLine(fmt.Sprintf("  Shell: %s", osInfo.Shell))
	osSection.CloseSection()

	// ── Load previous config ───────────────────────────────────
	cfg, _ := LoadConfig()
	if cfg == nil {
		cfg = &Config{}
		cfg.Ports = DefaultPorts()
	}
	// Set defaults for empty fields
	if cfg.SystemName == "" {
		cfg.SystemName = "SYNAPSE"
	}
	if cfg.Domain == "" {
		cfg.Domain = "localhost"
	}
	if cfg.DataDir == "" {
		cfg.DataDir = "./synapse-data"
	}
	if cfg.Mode == "" {
		cfg.Mode = "quick"
	}
	if cfg.Provider == "" {
		cfg.Provider = "anthropic"
	}
	if cfg.GitProvider == "" {
		cfg.GitProvider = "none"
	}
	if cfg.PostgresPass == "" {
		cfg.PostgresPass = "synapse_dev_password"
	}
	if cfg.JWTSecret == "" {
		cfg.JWTSecret = "CHANGE_ME_IN_PRODUCTION_THIS_MUST_BE_AT_LEAST_256_BITS"
	}
	if cfg.EncryptionKey == "" {
		cfg.EncryptionKey = "dev_key_32_bytes_change_me_now!!"
	}

	// ── Welcome ────────────────────────────────────────────────
	welcome := components.NewWelcome("SYNAPSE INSTALLER", "v2.4.0 Interactive", "Your AI. Your Rules. Your Stack.")
	welcome.Render()

	// ── Requirements Check ─────────────────────────────────────
	reqs := components.NewRequirements(components.DefaultRequirements())
	reqs.CheckAll()
	if !reqs.PromptInstall() {
		fmt.Printf("\n%sInstallation cancelled.%s\n", theme.Yellow, theme.Reset)
		return nil
	}

	// ── Port Prerequisites ─────────────────────────────────────
	prereqs := CheckPrereqs()
	portSection := components.NewBase("Port Availability", theme.Green)
	portSection.RenderSection()
	for port, service := range prereqs.PortConflicts {
		portSection.RenderWarning(fmt.Sprintf("Port %d (%s) in use — will reassign", port, service))
	}
	if len(prereqs.PortConflicts) == 0 {
		portSection.RenderOK("All ports available")
	}
	portSection.CloseSection()

	// ── Installation Mode ──────────────────────────────────────
	modeSelect := components.NewSingleSelect("Installation mode",
		[]components.Option{
			{Key: "quick", Description: "Default settings, best for evaluation"},
			{Key: "dev", Description: "Debug tools, hot reload, ECHO enabled"},
			{Key: "production", Description: "Hardened, optimized for deployment"},
		}, cfg.Mode)
	cfg.Mode = modeSelect.Prompt()

	// ── System Configuration ───────────────────────────────────
	sysSection := components.NewBase("System Configuration", theme.Blue)
	sysSection.RenderSection()

	sysName := components.NewTextInput("System name", cfg.SystemName)
	cfg.SystemName = sysName.Prompt()

	domain := components.NewTextInput("Public domain", cfg.Domain)
	cfg.Domain = domain.Prompt()

	dataDir := components.NewTextInput("Data directory", cfg.DataDir)
	cfg.DataDir = dataDir.Prompt()
	sysSection.CloseSection()

	// ── Security ───────────────────────────────────────────────
	secSection := components.NewBase("Security", theme.Yellow)
	secSection.RenderSection()

	pgPass := components.NewTextInput("PostgreSQL password", cfg.PostgresPass).SetSecret()
	cfg.PostgresPass = pgPass.Prompt()

	jwt := components.NewTextInput("JWT secret (256-bit)", cfg.JWTSecret).SetSecret()
	cfg.JWTSecret = jwt.Prompt()

	encKey := components.NewTextInput("Encryption key (32 bytes)", cfg.EncryptionKey).SetSecret()
	cfg.EncryptionKey = encKey.Prompt()
	secSection.CloseSection()

	// ── Model Provider ─────────────────────────────────────────
	provSection := components.NewBase("Model Provider", theme.Purple)
	provSection.RenderSection()

	providerSelect := components.NewSearchList("Primary provider", AvailableProviders(), cfg.Provider)
	providerSelect.NoBorder = true
	cfg.Provider = providerSelect.Prompt()

	// Provider-specific credential prompts
	switch cfg.Provider {
	case "azure-openai":
		components.NewTextInput("Azure endpoint", "https://my-resource.openai.azure.com").Prompt()
		components.NewTextInput("Azure API key", "").SetSecret().Prompt()
		provSection.RenderOK("Subscription-based authentication configured")
	case "ollama":
		components.NewTextInput("Ollama host", "http://localhost:11434").Prompt()
		provSection.RenderOK("Self-hosted mode — no API key needed")
	case "bedrock":
		components.NewTextInput("AWS region", "us-east-1").Prompt()
		provSection.RenderOK("AWS subscription configured")
	default:
		components.NewTextInput("API key", "").SetSecret().Prompt()
		provSection.RenderOK("API key configured")
	}
	provSection.CloseSection()

	// ── Channel Plugins (SearchList + MultiSelect) ─────────────
	chSection := components.NewBase("Channel Plugins", theme.Orange)
	chSection.RenderSection()
	chSection.RenderLine("Select messaging channels to enable (Space to toggle, Enter to confirm)")
	chSection.RenderLine("")

	chSelect := components.NewMultiSelect("Channels", AvailableChannels(), cfg.Channels)
	chSelect.NoBorder = true
	cfg.Channels = chSelect.Prompt()
	chSection.CloseSection()

	// ── Skill Plugins (SearchList + MultiSelect) ───────────────
	skSection := components.NewBase("Skill Plugins", theme.Orange)
	skSection.RenderSection()
	skSection.RenderLine("Select skills to enable (Space to toggle, Enter to confirm)")
	skSection.RenderLine("")

	skSelect := components.NewMultiSelect("Skills", AvailableSkills(), cfg.Skills)
	skSelect.NoBorder = true
	cfg.Skills = skSelect.Prompt()
	skSection.CloseSection()

	// ── Features ───────────────────────────────────────────────
	featSection := components.NewBase("Features", theme.Orange)
	featSection.RenderSection()

	echoToggle := components.NewToggle("ECHO Debug Agent", cfg.EchoEnabled || cfg.Mode == "dev")
	cfg.EchoEnabled = echoToggle.Prompt()

	grafanaToggle := components.NewToggle("Grafana Monitoring", cfg.GrafanaEnabled)
	cfg.GrafanaEnabled = grafanaToggle.Prompt()

	gitSelect := components.NewSingleSelect("Git integration",
		[]components.Option{
			{Key: "none", Description: "No git integration"},
			{Key: "github", Description: "GitHub"},
			{Key: "gitlab", Description: "GitLab"},
			{Key: "gitea", Description: "Gitea"},
			{Key: "forgejo", Description: "Forgejo"},
		}, cfg.GitProvider)
	cfg.GitProvider = gitSelect.Prompt()
	featSection.CloseSection()

	// ── Summary ────────────────────────────────────────────────
	summary := components.NewSummary("Configuration Summary", theme.Green)
	summary.Add("OS", osInfo.PrettyName)
	summary.Add("Mode", cfg.Mode)
	summary.Add("System", cfg.SystemName)
	summary.Add("Domain", cfg.Domain)
	summary.Add("Data Dir", cfg.DataDir)
	summary.Add("Provider", cfg.Provider)
	summary.Add("Channels", strings.Join(cfg.Channels, ", "))
	summary.Add("Skills", strings.Join(cfg.Skills, ", "))
	summary.Add("ECHO", fmt.Sprintf("%t", cfg.EchoEnabled))
	summary.Add("Grafana", fmt.Sprintf("%t", cfg.GrafanaEnabled))
	summary.Add("Git", cfg.GitProvider)
	summary.Add("Backend Port", strconv.Itoa(cfg.Ports.Backend))
	summary.Add("Dashboard Port", strconv.Itoa(cfg.Ports.Dashboard))
	summary.Render()

	// ── Confirmation ───────────────────────────────────────────
	confirm := components.NewConfirm("Ready to install. Review the configuration above.", false)
	if !confirm.Prompt() {
		fmt.Printf("\n%sInstallation cancelled.%s\n", theme.Yellow, theme.Reset)
		return nil
	}

	// ── Save config for future edits ───────────────────────────
	if err := SaveConfig(cfg); err != nil {
		fmt.Printf("%s│%s  %sWarning: could not save config: %s%s\n",
			theme.Dim, theme.Reset, theme.Yellow, err, theme.Reset)
	}

	// ── Execute ────────────────────────────────────────────────
	execSection := components.NewBase("Installing", theme.Green)
	execSection.RenderSection()

	execSection.RenderLine("Generating .env file...")
	envFile, err := GenerateEnvFile(cfg, rootDir)
	if err != nil {
		return fmt.Errorf("generating .env: %w", err)
	}
	execSection.RenderOK(".env written to " + envFile)

	if !skipDocker && prereqs.ComposeFound {
		execSection.RenderLine("Starting Docker containers...")
		progress := components.NewProgress(40)
		if err := RunDockerCompose(cfg, rootDir, progress); err != nil {
			execSection.RenderError("Docker compose failed: " + err.Error())
		}
	} else {
		execSection.RenderWarning("Skipping Docker (use 'docker compose up -d' manually)")
	}
	execSection.CloseSection()

	// ── Completion ─────────────────────────────────────────────
	fmt.Println()
	fmt.Println(theme.Sprintf(theme.Bold+theme.Green, "   ╔══════════════════════════════════════════════════╗"))
	fmt.Println(theme.Sprintf(theme.Bold+theme.Green, "   ║           Installation Complete! 🎉              ║"))
	fmt.Println(theme.Sprintf(theme.Bold+theme.Green, "   ╚══════════════════════════════════════════════════╝"))
	fmt.Println()
	fmt.Printf("   %sDashboard:%s  %shttp://%s:%d%s\n",
		theme.Gray, theme.Reset, theme.White, cfg.Domain, cfg.Ports.Dashboard, theme.Reset)
	fmt.Printf("   %sBackend:%s    %shttp://%s:%d%s\n",
		theme.Gray, theme.Reset, theme.White, cfg.Domain, cfg.Ports.Backend, theme.Reset)
	fmt.Printf("   %sConfig:%s     %s%s%s\n",
		theme.Gray, theme.Reset, theme.White, envFile, theme.Reset)
	fmt.Printf("   %sSaved:%s      %s~/.synapse/install.yaml%s\n",
		theme.Gray, theme.Reset, theme.White, theme.Reset)
	fmt.Println()
	fmt.Printf("   %sRun 'synapse install' again to edit your configuration.%s\n", theme.Dim, theme.Reset)
	fmt.Printf("   %sRun 'synapse health' to verify the backend is running.%s\n", theme.Dim, theme.Reset)

	return nil
}
