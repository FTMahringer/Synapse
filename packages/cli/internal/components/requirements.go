package components

import (
	"fmt"
	"os/exec"
	"strings"

	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// ── Requirement types ──────────────────────────────────────────────

// RequirementStatus represents the installation state of a requirement.
type RequirementStatus int

const (
	StatusUnknown RequirementStatus = iota
	StatusNotInstalled
	StatusInstalled
	StatusVersionMismatch
	StatusUpdateAvailable
)

// PackageManagerInfo describes a detected package manager.
type PackageManagerInfo struct {
	Name       string // "apt", "brew", "winget", etc.
	Cmd        string // binary name
	Detected   bool
	SetupRepo  string // command to add official repo if needed
	InstallFmt string // format string: %s = package name
	UpdateCmd  string // command to update package list
	UpgradeFmt string // format string: %s = package name, for upgrades
}

// Requirement defines a single system requirement.
type Requirement struct {
	Name           string            // Display name (e.g., "Docker")
	Command        string            // Command to check existence (e.g., "docker")
	VersionCmd     string            // Command to get version (e.g., "docker --version")
	MinVersion     string            // Minimum required version (e.g., "24.0.0")
	ScriptInstall  string            // Universal install script (e.g., "curl -fsSL https://get.docker.com | sh")
	PkgInstall     map[string]string // package manager -> package name
	RepoSetupCmds  map[string]string // package manager -> repo setup command
	InstallLabel   string            // Human-readable fallback URL
	Status         RequirementStatus
	CurrentVersion string
}

// Requirements is a component that checks system prerequisites and offers to install them.
type Requirements struct {
	BaseComponent
	Requirements []Requirement
	PkgMgr       *PackageManagerInfo // Selected package manager
}

// NewRequirements creates a requirements checker component.
func NewRequirements(reqs []Requirement) *Requirements {
	return &Requirements{
		BaseComponent: NewBase("Prerequisites", theme.Green),
		Requirements:  reqs,
	}
}

// CheckAll runs all requirement checks.
func (r *Requirements) CheckAll() {
	r.RenderSection()

	for i, req := range r.Requirements {
		r.Requirements[i].Status = r.checkRequirement(&req)
		req = r.Requirements[i]

		switch req.Status {
		case StatusInstalled:
			ver := req.CurrentVersion
			if ver != "" {
				ver = " (" + ver + ")"
			}
			r.RenderBullet(true, fmt.Sprintf("%s%s", req.Name, ver))
		case StatusNotInstalled:
			r.RenderBullet(false, fmt.Sprintf("%s — not installed", req.Name))
		case StatusVersionMismatch:
			r.RenderLine(theme.Warning(fmt.Sprintf("%s: installed %s, need %s",
				req.Name, req.CurrentVersion, req.MinVersion)))
		case StatusUpdateAvailable:
			r.RenderLine(theme.Warning(fmt.Sprintf("%s: update available (%s → latest)",
				req.Name, req.CurrentVersion)))
		}
	}

	r.CloseSection()
}

func (r *Requirements) checkRequirement(req *Requirement) RequirementStatus {
	_, err := exec.LookPath(req.Command)
	if err != nil {
		return StatusNotInstalled
	}

	if req.VersionCmd != "" {
		ver, err := getVersion(req.VersionCmd)
		if err == nil && ver != "" {
			req.CurrentVersion = ver
			if req.MinVersion != "" {
				cmp := compareVersions(ver, req.MinVersion)
				if cmp < 0 {
					return StatusVersionMismatch
				}
			}
		}
	}

	return StatusInstalled
}

// ── Package manager selection ──────────────────────────────────────

// DetectPackageManagers finds all installed package managers on the system.
func DetectPackageManagers() []PackageManagerInfo {
	all := allPackageManagers()
	detected := make([]PackageManagerInfo, 0, len(all))
	for _, pm := range all {
		if _, err := exec.LookPath(pm.Cmd); err == nil {
			pm.Detected = true
			detected = append(detected, pm)
		}
	}
	return detected
}

func allPackageManagers() []PackageManagerInfo {
	return []PackageManagerInfo{
		{
			Name: "winget", Cmd: "winget",
			InstallFmt: "winget install %s",
			UpgradeFmt: "winget upgrade %s",
		},
		{
			Name: "choco", Cmd: "choco",
			InstallFmt: "choco install -y %s",
			UpgradeFmt: "choco upgrade -y %s",
		},
		{
			Name: "scoop", Cmd: "scoop",
			InstallFmt: "scoop install %s",
			UpgradeFmt: "scoop update %s",
		},
		{
			Name: "brew", Cmd: "brew",
			InstallFmt: "brew install %s",
			UpgradeFmt: "brew upgrade %s",
		},
		{
			Name: "macports", Cmd: "port",
			InstallFmt: "sudo port install %s",
			UpgradeFmt: "sudo port upgrade %s",
		},
		{
			Name: "apt", Cmd: "apt",
			SetupRepo:  "sudo apt-get update",
			InstallFmt: "sudo apt-get install -y %s",
			UpdateCmd:  "sudo apt-get update",
			UpgradeFmt: "sudo apt-get upgrade -y %s",
		},
		{
			Name: "apt-get", Cmd: "apt-get",
			SetupRepo:  "sudo apt-get update",
			InstallFmt: "sudo apt-get install -y %s",
			UpdateCmd:  "sudo apt-get update",
			UpgradeFmt: "sudo apt-get upgrade -y %s",
		},
		{
			Name: "dnf", Cmd: "dnf",
			InstallFmt: "sudo dnf install -y %s",
			UpgradeFmt: "sudo dnf upgrade -y %s",
		},
		{
			Name: "yum", Cmd: "yum",
			InstallFmt: "sudo yum install -y %s",
			UpgradeFmt: "sudo yum update -y %s",
		},
		{
			Name: "pacman", Cmd: "pacman",
			SetupRepo:  "sudo pacman -Sy",
			InstallFmt: "sudo pacman -S --noconfirm %s",
			UpgradeFmt: "sudo pacman -Syu --noconfirm %s",
		},
		{
			Name: "zypper", Cmd: "zypper",
			InstallFmt: "sudo zypper install -y %s",
			UpgradeFmt: "sudo zypper update -y %s",
		},
		{
			Name: "apk", Cmd: "apk",
			SetupRepo:  "sudo apk update",
			InstallFmt: "sudo apk add %s",
			UpgradeFmt: "sudo apk upgrade %s",
		},
	}
}

// SelectPackageManager lets the user choose a package manager from detected ones.
// Returns nil if none detected or user skipped.
func SelectPackageManager(title string) *PackageManagerInfo {
	detected := DetectPackageManagers()
	if len(detected) == 0 {
		return nil
	}
	if len(detected) == 1 {
		return &detected[0]
	}

	opts := make([]Option, len(detected))
	for i, pm := range detected {
		desc := ""
		switch pm.Name {
		case "winget", "choco", "scoop":
			desc = "Windows package manager"
		case "brew", "macports":
			desc = "macOS package manager"
		case "apt", "apt-get", "dnf", "yum", "pacman", "zypper", "apk":
			desc = "Linux package manager"
		}
		opts[i] = Option{Key: pm.Name, Description: desc}
	}

	sel := NewSingleSelect(title, opts, detected[0].Name)
	chosen := sel.Prompt()

	for _, pm := range detected {
		if pm.Name == chosen {
			return &pm
		}
	}
	return &detected[0]
}

// ── Install logic ──────────────────────────────────────────────────

// PromptInstall asks the user how to handle missing/outdated requirements.
func (r *Requirements) PromptInstall() bool {
	missing := r.getActionable()
	if len(missing) == 0 {
		r.RenderOK("All prerequisites satisfied")
		return true
	}

	// First, let user select package manager (only if one wasn't pre-set)
	if r.PkgMgr == nil {
		r.RenderSection()
		r.RenderLine("Select package manager for installations:")
		picked := SelectPackageManager("Package manager")
		if picked != nil {
			r.PkgMgr = picked
			r.RenderOK(fmt.Sprintf("Using: %s", picked.Name))
		} else {
			r.RenderWarning("No package manager selected — will try scripts/fallbacks")
		}
		r.CloseSection()
	}

	r.RenderSection()
	r.RenderLine("Some prerequisites need attention:")
	r.RenderLine("")

	action := NewSingleSelect("How would you like to proceed?",
		[]Option{
			{Key: "auto", Description: "Install/update all requirements [recommended]"},
			{Key: "choose", Description: "Choose individually what to install"},
			{Key: "skip", Description: "Skip — I'll handle it myself"},
		}, "auto").Prompt()

	switch action {
	case "auto":
		return r.installAll(missing)
	case "choose":
		return r.installChoose(missing)
	case "skip":
		r.RenderWarning("Skipping prerequisite installation")
		return true
	}

	return true
}

func (r *Requirements) getActionable() []Requirement {
	var actionable []Requirement
	for _, req := range r.Requirements {
		if req.Status != StatusInstalled {
			actionable = append(actionable, req)
		}
	}
	return actionable
}

func (r *Requirements) installAll(reqs []Requirement) bool {
	success := true
	for _, req := range reqs {
		if !r.installOne(req) {
			success = false
		}
	}
	return success
}

func (r *Requirements) installChoose(reqs []Requirement) bool {
	opts := make([]Option, len(reqs))
	for i, req := range reqs {
		status := "missing"
		if req.Status == StatusVersionMismatch {
			status = fmt.Sprintf("needs %s (have %s)", req.MinVersion, req.CurrentVersion)
		}
		opts[i] = Option{Key: req.Name, Description: status}
	}

	selected := NewMultiSelect("Select requirements to install", opts, nil).Prompt()

	success := true
	for _, req := range reqs {
		if Contains(selected, req.Name) {
			if !r.installOne(req) {
				success = false
			}
		}
	}
	return success
}

// installOne installs a single requirement using the best available method.
func (r *Requirements) installOne(req Requirement) bool {
	// Priority: 1) Package manager install 2) Script install 3) Manual URL
	if r.PkgMgr != nil {
		if pkgName, ok := req.PkgInstall[r.PkgMgr.Name]; ok && pkgName != "" {
			r.RenderLine(fmt.Sprintf("Installing %s via %s...", req.Name, r.PkgMgr.Name))

			// Run repo setup if needed
			if setupCmd, ok := req.RepoSetupCmds[r.PkgMgr.Name]; ok && setupCmd != "" {
				r.RenderLine(fmt.Sprintf("  Setting up repository..."))
				parts := strings.Fields(setupCmd)
				cmd := exec.Command(parts[0], parts[1:]...)
				if out, err := cmd.CombinedOutput(); err != nil {
					r.RenderWarning(fmt.Sprintf("Repo setup warning: %s", string(out)))
				}
			}

			// Run install
			installCmd := fmt.Sprintf(r.PkgMgr.InstallFmt, pkgName)
			parts := strings.Fields(installCmd)
			cmd := exec.Command(parts[0], parts[1:]...)
			if out, err := cmd.CombinedOutput(); err != nil {
				r.RenderError(fmt.Sprintf("Install failed: %s", string(out)))
				return r.installScriptFallback(req)
			}
			r.RenderOK(fmt.Sprintf("%s installed via %s", req.Name, r.PkgMgr.Name))
			return true
		}
	}

	return r.installScriptFallback(req)
}

// installScriptFallback tries the universal install script, then falls back to manual URL.
func (r *Requirements) installScriptFallback(req Requirement) bool {
	if req.ScriptInstall != "" {
		r.RenderLine(fmt.Sprintf("Installing %s via script...", req.Name))
		parts := strings.Fields(req.ScriptInstall)

		var cmd *exec.Cmd
		if len(parts) >= 3 && parts[0] == "sh" && parts[1] == "-c" {
			// "sh -c 'curl ... | sh'" style
			script := strings.Join(parts[2:], " ")
			script = strings.Trim(script, "'\"")
			cmd = exec.Command("sh", "-c", script)
		} else {
			cmd = exec.Command(parts[0], parts[1:]...)
		}

		if out, err := cmd.CombinedOutput(); err != nil {
			r.RenderError(fmt.Sprintf("Script install failed: %s", string(out)))
			r.RenderLine(theme.Gray + "  Install manually: " + req.InstallLabel + theme.Reset)
			return false
		}
		r.RenderOK(fmt.Sprintf("%s installed via script", req.Name))
		return true
	}

	r.RenderError(fmt.Sprintf("No install method available for %s", req.Name))
	r.RenderLine(theme.Gray + "  Install manually: " + req.InstallLabel + theme.Reset)
	return false
}

// ── Helper functions ───────────────────────────────────────────────

func getVersion(cmdStr string) (string, error) {
	parts := strings.Fields(cmdStr)
	if len(parts) == 0 {
		return "", fmt.Errorf("empty command")
	}
	cmd := exec.Command(parts[0], parts[1:]...)
	out, err := cmd.Output()
	if err != nil {
		return "", err
	}
	output := string(out)
	for _, word := range strings.Fields(output) {
		if isVersionString(word) {
			return word, nil
		}
	}
	return strings.TrimSpace(output), nil
}

func isVersionString(s string) bool {
	s = strings.TrimPrefix(s, "v")
	if len(s) == 0 {
		return false
	}
	if s[0] < '0' || s[0] > '9' {
		return false
	}
	return strings.Contains(s, ".")
}

func compareVersions(v1, v2 string) int {
	v1 = strings.TrimPrefix(v1, "v")
	v2 = strings.TrimPrefix(v2, "v")
	p1 := strings.Split(v1, ".")
	p2 := strings.Split(v2, ".")
	maxLen := len(p1)
	if len(p2) > maxLen {
		maxLen = len(p2)
	}
	for i := 0; i < maxLen; i++ {
		var n1, n2 int
		if i < len(p1) {
			fmt.Sscanf(p1[i], "%d", &n1)
		}
		if i < len(p2) {
			fmt.Sscanf(p2[i], "%d", &n2)
		}
		if n1 < n2 {
			return -1
		}
		if n1 > n2 {
			return 1
		}
	}
	return 0
}

// ── Default requirements for SYNAPSE ───────────────────────────────

// DefaultRequirements returns the standard SYNAPSE prerequisites
// with cross-platform package manager and script-based install support.
func DefaultRequirements() []Requirement {
	return []Requirement{
		{
			Name:          "Docker",
			Command:       "docker",
			VersionCmd:    "docker --version",
			MinVersion:    "24.0.0",
			ScriptInstall: "sh -c 'curl -fsSL https://get.docker.com | sh'",
			PkgInstall: map[string]string{
				"winget":  "Docker.DockerDesktop",
				"choco":   "docker-desktop",
				"scoop":   "docker",
				"brew":    "--cask docker",
				"apt":     "docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",
				"apt-get": "docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",
				"dnf":     "docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",
				"yum":     "docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",
				"pacman":  "docker docker-compose",
				"zypper":  "docker docker-compose",
				"apk":     "docker docker-compose",
			},
			RepoSetupCmds: map[string]string{
				"apt":     "sh -c 'curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && echo \"deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable\" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null && sudo apt-get update'",
				"apt-get": "sh -c 'curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && echo \"deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable\" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null && sudo apt-get update'",
				"dnf":     "sh -c 'sudo dnf -y install dnf-plugins-core && sudo dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo'",
				"yum":     "sh -c 'sudo yum install -y yum-utils && sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo'",
			},
			InstallLabel: "https://docs.docker.com/get-docker/",
		},
		{
			Name:          "Docker Compose",
			Command:       "docker",
			VersionCmd:    "docker compose version",
			MinVersion:    "2.20.0",
			ScriptInstall: "sh -c 'docker compose version'", // comes with Docker
			PkgInstall: map[string]string{
				// Included in Docker packages above, but some need separate
				"pacman": "docker-compose",
			},
			InstallLabel: "Included with Docker Desktop / Docker Engine",
		},
		{
			Name:          "Git",
			Command:       "git",
			VersionCmd:    "git --version",
			MinVersion:    "2.30.0",
			ScriptInstall: "sh -c 'curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg && echo \"deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main\" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null && sudo apt-get update && sudo apt-get install -y git'",
			PkgInstall: map[string]string{
				"winget":   "Git.Git",
				"choco":    "git",
				"scoop":    "git",
				"brew":     "git",
				"macports": "git",
				"apt":      "git",
				"apt-get":  "git",
				"dnf":      "git",
				"yum":      "git",
				"pacman":   "git",
				"zypper":   "git",
				"apk":      "git",
			},
			InstallLabel: "https://git-scm.com/downloads",
		},
		{
			Name:          "Go",
			Command:       "go",
			VersionCmd:    "go version",
			MinVersion:    "1.26.0",
			ScriptInstall: "sh -c 'wget https://go.dev/dl/go1.26.linux-amd64.tar.gz && sudo tar -C /usr/local -xzf go1.26.linux-amd64.tar.gz && export PATH=$PATH:/usr/local/go/bin'",
			PkgInstall: map[string]string{
				"winget":   "GoLang.Go",
				"choco":    "golang",
				"scoop":    "go",
				"brew":     "go",
				"macports": "go",
				"apt":      "golang-go",
				"apt-get":  "golang-go",
				"dnf":      "golang",
				"yum":      "golang",
				"pacman":   "go",
				"zypper":   "go",
				"apk":      "go",
			},
			InstallLabel: "https://go.dev/dl/",
		},
	}
}

// Ensure theme is used
var _ = theme.Blue
