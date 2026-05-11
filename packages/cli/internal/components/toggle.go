package components

import (
	"github.com/synapse-dev/synapse-cli/internal/terminal"
	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// Toggle is an on/off switch component with Enter/Space to toggle.
type Toggle struct {
	BaseComponent
	Label   string
	Default bool
	Value   bool
}

// NewToggle creates a toggle component.
func NewToggle(label string, defaultVal bool) *Toggle {
	return &Toggle{
		BaseComponent: NewBase("Toggle", theme.Orange),
		Label:         label,
		Default:       defaultVal,
		Value:         defaultVal,
	}
}

// Prompt displays the toggle and waits for Enter/Space to confirm or toggle.
func (t *Toggle) Prompt() bool {
	t.RenderToggle(t.Label, t.Value)
	t.RenderLine("  Enter/Space to toggle • any other key to keep")

	key, err := t.ReadKey()
	if err != nil {
		return t.Value
	}

	switch key.Key {
	case terminal.KeyEnter, terminal.KeySpace:
		t.Value = !t.Value
		// Re-render with new state
		t.RenderToggle(t.Label, t.Value)
	}

	return t.Value
}

// Done returns true after Prompt has been called.
func (t *Toggle) Done() bool { return true }

// Ensure terminal and theme are used
var _ = terminal.KeyEnter
var _ = theme.Blue
