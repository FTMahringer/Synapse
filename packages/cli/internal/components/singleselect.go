package components

import (
	"github.com/synapse-dev/synapse-cli/internal/terminal"
	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// Option represents a selectable item.
type Option struct {
	Key         string
	Description string
}

// SingleSelect is a radio-list selection component with arrow-key navigation.
// Use ↑/↓ to navigate, Enter/Space to confirm, Escape to cancel.
type SingleSelect struct {
	BaseComponent
	Label   string
	Options []Option
	Default string
	Value   string
	index   int
}

// NewSingleSelect creates a single-select component.
func NewSingleSelect(label string, options []Option, defaultVal string) *SingleSelect {
	idx := FindOptionIndex(options, defaultVal)
	return &SingleSelect{
		BaseComponent: NewBase("Select", theme.Blue),
		Label:         label,
		Options:       options,
		Default:       defaultVal,
		Value:         defaultVal,
		index:         idx,
	}
}

// Prompt displays the options with arrow-key navigation and reads a selection.
func (s *SingleSelect) Prompt() string {
	s.RenderSection()
	s.RenderLine(s.Label)
	s.RenderLine("")

	// Initial render
	s.renderOptions()

	for {
		newIdx, err := s.NavigateList(s.index, len(s.Options))
		if err != nil {
			return s.Value
		}

		if newIdx == -1 {
			// Escape pressed — keep current value
			s.CloseSection()
			return s.Value
		}

		if newIdx == s.index {
			// Enter or Space pressed — confirm selection
			s.Value = s.Options[s.index].Key
			s.CloseSection()
			return s.Value
		}

		// Arrow key moved — re-render
		s.index = newIdx
		s.renderOptions()
	}
}

// renderOptions redraws the option list.
func (s *SingleSelect) renderOptions() {
	// Move up to the start of options
	terminal.MoveCursorUp(len(s.Options) + 1)
	terminal.ClearLine()

	for i, opt := range s.Options {
		selected := opt.Key == s.Value
		highlighted := i == s.index
		s.RenderOption(opt.Key, opt.Description, selected, highlighted)
	}
}

// Done returns true after Prompt has been called.
func (s *SingleSelect) Done() bool { return s.Value != "" }

// Ensure theme is used
var _ = theme.Blue
