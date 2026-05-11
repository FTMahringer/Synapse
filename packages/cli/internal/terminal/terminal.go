// Package terminal provides raw terminal input handling.
// Supports arrow keys, Enter, Space, Escape, and regular text input
// without requiring external dependencies.
package terminal

import (
	"fmt"
	"os"
	"os/exec"
	"runtime"
)

// Key represents a single key press.
type Key int

const (
	KeyUnknown Key = iota
	KeyEnter
	KeySpace
	KeyEscape
	KeyTab
	KeyBackspace
	KeyDelete
	KeyUp
	KeyDown
	KeyLeft
	KeyRight
	KeyHome
	KeyEnd
	KeyRune // Any printable character
)

// Event represents a terminal input event.
type Event struct {
	Key  Key
	Rune rune // The actual character if Key == KeyRune
}

// ── Raw terminal mode ──────────────────────────────────────────────

var rawMode bool

// EnableRawMode puts the terminal into raw mode (no echo, no line buffering).
func EnableRawMode() error {
	if rawMode {
		return nil
	}

	switch runtime.GOOS {
	case "windows":
		if err := enableRawModeWindows(); err != nil {
			return fmt.Errorf("raw mode: %w", err)
		}
	default:
		if err := enableRawModeUnix(); err != nil {
			return fmt.Errorf("raw mode: %w", err)
		}
	}

	rawMode = true
	return nil
}

// DisableRawMode restores the terminal to cooked mode.
func DisableRawMode() {
	if !rawMode {
		return
	}

	switch runtime.GOOS {
	case "windows":
		disableRawModeWindows()
	default:
		disableRawModeUnix()
	}

	rawMode = false
}

// ── Reading input ──────────────────────────────────────────────────

// ReadEvent reads a single key event from stdin.
func ReadEvent() (Event, error) {
	buf := make([]byte, 8)
	n, err := os.Stdin.Read(buf)
	if err != nil {
		return Event{}, err
	}

	if n == 0 {
		return Event{Key: KeyUnknown}, nil
	}

	b := buf[0]

	// Escape sequences (arrow keys, etc.)
	if b == 0x1b {
		if n >= 3 && buf[1] == '[' {
			switch buf[2] {
			case 'A':
				return Event{Key: KeyUp}, nil
			case 'B':
				return Event{Key: KeyDown}, nil
			case 'C':
				return Event{Key: KeyRight}, nil
			case 'D':
				return Event{Key: KeyLeft}, nil
			case 'H':
				return Event{Key: KeyHome}, nil
			case 'F':
				return Event{Key: KeyEnd}, nil
			case '3':
				if n >= 4 && buf[3] == '~' {
					return Event{Key: KeyDelete}, nil
				}
			}
		}
		return Event{Key: KeyEscape}, nil
	}

	switch b {
	case 0x0d, 0x0a:
		return Event{Key: KeyEnter}, nil
	case 0x20:
		return Event{Key: KeySpace}, nil
	case 0x1b:
		return Event{Key: KeyEscape}, nil
	case 0x09:
		return Event{Key: KeyTab}, nil
	case 0x7f, 0x08:
		return Event{Key: KeyBackspace}, nil
	}

	return Event{Key: KeyRune, Rune: rune(b)}, nil
}

// ReadLine reads a full line of text with basic editing support.
// Returns the final string when Enter is pressed.
func ReadLine() (string, error) {
	if err := EnableRawMode(); err != nil {
		// Fallback to simple Scanln
		var input string
		fmt.Scanln(&input)
		return input, nil
	}
	defer DisableRawMode()

	var chars []rune
	cursor := 0

	for {
		evt, err := ReadEvent()
		if err != nil {
			return "", err
		}

		switch evt.Key {
		case KeyEnter:
			fmt.Println()
			return string(chars), nil

		case KeyBackspace:
			if cursor > 0 {
				cursor--
				chars = append(chars[:cursor], chars[cursor+1:]...)
				redisplayLine(chars, cursor)
			}

		case KeyDelete:
			if cursor < len(chars) {
				chars = append(chars[:cursor], chars[cursor+1:]...)
				redisplayLine(chars, cursor)
			}

		case KeyLeft:
			if cursor > 0 {
				cursor--
				fmt.Print("\033[D")
			}

		case KeyRight:
			if cursor < len(chars) {
				cursor++
				fmt.Print("\033[C")
			}

		case KeyHome:
			fmt.Print("\033[" + itoa(cursor) + "D")
			cursor = 0

		case KeyEnd:
			diff := len(chars) - cursor
			fmt.Print("\033[" + itoa(diff) + "C")
			cursor = len(chars)

		case KeyRune:
			// Insert the character at cursor position
			chars = append(chars[:cursor], append([]rune{evt.Rune}, chars[cursor:]...)...)
			cursor++
			redisplayLine(chars, cursor)
		}
	}
}

