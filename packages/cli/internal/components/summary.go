package components

import (
	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// SummaryItem is a key-value pair for the summary view.
type SummaryItem struct {
	Key   string
	Value string
}

// Summary renders a read-only key-value review inside a bordered section.
type Summary struct {
	BaseComponent
	Title string
	Color string
	Items []SummaryItem
}

// NewSummary creates a summary display.
func NewSummary(title, color string) *Summary {
	return &Summary{
		BaseComponent: NewBase(title, color),
		Title:         title,
		Color:         color,
	}
}

// Add appends a key-value pair.
func (s *Summary) Add(key, value string) {
	s.Items = append(s.Items, SummaryItem{Key: key, Value: value})
}

// Render outputs the summary as a bordered section.
func (s *Summary) Render() {
	s.RenderSection()
	for _, item := range s.Items {
		displayValue := item.Value
		if len(displayValue) > 40 {
			displayValue = displayValue[:40] + "…"
		}
		s.RenderLabel(item.Key, displayValue)
	}
	s.CloseSection()
}

// Ensure theme is used
var _ = theme.Blue
