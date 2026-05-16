---
name: "💡 Projektidee"
description: "Neue Projektidee für Synapse"
title: "💡 [Idee]: "
labels: ["future idea"]
body:
  - type: markdown
    attributes:
      value: "## 💡 Projektidee\n\nEine von der Nova-Researcher-Pipeline generierte Projektidee."
  - type: textarea
    id: summary
    attributes:
      label: Kurzbeschreibung
      description: "Was ist die Idee in 2-3 Sätzen?"
    validations:
      required: true
  - type: textarea
    id: learning
    attributes:
      label: Lernwert
      description: "Was kann Fynn dabei lernen? Neue Technologien, Konzepte, Architekturen?"
    validations:
      required: true
  - type: textarea
    id: usefulness
    attributes:
      label: Nutzen
      description: "Löst es ein echtes Problem? Was bringt es?"
    validations:
      required: true
  - type: textarea
    id: techstack
    attributes:
      label: Vorgeschlagener Tech-Stack
      placeholder: "Spring Boot, Vue 3, Docker, ..."
  - type: dropdown
    id: effort
    attributes:
      label: Aufwand
      description: "Grobe Einschätzung"
      options:
        - Klein (Stunden)
        - Mittel (Tage)
        - Groß (Wochen)
      default: 1
    validations:
      required: true
  - type: textarea
    id: todos
    attributes:
      label: Erste Schritte
      description: "Konkrete Todo-Liste zum Starten"
      placeholder: "- [ ] Schritt 1..."

