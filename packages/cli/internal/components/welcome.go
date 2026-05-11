package components

import (
	"fmt"
	"strings"

	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// Welcome renders the branded splash screen.
type Welcome struct {
	BaseComponent
	Title   string
	Version string
	Tagline string
}

// NewWelcome creates a welcome splash.
func NewWelcome(title, version, tagline string) *Welcome {
	return &Welcome{
		BaseComponent: NewBase("Welcome", theme.Blue),
		Title:         title,
		Version:       version,
		Tagline:       tagline,
	}
}

// Render outputs the welcome banner.
func (w *Welcome) Render() {
	var b strings.Builder
	b.WriteString("\n")
	b.WriteString(theme.Sprintf(theme.Bold+theme.Blue, "   ╔══════════════════════════════════════════════════╗"))
	b.WriteString("\n")
	b.WriteString(theme.Sprintf(theme.Bold+theme.Blue, "   ║                                                  ║"))
	b.WriteString("\n")
	b.WriteString(fmt.Sprintf("   ║           %s%s%s%s                ║\n",
		theme.White, theme.Bold, w.Title, theme.Blue))
	b.WriteString("\n")
	b.WriteString(fmt.Sprintf("   ║           %s%s%s%s               ║\n",
		theme.Gray, w.Version, theme.Blue, theme.Bold))
	b.WriteString("\n")
	b.WriteString(theme.Sprintf(theme.Bold+theme.Blue, "   ║                                                  ║"))
	b.WriteString("\n")
	b.WriteString(theme.Sprintf(theme.Bold+theme.Blue, "   ╚══════════════════════════════════════════════════╝"))
	b.WriteString("\n")
	b.WriteString(fmt.Sprintf("\n%s   %s%s\n", theme.Gray, w.Tagline, theme.Reset))
	fmt.Print(b.String())
}
