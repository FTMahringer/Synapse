package tui

import (
	"fmt"
	"strings"

	"github.com/synapse-dev/synapse-cli/internal/api"
)

const (
	reset  = "\033[0m"
	bold   = "\033[1m"
	cyan   = "\033[36m"
	green  = "\033[32m"
	dim    = "\033[2m"
	yellow = "\033[33m"
)

// PrintOverview fetches and prints a terminal overview (no external deps).
func PrintOverview(client *api.Client) {
	fmt.Printf("\n%s%sSYNAPSE%s  Operator Overview\n", bold, cyan, reset)
	fmt.Println(dim + strings.Repeat("─", 48) + reset)

	var health map[string]any
	if err := client.Get("/api/health", &health); err != nil {
		fmt.Printf("%s  health: %s%s\n", yellow, err.Error(), reset)
	} else {
		fmt.Printf("  %sstatus%s   %s\n", dim, reset, health["status"])
		fmt.Printf("  %sversion%s  %s\n", dim, reset, health["version"])
		fmt.Printf("  %ssystem%s   %s\n", dim, reset, health["systemName"])
		fmt.Printf("  %secho%s     %s\n", dim, reset, health["echoActivation"])
	}

	fmt.Println()
	fmt.Println(dim + strings.Repeat("─", 48) + reset)

	var agents []map[string]any
	if err := client.Get("/api/agents", &agents); err != nil {
		fmt.Printf("%s  agents: %s%s\n", yellow, err.Error(), reset)
	} else {
		fmt.Printf("  %s%sAgents%s (%d)\n", bold, cyan, reset, len(agents))
		for _, a := range agents {
			fmt.Printf("  %s%-24s%s %s\n", dim, a["id"], reset, a["name"])
		}
	}

	fmt.Println()
	fmt.Println(dim + strings.Repeat("─", 48) + reset)

	var runtimes []map[string]any
	if err := client.Get("/api/agents/runtime", &runtimes); err == nil {
		fmt.Printf("  %s%sRuntime States%s\n", bold, cyan, reset)
		for _, r := range runtimes {
			state := fmt.Sprint(r["state"])
			color := green
			if state != "ACTIVE" {
				color = yellow
			}
			fmt.Printf("  %-24s %s%s%s\n", r["agentId"], color, state, reset)
		}
	}

	fmt.Println()
}
