package cmd

import (
	"fmt"
	"strings"

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
			status := fmt.Sprint(p["status"])
			loaderState := fmt.Sprint(p["loaderState"])
			trustTier := fmt.Sprint(p["trustTier"])
			output.Row(
				fmt.Sprint(p["id"]),
				fmt.Sprint(p["name"]),
				fmt.Sprint(p["type"]),
				fmt.Sprint(p["version"]),
				status,
				loaderState,
				trustTier,
			)
		}
		return nil
	},
}

var pluginsInfoCmd = &cobra.Command{
	Use:   "info <pluginId>",
	Short: "Show detailed plugin info",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp map[string]any
		if err := client.Get("/api/plugins/"+args[0], &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header("Plugin: " + args[0])
		output.KV("Name", fmt.Sprint(resp["name"]))
		output.KV("Type", fmt.Sprint(resp["type"]))
		output.KV("Version", fmt.Sprint(resp["version"]))
		output.KV("Status", fmt.Sprint(resp["status"]))
		output.KV("Loader State", fmt.Sprint(resp["loaderState"]))
		output.KV("Trust Tier", fmt.Sprint(resp["trustTier"]))
		output.KV("Storage Tier", fmt.Sprint(resp["storageTier"]))
		if deps, ok := resp["dependencies"].([]any); ok && len(deps) > 0 {
			list := make([]string, len(deps))
			for i, d := range deps {
				list[i] = fmt.Sprint(d)
			}
			output.KV("Dependencies", strings.Join(list, ", "))
		}
		if msg := resp["errorMessage"]; msg != nil && msg != "" {
			output.KV("Error", fmt.Sprint(msg))
		}
		return nil
	},
}

var pluginsLoadCmd = &cobra.Command{
	Use:   "load <pluginId>",
	Short: "Load a plugin into the JVM",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		var resp map[string]any
		if err := client.Post("/api/plugins/"+args[0]+"/load", nil, &resp); err != nil {
			return err
		}
		output.OK("Plugin loaded: " + args[0])
		return nil
	},
}

var pluginsUnloadCmd = &cobra.Command{
	Use:   "unload <pluginId>",
	Short: "Unload a plugin from the JVM",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		if err := client.Post("/api/plugins/"+args[0]+"/unload", nil, nil); err != nil {
			return err
		}
		output.OK("Plugin unloaded: " + args[0])
		return nil
	},
}

var pluginsReloadCmd = &cobra.Command{
	Use:   "reload <pluginId>",
	Short: "Reload a plugin",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		var resp map[string]any
		if err := client.Post("/api/plugins/"+args[0]+"/reload", nil, &resp); err != nil {
			return err
		}
		output.OK("Plugin reloaded: " + args[0])
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

var pluginsUninstallCmd = &cobra.Command{
	Use:   "uninstall <pluginId>",
	Short: "Uninstall a plugin",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		if err := client.Delete("/api/plugins/" + args[0]); err != nil {
			return err
		}
		output.OK("Plugin uninstalled: " + args[0])
		return nil
	},
}

var pluginsInstallCmd = &cobra.Command{
	Use:   "install <manifest-json>",
	Short: "Install a plugin from manifest JSON",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)

		var manifest map[string]any
		if err := client.Post("/api/plugins/install", args[0], &manifest); err != nil {
			return err
		}
		output.OK("Plugin installed")
		return nil
	},
}

var pluginsValidateCmd = &cobra.Command{
	Use:   "validate <jarPath>",
	Short: "Validate a plugin JAR (manifest + bytecode scan)",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp map[string]any
		if err := client.Post("/api/plugins/sandbox/scan", map[string]string{"jarPath": args[0]}, &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		clean := resp["clean"]
		if clean == true {
			output.OK("JAR is clean — no forbidden references found")
		} else {
			output.Error("JAR contains forbidden references")
			if violations, ok := resp["violations"].([]any); ok {
				for _, v := range violations {
					if vm, ok := v.(map[string]any); ok {
						output.Row(
							fmt.Sprint(vm["classFile"]),
							fmt.Sprint(vm["type"]),
							fmt.Sprint(vm["reference"]),
						)
					}
				}
			}
		}
		return nil
	},
}

var pluginsResolveCmd = &cobra.Command{
	Use:   "resolve-deps <pluginId>",
	Short: "Resolve dependencies for a plugin",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp map[string]any
		if err := client.Post("/api/plugins/"+args[0]+"/resolve-deps", nil, &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		success := resp["success"]
		if success == true {
			output.OK("Dependency resolution passed")
		} else {
			output.Error("Dependency resolution failed: " + fmt.Sprint(resp["message"]))
		}

		if items, ok := resp["items"].([]any); ok {
			output.Separator()
			for _, item := range items {
				if im, ok := item.(map[string]any); ok {
					output.Row(
						fmt.Sprint(im["dependencyId"]),
						fmt.Sprint(im["versionSpec"]),
						fmt.Sprint(im["action"]),
					)
				}
			}
		}
		return nil
	},
}

