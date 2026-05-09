package config

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/spf13/viper"
)

type Profile struct {
	Host  string `mapstructure:"host"`
	Token string `mapstructure:"token"`
}

// Init loads config from ~/.synapse/config.yaml
func Init() {
	home, err := os.UserHomeDir()
	if err != nil {
		return
	}

	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(filepath.Join(home, ".synapse"))
	viper.SetEnvPrefix("SYNAPSE")
	viper.AutomaticEnv()

	// Defaults
	viper.SetDefault("profiles.default.host", "http://localhost:8080")

	if err := viper.ReadInConfig(); err != nil {
		// Config file is optional
		if _, ok := err.(viper.ConfigFileNotFoundError); !ok {
			fmt.Fprintln(os.Stderr, "warning: config error:", err)
		}
	}
}

func GetProfile(name string) Profile {
	var p Profile
	key := "profiles." + name
	if err := viper.UnmarshalKey(key, &p); err != nil || p.Host == "" {
		p.Host = viper.GetString("profiles.default.host")
	}
	return p
}

func SetToken(profile, token string) error {
	home, err := os.UserHomeDir()
	if err != nil {
		return err
	}

	dir := filepath.Join(home, ".synapse")
	if err := os.MkdirAll(dir, 0700); err != nil {
		return err
	}

	viper.Set("profiles."+profile+".token", token)
	return viper.WriteConfigAs(filepath.Join(dir, "config.yaml"))
}

func ClearToken(profile string) error {
	viper.Set("profiles."+profile+".token", "")
	home, _ := os.UserHomeDir()
	return viper.WriteConfigAs(filepath.Join(home, ".synapse", "config.yaml"))
}
