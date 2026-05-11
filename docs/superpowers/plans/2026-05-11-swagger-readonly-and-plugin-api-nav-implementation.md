# Swagger Read-Only + Plugin API Sidebar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver a clean, full-width, read-only Swagger-style API reference with example outputs, and split Plugin API reference into the Plugins sidebar using a dedicated plugin API spec.

**Architecture:** Replace current ReDoc embedding with a local Swagger UI bundle mounted through a reusable React component and a small initializer script. Keep specs in `static/openapi/` as authoritative docs contracts (`core-api.yaml` and `plugin-api.yaml`) and render two pages with shared read-only configuration. Route core API docs through `API Reference` and plugin API docs through a dedicated category inside the Plugins sidebar.

**Tech Stack:** Docusaurus 3.10.1, React 19, TypeScript, MDX, OpenAPI 3.0 YAML, swagger-ui-dist

---

### Task 1: Add local Swagger UI runtime and reusable read-only renderer

**Files:**
- Modify: `synapse-docs/package.json`
- Modify: `synapse-docs/package-lock.json`
- Create: `synapse-docs/src/components/SwaggerReadonly.tsx`
- Create: `synapse-docs/static/swagger/swagger-ui-bundle.js`
- Create: `synapse-docs/static/swagger/swagger-ui-standalone-preset.js`
- Create: `synapse-docs/static/swagger/swagger-ui.css`
- Modify: `synapse-docs/src/css/custom.css`
- Test: `synapse-docs` build

- [ ] **Step 1: Add Swagger UI dependency**

Run:
```bash
cd synapse-docs
npm install swagger-ui-dist
```

Expected: `package.json` and `package-lock.json` include `swagger-ui-dist`.

- [ ] **Step 2: Copy local Swagger UI assets to static folder**

Run:
```bash
cd synapse-docs
mkdir -p static/swagger
cp node_modules/swagger-ui-dist/swagger-ui-bundle.js static/swagger/
cp node_modules/swagger-ui-dist/swagger-ui-standalone-preset.js static/swagger/
cp node_modules/swagger-ui-dist/swagger-ui.css static/swagger/
```

Expected: static Swagger assets exist and are served by Docusaurus.

- [ ] **Step 3: Create reusable read-only Swagger renderer component**

```tsx
import React, {useEffect, useRef} from 'react';
import BrowserOnly from '@docusaurus/BrowserOnly';
import useBaseUrl from '@docusaurus/useBaseUrl';

type Props = { specPath: string };

function SwaggerRuntime({specPath}: Props): JSX.Element {
  const mountRef = useRef<HTMLDivElement | null>(null);
  const specUrl = useBaseUrl(specPath);

  useEffect(() => {
    const w = window as any;
    if (!w.SwaggerUIBundle || !mountRef.current) return;
    w.SwaggerUIBundle({
      url: specUrl,
      domNode: mountRef.current,
      deepLinking: true,
      docExpansion: 'none',
      defaultModelsExpandDepth: -1,
      supportedSubmitMethods: [],
      presets: [w.SwaggerUIBundle.presets.apis, w.SwaggerUIStandalonePreset],
      layout: 'BaseLayout',
    });
  }, [specUrl]);

  return <div className="swagger-readonly" ref={mountRef} />;
}

export default function SwaggerReadonly(props: Props): JSX.Element {
  return (
    <BrowserOnly fallback={<p>Loading API reference…</p>}>
      {() => <SwaggerRuntime {...props} />}
    </BrowserOnly>
  );
}
```

- [ ] **Step 4: Add scoped CSS for full-width API reference and theme cleanup**

```css
.api-reference-page .container {
  max-width: 100% !important;
}

.swagger-readonly .swagger-ui .topbar {
  display: none;
}

.swagger-readonly .swagger-ui .scheme-container {
  display: none;
}
```

- [ ] **Step 5: Verify failing build if assets/component wiring is broken**

Run: `cd synapse-docs && npm run build`  
Expected: If miswired, build fails with missing imports or runtime asset path issues.

- [ ] **Step 6: Fix any missing imports/paths until build succeeds**

Run: `cd synapse-docs && npm run build`  
Expected: PASS.

- [ ] **Step 7: Commit runtime foundation**

```bash
git add synapse-docs/package.json \
        synapse-docs/package-lock.json \
        synapse-docs/src/components/SwaggerReadonly.tsx \
        synapse-docs/static/swagger \
        synapse-docs/src/css/custom.css
git commit -m "docs(api): add local readonly swagger renderer" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 2: Split specs into core and plugin API, add examples, and wire core API page

**Files:**
- Create: `synapse-docs/static/openapi/core-api.yaml`
- Create: `synapse-docs/static/openapi/plugin-api.yaml`
- Modify: `synapse-docs/static/openapi/synapse-rest-api.yaml` (optional transitional copy or deprecate)
- Modify: `synapse-docs/docs/api/rest-api.mdx`
- Test: `synapse-docs` build

- [ ] **Step 1: Create `core-api.yaml` by extracting platform operational endpoints**

```yaml
openapi: 3.0.3
info:
  title: SYNAPSE Core REST API
  version: v2.4.0
