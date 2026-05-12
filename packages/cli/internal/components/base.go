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

// RenderSection opens a bordered section with the title.
func (b *BaseComponent) RenderSection() {
	titleLen := len(b.Title)
	// Account for: ┌─ Title ──... where ┌─ = 2, spaces around title = 2
	line := strings.Repeat("─", sectionInnerWidth-titleLen)
	fmt.Printf("\n%s┌─%s %s%s%s %s%s%s\n",
		theme.Dim, theme.Reset,
		b.Color, theme.Bold, b.Title, theme.Reset,
		theme.Dim, line)
	fmt.Printf("%s│%s\n", theme.Dim, theme.Reset)
}

// RenderLine prints a content line inside the section.
func (b *BaseComponent) RenderLine(text string) {
	fmt.Printf("%s│%s  %s\n", theme.Dim, theme.Reset, text)
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
	fmt.Printf("%s│%s  %s%s%s %s[%s]%s: ",
		theme.Dim, theme.Reset,
		theme.White, label, theme.Reset,
		theme.Dim, display, theme.Reset)
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
	fmt.Print(theme.Dim + "│" + theme.Reset + "  " + color + " " + marker + theme.Reset + " " + highlight + key + theme.Reset + " " + theme.Gray + desc + theme.Reset + "\n")
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
	fmt.Printf("%s│%s  %s%s%s %s%s%s %s%s%s\n",
		theme.Dim, theme.Reset,
		color, marker, theme.Reset,
		highlight, key, theme.Reset,
		theme.Gray, desc, theme.Reset)
}

// RenderToggle prints a toggle state.
func (b *BaseComponent) RenderToggle(label string, enabled bool) {
	state := theme.Sprintf(theme.Gray, "[○] Disabled")
	if enabled {
		state = theme.Sprintf(theme.Green, "[●] Enabled")
	}
	fmt.Printf("%s│%s  %s%s%s  %s\n", theme.Dim, theme.Reset, theme.White, label, theme.Reset, state)
}

// RenderProgress prints a progress bar.
func (b *BaseComponent) RenderProgress(percent, width int, message string) {
	fmt.Printf("%s│%s  %s %s%d%%%s\n%s│%s  %s%s%s\n",
		theme.Dim, theme.Reset,
		theme.ProgressBar(percent, width),
		theme.Bold, percent, theme.Reset,
		theme.Dim, theme.Reset,
		theme.Gray, message, theme.Reset)
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
	// Match RenderSection: ┌─ = 2 chars prefix, so close needs └ + (innerWidth+2) dashes
	// Actually: ┌─Title──... has 2 prefix chars, └Title──... would have 1 prefix char
	// We want the total line length to match: prefix + content should align
	// RenderSection: "┌─ Title " + title + " " + dashes = 2 + 1 + titleLen + 1 + (innerWidth-titleLen) = innerWidth + 4
	// But visible chars: ┌─(2) + space(1) + title + space(1) + dashes = 4 + titleLen + (innerWidth-titleLen) = innerWidth + 4
	// CloseSection: └(1) + dashes = should be innerWidth + 3 to match? No...
	// Let's just make them the same total width: innerWidth + 4 visible chars on top, so bottom should be innerWidth + 3
	// Actually simpler: count the visible chars in RenderSection output
	// "┌─ Prerequisites ─────────────────────────────────────────────"
	//  ^^                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//  2 chars prefix    46 dashes (for 12-char title, innerWidth=56)
	//  Total: 2 + 1 + 12 + 1 + 46 = 62 visible chars
	// Close should be: "└─────────────────────────────────────────────────────────────"
	//  ^               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//  1 char prefix   61 dashes
	// Total: 1 + 61 = 62 visible chars ✓
	line := strings.Repeat("─", sectionInnerWidth+3)
	fmt.Printf("%s│%s\n%s└%s%s%s\n", theme.Dim, theme.Reset, theme.Dim, line, theme.Reset, "")
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
	fmt.Printf("\n%s│%s  %s%s%s\n", theme.Dim, theme.Reset, theme.Yellow, message, theme.Reset)
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
