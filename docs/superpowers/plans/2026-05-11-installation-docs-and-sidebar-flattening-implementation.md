# Installation Docs and Sidebar Flattening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make onboarding clearer by routing homepage Get Started to Introduction, simplifying Quick Install around installer scripts, moving full install depth into Deployment variants, and flattening sidebar structures (including Plugins overview items).

**Architecture:** Keep Getting Started concise and action-oriented, with a strong handoff to Deployment for full operational detail. Use sidebars as the single source of navigation structure, removing unnecessary wrapper groups while preserving meaningful grouped sections for deployment variants and plugin ecosystems.

**Tech Stack:** Docusaurus 3.10.1, MDX docs, TypeScript sidebar config (`sidebars.ts`), React homepage (`src/pages/index.tsx`)

---

### Task 1: Homepage CTA and Getting Started quick-install refresh

**Files:**
- Modify: `synapse-docs/src/pages/index.tsx`
- Modify: `synapse-docs/docs/getting-started/quick-start.mdx`
- Test: `synapse-docs` docs build

- [ ] **Step 1: Update homepage Get Started button to Introduction**

Change:
```tsx
to="/docs/getting-started/quick-start"
```

To:
```tsx
to="/docs/getting-started/introduction"
```

- [ ] **Step 2: Keep install CTA explicit and stable**

Verify secondary CTA remains:
```tsx
to="/docs/getting-started/installation"
```

If needed, update label to avoid implying full deployment depth:
```tsx
Install SYNAPSE
```

- [ ] **Step 3: Replace quick-start with installer-first workflow**

Rewrite `getting-started/quick-start.mdx` sections to:

```mdx
# Quick Start

Get SYNAPSE running quickly using installer scripts.

## Linux/macOS
```bash
git clone https://github.com/FTMahringer/Synapse.git
cd Synapse
chmod +x install.sh
./install.sh
```

## Windows (PowerShell)
```powershell
git clone https://github.com/FTMahringer/Synapse.git
cd Synapse
.\installer\install.ps1
```
```

- [ ] **Step 4: Add concise verification and defaults warning**

Include:
```mdx
## Verify
- Dashboard: `http://localhost:3000`
- Health: `http://localhost:8080/api/health`

Default credentials are `admin` / `admin` on first run. Change immediately after login.
```

- [ ] **Step 5: Add explicit handoff to full deployment guides**

Include:
```mdx
Need advanced setup (production hardening, bare-metal, Kubernetes, backup/monitoring)?
Go to [Full Installation Guides](../deployment/docker-compose.mdx).
```

- [ ] **Step 6: Build docs and verify landing flow**

Run:
```bash
cd synapse-docs
npm run build
```

Expected:
- build succeeds
- homepage CTA routes to introduction
- quick-start is concise and installer-first

- [ ] **Step 7: Commit Task 1**

```bash
git add synapse-docs/src/pages/index.tsx \
        synapse-docs/docs/getting-started/quick-start.mdx
git commit -m "docs(getting-started): make quick start installer-first" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 2: Restructure Getting Started installation page to lightweight gateway

**Files:**
- Modify: `synapse-docs/docs/getting-started/installation.mdx`
- Test: `synapse-docs` docs build

- [ ] **Step 1: Replace monolithic install content with gateway format**

New structure:
```mdx
# Installation

This page helps you choose the right install path.

## Recommended first step
Use [Quick Start](./quick-start.mdx) for installer-based setup.

## Full installation variants
- [Docker Compose Deployment](../deployment/docker-compose.mdx)
- [Bare-Metal Deployment](../deployment/bare-metal.mdx)
- [Kubernetes Deployment](../deployment/kubernetes.mdx)
- [Deployment Troubleshooting](../deployment/troubleshooting.mdx)
```

- [ ] **Step 2: Keep only minimal requirement summary**

Add compact prerequisites table and avoid duplicating entire deployment procedures.

- [ ] **Step 3: Add “when to use deployment docs” guidance**

Include:
```mdx
Use Deployment docs when you need production hardening, custom networking, backups, reverse proxy, or scaling.
```

- [ ] **Step 4: Build docs and verify no duplicate long install flows remain**

Run:
```bash
cd synapse-docs
npm run build
```

Expected:
- build passes
- installation page acts as entry/gateway

- [ ] **Step 5: Commit Task 2**