// redisplayLine rewrites the current line.
func redisplayLine(chars []rune, cursor int) {
	// Clear from cursor to end of line
	fmt.Print("\033[K")
	// Print the full content
	fmt.Print(string(chars))
	// Move cursor back to position
	back := len(chars) - cursor
	if back > 0 {
		fmt.Print("\033[" + itoa(back) + "D")
	}
}

// ── Select-style input ─────────────────────────────────────────────

// SelectEvent reads arrow-key navigation events for list selection.
// Returns the selected index when Enter/Space is pressed, or -1 for Escape.
func SelectEvent(currentIndex, itemCount int) (int, error) {
	if err := EnableRawMode(); err != nil {
		return -1, err
	}
	defer DisableRawMode()

	for {
		evt, err := ReadEvent()
		if err != nil {
			return -1, err
		}

		switch evt.Key {
		case KeyUp:
			if currentIndex > 0 {
				return currentIndex - 1, nil
			}
			return currentIndex, nil

		case KeyDown:
			if currentIndex < itemCount-1 {
				return currentIndex + 1, nil
			}
			return currentIndex, nil

		case KeyEnter, KeySpace:
			return currentIndex, nil

		case KeyEscape:
			return -1, nil
		}
	}
}

// MultiSelectEvent reads arrow-key navigation events for multi-select lists.
// Returns the new index, and a separate "action" indicating what key was pressed.
func MultiSelectEvent(currentIndex, itemCount int) (newIndex int, action string, err error) {
	if err := EnableRawMode(); err != nil {
		return -1, "", err
	}
	defer DisableRawMode()

	for {
		evt, err := ReadEvent()
		if err != nil {
			return -1, "", err
		}

		switch evt.Key {
		case KeyUp:
			if currentIndex > 0 {
				return currentIndex - 1, "move", nil
			}
			return currentIndex, "move", nil

		case KeyDown:
			if currentIndex < itemCount-1 {
				return currentIndex + 1, "move", nil
			}
			return currentIndex, "move", nil

		case KeySpace:
			return currentIndex, "toggle", nil

		case KeyEnter:
			return currentIndex, "confirm", nil

		case KeyEscape:
			return -1, "cancel", nil
		}
	}
}

// ── Platform-specific raw mode ─────────────────────────────────────

// Windows raw mode using PowerShell to disable echo and line buffering.
var windowsRawRestore string

func enableRawModeWindows() error {
	// Use PowerShell to save and set console mode
	cmd := exec.Command("powershell", "-NoProfile", "-Command",
		"$m = [Console]::TreatControlCAsInput; $h = [Console]::In; "+
			"$m | Out-File -FilePath $env:TEMP\\_synapse_term_mode.txt; "+
			"[Console]::TreatControlCAsInput = $true; "+
			"$h.ReadKey('NoEcho,IncludeKeyDown') | Out-Null")
	return cmd.Run()
}

func disableRawModeWindows() {
	// Restore via PowerShell
	exec.Command("powershell", "-NoProfile", "-Command",
		"[Console]::TreatControlCAsInput = $false").Run()
}

// Unix raw mode using termios.
func enableRawModeUnix() error {
	cmd := exec.Command("sh", "-c",
		"stty -echo -icanon min 1 time 0 2>/dev/null")
	return cmd.Run()
}

func disableRawModeUnix() {
	exec.Command("sh", "-c",
		"stty echo icanon 2>/dev/null").Run()
}

// itoa is a simple int to string converter (avoid strconv import).
func itoa(n int) string {
	if n == 0 {
		return "0"
	}
	s := ""
	neg := false
	if n < 0 {
		neg = true
		n = -n
	}
	for n > 0 {
		s = string(rune('0'+n%10)) + s
		n /= 10
	}
	if neg {
		return "-" + s
	}
	return s
}

// ClearLine clears the current line from cursor position.
func ClearLine() {
	fmt.Print("\033[K")
}

// MoveCursorUp moves cursor up n lines.
func MoveCursorUp(n int) {
	fmt.Print("\033[" + itoa(n) + "A")
}