var pluginsLogsCmd = &cobra.Command{
	Use:   "logs <pluginId>",
	Short: "Show last system logs for a plugin",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		limit, _ := cmd.Flags().GetInt("limit")
		if limit <= 0 {
			limit = 50
		}

		var resp []map[string]any
		if err := client.Get(fmt.Sprintf("/api/logs?limit=%d", limit), &resp); err != nil {
			return err
		}

		pluginId := args[0]
		output.Header("Logs for " + pluginId)
		output.Separator()
		count := 0
		for _, log := range resp {
			source := fmt.Sprint(log["source"])
			payload := fmt.Sprint(log["payload"])
			if strings.Contains(source, pluginId) || strings.Contains(payload, pluginId) {
				output.Row(
					fmt.Sprint(log["timestamp"]),
					fmt.Sprint(log["level"]),
					fmt.Sprint(log["event"]),
					source,
				)
				count++
			}
		}
		if count == 0 {
			output.Row("No logs found for plugin")
		}
		return nil
	},
}

var pluginsStatusCmd = &cobra.Command{
	Use:   "status",
	Short: "Show plugin loader status (loaded plugins)",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp []map[string]any
		if err := client.Get("/api/plugins/loader/status", &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header(fmt.Sprintf("Loaded Plugins (%d)", len(resp)))
		output.Separator()
		for _, p := range resp {
			types := []string{}
			if p["isChannel"] == true {
				types = append(types, "Channel")
			}
			if p["isModelProvider"] == true {
				types = append(types, "ModelProvider")
			}
			typeStr := strings.Join(types, ", ")
			if typeStr == "" {
				typeStr = "-"
			}
			output.Row(
				fmt.Sprint(p["pluginId"]),
				fmt.Sprint(p["version"]),
				typeStr,
				fmt.Sprint(p["loadedAt"]),
			)
		}
		return nil
	},
}

var pluginsOrphansCmd = &cobra.Command{
	Use:   "orphans",
	Short: "List orphaned staging JARs",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp map[string]any
		if err := client.Get("/api/plugins/loader/orphans", &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		hasOrphans := resp["hasOrphans"]
		if hasOrphans != true {
			output.OK("No orphaned staging JARs")
			return nil
		}

		count := resp["count"]
		output.Header(fmt.Sprintf("Orphaned JARs (%v)", count))
		output.Separator()
		if jars, ok := resp["jars"].([]any); ok {
			for _, jar := range jars {
				output.Row(fmt.Sprint(jar))
			}
		}
		return nil
	},
}

var pluginsPromoteCmd = &cobra.Command{
	Use:   "promote",
	Short: "Promote all staging JARs to system/",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		if err := client.Post("/api/plugins/loader/promote", nil, nil); err != nil {
			return err
		}
		output.OK("Staging JARs promoted to system/")
		return nil
	},
}

var pluginsPublishCmd = &cobra.Command{
	Use:   "publish <pluginId>",
	Short: "Print submission guidance for publishing a plugin",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		output.Header("Plugin Publishing Guide")
		output.Separator()
		output.Row("Official plugins:", "Submit PR to github.com/FTMahringer/synapse-plugins")
		output.Row("Community plugins:", "Submit PR to github.com/FTMahringer/synapse-plugins-community")
		output.Separator()
		output.Row("Requirements:")
		output.Row("  - JAR must pass bytecode scan (no forbidden references)")
		output.Row("  - Manifest must be valid with semver version")
		output.Row("  - All hard dependencies must be resolvable")
		output.Row("  - Plugin must load without errors")
		output.Row("  - Include README.md with usage instructions")
		output.Separator()
		output.OK("Plugin ID: " + args[0])
		return nil
	},
}

func init() {
	pluginsLogsCmd.Flags().IntP("limit", "n", 50, "Number of log entries to show")

	pluginsCmd.AddCommand(
		pluginsListCmd,
		pluginsInfoCmd,
		pluginsLoadCmd,
		pluginsUnloadCmd,
		pluginsReloadCmd,
		pluginsEnableCmd,
		pluginsDisableCmd,
		pluginsUninstallCmd,
		pluginsInstallCmd,
		pluginsValidateCmd,
		pluginsResolveCmd,
		pluginsLogsCmd,
		pluginsStatusCmd,
		pluginsOrphansCmd,
		pluginsPromoteCmd,
		pluginsPublishCmd,
	)
	rootCmd.AddCommand(pluginsCmd)
}
