// Package theme provides the SYNAPSE brand visual identity.
// All components should use these colors for consistency.
package theme

import (
	"fmt"
	"strings"
)

// ANSI terminal color codes matching the SYNAPSE brand palette.
const (
	Reset     = "\033[0m"
	Bold      = "\033[1m"
	Dim       = "\033[2m"
	Italic    = "\033[3m"
	Underline = "\033[4m"

	// Background
	BG      = "\033[48;2;15;17;23m" // #0F1117
	Surface = "\033[48;2;24;28;39m" // #181C27

	// Agent layer colors
	Blue   = "\033[38;2;123;159;224m" // #7B9FE0  Main Agent
	Purple = "\033[38;2;176;127;232m" // #B07FE8  AI-Firm
	Orange = "\033[38;2;224;123;90m"  // #E07B5A  Teams
	Green  = "\033[38;2;76;175;135m"  // #4CAF87  ECHO

	// UI colors
	White  = "\033[38;2;220;220;220m" // text
	Gray   = "\033[38;2;120;130;150m" // dim text
	Red    = "\033[38;2;255;80;80m"   // error
	Yellow = "\033[38;2;255;200;50m"  // warning
)

// ── Styled helpers ─────────────────────────────────────────────────

// Sprintf applies a color/style to a format string.
func Sprintf(style, format string, a ...any) string {
	return style + fmt.Sprintf(format, a...) + Reset
}

// Header returns a styled header line.
func Header(text string) string {
	return Bold + Blue + text + Reset
}

// OK returns a green checkmark message.
func OK(text string) string {
	return Sprintf(Green, "✓ ") + text + Reset
}

// Error returns a red X message.
func Error(text string) string {
	return Sprintf(Red, "✗ ") + text + Reset
}

// Warning returns a yellow warning message.
func Warning(text string) string {
	return Sprintf(Yellow, "⚠ ") + text + Reset
}

// DimText returns dimmed text.
func DimText(text string) string {
	return Dim + text + Reset
}

// Label returns a styled label:value pair.
func Label(label, value string) string {
	return fmt.Sprintf("  %s%s:%s %s%s%s", Dim, label, Reset, White, value, Reset)
}

// Separator returns a horizontal line.
func Separator(length int) string {
	return Dim + strings.Repeat("─", length) + Reset
}

// ── Box drawing ────────────────────────────────────────────────────

// Box draws a bordered box around content lines.
func Box(title string, color string, lines ...string) string {
	var b strings.Builder
	width := 60
	for _, l := range lines {
		if len(l) > width {
			width = len(l)
		}
	}
	if len(title) > width-4 {
		width = len(title) + 4
	}
	titleLen := len(title) + 2 // +2 for spaces

	b.WriteString("\n")
	b.WriteString(Dim + "┌─" + Reset + " " + color + Bold + title + Reset + " " + Dim + strings.Repeat("─", width-titleLen) + Reset + "\n")
	b.WriteString(Dim + "│" + Reset + "\n")
	for _, l := range lines {
		b.WriteString(Dim + "│" + Reset + "  " + l + "\n")
	}
	b.WriteString(Dim + "│" + Reset + "\n")
	b.WriteString(Dim + "└" + strings.Repeat("─", width+2) + Reset + "\n")
	return b.String()
}

// Bullet returns a styled bullet point.
func Bullet(checked bool, text string) string {
	if checked {
		return fmt.Sprintf("  %s●%s %s", Green, Reset, text)
	}
	return fmt.Sprintf("  %s○%s %s", Gray, Reset, text)
}

// ProgressBar renders a progress bar.
func ProgressBar(percent int, width int) string {
	filled := percent * width / 100
	empty := width - filled
	return Green + strings.Repeat("█", filled) + Gray + strings.Repeat("░", empty) + Reset
}