// MoveCursorDown moves cursor down n lines.
func MoveCursorDown(n int) {
	fmt.Print("\033[" + itoa(n) + "B")
}

// HideCursor hides the terminal cursor.
func HideCursor() {
	fmt.Print("\033[?25l")
}

// ShowCursor shows the terminal cursor.
func ShowCursor() {
	fmt.Print("\033[?25h")
}

// ReadSecret reads a password-style input (masked, no echo).
func ReadSecret() (string, error) {
	if err := EnableRawMode(); err != nil {
		// Fallback
		var input string
		fmt.Scanln(&input)
		return input, nil
	}
	defer DisableRawMode()

	var chars []rune
	for {
		evt, err := ReadEvent()
		if err != nil {
			return "", err
		}

		switch evt.Key {
		case KeyEnter:
			fmt.Println()
			return string(chars), nil

		case KeyBackspace:
			if len(chars) > 0 {
				chars = chars[:len(chars)-1]
				fmt.Print("\b \b")
			}

		case KeyRune:
			chars = append(chars, evt.Rune)
			fmt.Print("*")
		}
	}
}

// ReadKey reads a single key press and returns it.
func ReadKey() (Event, error) {
	if err := EnableRawMode(); err != nil {
		return Event{Key: KeyEnter}, nil
	}
	defer DisableRawMode()
	return ReadEvent()
}

// ReadString reads a line of text, optionally masked.
func ReadString(masked bool) (string, error) {
	if masked {
		return ReadSecret()
	}
	return ReadLine()
}

// ── ANSI helpers ───────────────────────────────────────────────────

// PrintAt prints text at a specific row, column.
func PrintAt(row, col int, text string) {
	fmt.Printf("\033[%d;%dH%s", row, col, text)
}

// SaveCursor saves the cursor position.
func SaveCursor() {
	fmt.Print("\033[s")
}

// RestoreCursor restores the saved cursor position.
func RestoreCursor() {
	fmt.Print("\033[u")
}

// Ensure terminal utilities are available
func init() {
	// On Windows, try to enable VT processing for better ANSI support
	if runtime.GOOS == "windows" {
		exec.Command("powershell", "-NoProfile", "-Command",
			"if ($host.UI.RawUI) { $host.UI.RawUI.ForegroundColor = $host.UI.RawUI.ForegroundColor }").Run()
	}
}

// ReadLineWithDefault reads a line with a pre-filled default value.
func ReadLineWithDefault(defaultVal string) (string, error) {
	if err := EnableRawMode(); err != nil {
		var input string
		fmt.Scanln(&input)
		if input == "" {
			return defaultVal, nil
		}
		return input, nil
	}
	defer DisableRawMode()

	chars := []rune(defaultVal)
	cursor := len(chars)

	// Display the default
	fmt.Print(string(chars))

	for {
		evt, err := ReadEvent()
		if err != nil {
			return "", err
		}

		switch evt.Key {
		case KeyEnter:
			fmt.Println()
			return string(chars), nil

		case KeyBackspace:
			if cursor > 0 {
				cursor--
				chars = append(chars[:cursor], chars[cursor+1:]...)
				redisplayLine(chars, cursor)
			}

		case KeyDelete:
			if cursor < len(chars) {
				chars = append(chars[:cursor], chars[cursor+1:]...)
				redisplayLine(chars, cursor)
			}

		case KeyLeft:
			if cursor > 0 {
				cursor--
				fmt.Print("\033[D")
			}

		case KeyRight:
			if cursor < len(chars) {
				cursor++
				fmt.Print("\033[C")
			}

		case KeyHome:
			fmt.Print("\033[" + itoa(cursor) + "D")
			cursor = 0

		case KeyEnd:
			diff := len(chars) - cursor
			fmt.Print("\033[" + itoa(diff) + "C")
			cursor = len(chars)

		case KeyRune:
			chars = append(chars[:cursor], append([]rune{evt.Rune}, chars[cursor:]...)...)
			cursor++
			redisplayLine(chars, cursor)
		}
	}
}

