# Bundle System

Bundles are installable collections of plugins and configuration notes. They reference plugin IDs; they do not ship executable code.

## Bundle Format

Bundle YAML contains `id`, `name`, `version`, `author`, `description`, `plugins`, `tags`, `license`, `config_notes`, and optional `repository`.

## Main Agent Path

The user opens Store bundle creation or asks the Main Agent to create a bundle. The Main Agent gathers plugin selections, metadata, license, tags, and config notes, previews YAML, asks for confirmation, and opens a community pull request when Git provider access is configured.

## Manual Path

Create `bundles/<bundle-id>.yml` in the community bundles repository and open a pull request. CI validates schema, semver ranges, referenced plugin IDs, and required metadata.

## Official vs Community

Official Store content is curated and verified. Community bundles are public, PR-based, and always labeled as community content.

## Logging

Bundle create, validate, submit, install, and failure events use `STORE`.
