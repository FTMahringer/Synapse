package cmd

import (
	"bufio"
	"fmt"
	"strings"

	"github.com/spf13/cobra"
	"github.com/synapse-dev/synapse-cli/internal/output"
)

var logsCmd = &cobra.Command{
	Use:   "logs",
	Short: "View system logs",
}

var logsListCmd = &cobra.Command{
	Use:   "list",
	Short: "Fetch recent system logs",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		limit, _ := cmd.Flags().GetInt("limit")
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp []map[string]any
		if err := client.Get(fmt.Sprintf("/api/logs?limit=%d", limit), &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header(fmt.Sprintf("System Logs (%d)", len(resp)))
		output.Separator()
		for _, log := range resp {
			level := fmt.Sprint(log["level"])
			cat := fmt.Sprint(log["category"])
			event := fmt.Sprint(log["event"])
			ts := fmt.Sprint(log["timestamp"])
			output.Row(ts[:min(19, len(ts))], level, cat, event)
		}
		return nil
	},
}

var logsStreamCmd = &cobra.Command{
	Use:   "stream",
	Short: "Stream live logs via SSE",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)

		output.Header("Streaming live logs (Ctrl+C to stop)…")

		body, err := client.GetStream("/api/logs/stream")
		if err != nil {
			return fmt.Errorf("stream failed: %w", err)
		}
		defer body.Close()

		scanner := bufio.NewScanner(body)
		for scanner.Scan() {
			line := scanner.Text()
			if strings.HasPrefix(line, "data:") {
				data := strings.TrimPrefix(line, "data:")
				fmt.Println(strings.TrimSpace(data))
			}
		}
		return scanner.Err()
	},
}

func init() {
	logsListCmd.Flags().IntP("limit", "n", 25, "Number of logs to fetch")
	logsCmd.AddCommand(logsListCmd, logsStreamCmd)
	rootCmd.AddCommand(logsCmd)
}
