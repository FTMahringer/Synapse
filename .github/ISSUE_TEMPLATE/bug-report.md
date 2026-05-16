---
name: "🐛 Bug Report"
description: "Erstelle einen Bug-Report"
title: "🐛 [Bug]: "
labels: ["bug"]
body:
  - type: textarea
    id: description
    attributes:
      label: Beschreibung
      description: "Was ist passiert? Was sollte passieren?"
    validations:
      required: true
  - type: textarea
    id: steps
    attributes:
      label: Schritte zum Reproduzieren
      placeholder: "1. Gehe zu ...\n2. Klicke auf ...\n3. Fehler"
    validations:
      required: true
  - type: textarea
    id: logs
    attributes:
      label: Logs / Fehlermeldungen
      render: shell
  - type: dropdown
    id: env
    attributes:
      label: Umgebung
      options:
        - Docker
        - Bare Metal
        - Proxmox LXC
        - Andere
