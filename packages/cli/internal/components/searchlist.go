package components

import (
	"fmt"
	"strings"

	"github.com/synapse-dev/synapse-cli/internal/terminal"
	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// SearchList is a filter-as-you-type list component with arrow-key navigation.
// Type to filter, ↑/↓ to navigate, Enter to confirm, Escape to cancel.
type SearchList struct {
	BaseComponent
	Label    string
	Options  []Option
	Default  string
	Value    string
	index    int
	filter   string
	filtered []Option
	NoBorder bool // When true, doesn't render its own section border (for use inside another section)
}

// NewSearchList creates a searchable list component.
func NewSearchList(label string, options []Option, defaultVal string) *SearchList {
	idx := FindOptionIndex(options, defaultVal)
	return &SearchList{
		BaseComponent: NewBase("Search", theme.Purple),
		Label:         label,
		Options:       options,
		Default:       defaultVal,
		Value:         defaultVal,
		index:         idx,
		filtered:      options,
	}
}

// Prompt displays the searchable list with live filtering.
func (s *SearchList) Prompt() string {
	if !s.NoBorder {
		s.RenderSection()
	}
	s.RenderLine(s.Label)
	s.RenderLine("  Type to filter • ↑/↓ navigate • Enter confirm • Esc cancel")
	s.RenderLine("")

	// Show all options initially
	s.filtered = s.Options
	s.renderFiltered()

	// Search input line
	fmt.Printf("%s│%s  %sSearch:%s ", theme.Dim, theme.Reset, theme.White, theme.Reset)

	// Read with live filtering
	filter, err := terminal.ReadFilteredLine("", func(val string) {
		s.filter = val
		s.applyFilter()
		s.renderFiltered()
	})
	if err != nil {
		if !s.NoBorder {
			s.CloseSection()
		}
		return s.Value
	}

	// If filter is empty, use default
	if filter == "" {
		if !s.NoBorder {
			s.CloseSection()
		}
		return s.Default
	}

	// Try exact match first
	for _, opt := range s.filtered {
		if strings.EqualFold(opt.Key, filter) {
			s.Value = opt.Key
			if !s.NoBorder {
				s.CloseSection()
			}
			return s.Value
		}
	}

	// If we have filtered results, use the first one
	if len(s.filtered) > 0 {
		s.Value = s.filtered[0].Key
		if !s.NoBorder {
			s.CloseSection()
		}
		return s.Value
	}

	// No match — use default
	s.RenderWarning(fmt.Sprintf("No match for '%s'. Using default: %s", filter, s.Default))
	if !s.NoBorder {
		s.CloseSection()
	}
	return s.Default
}

// applyFilter filters options based on current filter string.
func (s *SearchList) applyFilter() {
	if s.filter == "" {
		s.filtered = s.Options
		return
	}
	lower := strings.ToLower(s.filter)
	s.filtered = nil
	for _, opt := range s.Options {
		if strings.Contains(strings.ToLower(opt.Key), lower) ||
			strings.Contains(strings.ToLower(opt.Description), lower) {
			s.filtered = append(s.filtered, opt)
		}
	}
	if s.filtered == nil {
		s.filtered = []Option{} // empty but not nil
	}
}

// renderFiltered redraws the filtered option list.
func (s *SearchList) renderFiltered() {
	// Move up to the start of options
	terminal.MoveCursorUp(len(s.Options) + 1)
	terminal.ClearLine()

	if len(s.filtered) == 0 {
		s.RenderLine(theme.Gray + "No matches found" + theme.Reset)
		return
	}

	for i, opt := range s.filtered {
		selected := opt.Key == s.Value
		highlighted := i == s.index
		s.RenderOption(opt.Key, opt.Description, selected, highlighted)
	}
}

// Done returns true after Prompt has been called.
func (s *SearchList) Done() bool { return s.Value != "" }

// Ensure theme is used
var _ = theme.Blue
