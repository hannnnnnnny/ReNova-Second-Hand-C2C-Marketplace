# NovaCart Multi-Merchant Ecommerce Website Builder Platform

NovaCart is a production-style portfolio SaaS ecommerce platform where many merchants can create, customize, manage, and publish their own online stores. It includes a platform marketing website, merchant onboarding flow, template selector, store builder controls, protected merchant admin workspace, and generated customer storefront routes.

NovaCart is original in naming, layout, copy, seed data, and local artwork. It is a demo-safe project, not a live payment-processing store or a clone of any existing ecommerce platform.

## Live Preview

Temporary free preview:

[https://honest-lions-being-appreciate.trycloudflare.com](https://honest-lions-being-appreciate.trycloudflare.com)

This URL is served through a free Cloudflare Quick Tunnel from the local Vite app. It is useful for quickly opening and testing the project, but it only stays online while the local dev server and tunnel process are running on this machine. For a permanent portfolio link, deploy the `frontend` folder to Vercel, Netlify, or Cloudflare Pages using the deployment settings below.

Recommended demo paths:

- Platform home: `/`
- Template gallery: `/templates`
- Page preview gallery: `/templates/demo-fashion`
- Fashion storefront: `/store/demo-fashion`
- Store catalog: `/store/demo-fashion/products`
- Demo checkout: `/store/demo-fashion/checkout`
- Customer orders: `/store/demo-fashion/orders`
- Merchant admin: `/admin/dashboard`

## Platform Direction

NovaCart is no longer positioned as one fashion store. It is a website builder and commerce operations platform for independent merchants.

- Public platform site: `/`, `/features`, `/templates`, `/templates/:templateId`, `/pricing`
- Merchant access: `/login`, `/signup`, `/onboarding`
- Merchant workspace: `/admin/dashboard`, `/admin/store-setup`, `/admin/products`, `/admin/orders`, `/admin/customers`, `/admin/inventory`, `/admin/promotions`, `/admin/analytics`, `/admin/templates`, `/admin/theme-editor`, `/admin/support`, `/admin/refunds`, `/admin/settings`
- Generated storefronts: `/store/demo-fashion`, `/store/demo-sports`, `/store/demo-home`, `/store/demo-boutique`
- Store-specific shopping paths: `/store/:storeSlug/products`, `/store/:storeSlug/products/:productId`, `/store/:storeSlug/cart`, `/store/:storeSlug/checkout`, `/store/:storeSlug/order-success`, `/store/:storeSlug/orders`, `/store/:storeSlug/orders/:orderId`, `/store/:storeSlug/support`

## Visual Preview

These images are regenerated from the current running Vue app and use cache-busting filenames so the GitHub preview reflects the latest UI.

### Platform Builder Home

![NovaCart platform builder home](docs/assets/screenshots/platform-home-current.png)

### Generated Merchant Storefront

![NovaCart generated merchant storefront](docs/assets/screenshots/generated-storefront-current.png)

### Store Catalog

![NovaCart generated storefront catalog](docs/assets/screenshots/generated-catalog-current.png)

### Variant-Aware Product Detail

![NovaCart generated product detail](docs/assets/screenshots/generated-product-detail-current.png)

### Cart, Checkout, And Care

![NovaCart generated cart](docs/assets/screenshots/generated-cart-current.png)

![NovaCart generated checkout](docs/assets/screenshots/generated-checkout-current.png)

![NovaCart generated order success](docs/assets/screenshots/generated-order-success-current.png)

![NovaCart generated support and refund flow](docs/assets/screenshots/generated-support-refund-current.png)

### Merchant Operations Dashboard

![NovaCart merchant operations dashboard](docs/assets/screenshots/admin-dashboard-current.png)

## What NovaCart Is

NovaCart is a multi-merchant ecommerce website builder. Merchants can choose a template, define store basics, add products, configure brand settings, preview the generated storefront, and operate commerce workflows from a dashboard.

The project demonstrates real ecommerce architecture: a Spring Boot REST API, MySQL persistence, JPA entities, DTO validation, service-layer business logic, JWT-protected admin APIs, transactional checkout, stock movement tracking, Vue 3 platform and storefront experiences, and a merchant admin workspace.

## Current Product Scope

NovaCart is designed around three connected experiences:

- Platform experience: landing pages, feature pages, template showcase, pricing/demo plans, login, signup, and onboarding.
- Merchant experience: protected admin workspace for store setup, catalog, orders, customers, inventory, promotions, templates, theme editing, support, refunds, analytics, and settings.
- Shopper experience: generated storefronts under `/store/:storeSlug` with merchant-specific branding, product discovery, cart, checkout, order success, support, and refund request flows.

## Multi-Merchant Store Builder

- Merchant onboarding creates a store name, slug, category, description, template, first products, brand color, logo text, currency, and shipping message.
- Template options include Fashion Boutique, Thrift Classic, Sports Gear, Home Living, and Minimal Modern.
- Store switcher lets the admin workspace move between demo and merchant-created stores.
- Theme editor updates logo text, brand color, hero copy, announcement text, font style, and button style through a frontend store data layer.
- Generated storefronts use store-specific names, colors, template styling, categories, products, cart, and local demo checkout.
- Backend groundwork includes `MerchantAccount` and `MerchantStore` entities, repositories, seed demo stores, and public store lookup endpoints.

## Generated Customer Storefronts

- Storefronts live under `/store/:storeSlug`, which keeps the platform homepage separate from merchant stores.
- Each storefront displays the merchant logo/name, selected template style, announcement bar, hero section, category navigation, featured products, product grid, variant-aware product detail pages, cart, checkout, order success, and support/refund request flow.
- Demo stores are included for fashion, sports, home goods, and a minimal boutique so visitors can see multiple merchant storefronts.
- Generated storefront imagery uses original local demo JPEG assets under `frontend/public/demo-images` for realistic ecommerce photography-style previews.
- Shoppers can save products per store, filter to saved products, select size/color variants, edit cart quantities, apply demo promotions, choose delivery methods, select demo-safe payment methods, enter postal details, view order success, track delivery status, and submit support or refund requests.
- Generated storefront checkout is demo-safe and local for mock stores. It validates realistic payment fields but never processes real card, wallet, PayPal, BNPL, cash, or bank-transfer payments. The existing backend checkout remains available for backend-powered commerce flows.
- Legacy single-store paths such as `/products`, `/cart`, `/checkout`, `/support`, and `/refund-request` redirect into the `demo-fashion` generated storefront so older links do not break.

## Merchant Admin

- JWT-protected admin login with persisted session handling and clear expired-session behavior.
- Dashboard with current store context, store setup checklist, revenue, order, visitor, conversion, average order value, refund, support, stock, sales trend, top products, and quick actions.
- Store setup page for details, slug, description, shipping message, preview, and publish state.
- Templates page and theme editor for store builder workflows.
- Product management with thumbnail table, search, status/category/collection/sale filters, active/draft/archive status, stock badges, featured markers, preview links, edit links, archive/reactivate, bulk archive, bulk collection assignment, and inline selected-product markdown creation.
- Collection management for Spring Edit, Summer Essentials, Workwear Capsule, Evening Details, Active Weekend, and End of Season Sale with campaign images and product assignment.
- Promotion management for percentage or fixed discounts targeted by selected products, categories, collections, seasons, or tags.
- Order management with status tabs, search, region filtering, payment state, refund state, fulfillment state, order detail, and safe status transitions.
- Refund and support queues with status summaries, topbar notification counts, merchant-only notes, and workflow updates.
- Customer records and analytics based on guest checkout email.
- Inventory workspace with low-stock thresholds, manual adjustments with reasons, warning cards, and stock movement history.

## Core Workflows

```mermaid
flowchart LR
  Home["Storefront Home"] --> Catalog["Store Catalog"]
  Catalog --> Detail["Product Detail"]
  Detail --> Cart["Cart"]
  Detail --> Checkout["Buy Now"]
  Cart --> Checkout
  Checkout --> Success["Order Success"]
  Success --> Support["Support Ticket"]
  Success --> Refund["Refund Request"]
```

```mermaid
flowchart LR
  Login["Admin Login"] --> Dashboard["Dashboard"]
  Dashboard --> Products["Products"]
  Dashboard --> Collections["Collections"]
  Dashboard --> Promotions["Promotions"]
  Products --> Catalog["Customer Catalog"]
  Promotions --> Cart["Discounted Cart"]
```

```mermaid
flowchart LR
  Order["Customer Order"] --> Orders["Admin Orders"]
  Order --> Refund["Refund Queue"]
  Order --> Support["Support Queue"]
  Orders --> Inventory["Stock Movements"]
  Refund --> Analytics["Care Metrics"]
  Support --> Analytics
```

## Tech Stack

- Backend: Java 21, Spring Boot 4, Maven
- Frontend: Vue 3, Vite, Vue Router, Pinia, Axios
- Database: MySQL for runtime, H2 for tests
- ORM: Spring Data JPA and Hibernate
- Authentication: JWT with BCrypt password hashing
- Styling: Custom responsive CSS
- API style: RESTful JSON
- Deployment: Dockerfiles and Docker Compose

## Project Structure

```text
NovaCart-Fashion-Commerce-Platform/
  backend/
    src/main/java/com/novacart/store/
      config/ controller/ dto/ entity/ exception/
      repository/ security/ service/ service/impl/
    src/main/resources/
    src/test/
  frontend/
    src/
      api/ assets/ components/ layouts/ pages/
      router/ stores/ utils/
  docs/
    API.md
    ARCHITECTURE.md
    DEPLOYMENT.md
    DEVELOPMENT.md
    PRODUCT_REQUIREMENTS.md
```

## Quick Start

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Default backend URL:

```text
http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Default frontend URL:

```text
http://localhost:5173
```

### Default Admin Account

```text
Email: admin@novacart.local
Password: NovaCartAdmin123!
```

Use this account only for local development.

## Environment Variables

Backend variables can be copied from [backend/.env.example](backend/.env.example).

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=novacart
DB_USERNAME=novacart_user
DB_PASSWORD=novacart_password
JWT_SECRET=replace-with-a-long-random-secret
JWT_EXPIRATION_MINUTES=120
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

Frontend variables can be copied from [frontend/.env.example](frontend/.env.example).

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## MySQL Setup

```sql
CREATE DATABASE novacart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'novacart_user'@'localhost' IDENTIFIED BY 'novacart_password';
GRANT ALL PRIVILEGES ON novacart.* TO 'novacart_user'@'localhost';
FLUSH PRIVILEGES;
```

## Docker Setup

```bash
docker compose up --build
```

Services:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- MySQL: `localhost:3306`

## Deploy The Frontend

The Vue frontend can be deployed for free on common static hosts. The repository includes frontend deployment config for:

- Vercel: [frontend/vercel.json](frontend/vercel.json)
- Netlify: [frontend/netlify.toml](frontend/netlify.toml)

Build settings:

```text
Root directory: frontend
Build command: npm run build
Output directory: dist
```

For a frontend-only portfolio demo, leave `VITE_API_BASE_URL` unset. The generated storefront, cart, checkout, local order tracking, and visual admin screens are still usable. For a full backend-connected deployment, set:

```text
VITE_API_BASE_URL=https://your-backend-domain.example/api
```

Then deploy the Spring Boot backend and MySQL database separately, and set the backend `CORS_ALLOWED_ORIGINS` to the deployed frontend URL.

Temporary sharing from a local machine:

```powershell
cd frontend
npm run dev -- --host 127.0.0.1 --port 5174
npx cloudflared tunnel --url http://127.0.0.1:5174
```

## Quality Checks

Backend:

```bash
cd backend
./mvnw test
```

Frontend:

```bash
cd frontend
npm install
npm run build
npm run test:unit
```

Smoke checks used during this pass included platform pages, generated storefront pages, legacy redirects, admin routes, support/refund routes, and the global not-found route through a temporary Vite dev server.

## Documentation

- API reference: [docs/API.md](docs/API.md)
- Architecture: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- Product requirements: [docs/PRODUCT_REQUIREMENTS.md](docs/PRODUCT_REQUIREMENTS.md)
- Development guide: [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)
- Deployment guide: [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)

## Security Notes

- The seeded admin account is for local development only.
- Change `JWT_SECRET` before deploying anywhere public.
- Store database credentials and JWT secrets in environment variables or a secret manager.
- No real payment provider is connected. Demo checkout creates orders and stock movements but does not authorize, capture, or refund real money.
- Public storefront APIs are open by design. Admin APIs require a valid bearer token.
- Password hashes use BCrypt and are never returned from API responses.

## Limitations And Roadmap

- Add a real payment provider before accepting live payments.
- Add customer accounts, saved addresses, and customer order history.
- Add deeper server-side pagination for large admin datasets.
- Add admin audit logs for product, promotion, fulfillment, refund, and inventory changes.
- Add production observability, structured logs, rate limiting, backups, and monitoring.
- Expand end-to-end test coverage for all merchant workflows.

## Troubleshooting

- MySQL connection failure: verify host, port, database, user, password, and privileges.
- Frontend cannot reach backend: confirm `VITE_API_BASE_URL` points to `http://localhost:8080/api`.
- CORS issue: confirm `CORS_ALLOWED_ORIGINS` includes the frontend origin.
- Port already in use: stop the existing process or start Vite on another port and update CORS during local testing.
- JWT errors: set a long `JWT_SECRET` and clear stale browser storage after changing auth settings.
