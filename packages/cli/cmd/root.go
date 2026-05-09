package cmd

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
	"github.com/synapse-dev/synapse-cli/internal/config"
)

var rootCmd = &cobra.Command{
	Use:   "synapse",
	Short: "SYNAPSE operator CLI",
	Long:  "Command-line interface for the SYNAPSE AI platform.",
}

func Execute() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}

func init() {
	cobra.OnInitialize(config.Init)

	rootCmd.PersistentFlags().StringP("profile", "p", "default", "Config profile to use")
	rootCmd.PersistentFlags().StringP("host", "H", "", "Backend host (overrides profile)")
	rootCmd.PersistentFlags().BoolP("json", "j", false, "Output raw JSON")
}
