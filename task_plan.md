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
| 3. Code quality and performance | Complete | Remove dead code, reduce duplication, optimize images/lazy loading, improve component semantics and maintainability. | Removed the browser-prompt bulk discount path and replaced it with validated inline admin controls; frontend build/tests pass. |
| 4. Accessibility and SEO | Complete | Add/verify labels, aria state, keyboard focus, semantic landmarks, meta title/description/Open Graph/social metadata. | Product favorites use ARIA pressed state, generated checkout inputs have labels/autocomplete, images use alt/lazy/async decoding, and metadata describes NovaCart as a multi-merchant platform. |
| 5. Final verification and delivery | In Progress | Run backend/frontend tests/builds, route smoke checks, update docs/plan, commit and push. | Backend tests, frontend install/build/unit tests, Docker config, and route smoke checks pass; final commit and push remain. |

## 2026-05-21 Follow-Up Upgrade Plan

| Phase | Status | Scope | Exit Criteria |
|---|---|---|---|
| 6. Admin UX hardening | Complete | Replace native destructive-action confirmations with reusable accessible merchant-operation dialogs. | Frontend unit tests/build pass; commit created. |
| 7. Storefront product richness | Complete | Add product proof signals, merchandising labels, review notes, delivery promises, and recently viewed browsing history. | Frontend unit tests/build pass; commit created. |
| 8. SEO and AI-search readiness | Complete | Add route metadata, canonical handling, structured data, robots/sitemap, llms.txt, and product-marketing context. | Frontend unit tests/build pass; commit created. |
| 9. Backend auth security | Complete | Add failed-login throttling and regression coverage. | Backend Maven tests pass; commit created. |
| 10. Architecture documentation | Complete | Add C4-style context/container docs and ADR for preserving the architecture. | Docs committed and final verification rerun. |
| 11. Final verification | Complete | Rerun frontend tests/build, backend tests, and local route smoke checks. | Unit/build/backend checks pass; route smoke passes; browser-render tooling limitation documented. |

## 2026-05-21 Customer Shopping Rescue Plan

| Phase | Status | Scope | Exit Criteria |
|---|---|---|---|
| 12. Generated storefront rescue | Complete | Replace weak local-store placeholder copy, dedupe category/navigation labels, reduce heavy serif typography, and restructure the hero/service/category/product rhythm. | Frontend unit tests/build pass; clean screenshots verified on `127.0.0.1:5174`. |
| 13. Cart and demo checkout realism | Complete | Add promo codes, correct discount totals, dynamic shipping estimates, demo-safe payment method selection, card validation, billing-address handling, and safer order persistence. | Unit tests cover promotions and payment/tracking utilities; frontend build passes. |
| 14. Customer logistics | Complete | Add order history and order tracking routes with carrier, tracking number, estimated delivery, address, payment, fulfillment timeline, and reorder/support actions. | Tracking page screenshot verified with realistic local demo order data. |

## Errors Encountered

| Time | Error | Attempt | Resolution |
|---|---|---|---|
| 2026-05-20 | `python` resolved to Windows Store alias when running ui-ux-pro-max helper. | Ran `python ...search.py`. | Used Codex bundled Python at `C:\Users\harry\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`. |
| 2026-05-21 | Existing `localhost:5173` server was serving an older Vite bundle during screenshot checks. | Captured a screenshot from `localhost:5173`. | Started a clean Vite dev server on `127.0.0.1:5174` and verified the current code there. |
| 2026-05-21 | `require('playwright')` was not available from project `node_modules`; only the CLI was available through `npx`. | Tried a Node-based Playwright flow script. | Used `npx playwright screenshot` plus unit/build coverage and storage-state screenshots for visual QA without adding a heavy dependency. |

## Commit Log For This Task
- `33c3784 chore: add project improvement roadmap`
- `8582a6d feat: improve generated storefront shopping reliability`
- `7e1225b feat: polish merchant admin interactions`
- `bcaf0be refactor: improve admin bulk discount controls`
- `604defe chore: record final verification status`
- `584b12b docs: update README for multi-merchant platform release`
- `c0d9d8c docs: refresh platform hero preview`
- `19b1800 feat: add accessible admin confirmations`
- `a5bdfee feat: enrich storefront product discovery`
- `ee69b38 feat: add route SEO and AI search context`
- `ae92cd2 feat: throttle repeated admin login failures`
- `2e2ba1e docs: add architecture decision and C4 map`
- `0e1bfd2 feat: polish generated storefront experience`
- Pending cart, checkout, and tracking commit.