paths:
  /api/health:
    get:
      responses:
        '200':
          description: Health status
          content:
            application/json:
              examples:
                healthy:
                  value:
                    systemName: SYNAPSE
                    version: v2.4.0
                    status: UP
```

- [ ] **Step 2: Add example outputs for all key core endpoint groups**

```yaml
examples:
  success:
    value:
      id: "7ad8c0bd-7166-418e-bf4b-8fb4f37f5fef"
      status: "ACTIVE"
      createdAt: "2026-05-11T19:00:00Z"
```

Required groups: health/readiness, memory, collaboration, planning, tools.

- [ ] **Step 3: Add shared error schema and reusable examples**

```yaml
components:
  schemas:
    ErrorResponse:
      type: object
      properties:
        code: { type: string }
        message: { type: string }
        timestamp: { type: string, format: date-time }
```

Include common 401/403/422/429 examples where applicable.

- [ ] **Step 4: Create `plugin-api.yaml` with plugin-specific contract scope**

```yaml
openapi: 3.0.3
info:
  title: SYNAPSE Plugin API
  version: v2.4.0
paths: {}
```

Then fill with current plugin-facing API endpoints already documented in Plugins docs (no invented runtime-only paths).

- [ ] **Step 5: Update core API MDX page to use reusable Swagger component**

```mdx
---
sidebar_position: 2
title: REST API (Core)
---

import Head from '@docusaurus/Head';
import SwaggerReadonly from '@site/src/components/SwaggerReadonly';

<Head>
  <link rel="stylesheet" href="/swagger/swagger-ui.css" />
  <script src="/swagger/swagger-ui-bundle.js"></script>
  <script src="/swagger/swagger-ui-standalone-preset.js"></script>
</Head>

<div className="api-reference-page">
  <SwaggerReadonly specPath="/openapi/core-api.yaml" />
</div>
```

- [ ] **Step 6: Run docs build and verify examples are visible in rendered operations**

Run: `cd synapse-docs && npm run build`  
Expected: PASS; core API renders read-only accordion with response examples.

- [ ] **Step 7: Commit spec split + core page integration**

```bash
git add synapse-docs/static/openapi/core-api.yaml \
        synapse-docs/static/openapi/plugin-api.yaml \
        synapse-docs/docs/api/rest-api.mdx
git commit -m "docs(api): split core and plugin openapi specs" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 3: Add Plugin API reference under Plugins sidebar

**Files:**
- Create: `synapse-docs/docs/plugins/plugin-api-reference.mdx`
- Modify: `synapse-docs/sidebars.ts`
- Test: `synapse-docs` build

- [ ] **Step 1: Create plugin API reference page using same Swagger component**

```mdx
---
title: Plugin API Reference
---

import Head from '@docusaurus/Head';
import SwaggerReadonly from '@site/src/components/SwaggerReadonly';

<Head>
  <link rel="stylesheet" href="/swagger/swagger-ui.css" />
  <script src="/swagger/swagger-ui-bundle.js"></script>
  <script src="/swagger/swagger-ui-standalone-preset.js"></script>
</Head>

<div className="api-reference-page">
  <SwaggerReadonly specPath="/openapi/plugin-api.yaml" />
</div>
```

- [ ] **Step 2: Add Plugins sidebar category for Plugin API**

```ts
{
  type: 'category',
  label: 'Plugin API Reference',
  items: ['plugins/plugin-api-reference'],
}
```

Place as peer category alongside Overview/Official/Community/Development.

- [ ] **Step 3: Ensure API sidebar remains focused on core platform API**

```ts
apiSidebar: [
  {
    type: 'category',
    label: 'API Reference',
    items: ['api/overview', 'api/rest-api', 'api/websocket-api', 'api/authentication', 'api/error-handling'],
  },
]
```

- [ ] **Step 4: Build docs and verify sidebar placement**

Run: `cd synapse-docs && npm run build`  
Expected: PASS; Plugin API page appears under Plugins sidebar, not API sidebar.

- [ ] **Step 5: Commit plugin sidebar integration**

```bash
git add synapse-docs/docs/plugins/plugin-api-reference.mdx \
        synapse-docs/sidebars.ts
git commit -m "docs(plugins): add plugin api reference sidebar section" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 4: Final verification and push strategy

**Files:**
- Modify: none expected
- Test: docs build and git history

- [ ] **Step 1: Verify build from clean working tree**

Run:
```bash
cd synapse-docs
npm run build
```

Expected: PASS.

- [ ] **Step 2: Verify read-only behavior and collapsed operations config**

Run:
```bash
npm run start
```

Manual expected checks:
- no Try it out controls
- operations collapsed by default
- example outputs visible under responses
- API pages use full width compared to default doc pages

- [ ] **Step 3: Verify commit sequence**

Run:
```bash
git --no-pager log --oneline -n 6
```

Expected: three focused commits for renderer foundation, spec split/core wiring, plugin sidebar integration.

- [ ] **Step 4: Push when approved**

Run:
```bash
git push origin main
```

Expected: remote updated with all API docs UX improvements.

