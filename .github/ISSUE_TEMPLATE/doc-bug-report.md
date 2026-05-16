---
name: "📖 Doc Bug Report"
description: "Fehler oder Verbesserungsvorschlag in der Dokumentation melden"
title: "📖 [Doc]: "
labels: ["documentation"]
body:
  - type: input
    id: page
    attributes:
      label: Seite / URL
      description: "Link zur betroffenen Docusaurus-Seite"
      placeholder: "https://ftmahringer.github.io/Synapse-docs/docs/..."
    validations:
      required: true
  - type: textarea
    id: what
    attributes:
      label: Was ist falsch / fehlt?
      description: "Beschreibe den Fehler oder was fehlt"
    validations:
      required: true
  - type: textarea
    id: suggestion
    attributes:
      label: Vorschlag
      description: "Wie sollte es stattdessen aussehen?"
  - type: dropdown
    id: type
    attributes:
      label: Art
      options:
        - Tippfehler / Grammatik
        - Falsche Information
        - Veraltete Information
        - Fehlende Information
        - Unklar / Schwer verständlich
        - Anderes
