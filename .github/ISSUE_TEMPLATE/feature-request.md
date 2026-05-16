---
name: "✨ Feature Request"
description: "Neue Funktion oder Verbesserung vorschlagen"
title: "✨ [Feature]: "
labels: ["enhancement"]
body:
  - type: textarea
    id: problem
    attributes:
      label: Problem / Motivation
      description: "Welches Problem soll gelöst werden?"
    validations:
      required: true
  - type: textarea
    id: solution
    attributes:
      label: Vorgeschlagene Lösung
      description: "Wie sollte die Lösung aussehen?"
    validations:
      required: true
  - type: textarea
    id: alternatives
    attributes:
      label: Alternativen
      description: "Gibt es andere Ansätze?"
  - type: dropdown
    id: priority
    attributes:
      label: Priorität
      options:
        - Niedrig
        - Mittel
        - Hoch
