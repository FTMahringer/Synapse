package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
	"github.com/synapse-dev/synapse-cli/internal/api"
	"github.com/synapse-dev/synapse-cli/internal/config"
	"github.com/synapse-dev/synapse-cli/internal/output"
)

var healthCmd = &cobra.Command{
	Use:   "health",
	Short: "Check backend health",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp map[string]any
		if err := client.Get("/api/health", &resp); err != nil {
			return fmt.Errorf("health check failed: %w", err)
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header("Backend Health")
		output.KV("Status", fmt.Sprint(resp["status"]))
		output.KV("Version", fmt.Sprint(resp["version"]))
		output.KV("System", fmt.Sprint(resp["systemName"]))
		output.KV("ECHO", fmt.Sprint(resp["echoActivation"]))
		output.KV("Timestamp", fmt.Sprint(resp["timestamp"]))
		return nil
	},
}

func clientFromCmd(cmd *cobra.Command) *api.Client {
	profile, _ := cmd.Flags().GetString("profile")
	p := config.GetProfile(profile)
	host, _ := cmd.Flags().GetString("host")
	if host != "" {
		p.Host = host
	}
	return api.NewClient(p.Host, p.Token)
}

func init() {
	rootCmd.AddCommand(healthCmd)
}
