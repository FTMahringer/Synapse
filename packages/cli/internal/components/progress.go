package components

import (
	"fmt"

	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// Progress displays a progress bar with a message.
type Progress struct {
	BaseComponent
	Message string
	Percent int
	Width   int
}

// NewProgress creates a progress display.
func NewProgress(width int) *Progress {
	return &Progress{
		BaseComponent: NewBase("Progress", theme.Green),
		Width:         width,
	}
}

// Update sets the current progress state.
func (p *Progress) Update(percent int, message string) {
	p.Percent = percent
	p.Message = message
}

// Render outputs the progress bar.
func (p *Progress) Render() {
	p.RenderProgress(p.Percent, p.Width, p.Message)
}

// ── Spinner ────────────────────────────────────────────────────────

// Spinner is a simple animated spinner for indeterminate progress.
type Spinner struct {
	BaseComponent
	Message string
	frames  []string
	index   int
}

// NewSpinner creates a spinner component.
func NewSpinner(message string) *Spinner {
	return &Spinner{
		BaseComponent: NewBase("Spinner", theme.Green),
		Message:       message,
		frames:        []string{"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"},
	}
}

// NextFrame returns the next spinner frame and advances.
func (s *Spinner) NextFrame() string {
	frame := s.frames[s.index]
	s.index = (s.index + 1) % len(s.frames)
	return frame
}

// Render outputs the spinner with message.
func (s *Spinner) Render() {
	fmt.Printf("%s│%s  %s%s%s %s%s%s\n",
		theme.Dim, theme.Reset,
		theme.Green, s.NextFrame(), theme.Reset,
		theme.Gray, s.Message, theme.Reset)
}

// Ensure theme is used
var _ = theme.Blue
