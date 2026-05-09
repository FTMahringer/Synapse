package cmd

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"github.com/synapse-dev/synapse-cli/internal/config"
	"github.com/synapse-dev/synapse-cli/internal/output"
)

var configCmd = &cobra.Command{
	Use:   "config",
	Short: "Manage CLI configuration and profiles",
}

var configListCmd = &cobra.Command{
	Use:   "list",
	Short: "List all configured profiles",
	Run: func(cmd *cobra.Command, args []string) {
		profiles, ok := viper.Get("profiles").(map[string]any)
		if !ok || len(profiles) == 0 {
			output.Header("Profiles")
			fmt.Println("No profiles configured. Use 'synapse config set' to add one.")
			return
		}

		output.Header(fmt.Sprintf("Profiles (%d)", len(profiles)))
		output.Separator()
		for name, val := range profiles {
			p, _ := val.(map[string]any)
			host := ""
			if p != nil {
				host = fmt.Sprint(p["host"])
			}
			output.KV(name, host)
		}
	},
}

var configSetCmd = &cobra.Command{
	Use:   "set <profile> <host>",
	Short: "Set or update a profile",
	Args:  cobra.ExactArgs(2),
	RunE: func(cmd *cobra.Command, args []string) error {
		profile, host := args[0], args[1]
		viper.Set("profiles."+profile+".host", host)

		home, _ := os.UserHomeDir()
		dir := filepath.Join(home, ".synapse")
		if err := os.MkdirAll(dir, 0700); err != nil {
			return err
		}
		if err := viper.WriteConfigAs(filepath.Join(dir, "config.yaml")); err != nil {
			return err
		}
		output.OK(fmt.Sprintf("Profile '%s' → %s", profile, host))
		return nil
	},
}

var configShowCmd = &cobra.Command{
	Use:   "show [profile]",
	Short: "Show profile details",
	Run: func(cmd *cobra.Command, args []string) {
		name := "default"
		if len(args) > 0 {
			name = args[0]
		}
		p := config.GetProfile(name)
		output.Header("Profile: " + name)
		output.KV("Host", p.Host)
		if p.Token != "" {
			output.KV("Token", p.Token[:min(12, len(p.Token))]+"…")
		} else {
			output.KV("Token", "(not set)")
		}
	},
}

func init() {
	configCmd.AddCommand(configListCmd, configSetCmd, configShowCmd)
	rootCmd.AddCommand(configCmd)
}
