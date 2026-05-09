package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
	"github.com/synapse-dev/synapse-cli/internal/output"
)

var agentsCmd = &cobra.Command{
	Use:   "agents",
	Short: "Manage agents",
}

var agentsListCmd = &cobra.Command{
	Use:   "list",
	Short: "List all agents",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp []map[string]any
		if err := client.Get("/api/agents", &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header(fmt.Sprintf("Agents (%d)", len(resp)))
		output.Separator()
		for _, a := range resp {
			output.KV(fmt.Sprint(a["id"]), fmt.Sprintf("%s  %s", a["name"], a["type"]))
		}
		return nil
	},
}

var agentsRuntimeCmd = &cobra.Command{
	Use:   "runtime",
	Short: "Show agent runtime states",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp []map[string]any
		if err := client.Get("/api/agents/runtime", &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header("Agent Runtime States")
		output.Separator()
		for _, r := range resp {
			output.Row(fmt.Sprint(r["agentId"]), fmt.Sprint(r["state"]))
		}
		return nil
	},
}

var agentsActivateCmd = &cobra.Command{
	Use:   "activate <agentId>",
	Short: "Activate an agent",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		var resp map[string]any
		if err := client.Post("/api/agents/"+args[0]+"/activate", nil, &resp); err != nil {
			return err
		}
		output.OK(fmt.Sprintf("Agent %s activated (state: %s)", args[0], resp["state"]))
		return nil
	},
}

var agentsPauseCmd = &cobra.Command{
	Use:   "pause <agentId>",
	Short: "Pause an agent",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		var resp map[string]any
		if err := client.Post("/api/agents/"+args[0]+"/pause", nil, &resp); err != nil {
			return err
		}
		output.OK(fmt.Sprintf("Agent %s paused", args[0]))
		return nil
	},
}

func init() {
	agentsCmd.AddCommand(agentsListCmd, agentsRuntimeCmd, agentsActivateCmd, agentsPauseCmd)
	rootCmd.AddCommand(agentsCmd)
}
