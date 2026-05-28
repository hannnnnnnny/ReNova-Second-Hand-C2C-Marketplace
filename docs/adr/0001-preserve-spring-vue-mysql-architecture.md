# ADR-0001: Preserve Spring Boot + Vue 3 + MySQL Architecture

## Status

Superseded by the ReNova marketplace rebuild.

## Context

The project originally explored a NovaCart ecommerce/storefront-builder direction. The active codebase has since been rebuilt as ReNova, a peer-to-peer second-hand marketplace. The architecture decision that still matters is the stack choice: Vue 3 frontend, Spring Boot backend, and MySQL runtime persistence.

## Decision

Keep the Spring Boot + Vue 3 + MySQL foundation while aligning product scope, API documentation, and tests to the ReNova marketplace domain.

## Consequences

- Existing Java package names and local database defaults may still contain `novacart` for compatibility.
- Product documentation must describe ReNova marketplace routes and workflows, not removed storefront-builder APIs.
- Future website-builder work should be introduced as a new backend-persisted feature, not as frontend-only mock data.
