package components

import (
	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// Confirm is a yes/no confirmation prompt.
type Confirm struct {
	BaseComponent
	Message string
	Default bool
	Value   bool
}

// NewConfirm creates a confirmation prompt.
func NewConfirm(message string, defaultVal bool) *Confirm {
	return &Confirm{
		BaseComponent: NewBase("Confirm", theme.Yellow),
		Message:       message,
		Default:       defaultVal,
		Value:         defaultVal,
	}
}

// Prompt displays the confirmation and reads input.
func (c *Confirm) Prompt() bool {
	c.Value = c.Confirm(c.Message, c.Default)
	return c.Value
}

// Done returns true after Prompt has been called.
func (c *Confirm) Done() bool { return true }

// Ensure theme is used
var _ = theme.Blue
