package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
	"github.com/synapse-dev/synapse-cli/internal/output"
)

var providersCmd = &cobra.Command{
	Use:   "providers",
	Short: "Manage model providers",
}

var providersListCmd = &cobra.Command{
	Use:   "list",
	Short: "List configured model providers",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp []map[string]any
		if err := client.Get("/api/providers", &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header(fmt.Sprintf("Model Providers (%d)", len(resp)))
		output.Separator()
		for _, p := range resp {
			enabled := "disabled"
			if fmt.Sprint(p["enabled"]) == "true" {
				enabled = "enabled"
			}
			output.Row(fmt.Sprint(p["id"]), fmt.Sprint(p["name"]), fmt.Sprint(p["type"]), enabled)
		}
		return nil
	},
}

var providersTestCmd = &cobra.Command{
	Use:   "test <providerId>",
	Short: "Test a model provider connection",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		var resp map[string]any
		if err := client.Post("/api/providers/"+args[0]+"/test", nil, &resp); err != nil {
			return err
		}

		if fmt.Sprint(resp["success"]) == "true" {
			output.OK(fmt.Sprintf("Provider %s reachable: %s", args[0], resp["message"]))
		} else {
			output.Error(fmt.Sprintf("Provider %s failed: %s", args[0], resp["message"]))
		}
		return nil
	},
}

func init() {
	providersCmd.AddCommand(providersListCmd, providersTestCmd)
	rootCmd.AddCommand(providersCmd)
}
