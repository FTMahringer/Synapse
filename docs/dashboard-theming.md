# Dashboard Theming

The dashboard supports three theming levels: user overrides, dashboard block layout, and community themes.

## Theme Levels

| Level | Storage | Scope |
|---|---|---|
| User overrides | user settings JSON | Personal colors and density |
| Block layout | user settings JSON | Dashboard widgets and order |
| Community themes | store theme package | Shared JSON and CSS theme files |

## Theme Format

```json
{
  "name": "Operator Dark",
  "colors": {
    "--color-bg": "#0F1117",
    "--color-surface": "#181C27",
    "--color-main-agent": "#7B9FE0",
    "--color-ai-firm": "#B07FE8",
    "--color-agent-team": "#E07B5A",
    "--color-echo": "#4CAF87"
  }
}
```

## Main Agent Path

The user asks to change the dashboard theme or layout. The Main Agent previews the change, requests confirmation for shared themes, applies settings, and logs the update.

## Manual Path

Edit theme JSON, import it through Dashboard settings, or install a theme from the store. Invalid CSS variables are rejected.

## Logging

Theme import, export, apply, reset, and layout update events use `SYSTEM` and `STORE`.
