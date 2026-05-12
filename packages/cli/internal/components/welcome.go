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

const welcomeWidth = 50

// centerLine centers text within the welcome box width, accounting for ANSI codes.
func centerLine(text string, width int, borderColor string) string {
	plain := stripANSI(text)
	padding := width - len(plain)
	if padding < 0 {
		padding = 0
	}
	leftPad := padding / 2
	rightPad := padding - leftPad
	return fmt.Sprintf("   %s║%s%s%s%s║%s",
		borderColor, strings.Repeat(" ", leftPad), text, strings.Repeat(" ", rightPad), borderColor, theme.Reset)
}

// Render outputs the welcome banner.
func (w *Welcome) Render() {
	border := theme.Bold + theme.Blue
	topBorder := fmt.Sprintf("   %s╔%s╗%s", border, strings.Repeat("═", welcomeWidth), theme.Reset)
	botBorder := fmt.Sprintf("   %s╚%s╝%s", border, strings.Repeat("═", welcomeWidth), theme.Reset)
	emptyLine := fmt.Sprintf("   %s║%s║%s", border, strings.Repeat(" ", welcomeWidth), theme.Reset)

	fmt.Println()
	fmt.Println(topBorder)
	fmt.Println(emptyLine)
	fmt.Println(centerLine(theme.White+theme.Bold+w.Title+theme.Reset, welcomeWidth, border))
	fmt.Println(emptyLine)
	fmt.Println(centerLine(theme.Gray+w.Version+theme.Reset, welcomeWidth, border))
	fmt.Println(emptyLine)
	fmt.Println(botBorder)
	fmt.Printf("\n%s   %s%s\n", theme.Gray, w.Tagline, theme.Reset)
}
