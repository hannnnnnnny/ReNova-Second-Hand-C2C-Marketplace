# NovaCart C4 Context And Containers

## System Context

NovaCart is a multi-merchant ecommerce website builder and operations demo. It serves three main audiences:

- Shoppers browsing generated storefronts, product detail pages, carts, checkout, order success, support, and refund flows.
- Merchants managing setup, products, categories, collections, promotions, orders, inventory, analytics, customers, support, refunds, templates, and theme settings.
- Portfolio reviewers evaluating full-stack architecture, product depth, UX quality, testing, and documentation.

```mermaid
flowchart LR
  Shopper["Shopper<br/>Browses merchant storefronts"]
  Merchant["Merchant operator<br/>Runs catalog, orders, care, and setup"]
  Reviewer["Portfolio reviewer<br/>Reviews architecture, UX, and tests"]
  NovaCart["NovaCart<br/>Multi-merchant ecommerce builder"]
  BrowserStorage["Browser local storage<br/>Demo cart, favorites, viewed products, orders"]
  MySQL["MySQL<br/>Runtime commerce persistence"]
  Shopper -->|"Shops generated storefronts"| NovaCart
  Merchant -->|"Manages operations"| NovaCart
  Reviewer -->|"Evaluates project quality"| NovaCart
  NovaCart -->|"Reads/writes demo storefront state"| BrowserStorage
  NovaCart -->|"Persists backend commerce data"| MySQL
```

## Container View

```mermaid
flowchart LR
  Shopper["Shopper"]
  Merchant["Merchant operator"]
  Frontend["Vue 3 SPA<br/>Vite, Vue Router, Pinia, Axios, CSS"]
  Api["Spring Boot API<br/>Java 21, Spring Security, JPA"]
  Db["MySQL<br/>Runtime persistence"]
  TestDb["H2<br/>Automated tests"]
  LocalState["Browser local storage<br/>Generated storefront demo state"]
  Shopper -->|"Browses and checks out"| Frontend
  Merchant -->|"Runs operations"| Frontend
  Frontend -->|"REST JSON via Axios"| Api
  Frontend -->|"Cart, favorites, viewed, local orders"| LocalState
  Api -->|"JPA repositories"| Db
  Api -->|"Test profile"| TestDb
```

## Key Runtime Flows

```mermaid
flowchart LR
  Shopper["Shopper"] --> Storefront["Generated Storefront"]
  Storefront --> Product["Product Detail"]
  Product --> Cart["Storefront Cart"]
  Cart --> Checkout["Demo Checkout"]
  Checkout --> Success["Order Success"]
  Success --> Support["Support / Refund Request"]
```

```mermaid
flowchart LR
  Merchant["Merchant Login"] --> Dashboard["Admin Dashboard"]
  Dashboard --> Catalog["Products / Categories / Collections"]
  Dashboard --> Orders["Orders / Inventory"]
  Dashboard --> Care["Support / Refunds"]
  Dashboard --> Growth["Promotions / Analytics"]
  Dashboard --> StoreBuilder["Templates / Theme Editor / Store Setup"]
```

## Component Groups

- `frontend/src/pages/platform`: public SaaS pages for positioning, templates, pricing, signup, and onboarding.
- `frontend/src/pages/store`: generated merchant storefront browsing, product detail, cart, checkout, order success, support, and refund UX.
- `frontend/src/pages/admin`: protected merchant operations workspace.
- `frontend/src/stores`: Pinia state for auth, platform/generated stores, legacy cart, generated storefront carts, favorites, and recently viewed products.
- `frontend/src/api`: Axios API layer with admin, catalog, order, and platform API modules.
- `backend/src/main/java/com/novacart/store/controller`: REST entrypoints for public commerce, care, auth, and admin operations.
- `backend/src/main/java/com/novacart/store/service/impl`: business logic for authentication, checkout/order consistency, inventory movements, promotions, analytics, support, refunds, customers, categories, and collections.
- `backend/src/main/java/com/novacart/store/entity`: JPA domain model for the commerce platform.
- `backend/src/test`: backend controller/service regression coverage with H2.
