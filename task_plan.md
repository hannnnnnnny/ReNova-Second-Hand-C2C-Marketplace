# NovaCart Full-Project Improvement Plan

## Goal
Upgrade NovaCart into a polished, reliable, premium multi-merchant ecommerce website builder while preserving existing platform, storefront, admin, cart, checkout, support, refund, and backend API functionality.

## Active Design Direction
- Product type: multi-merchant ecommerce SaaS with generated premium storefronts and merchant dashboards.
- UI/UX skill guidance: restrained premium ecommerce palette, strong semantic structure, professional spacing, visible focus states, responsive layouts, and no decorative filler.
- Figma skill decision: searched for Figma-related skills. Official Figma MCP design-system skills exist, but no Figma file or MCP design source is attached, so they are not useful for this repo pass yet.

## Current Problems Found Before Coding
- `frontend/index.html` still uses the old `NovaCart Fashion Commerce Platform` title and lacks description, Open Graph, Twitter, theme-color, and canonical-style metadata for the multi-merchant SaaS product.
- Legacy customer components still reference named routes `products`, `support`, and `refund-request`, but those names are not registered in the current router. Legacy path redirects also do not preserve query parameters.
- `/support` and `/refund-request` links exist in legacy components, but those paths are not defined after the multi-store route repositioning.
- No catch-all 404 route exists.
- Generated storefront checkout lacks postal code and delivery method fields, uses weak email validation, and has no loading/submitting state.
- Generated storefront support/refund page does not initialize refund mode from a `mode=refund` query.
- Generated storefront cart localStorage is not normalized on load, so stale/corrupt local data can leak bad quantities/prices/options into the UI.
- Wishlist/favorites are not implemented for generated storefronts.
- Product images do not consistently opt into lazy/async decoding.
- `backend/pom.xml` still describes the backend as a fashion commerce platform rather than a multi-merchant builder.
- `backend/package-lock.json` appears accidental because the backend is Maven-only and has no package manifest.
- Admin/topbar date and channel filters are presentational only; if kept, they should be communicated as dashboard context controls or later wired to analytics.

## Phases

| Phase | Status | Scope | Exit Criteria |
|---|---|---|---|
| 0. Planning and audit | Complete | Create planning files, inspect repository structure, route map, UI flows, backend APIs, tests, docs, and current problems. | `task_plan.md`, `findings.md`, and `progress.md` exist; concrete issues listed before coding. |
| 1. Storefront UX and ecommerce reliability | Complete | Improve product browsing, cart/checkout validation, product-card accessibility, image loading, wishlist/favorites if scoped safely, and mobile storefront polish. | Storefront routes render cleanly; all visible controls have behavior/disabled states; build/tests pass. |
| 2. Platform/admin polish | Complete | Improve SaaS homepage, auth/onboarding, admin dashboard hierarchy, tables, filters, empty states, and settings/theme-editor clarity. | Admin care notifications, setup/theme save feedback, publish guard, and template-preview routing are implemented; frontend build/tests pass. |
| 3. Code quality and performance | In Progress | Remove dead code, reduce duplication, optimize images/lazy loading, improve component semantics and maintainability. | No obvious unused code in touched areas; no build warnings introduced. |
| 4. Accessibility and SEO | Pending | Add/verify labels, aria state, keyboard focus, semantic landmarks, meta title/description/Open Graph/social metadata. | Focus states visible; metadata describes NovaCart platform; checks pass. |
| 5. Final verification and delivery | Pending | Run backend/frontend tests/builds, route smoke checks, update docs/plan, commit and push. | Working tree clean, commits pushed, final report includes commands and remaining limitations. |

## Errors Encountered

| Time | Error | Attempt | Resolution |
|---|---|---|---|
| 2026-05-20 | `python` resolved to Windows Store alias when running ui-ux-pro-max helper. | Ran `python ...search.py`. | Used Codex bundled Python at `C:\Users\harry\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`. |

## Commit Log For This Task
- `33c3784 chore: add project improvement roadmap`
- `8582a6d feat: improve generated storefront shopping reliability`
- Pending implementation commit for Phase 2 platform/admin polish.