// ReadSecretWithDefault reads a password with a default value (shown as asterisks).
func ReadSecretWithDefault(defaultVal string) (string, error) {
	if err := EnableRawMode(); err != nil {
		var input string
		fmt.Scanln(&input)
		if input == "" {
			return defaultVal, nil
		}
		return input, nil
	}
	defer DisableRawMode()

	chars := []rune(defaultVal)

	// Show asterisks for the default
	for range chars {
		fmt.Print("*")
	}

	for {
		evt, err := ReadEvent()
		if err != nil {
			return "", err
		}

		switch evt.Key {
		case KeyEnter:
			fmt.Println()
			return string(chars), nil

		case KeyBackspace:
			if len(chars) > 0 {
				chars = chars[:len(chars)-1]
				fmt.Print("\b \b")
			}

		case KeyRune:
			chars = append(chars, evt.Rune)
			fmt.Print("*")
		}
	}
}

// ReadFilteredLine reads a line while calling a render function on each change (for search/filter).
func ReadFilteredLine(initial string, onChanged func(string)) (string, error) {
	if err := EnableRawMode(); err != nil {
		return initial, nil
	}
	defer DisableRawMode()

	chars := []rune(initial)
	cursor := len(chars)

	// Initial render
	onChanged(string(chars))

	for {
		// Print current input
		fmt.Print("\r\033[K" + string(chars))
		back := len(chars) - cursor
		if back > 0 {
			fmt.Print("\033[" + itoa(back) + "D")
		}

		evt, err := ReadEvent()
		if err != nil {
			return "", err
		}

		switch evt.Key {
		case KeyEnter:
			fmt.Println()
			return string(chars), nil

		case KeyEscape:
			fmt.Println()
			return initial, nil

		case KeyBackspace:
			if cursor > 0 {
				cursor--
				chars = append(chars[:cursor], chars[cursor+1:]...)
				onChanged(string(chars))
			}

		case KeyDelete:
			if cursor < len(chars) {
				chars = append(chars[:cursor], chars[cursor+1:]...)
				onChanged(string(chars))
			}

		case KeyLeft:
			if cursor > 0 {
				cursor--
			}

		case KeyRight:
			if cursor < len(chars) {
				cursor++
			}

		case KeyHome:
			cursor = 0

		case KeyEnd:
			cursor = len(chars)

		case KeyRune:
			chars = append(chars[:cursor], append([]rune{evt.Rune}, chars[cursor:]...)...)
			cursor++
			onChanged(string(chars))
		}
	}
}

// ReadKeyEvent reads a single key event without raw mode management.
// The caller is responsible for raw mode state.
func ReadKeyEvent() (Event, error) {
	return ReadEvent()
}

// EnsureCleanup restores terminal on panic.
func EnsureCleanup() {
	if r := recover(); r != nil {
		DisableRawMode()
		ShowCursor()
		panic(r)
	}
}

// ClearScreen clears the entire terminal.
func ClearScreen() {
	fmt.Print("\033[2J\033[H")
}

// GetTerminalWidth returns the terminal width (best effort).
func GetTerminalWidth() int {
	switch runtime.GOOS {
	case "windows":
		cmd := exec.Command("powershell", "-NoProfile", "-Command",
			"(Get-Host).UI.RawUI.WindowSize.Width")
		out, err := cmd.Output()
		if err == nil {
			var w int
			fmt.Sscanf(string(out), "%d", &w)
			if w > 0 {
				return w
			}
		}
	default:
		cmd := exec.Command("sh", "-c", "tput cols 2>/dev/null || echo 80")
		out, err := cmd.Output()
		if err == nil {
			var w int
			fmt.Sscanf(string(out), "%d", &w)
			if w > 0 {
				return w
			}
		}
	}
	return 80
}

// GetTerminalHeight returns the terminal height (best effort).
func GetTerminalHeight() int {
	switch runtime.GOOS {
	case "windows":
		cmd := exec.Command("powershell", "-NoProfile", "-Command",
			"(Get-Host).UI.RawUI.WindowSize.Height")
		out, err := cmd.Output()
		if err == nil {
			var h int
			fmt.Sscanf(string(out), "%d", &h)
			if h > 0 {
				return h
			}
		}
	default:
		cmd := exec.Command("sh", "-c", "tput lines 2>/dev/null || echo 24")
		out, err := cmd.Output()
		if err == nil {
			var h int
			fmt.Sscanf(string(out), "%d", &h)
			if h > 0 {
				return h
			}
		}
	}
	return 24
}

// ReadKeyWithPrompt reads a single key press with a prompt message.
func ReadKeyWithPrompt(prompt string) (Event, error) {
	fmt.Print(prompt)
	return ReadKey()
}

// IsPrintable checks if a rune is a printable character.
func IsPrintable(r rune) bool {
	return r >= 32 && r <= 126
}
