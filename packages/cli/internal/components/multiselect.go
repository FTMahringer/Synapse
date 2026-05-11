package components

import (
	"github.com/synapse-dev/synapse-cli/internal/terminal"
	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// MultiSelect is a checkbox-list component with arrow-key navigation.
// Use ↑/↓ to navigate, Space to toggle, Enter to confirm, Escape to cancel.
type MultiSelect struct {
	BaseComponent
	Label   string
	Options []Option
	Default []string // pre-selected keys
	Values  []string // selected keys after prompt
	index   int
}

// NewMultiSelect creates a multi-select component.
func NewMultiSelect(label string, options []Option, defaultVals []string) *MultiSelect {
	if defaultVals == nil {
		defaultVals = []string{}
	}
	return &MultiSelect{
		BaseComponent: NewBase("Multi-Select", theme.Orange),
		Label:         label,
		Options:       options,
		Default:       defaultVals,
		Values:        defaultVals,
		index:         0,
	}
}

// Prompt displays the options with arrow-key navigation and Space to toggle.
func (m *MultiSelect) Prompt() []string {
	m.RenderSection()
	m.RenderLine(m.Label)
	m.RenderLine("  ↑/↓ navigate • Space toggle • Enter confirm • Esc cancel")
	m.RenderLine("")

	// Initial render
	m.renderOptions()

	for {
		newIdx, action, err := m.MultiSelectNavigate(m.index, len(m.Options))
		if err != nil {
			return m.Values
		}

		switch action {
		case "cancel":
			// Escape pressed — keep current values
			m.CloseSection()
			return m.Values

		case "toggle":
			// Space pressed — toggle the current item
			m.index = newIdx
			key := m.Options[m.index].Key
			if Contains(m.Values, key) {
				for i, v := range m.Values {
					if v == key {
						m.Values = append(m.Values[:i], m.Values[i+1:]...)
						break
					}
				}
			} else {
				m.Values = append(m.Values, key)
			}
			m.renderOptions()

		case "confirm":
			// Enter pressed — confirm selection
			m.CloseSection()
			return m.Values

		case "move":
			// Arrow key moved
			m.index = newIdx
			m.renderOptions()
		}
	}
}

// renderOptions redraws the option list.
func (m *MultiSelect) renderOptions() {
	// Move up to the start of options
	terminal.MoveCursorUp(len(m.Options) + 2)
	terminal.ClearLine()

	for i, opt := range m.Options {
		checked := Contains(m.Values, opt.Key)
		highlighted := i == m.index
		m.RenderCheckbox(opt.Key, opt.Description, checked, highlighted)
	}
}

// Done returns true after Prompt has been called.
func (m *MultiSelect) Done() bool { return true }

// Ensure theme is used
var _ = theme.Blue
