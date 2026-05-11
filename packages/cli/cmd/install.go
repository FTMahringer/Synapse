package cmd

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/spf13/cobra"
	"github.com/synapse-dev/synapse-cli/internal/installer"
)

var installCmd = &cobra.Command{
	Use:   "install",
	Short: "Run the interactive SYNAPSE installation wizard",
	Long: `Run the interactive installation wizard for SYNAPSE.

The wizard will:
  1. Check prerequisites (Docker, ports)
  2. Guide you through configuration (mode, security, providers)
  3. Generate the .env file
  4. Optionally run Docker Compose

Use --non-interactive with --config-file for automated installs.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		nonInteractive, _ := cmd.Flags().GetBool("non-interactive")
		skipDocker, _ := cmd.Flags().GetBool("skip-docker")
		configFile, _ := cmd.Flags().GetString("config-file")

		// Determine root directory (where .env should go)
		rootDir, err := findRepoRoot()
		if err != nil {
			// Fallback to current working directory
			rootDir, err = os.Getwd()
			if err != nil {
				return fmt.Errorf("cannot determine working directory: %w", err)
			}
		}

		if nonInteractive {
			return runNonInteractive(rootDir, configFile, skipDocker)
		}

		return installer.RunWizard(rootDir, skipDocker)
	},
}

func runNonInteractive(rootDir, configFile string, skipDocker bool) error {
	if configFile == "" {
		return fmt.Errorf("--config-file is required in non-interactive mode")
	}

	// TODO: Parse YAML config file and run headless install
	// For now, return a placeholder
	return fmt.Errorf("non-interactive mode not yet implemented (use --config-file with a YAML file)")
}

// findRepoRoot walks up from the binary location to find the repo root
// (identified by presence of installer/compose directory).
func findRepoRoot() (string, error) {
	// Try the current working directory first
	cwd, err := os.Getwd()
	if err != nil {
		return "", err
	}

	dir := cwd
	for i := 0; i < 5; i++ { // Walk up at most 5 levels
		composePath := filepath.Join(dir, "installer", "compose")
		if info, err := os.Stat(composePath); err == nil && info.IsDir() {
			return dir, nil
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			break
		}
		dir = parent
	}

	return "", fmt.Errorf("repository root not found (no installer/compose directory)")
}

func init() {
	installCmd.Flags().BoolP("non-interactive", "n", false, "Skip interactive prompts, use config file")
	installCmd.Flags().Bool("skip-docker", false, "Generate .env only, don't run Docker Compose")
	installCmd.Flags().StringP("config-file", "c", "", "Path to YAML config for non-interactive install")
	rootCmd.AddCommand(installCmd)
}
