# OIDC/SSO Integration

Ideas for adding OIDC and SSO authentication to Synapse.

---

## OIDC Provider Framework

- **Category**: security
- **Description**: Generic OIDC provider abstraction layer. Pluggable provider architecture with standardized config schema, auto-discovery of issuer metadata, token validation, and user mapping.
- **Why useful**: Makes it easy to add new OIDC providers without changing core logic. Single framework supports PocketId, Authentik, Keycloak, Auth0, Azure AD, Google Workspace, Okta, and more.
- **Priority**: High — foundation for all OIDC integrations

---

## PocketId Integration

- **Category**: auth
- **Description**: Native PocketId OIDC integration. SSO login via PocketId, user provisioning from PocketId claims, role mapping from PocketId groups, session token exchange.
- **Why useful**: PocketId is likely the primary auth for Fynn's deployment. First-class support needed.
- **Priority**: High — first post-release priority

---

## Authentik Integration

- **Category**: auth
- **Description**: Authentik OIDC provider integration. Same as PocketId but configured for Authentik as alternative identity provider.
- **Why useful**: Authentik is popular in homelab/self-hosted community. Provides fallback/alternative to PocketId.
- **Priority**: Medium — popular community identity provider

---

## Auto-User Provisioning

- **Category**: auth
- **Description**: Automatic user creation on first OIDC login. Map OIDC claims to Synapse user attributes (name, email, role), optional approval workflow for new users, group-to-role mapping.
- **Why useful**: Users shouldn't need separate Synapse accounts when logging in via OIDC.
- **Priority**: High — required for OIDC to work smoothly

---

## OIDC Session Management

- **Category**: auth
- **Description**: OIDC token lifecycle management. Refresh token handling, session expiry, logout propagation to OIDC provider, single logout (SLO) support.
- **Why useful**: Sessions should work seamlessly with OIDC token lifecycle.
- **Priority**: Medium — operational hardening

---

## Multi-OIDC Provider Support

- **Category**: auth
- **Description**: Run multiple OIDC providers simultaneously. Provider priority/ranking, user identity resolution across providers, migration between providers.
- **Why useful**: Organizations may have multiple identity sources.
- **Priority**: Low — advanced scenario

---

## MFA/2FA Integration

- **Category**: auth
- **Description**: OIDC-based MFA support. TOTP, WebAuthn, SMS via OIDC provider, conditional MFA policies based on user/group/risk.
- **Why useful**: OIDC providers often handle MFA — Synapse should pass through or enforce MFA requirements.
- **Priority**: Low — future enhancement