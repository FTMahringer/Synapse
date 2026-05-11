package components

import (
	"github.com/synapse-dev/synapse-cli/internal/theme"
)

// TextInput is an interactive validated text input field with arrow-key editing.
type TextInput struct {
	BaseComponent
	Label    string
	Default  string
	Secret   bool
	Validate func(string) string // returns error message or ""
	Value    string
}

// NewTextInput creates a text input component.
func NewTextInput(label, defaultVal string) *TextInput {
	return &TextInput{
		BaseComponent: NewBase("Input", theme.Blue),
		Label:         label,
		Default:       defaultVal,
		Value:         defaultVal,
	}
}

// SetSecret masks the input (for passwords).
func (t *TextInput) SetSecret() *TextInput {
	t.Secret = true
	return t
}

// SetValidator adds a validation function.
// The function should return "" on success, or an error message on failure.
func (t *TextInput) SetValidator(fn func(string) string) *TextInput {
	t.Validate = fn
	return t
}

// Prompt displays the input prompt and reads a value with arrow-key editing.
func (t *TextInput) Prompt() string {
	for {
		t.RenderPrompt(t.Label, t.Default)

		var input string
		var err error

		if t.Secret {
			input, err = t.ReadSecretWithDefault(t.Default)
		} else {
			input, err = t.ReadLineWithDefault(t.Default)
		}

		if err != nil {
			t.Value = t.Default
			return t.Value
		}

		t.Value = input

		if t.Validate != nil {
			if errMsg := t.Validate(t.Value); errMsg != "" {
				t.RenderError(errMsg)
				continue
			}
		}

		return t.Value
	}
}

// Done returns true after Prompt has been called.
func (t *TextInput) Done() bool { return t.Value != "" || t.Default != "" }

// ── Convenience: PromptText ────────────────────────────────────────

// PromptText is a standalone helper that creates a TextInput, prompts, and returns the value.
func PromptText(label, defaultVal string) string {
	return NewTextInput(label, defaultVal).Prompt()
}

// PromptSecret is a standalone helper for masked input.
func PromptSecret(label, defaultVal string) string {
	return NewTextInput(label, defaultVal).SetSecret().Prompt()
}

// PromptTextWithValidator is a standalone helper with validation.
func PromptTextWithValidator(label, defaultVal string, validate func(string) string) string {
	return NewTextInput(label, defaultVal).SetValidator(validate).Prompt()
}

// Ensure theme is used
var _ = theme.Blue
