# NovaCart Audit Findings

## Skill Findings
- `planning-with-files` is active for persistent task tracking through `task_plan.md`, `findings.md`, and `progress.md`.
- `ui-ux-pro-max` design-system output recommended a premium black/gold-neutral ecommerce SaaS direction, clean typography, restrained effects, visible focus states, and semantic Vue templates.
- `ui-ux-pro-max` Vue guidance emphasized semantic elements and dynamically bound ARIA state.
- `find-skills` search for `figma design system` found:
  - `figma/mcp-server-guide@figma-create-design-system-rules` with about 1.6K installs.
  - `figma/mcp-server-guide@create-design-system-rules` with about 1.3K installs.
  - Lower-install community Figma implementation skills.
- Figma-related skill not installed yet because this task has no Figma file, design-token export, or Figma MCP source to consume. Installing it now would add little practical value.

## Repository State
- Branch: `main`.
- Remote: `origin` points to `https://github.com/hannnnnnnny/NovaCart-Fashion-Commerce-Platform.git`.
- Local branch is ahead of `origin/main` with this task's improvement commits.

## Audit Findings
- Technology stack:
  - Frontend: Vue 3, Vite, Pinia, Vue Router, Axios, lucide icons, Vitest.
  - Backend: Java 21, Spring Boot 4, Spring Security/JWT, JPA, MySQL runtime, H2 tests.
- Route structure now includes:
  - Platform pages under `/`, `/features`, `/templates`, `/pricing`, `/signup`, `/onboarding`.
  - Merchant generated storefront routes under `/store/:storeSlug`.
  - Protected merchant admin routes under `/admin/*`.
- Legacy single-store customer pages still exist under `frontend/src/pages` (`HomePage`, `ProductListPage`, `ProductDetailPage`, `CartPage`, `CheckoutPage`, `SupportPage`, `RefundRequestPage`, `OrderSuccessPage`) but public `/products`, `/cart`, and `/checkout` redirect to `/store/demo-fashion/...`.
- `frontend/index.html` metadata is now updated for the multi-merchant NovaCart SaaS positioning.
- `backend/pom.xml` description now reflects the multi-merchant ecommerce website builder direction.
- Removed accidental `backend/package-lock.json`; the backend remains Maven/Spring Boot only.
- A global catch-all/404 route now exists.
- Store/state audit:
  - `cart.js` has good normalization for the legacy backend-powered cart, including variant line keys and invalid localStorage cleanup.
  - `storefrontCart.js` now normalizes generated-store cart data loaded from localStorage.
  - `storefrontCart.js` computes totals from local product snapshots only, which is acceptable for frontend-generated demo stores but must be clearly treated as demo-safe.
  - Platform-created stores are stored in localStorage; this is intentional frontend groundwork but not backend-persistent yet.
- API audit:
  - Axios wrapper clears expired admin sessions and maps validation errors.
  - Public/store API wrappers exist, but generated storefronts still primarily use frontend data rather than backend `MerchantStore` data.
  - Admin API wrappers cover products, categories, collections, promotions, orders, refunds, support, analytics, customers, and inventory.
- Interaction audit:
  - Many buttons have real handlers or disabled states.
  - Legacy route names and support/refund redirects are restored so older customer components can resolve routes safely.
  - Template preview actions now apply template-specific preview color/image data on generated storefront routes.
- Generated storefront audit:
  - Product cards now show option summaries and correctly route shoppers to product detail when size/color selection is required.
  - Product detail supports size/color selectors and disables add/buy controls until required selections are made.
  - Generated checkout now includes postal code, delivery methods, email validation, and a submitting/loading state.
  - Generated support/refund flow now initializes refund mode from a `mode=refund` query.
  - Generated storefront favorites are implemented per store with saved-product filtering.
- Routing audit:
  - Legacy `/products`, `/cart`, `/checkout`, `/support`, and `/refund-request` route names/paths redirect into `demo-fashion` generated-store routes while preserving useful query parameters.

## 2026-05-21 Follow-Up Findings

- Native `window.confirm` dialogs remained in important merchant operations. Replacing them with a shared accessible confirmation dialog improves UX consistency, keyboard behavior, and product polish.
- Generated storefront products had solid images/options but could still feel thin. Ratings, review counts, delivery promises, merchandising labels, review highlights, and recently viewed history make the storefronts feel more like independent retail brands.
- SPA-wide metadata was mostly static. Route-level metadata and JSON-LD now cover platform pages, admin pages, generated stores, and generated product detail pages.
- AI-search context was missing. `llms.txt` and `.agents/product-marketing.md` now summarize NovaCart for AI assistants and future marketing/SEO work.
- Admin authentication did not throttle repeated failed login attempts. A lightweight in-memory throttle now blocks repeated failures without adding infrastructure dependencies.
- Architecture documentation existed, but there was no C4-style map or ADR capturing the decision to keep the Spring Boot + Vue + MySQL architecture. Both are now documented.
