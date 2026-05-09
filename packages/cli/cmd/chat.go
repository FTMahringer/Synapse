package cmd

import (
	"bufio"
	"fmt"
	"os"
	"strings"

	"github.com/spf13/cobra"
	"github.com/synapse-dev/synapse-cli/internal/output"
)

var chatCmd = &cobra.Command{
	Use:   "chat",
	Short: "Conversation commands",
}

var chatListCmd = &cobra.Command{
	Use:   "list",
	Short: "List conversations",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp []map[string]any
		if err := client.Get("/api/conversations", &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header(fmt.Sprintf("Conversations (%d)", len(resp)))
		output.Separator()
		for _, c := range resp {
			id := fmt.Sprint(c["id"])
			if len(id) > 8 {
				id = id[:8]
			}
			output.Row(id+"…", fmt.Sprint(c["agentId"]), fmt.Sprint(c["status"]))
		}
		return nil
	},
}

var chatNewCmd = &cobra.Command{
	Use:   "new",
	Short: "Create a new conversation",
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		agentID, _ := cmd.Flags().GetString("agent")

		var resp map[string]any
		if err := client.Post("/api/conversations", map[string]string{"agentId": agentID}, &resp); err != nil {
			return err
		}

		conv, _ := resp["conversation"].(map[string]any)
		if conv == nil {
			conv = resp
		}
		output.OK(fmt.Sprintf("Conversation created: %s", conv["id"]))
		return nil
	},
}

var chatSendCmd = &cobra.Command{
	Use:   "send <conversationId> <message>",
	Short: "Send a message to a conversation",
	Args:  cobra.MinimumNArgs(2),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		convID := args[0]
		message := strings.Join(args[1:], " ")

		var resp map[string]any
		if err := client.Post("/api/conversations/"+convID+"/messages",
			map[string]string{"content": message}, &resp); err != nil {
			return err
		}

		output.Header("Response")
		output.Separator()

		// Show assistant reply if returned
		if reply, ok := resp["assistantMessage"].(map[string]any); ok {
			fmt.Println(reply["content"])
		} else {
			output.JSON(resp)
		}
		return nil
	},
}

var chatInteractiveCmd = &cobra.Command{
	Use:   "interactive <conversationId>",
	Short: "Interactive REPL with a conversation",
	Aliases: []string{"repl"},
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		convID := args[0]

		output.Header(fmt.Sprintf("Chat — conversation %s (type 'exit' to quit)", convID[:min(8, len(convID))]))

		scanner := bufio.NewScanner(os.Stdin)
		for {
			fmt.Print("\n> ")
			if !scanner.Scan() {
				break
			}
			text := strings.TrimSpace(scanner.Text())
			if text == "" {
				continue
			}
			if text == "exit" || text == "quit" {
				break
			}

			var resp map[string]any
			if err := client.Post("/api/conversations/"+convID+"/messages",
				map[string]string{"content": text}, &resp); err != nil {
				output.Error(err.Error())
				continue
			}

			if reply, ok := resp["assistantMessage"].(map[string]any); ok {
				fmt.Println("\n" + fmt.Sprint(reply["content"]))
			} else {
				output.JSON(resp)
			}
		}

		fmt.Println("\nSession ended.")
		return nil
	},
}

var chatMessagesCmd = &cobra.Command{
	Use:   "messages <conversationId>",
	Short: "Show messages in a conversation",
	Args:  cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		client := clientFromCmd(cmd)
		rawJSON, _ := cmd.Flags().GetBool("json")

		var resp []map[string]any
		if err := client.Get("/api/conversations/"+args[0]+"/messages", &resp); err != nil {
			return err
		}

		if rawJSON {
			output.JSON(resp)
			return nil
		}

		output.Header(fmt.Sprintf("Messages (%d)", len(resp)))
		output.Separator()
		for _, m := range resp {
			role := fmt.Sprint(m["role"])
			content := fmt.Sprint(m["content"])
			if len(content) > 120 {
				content = content[:120] + "…"
			}
			fmt.Printf("[%s] %s\n", role, content)
		}
		return nil
	},
}

func init() {
	chatNewCmd.Flags().StringP("agent", "a", "main-agent", "Agent ID to use")
	chatCmd.AddCommand(chatListCmd, chatNewCmd, chatSendCmd, chatInteractiveCmd, chatMessagesCmd)
	rootCmd.AddCommand(chatCmd)
}
