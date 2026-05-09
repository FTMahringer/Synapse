package output

import (
	"encoding/json"
	"fmt"
	"os"
	"strings"
)

const (
	reset = "\033[0m"
	bold  = "\033[1m"
	cyan  = "\033[36m"
	green = "\033[32m"
	red   = "\033[31m"
	dim   = "\033[2m"
)

func Header(text string) {
	fmt.Printf("%s%s%s%s\n", bold, cyan, text, reset)
}

func KV(key, value string) {
	fmt.Printf("  %s%-14s%s %s\n", dim, key+":", reset, value)
}

func Row(cols ...string) {
	fmt.Println("  " + strings.Join(cols, "  "))
}

func OK(msg string) {
	fmt.Printf("%s✓%s %s\n", green, reset, msg)
}

func Error(msg string) {
	fmt.Fprintf(os.Stderr, "%s✗%s %s\n", red, reset, msg)
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
	fmt.Println(dim + strings.Repeat("─", 48) + reset)
}
