package tui

import (
	"fmt"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
	"github.com/synapse-dev/synapse-cli/internal/api"
)

type healthMsg struct {
	data map[string]any
	err  error
}

type agentsMsg struct {
	data []map[string]any
	err  error
}

type model struct {
	client  *api.Client
	health  map[string]any
	agents  []map[string]any
	loading bool
	err     string
	quitting bool
}

var (
	titleStyle  = lipgloss.NewStyle().Bold(true).Foreground(lipgloss.Color("86")).Padding(0, 1)
	boxStyle    = lipgloss.NewStyle().Border(lipgloss.RoundedBorder()).BorderForeground(lipgloss.Color("240")).Padding(0, 1)
	labelStyle  = lipgloss.NewStyle().Foreground(lipgloss.Color("240"))
	valueStyle  = lipgloss.NewStyle().Foreground(lipgloss.Color("252"))
	errorStyle  = lipgloss.NewStyle().Foreground(lipgloss.Color("196"))
)

func NewOverviewModel(client *api.Client) model {
	return model{client: client, loading: true}
}

func (m model) Init() tea.Cmd {
	return tea.Batch(fetchHealth(m.client), fetchAgents(m.client))
}

func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.KeyMsg:
		switch msg.String() {
		case "q", "ctrl+c", "esc":
			m.quitting = true
			return m, tea.Quit
		case "r":
			m.loading = true
			return m, tea.Batch(fetchHealth(m.client), fetchAgents(m.client))
		}
	case healthMsg:
		if msg.err != nil {
			m.err = msg.err.Error()
		} else {
			m.health = msg.data
		}
		m.loading = false
	case agentsMsg:
		if msg.err == nil {
			m.agents = msg.data
		}
	}
	return m, nil
}

func (m model) View() string {
	if m.quitting {
		return ""
	}

	out := titleStyle.Render("SYNAPSE") + "  Operator TUI\n\n"

	if m.loading {
		return out + "  Loading…\n"
	}

	if m.err != "" {
		return out + errorStyle.Render("  ✗ "+m.err) + "\n"
	}

	// Health panel
	healthContent := ""
	if m.health != nil {
		healthContent = fmt.Sprintf("%s  %s\n%s  %s\n%s  %s",
			labelStyle.Render("Status "), valueStyle.Render(fmt.Sprint(m.health["status"])),
			labelStyle.Render("Version"), valueStyle.Render(fmt.Sprint(m.health["version"])),
			labelStyle.Render("ECHO   "), valueStyle.Render(fmt.Sprint(m.health["echoActivation"])),
		)
	}

	// Agents panel
	agentContent := ""
	for _, a := range m.agents {
		agentContent += fmt.Sprintf("  %s  %s\n",
			labelStyle.Render(fmt.Sprint(a["id"])),
			valueStyle.Render(fmt.Sprint(a["name"])),
		)
	}
	if agentContent == "" {
		agentContent = labelStyle.Render("  No agents loaded")
	}

	out += lipgloss.JoinHorizontal(lipgloss.Top,
		boxStyle.Width(30).Render("Health\n\n"+healthContent),
		"  ",
		boxStyle.Width(36).Render(fmt.Sprintf("Agents (%d)\n\n", len(m.agents))+agentContent),
	)

	out += "\n\n" + labelStyle.Render("  r: refresh  q: quit")
	return out
}

func fetchHealth(client *api.Client) tea.Cmd {
	return func() tea.Msg {
		var data map[string]any
		err := client.Get("/api/health", &data)
		return healthMsg{data: data, err: err}
	}
}

func fetchAgents(client *api.Client) tea.Cmd {
	return func() tea.Msg {
		var data []map[string]any
		err := client.Get("/api/agents", &data)
		return agentsMsg{data: data, err: err}
	}
}
