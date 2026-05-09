package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
	"github.com/synapse-dev/synapse-cli/internal/output"
)

var pluginsCmd = &cobra.Command{
	Use:   "plugins",
	Short: "Manage plugins",
}

var pluginsListCmd = &cobra.Command{
	Use:   "list",
	Short: "List installed plugins",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp []map[string]any
		if err := client.Get("/api/plugins", &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header(fmt.Sprintf("Plugins (%d)", len(resp)))
		output.Separator()
		for _, p := range resp {
			output.Row(fmt.Sprint(p["id"]), fmt.Sprint(p["name"]), fmt.Sprint(p["type"]), fmt.Sprint(p["status"]))
		}
		return nil
	},
}

var pluginsEnableCmd = &cobra.Command{
	Use:   "enable <pluginId>",
	Short: "Enable a plugin",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		var resp map[string]any
		if err := client.Post("/api/plugins/"+args[0]+"/enable", nil, &resp); err != nil {
			return err
		}
		output.OK("Plugin enabled: " + args[0])
		return nil
	},
}

var pluginsDisableCmd = &cobra.Command{
	Use:   "disable <pluginId>",
	Short: "Disable a plugin",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		var resp map[string]any
		if err := client.Post("/api/plugins/"+args[0]+"/disable", nil, &resp); err != nil {
			return err
		}
		output.OK("Plugin disabled: " + args[0])
		return nil
	},
}

var storeCmd = &cobra.Command{
	Use:   "store",
	Short: "Browse store registry",
}

var storeListCmd = &cobra.Command{
	Use:   "list",
	Short: "List store entries",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")
		entryType, _ := cmd.Flags().GetString("type")

		path := "/api/store"
		if entryType != "" {
			path += "?type=" + entryType
		}

		var resp []map[string]any
		if err := client.Get(path, &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header(fmt.Sprintf("Store (%d entries)", len(resp)))
		output.Separator()
		for _, e := range resp {
			output.Row(fmt.Sprint(e["id"]), fmt.Sprint(e["type"]), fmt.Sprint(e["source"]), fmt.Sprint(e["version"]))
		}
		return nil
	},
}

func init() {
	storeListCmd.Flags().StringP("type", "t", "", "Filter by type (PLUGIN or BUNDLE)")
	storeCmd.AddCommand(storeListCmd)
	pluginsCmd.AddCommand(pluginsListCmd, pluginsEnableCmd, pluginsDisableCmd)
	rootCmd.AddCommand(pluginsCmd, storeCmd)
}
