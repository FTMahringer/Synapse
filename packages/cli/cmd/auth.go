package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"github.com/synapse-dev/synapse-cli/internal/api"
	"github.com/synapse-dev/synapse-cli/internal/config"
	"github.com/synapse-dev/synapse-cli/internal/output"
)

var authCmd = &cobra.Command{
	Use:   "auth",
	Short: "Authentication commands",
}

var loginCmd = &cobra.Command{
	Use:   "login",
	Short: "Sign in to the SYNAPSE backend",
	RunE: func(cmd *cobra.Command, args []string) error {
		profile, _ := cmd.Flags().GetString("profile")
		username, _ := cmd.Flags().GetString("username")
		password, _ := cmd.Flags().GetString("password")

		p := config.GetProfile(profile)
		host, _ := cmd.Flags().GetString("host")
		if host != "" {
			p.Host = host
		}

		client := api.NewClient(p.Host, "")

		var resp struct {
			Token    string `json:"token"`
			Username string `json:"username"`
			Role     string `json:"role"`
		}

		if err := client.Post("/api/auth/login", map[string]string{
			"username": username,
			"password": password,
		}, &resp); err != nil {
			return fmt.Errorf("login failed: %w", err)
		}

		if err := config.SetToken(profile, resp.Token); err != nil {
			return fmt.Errorf("failed to save token: %w", err)
		}

		output.OK(fmt.Sprintf("Logged in as %s (%s) on %s", resp.Username, resp.Role, p.Host))
		return nil
	},
}

var logoutCmd = &cobra.Command{
	Use:   "logout",
	Short: "Sign out and remove stored token",
	RunE: func(cmd *cobra.Command, args []string) error {
		profile, _ := cmd.Flags().GetString("profile")
		if err := config.ClearToken(profile); err != nil {
			return err
		}
		output.OK("Logged out from profile: " + profile)
		return nil
	},
}

var sessionCmd = &cobra.Command{
	Use:   "session",
	Short: "Show current session info",
	Run: func(cmd *cobra.Command, args []string) {
		profile, _ := cmd.Flags().GetString("profile")
		p := config.GetProfile(profile)

		output.Header("Session")
		output.KV("Profile", profile)
		output.KV("Host", p.Host)
		if p.Token != "" {
			output.KV("Token", p.Token[:min(12, len(p.Token))]+"...")
		} else {
			output.KV("Token", "(not set)")
		}
		_ = viper.Get("profiles")
	},
}

func init() {
	loginCmd.Flags().StringP("username", "u", "", "Username")
	loginCmd.Flags().StringP("password", "w", "", "Password")
	loginCmd.MarkFlagRequired("username")
	loginCmd.MarkFlagRequired("password")

	authCmd.AddCommand(loginCmd, logoutCmd, sessionCmd)
	rootCmd.AddCommand(authCmd)
}

func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}
