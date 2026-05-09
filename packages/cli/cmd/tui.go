package cmd

import (
	"github.com/spf13/cobra"
	"github.com/synapse-dev/synapse-cli/internal/tui"
)

var tuiCmd = &cobra.Command{
	Use:   "tui",
	Short: "Print a live overview of the SYNAPSE platform",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		tui.PrintOverview(client)
		return nil
	},
}

func init() {
	rootCmd.AddCommand(tuiCmd)
}
