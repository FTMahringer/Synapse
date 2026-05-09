package output

import (
	"encoding/json"
	"fmt"
	"os"
	"strings"

	"github.com/charmbracelet/lipgloss"
)

var (
	labelStyle  = lipgloss.NewStyle().Foreground(lipgloss.Color("240"))
	valueStyle  = lipgloss.NewStyle().Foreground(lipgloss.Color("252"))
	headerStyle = lipgloss.NewStyle().Bold(true).Foreground(lipgloss.Color("86"))
	errorStyle  = lipgloss.NewStyle().Foreground(lipgloss.Color("196"))
	okStyle     = lipgloss.NewStyle().Foreground(lipgloss.Color("82"))
)

func Header(text string) {
	fmt.Println(headerStyle.Render(text))
}

func KV(key, value string) {
	fmt.Printf("%s  %s\n", labelStyle.Render(key+":"), valueStyle.Render(value))
}

func Row(cols ...string) {
	fmt.Println(strings.Join(cols, "  "))
}

func OK(msg string) {
	fmt.Println(okStyle.Render("✓ " + msg))
}

func Error(msg string) {
	fmt.Fprintln(os.Stderr, errorStyle.Render("✗ "+msg))
}

func JSON(v any) {
	data, err := json.MarshalIndent(v, "", "  ")
	if err != nil {
		Error(err.Error())
		return
	}
	fmt.Println(string(data))
}

func Separator() {
	fmt.Println(labelStyle.Render(strings.Repeat("─", 48)))
}
