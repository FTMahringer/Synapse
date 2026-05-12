// Package components provides reusable TUI components for the SYNAPSE CLI.
// Each component is self-contained and can be composed into multi-step wizards.
package components

import (
	"fmt"
	"strings"

	"github.com/synapse-dev/synapse-cli/internal/terminal"
	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// ── BaseComponent ──────────────────────────────────────────────────

// BaseComponent provides shared rendering and input logic that all
// interactive components inherit. It handles:
//   - Section border rendering (title, lines, close)
//   - Arrow-key + Enter + Space navigation
//   - Cursor show/hide management
//   - Standardized prompt layout
type BaseComponent struct {
	Title   string
	Color   string
	content []string // lines rendered inside the section
}

// NewBase creates a new base component with a section title and color.
func NewBase(title, color string) BaseComponent {
	return BaseComponent{Title: title, Color: color}
}

// ── Section rendering ──────────────────────────────────────────────

const sectionInnerWidth = 56

// padRight pads text to the inner width with spaces, truncating if needed.
func padRight(text string, width int) string {
	// Strip ANSI codes for length calculation
	plain := stripANSI(text)
	if len(plain) >= width {
		return text
	}
	return text + strings.Repeat(" ", width-len(plain))
}

// stripANSI removes ANSI escape sequences from a string.
func stripANSI(s string) string {
	var result strings.Builder
	inEscape := false
	for _, r := range s {
		if r == '\x1b' {
			inEscape = true
			continue
		}
		if inEscape {
			if r == 'm' {
				inEscape = false
			}
			continue
		}
		result.WriteRune(r)
	}
	return result.String()
}

// borderLine returns a full horizontal border line with left and right corners.
func borderLine(left, right string, width int) string {
	mid := strings.Repeat("─", width)
	return theme.Dim + left + mid + right + theme.Reset
}

// RenderSection opens a bordered section with the title.
func (b *BaseComponent) RenderSection() {
	titleLen := len(b.Title)
	// Top border: ┌─ Title ──────────────────────────────────────┐
	prefix := "┌─ "
	suffix := "─┐"
	dashCount := sectionInnerWidth - titleLen - len(prefix) - len(suffix) + 4 // +4 for ANSI spacing
	if dashCount < 2 {
		dashCount = 2
	}
	fmt.Printf("\n%s%s%s%s%s %s%s%s %s%s%s\n",
		theme.Dim, prefix, theme.Reset,
		b.Color, theme.Bold, b.Title, theme.Reset,
		theme.Dim, strings.Repeat("─", dashCount), suffix, theme.Reset)
}

// renderContentLine prints a content line with both left and right borders.
func renderContentLine(text string) {
	padded := padRight(text, sectionInnerWidth)
	fmt.Printf("%s│%s %s %s│%s\n", theme.Dim, theme.Reset, padded, theme.Dim, theme.Reset)
}

// RenderLine prints a content line inside the section.
func (b *BaseComponent) RenderLine(text string) {
	renderContentLine(text)
}

// RenderLabel prints a styled key: value line.
func (b *BaseComponent) RenderLabel(key, value string) {
	b.RenderLine(theme.Label(key, value))
}

// RenderBullet prints a bullet point.
func (b *BaseComponent) RenderBullet(checked bool, text string) {
	b.RenderLine(theme.Bullet(checked, text))
}

// RenderPrompt prints a prompt line with optional default value.
func (b *BaseComponent) RenderPrompt(label, defaultVal string) {
	display := defaultVal
	if display == "" {
		display = " "
	}
	text := fmt.Sprintf("%s%s%s %s[%s]%s: ",
		theme.White, label, theme.Reset,
		theme.Dim, display, theme.Reset)
	// Don't pad — prompt needs cursor at end
	fmt.Printf("%s│%s %s", theme.Dim, theme.Reset, text)
}

// RenderOption prints a single selectable option with highlight.
func (b *BaseComponent) RenderOption(key, desc string, selected, highlighted bool) {
	marker := "○"
	color := theme.Gray
	if selected {
		marker = "●"
		color = theme.Green
	}
	highlight := ""
	if highlighted && !selected {
		highlight = theme.White
	} else if highlighted && selected {
		highlight = theme.Green + theme.Bold
	} else {
		highlight = theme.Gray
	}
	text := color + " " + marker + theme.Reset + " " + highlight + key + theme.Reset + " " + theme.Gray + desc + theme.Reset
	renderContentLine(text)
}

// RenderCheckbox prints a checkbox option (for multi-select).
func (b *BaseComponent) RenderCheckbox(key, desc string, checked, highlighted bool) {
	marker := "☐"
	color := theme.Gray
	if checked {
		marker = "☑"
		color = theme.Green
	}
	highlight := theme.Gray
	if highlighted {
		highlight = theme.White
	}
	text := fmt.Sprintf("%s%s%s %s%s%s %s%s%s",
		color, marker, theme.Reset,
		highlight, key, theme.Reset,
		theme.Gray, desc, theme.Reset)
	renderContentLine(text)
}

// RenderToggle prints a toggle state.
func (b *BaseComponent) RenderToggle(label string, enabled bool) {
	state := theme.Sprintf(theme.Gray, "[○] Disabled")
	if enabled {
		state = theme.Sprintf(theme.Green, "[●] Enabled")
	}
	text := fmt.Sprintf("%s%s%s  %s", theme.White, label, theme.Reset, state)
	renderContentLine(text)
}

// RenderProgress prints a progress bar.
func (b *BaseComponent) RenderProgress(percent, width int, message string) {
	b.RenderLine(fmt.Sprintf("%s %s%d%%%s", theme.ProgressBar(percent, width), theme.Bold, percent, theme.Reset))
	b.RenderLine(fmt.Sprintf("%s%s%s", theme.Gray, message, theme.Reset))
}

// RenderOK prints a green checkmark message.
func (b *BaseComponent) RenderOK(text string) {
	b.RenderLine(theme.OK(text))
}

// RenderWarning prints a yellow warning message.
func (b *BaseComponent) RenderWarning(text string) {
	b.RenderLine(theme.Warning(text))
}

// RenderError prints a red error message.
func (b *BaseComponent) RenderError(text string) {
	b.RenderLine(theme.Error(text))
}

// CloseSection closes the bordered section.
func (b *BaseComponent) CloseSection() {
	fmt.Println(borderLine("└", "┘", sectionInnerWidth+2))
}

// ── Input helpers ──────────────────────────────────────────────────

// ReadLine reads a line of text with arrow-key editing support.
func (b *BaseComponent) ReadLine() (string, error) {
	return terminal.ReadLine()
}

// ReadLineWithDefault reads a line with a pre-filled default.
func (b *BaseComponent) ReadLineWithDefault(defaultVal string) (string, error) {
	return terminal.ReadLineWithDefault(defaultVal)
}

// ReadSecret reads a masked password input.
func (b *BaseComponent) ReadSecret() (string, error) {
	return terminal.ReadSecret()
}

// ReadSecretWithDefault reads a masked input with a default.
func (b *BaseComponent) ReadSecretWithDefault(defaultVal string) (string, error) {
	return terminal.ReadSecretWithDefault(defaultVal)
}

// ReadKey reads a single key press.
func (b *BaseComponent) ReadKey() (terminal.Event, error) {
	return terminal.ReadKey()
}

// ── Navigation ─────────────────────────────────────────────────────

// NavigateList handles arrow-key navigation for a list of items.
// Returns the new highlighted index, or -1 if Escape was pressed.
// Enter or Space both confirm the selection.
func (b *BaseComponent) NavigateList(currentIndex, itemCount int) (int, error) {
	return terminal.SelectEvent(currentIndex, itemCount)
}

// MultiSelectNavigate handles arrow-key navigation for multi-select lists.
// Returns (newIndex, action, error) where action is "move", "toggle", "confirm", or "cancel".
// Space toggles the current item, Enter confirms all selections.
func (b *BaseComponent) MultiSelectNavigate(currentIndex, itemCount int) (int, string, error) {
	return terminal.MultiSelectEvent(currentIndex, itemCount)
}

// Confirm prompts for a yes/no answer.
func (b *BaseComponent) Confirm(message string, defaultVal bool) bool {
	defaultStr := "N"
	if defaultVal {
		defaultStr = "Y"
	}
	renderContentLine(fmt.Sprintf("%s%s%s", theme.Yellow, message, theme.Reset))
	fmt.Printf("%s│%s  Proceed? (y/N) [%s%s%s]: ", theme.Dim, theme.Reset, theme.White, defaultStr, theme.Reset)

	input, err := terminal.ReadLine()
	if err != nil {
		return defaultVal
	}
	input = strings.TrimSpace(strings.ToLower(input))
	if input == "" {
		return defaultVal
	}
	return input == "y" || input == "yes"
}

// WaitForEnter waits for the user to press Enter.
func (b *BaseComponent) WaitForEnter() {
	fmt.Printf("%s│%s  %sPress Enter to continue...%s", theme.Dim, theme.Reset, theme.Gray, theme.Reset)
	terminal.ReadKey()
	fmt.Println()
}

// ClearScreen clears the terminal.
func (b *BaseComponent) ClearScreen() {
	terminal.ClearScreen()
}

// ── Helper: option index lookup ────────────────────────────────────

// FindOptionIndex finds the index of an option by key.
func FindOptionIndex(options []Option, key string) int {
	for i, opt := range options {
		if opt.Key == key {
			return i
		}
	}
	return 0
}

// FindOptionKeys returns just the keys from a list of options.
func FindOptionKeys(options []Option) []string {
	keys := make([]string, len(options))
	for i, opt := range options {
		keys[i] = opt.Key
	}
	return keys
}

// Contains checks if a string is in a slice.
func Contains(slice []string, s string) bool {
	for _, item := range slice {
		if item == s {
			return true
		}
	}
	return false
}
