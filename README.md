# NovaCart Fashion Commerce Platform

NovaCart is a production-style portfolio ecommerce platform for fashion and lifestyle merchants. It combines a premium customer storefront with a protected merchant operations workspace for catalog, collections, promotions, orders, refunds, support, customers, analytics, and inventory.

NovaCart is original in naming, layout, copy, seed data, and local artwork. It is a demo-safe project, not a live payment-processing store.

## Visual Preview

### Premium Storefront

![NovaCart storefront home](docs/assets/screenshots/storefront-home.png)

### Fashion Catalog

![NovaCart fashion catalog](docs/assets/screenshots/fashion-catalog.png)

### Product Detail

![NovaCart product detail](docs/assets/screenshots/product-detail.png)

### Cart And Checkout

![NovaCart cart](docs/assets/screenshots/cart.png)

![NovaCart checkout](docs/assets/screenshots/checkout.png)

### Order Confirmation And Care

![NovaCart order success](docs/assets/screenshots/order-success.png)

![NovaCart support and refund flow](docs/assets/screenshots/support-refund.png)

### Merchant Operations Dashboard

![NovaCart admin dashboard](docs/assets/screenshots/admin-dashboard.png)

### Catalog Operations

![NovaCart admin products](docs/assets/screenshots/admin-products.png)

![NovaCart admin collections](docs/assets/screenshots/admin-collections.png)

![NovaCart admin promotions](docs/assets/screenshots/admin-promotions.png)

### Orders, Care, And Analytics

![NovaCart admin orders](docs/assets/screenshots/admin-orders.png)

![NovaCart admin refunds](docs/assets/screenshots/admin-refunds.png)

![NovaCart admin support](docs/assets/screenshots/admin-support.png)

![NovaCart admin analytics](docs/assets/screenshots/admin-analytics.png)

## What NovaCart Is

NovaCart is a full-stack fashion commerce system for clothing, bags, jewelry, accessories, shoes, sportswear, activewear, sports equipment, seasonal collections, and sale campaigns.

The project demonstrates real ecommerce architecture: a Spring Boot REST API, MySQL persistence, JPA entities, DTO validation, service-layer business logic, JWT-protected admin APIs, transactional checkout, stock movement tracking, a Vue 3 storefront, and a merchant admin workspace.

## Customer Storefront

- Premium fashion homepage with editorial hero, seasonal campaign, category navigation, featured collections, new arrivals, best sellers, sale campaign, and customer-care highlights.
- Catalog browsing with server-backed search, category, collection, size, color, material, label, season, tag, sale, availability, price range, sorting, active filter chips, and pagination.
- Product cards with fashion artwork, collection ribbons, discount badges, stock status, color swatches, size availability, quick add, and detail links.
- Product detail pages with gallery artwork, collection path, fictional label, SKU, season, price hierarchy, size/color selectors, quantity control, stock guardrails, add-to-cart, buy-now, shipping notes, refund window, support link, and related products.
- Cart and checkout with selected size/color, quantity editing, discount totals, shipping estimate, tax estimate, demo payment method, refund acknowledgement, validation, and order creation.
- Order success, support ticket, and refund request flows that use order number and email context.

## Merchant Admin

- JWT-protected admin login with persisted session handling and clear expired-session behavior.
- Dashboard with revenue, order, refund, stock, sales trend, top regions, best sellers, and customer preference signals.
- Product management with thumbnail table, search, status/category/collection/sale filters, active/draft/archive status, stock badges, featured markers, preview links, edit links, archive/reactivate, bulk archive, bulk collection assignment, and selected-product markdown creation.
- Collection management for Spring Edit, Summer Essentials, Workwear Capsule, Evening Details, Active Weekend, and End of Season Sale with campaign images and product assignment.
- Promotion management for percentage or fixed discounts targeted by selected products, categories, collections, seasons, or tags.
- Order management with status tabs, search, region filtering, payment state, refund state, fulfillment state, order detail, and safe status transitions.
- Refund and support queues with status summaries, merchant-only notes, and workflow updates.
- Customer records and analytics based on guest checkout email.
- Inventory workspace with low-stock thresholds, manual adjustments with reasons, warning cards, and stock movement history.

## Core Workflows

```mermaid
flowchart LR
  Home["Storefront Home"] --> Catalog["Fashion Catalog"]
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
novacart-ecommerce/
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
