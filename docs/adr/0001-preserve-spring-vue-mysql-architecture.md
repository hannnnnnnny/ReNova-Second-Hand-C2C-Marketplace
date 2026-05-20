# ADR-0001: Preserve Spring Boot + Vue 3 + MySQL Architecture

## Status

Accepted

## Context

NovaCart is being upgraded from a basic ecommerce portfolio demo into a more polished multi-merchant ecommerce website builder. The requested direction is to improve UI quality, shopper flows, merchant admin operations, demo data richness, SEO, accessibility, security, performance, tests, and documentation.

The existing codebase already has a clear full-stack split:

- Vue 3 SPA for platform pages, generated storefronts, and merchant admin.
- Spring Boot REST API for public and admin commerce operations.
- MySQL runtime persistence with H2 tests.
- JWT-protected admin APIs.
- Docker and local development documentation.

## Decision Drivers

- Preserve working routes and current project architecture.
- Demonstrate production-style full-stack judgment without unnecessary stack churn.
- Keep the project easy for reviewers to run locally.
- Avoid heavy dependencies unless they materially improve the project.
- Keep demo checkout safe and avoid real payment processing.

## Considered Options

### Option 1: Preserve The Existing Architecture

Pros:
- Matches the repository goal and current docs.
- Lets improvements focus on product quality and implementation depth.
- Reduces regression risk.
- Keeps backend tests, Docker config, and API shape useful.

Cons:
- Vue SPA metadata requires runtime SEO handling rather than server-rendered page HTML.
- Browser-local generated storefront state is demo-oriented and would need server persistence for real production multi-tenancy.

### Option 2: Migrate Frontend To A Server-Rendered Framework

Pros:
- Better native SEO primitives.
- Easier static generation for marketing and storefront pages.

Cons:
- Large migration cost.
- High risk of breaking existing routes and workflows.
- Distracts from the requested portfolio upgrade.

### Option 3: Replace Backend Or Database

Pros:
- Could optimize for a different deployment target.

Cons:
- Does not solve the main product-quality gaps.
- Would remove useful Spring Boot/JPA/JWT evidence from the portfolio.

## Decision

Preserve the Spring Boot + Vue 3 + MySQL architecture and deepen it with production-style improvements: richer storefront UX, more realistic demo commerce data, stronger merchant admin flows, route metadata and structured data, admin login throttling, accessible confirmation dialogs, and better documentation.

## Consequences

- The project remains familiar and runnable for reviewers.
- SEO is improved through dynamic client-side metadata and structured data, while full SSR remains a future enhancement.
- Generated storefront cart/order/favorite/recently-viewed state remains demo-safe in browser storage.
- Backend remains the source for durable catalog/order/admin workflows.
- Future work can focus on persisted multi-tenant storefront state, staff permissions, payment-provider sandboxing, and deployment-specific SEO configuration.