```bash
git add synapse-docs/docs/getting-started/installation.mdx
git commit -m "docs(install): turn getting-started installation into guide gateway" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 3: Sidebar flattening and plugin overview direct items

**Files:**
- Modify: `synapse-docs/sidebars.ts`
- Test: `synapse-docs` docs build

- [ ] **Step 1: Remove top-level wrapper category from Getting Started sidebar**

Change from:
```ts
gettingStartedSidebar: [
  {
    type: "category",
    label: "Getting Started",
    items: [...]
  }
]
```

To direct items:
```ts
gettingStartedSidebar: [
  "getting-started/introduction",
  "getting-started/quick-start",
  "getting-started/installation",
  "getting-started/first-agent",
]
```

- [ ] **Step 2: Flatten Plugins overview items**

Replace:
```ts
{
  type: "category",
  label: "Overview",
  items: ["plugins/overview", "plugins/architecture"],
}
```

With direct entries:
```ts
"plugins/overview",
"plugins/architecture",
```

- [ ] **Step 3: Preserve meaningful grouped sections**

Keep these categories intact:
- `Plugin API Reference`
- `Official Plugins`
- `Community Plugins`
- `Development`
- Deployment and Environment Variables groups where they provide structure

- [ ] **Step 4: Build docs and verify sidebar behavior**

Run:
```bash
cd synapse-docs
npm run build
```

Expected:
- build passes
- no global-wrapper style top-level group in Getting Started
- plugin overview + architecture appear as normal sidebar items

- [ ] **Step 5: Commit Task 3**

```bash
git add synapse-docs/sidebars.ts
git commit -m "docs(nav): flatten getting-started and plugin overview sidebar items" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 4: Deployment installation grouping polish and final validation

**Files:**
- Modify: `synapse-docs/docs/deployment/docker-compose.mdx` (if needed for installer-first clarity + links)
- Modify: `synapse-docs/docs/deployment/bare-metal.mdx` (if needed for consistency links)
- Modify: `synapse-docs/docs/deployment/kubernetes.mdx` (if needed for consistency links)
- Modify: `synapse-docs/docs/deployment/troubleshooting.mdx` (if needed for consistency links)
- Modify: `synapse-docs/sidebars.ts` (only if grouping labels/order need small final alignment)
- Test: `synapse-docs` build

- [ ] **Step 1: Ensure deployment pages are variant-owned and cross-linked**

Add consistent header “Related guides” block where missing:
```mdx
## Related Deployment Guides
- [Docker Compose](./docker-compose.mdx)
- [Bare-Metal](./bare-metal.mdx)
- [Kubernetes](./kubernetes.mdx)
- [Troubleshooting](./troubleshooting.mdx)
```

- [ ] **Step 2: Ensure docker-compose guide mentions installer option early**

Add near top:
```mdx
Prefer automatic setup? Use `install.sh` (Linux/macOS) or `install.ps1` (Windows), then use this page for advanced customization.
```

- [ ] **Step 3: Verify deployment sidebar grouping order is clear**

Desired order inside Deployment:
1. Docker Compose
2. Bare-Metal
3. Kubernetes
4. Environment Variables (group)
5. Reverse Proxy
6. Backup & Restore
7. Troubleshooting

- [ ] **Step 4: Final build and smoke navigation check**

Run:
```bash
cd synapse-docs
npm run build
```

Expected:
- build succeeds
- homepage button and sidebars route correctly
- installation docs no longer duplicate large procedural content in Getting Started

- [ ] **Step 5: Commit Task 4**

```bash
git add synapse-docs/docs/deployment/docker-compose.mdx \
        synapse-docs/docs/deployment/bare-metal.mdx \
        synapse-docs/docs/deployment/kubernetes.mdx \
        synapse-docs/docs/deployment/troubleshooting.mdx \
        synapse-docs/sidebars.ts
git commit -m "docs(deployment): group installation variants and improve installer guidance" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 5: Final verification and push

**Files:**
- Modify: none expected
- Test: build/log/history checks

- [ ] **Step 1: Verify commit sequence**

Run:
```bash
git -C synapse-docs --no-pager log --oneline -n 10
```

Expected: focused commits for quick-start, installation gateway, sidebar flattening, and deployment grouping polish.

- [ ] **Step 2: Verify clean tree**

Run:
```bash
git -C synapse-docs --no-pager status --short
```

Expected: no pending tracked changes.

- [ ] **Step 3: Push docs updates**

Run:
```bash
git -C synapse-docs push origin main
```

Expected: remote updated with installation/nav clarity improvements.

