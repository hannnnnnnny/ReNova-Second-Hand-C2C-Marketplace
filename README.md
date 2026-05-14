# NovaCart Ecommerce

NovaCart Ecommerce is a full-stack online store system for small merchants. It includes a public storefront, a protected merchant administration workspace, MySQL persistence, JWT authentication, checkout with stock validation, order management, inventory warnings, and a RESTful JSON API.

The project is intentionally original in naming, layout, and content. All visible text, seed data, API messages, comments, documentation, and commit messages are written in English.

## Product Features

### Storefront Experience

- Responsive storefront homepage with a polished hero section, category highlights, featured products, and trust/value cards.
- Product catalog with search, category filtering, price sorting, stock badges, loading states, empty states, and friendly error states.
- Product detail pages with large product imagery, category context, clear price hierarchy, quantity selection, stock status, related products, and add-to-cart feedback.
- Cart page with item summaries, quantity controls, stock-aware limits, remove actions, subtotal calculation, and a clear checkout path.
- Checkout flow with customer information, shipping address fields, order summary, basic validation, disabled processing state, and backend stock validation.
- Order success page with confirmation messaging, order number, item summary, total amount, and clear next steps.

### Merchant Admin Workspace

- Protected admin login with JWT authentication, persisted sessions, expired-session handling, and BCrypt password support on the backend.
- Dashboard metrics for total products, active products, total orders, total revenue, low-stock products, and recent order activity.
- Product management with searchable tables, status filters, inventory indicators, create/edit forms, validation feedback, and delete confirmation.
- Category management with create, edit, delete, empty-state, and feedback patterns for catalog organization.
- Order management with search, status filtering, order detail views, customer information, item summaries, totals, and status updates.
- Inventory warning screen with configurable low-stock threshold, replenishment indicators, and fast navigation back to product management.

### Platform Capabilities

- RESTful JSON API with consistent response envelopes and centralized error handling.
- MySQL persistence through Spring Data JPA and Hibernate.
- Transactional checkout logic that prevents insufficient-stock purchases and negative inventory.
- Public storefront APIs separated from protected admin APIs.
- Vue 3 frontend with Vue Router, Pinia state management, Axios API wrappers, and reusable UI components.
- English-only interface, documentation, API messages, seed data, and comments.

## Key Advantages

- Built as a full-stack production-style project rather than a single-page mockup.
- Clear separation between controllers, services, repositories, DTOs, entities, security, and frontend API modules.
- Original visual design and wording created specifically for NovaCart.
- Responsive layouts for storefront and admin workflows across mobile, tablet, and desktop screens.
- Practical merchant workflows covering catalog setup, checkout, fulfillment, and inventory monitoring.
- Safer checkout behavior through backend stock checks and frontend quantity controls.
- Developer-friendly setup with Maven wrapper, Vite scripts, environment examples, tests, and troubleshooting notes.
- Maintainable UI system with shared components for page headers, product cards, loading states, empty states, error messages, status badges, quantity controls, metrics, and feedback.

## Application Preview

Run the backend and frontend locally, then open the Vite app at `http://localhost:5173`.

The screenshots below are generated from the current Vue interface with representative preview data, so readers can see the product experience before running the project locally.

### Storefront Screens

| Home | Product Catalog |
| --- | --- |
| ![NovaCart storefront home preview](docs/preview/storefront-home.png) | ![NovaCart product catalog preview](docs/preview/product-catalog.png) |
| Premium landing page with hero messaging, category highlights, featured products, and trust/value cards. | Searchable catalog with category filters, price sorting, product cards, stock badges, and add-to-cart actions. |

| Product Detail | Cart |
| --- | --- |
| ![NovaCart product detail preview](docs/preview/product-detail.png) | ![NovaCart cart preview](docs/preview/cart.png) |
| Detailed product page with image focus, category label, price hierarchy, stock indicator, quantity selector, related products, and cart feedback. | Cart review page with item images, quantity controls, stock-aware limits, line totals, subtotal summary, and checkout action. |

| Checkout | Order Success |
| --- | --- |
| ![NovaCart checkout preview](docs/preview/checkout.png) | ![NovaCart order success preview](docs/preview/order-success.png) |
| Checkout form with customer details, shipping address, order summary, validation states, and protected submit behavior. | Confirmation page with order number, item summary, order total, next-step messaging, and shopping links. |

### Admin Screens

| Admin Login | Dashboard |
| --- | --- |
| ![NovaCart admin login preview](docs/preview/admin-login.png) | ![NovaCart admin dashboard preview](docs/preview/admin-dashboard.png) |
| Professional admin sign-in screen with secure access messaging, password visibility control, loading state, and error feedback. | Merchant dashboard with revenue, order, product, category, recent order, and low-stock signals. |

| Product Management | Order Management |
| --- | --- |
| ![NovaCart admin products preview](docs/preview/admin-products.png) | ![NovaCart admin orders preview](docs/preview/admin-orders.png) |
| Product table with search, status filters, category context, pricing, stock badges, active/inactive badges, edit actions, and delete confirmation. | Order table with customer details, status badges, total amount, placed date, search, status filtering, and order detail links. |

| Inventory Warnings |
| --- |
| ![NovaCart admin inventory preview](docs/preview/admin-inventory.png) |
| Inventory screen with configurable warning threshold, refresh action, low-stock cards, active/inactive context, and quick product management access. |

### Feature Map

| Area | Route | Preview Focus |
| --- | --- | --- |
| Storefront Home | `/` | Hero section, category highlights, featured products, trust/value cards, and responsive navigation. |
| Product Catalog | `/products` | Search, filtering, sorting, product cards, stock indicators, and add-to-cart actions. |
| Product Detail | `/products/:id` | Product imagery, details, price, quantity selector, stock messaging, and related products. |
| Cart | `/cart` | Cart items, quantity controls, subtotal summary, remove actions, and checkout call to action. |
| Checkout | `/checkout` | Customer form, shipping address, order summary, validation states, and order creation flow. |
| Order Success | `/order-success/:id` | Confirmation message, order number, totals, item summary, and next-step actions. |
| Admin Login | `/admin/login` | Secure admin entry screen with error handling and loading state. |
| Admin Dashboard | `/admin/dashboard` | Metrics, recent orders, low-stock products, and operational overview. |
| Admin Products | `/admin/products` | Searchable product table, status badges, stock badges, and management actions. |
| Admin Categories | `/admin/categories` | Category list, create/edit form, empty state, and delete confirmation. |
| Admin Orders | `/admin/orders` | Searchable order table, status filters, totals, dates, and detail links. |
| Admin Inventory | `/admin/inventory` | Low-stock threshold controls and replenishment warning cards. |

Suggested preview flow:

1. Open `/` and review the storefront landing experience.
2. Browse `/products`, search for a product, filter by category, and sort by price.
3. Open a product detail page, choose a quantity, and add the item to the cart.
4. Review `/cart`, adjust quantities, then continue to `/checkout`.
5. Place an order and review the confirmation page.
6. Sign in at `/admin/login` with the default admin account and inspect dashboard, products, categories, orders, and inventory pages.

```mermaid
flowchart LR
  Home["Storefront Home"] --> Catalog["Product Catalog"]
  Catalog --> Detail["Product Detail"]
  Detail --> Cart["Cart"]
  Cart --> Checkout["Checkout"]
  Checkout --> Success["Order Success"]
  Login["Admin Login"] --> Dashboard["Admin Dashboard"]
  Dashboard --> Products["Products"]
  Dashboard --> Orders["Orders"]
  Dashboard --> Inventory["Inventory"]
```

## Tech Stack

- Backend: Java 21, Spring Boot 4.0.6, Maven wrapper
- Frontend: Vue 3, Vite, Vue Router, Pinia, Axios
- Database: MySQL for application runtime
- Test database: H2 in MySQL compatibility mode
- ORM: Spring Data JPA and Hibernate
- Authentication: JWT with BCrypt password hashing
- Styling: Responsive custom CSS
- API style: RESTful JSON

## Folder Structure

```text
novacart-ecommerce/
  backend/
    src/main/java/com/novacart/store/
      config/
      controller/
      dto/
      entity/
      exception/
      repository/
      security/
      service/
      service/impl/
    src/main/resources/
    src/test/java/com/novacart/store/
    src/test/resources/
  frontend/
    src/
      api/
      assets/
      components/
      layouts/
      pages/
      router/
      stores/
      utils/
  docs/
```

## Backend Setup

The backend uses the Maven wrapper, so a global Maven install is not required.

Requirements:

- Java 21
- MySQL 8 or compatible

From the `backend/` directory:

```bash
./mvnw test
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

## Frontend Setup

Requirements:

- Node.js 20 or newer
- npm

From the `frontend/` directory:

```bash
npm install
npm run dev
npm run build
```

On Windows PowerShell, use `npm.cmd` if script execution policy blocks the npm shim:

```powershell
npm.cmd install
npm.cmd run dev
npm.cmd run build
```

The Vite development server runs on:

```text
http://localhost:5173
```

## MySQL Setup

Create a local database and user for NovaCart:

```sql
CREATE DATABASE novacart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'novacart_user'@'localhost' IDENTIFIED BY 'novacart_password';
GRANT ALL PRIVILEGES ON novacart.* TO 'novacart_user'@'localhost';
FLUSH PRIVILEGES;
```

The backend can create the database automatically when the configured MySQL user has permission, but creating it explicitly is clearer for local development.

## Environment Variables

Backend values are documented in [backend/.env.example](backend/.env.example).

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=novacart
DB_USERNAME=novacart_user
DB_PASSWORD=novacart_password
JWT_SECRET=replace-with-a-long-random-secret-for-local-development
JWT_EXPIRATION_MINUTES=120
CORS_ALLOWED_ORIGINS=http://localhost:5173
SERVER_PORT=8080
```

Frontend values are documented in [frontend/.env.example](frontend/.env.example).

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

Secrets must not be committed. Use environment variables or ignored local files for machine-specific settings.

## Seed Data

The backend seeds:

- One admin account
- Storefront categories
- Sample products with English names, descriptions, and image URLs

Default admin account:

```text
Email: admin@novacart.local
Password: NovaCartAdmin123!
```

Change the seeded password before using the project beyond local development.

## API Overview

Public APIs:

- `GET /api/public/categories`
- `GET /api/public/products`
- `GET /api/public/products/{id}`
- `POST /api/public/orders`
- `GET /api/public/orders/{id}`

Admin authentication:

- `POST /api/admin/auth/login`

Admin catalog APIs:

- `GET /api/admin/categories`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `DELETE /api/admin/categories/{id}`
- `GET /api/admin/products`
- `GET /api/admin/products/{id}`
- `POST /api/admin/products`
- `PUT /api/admin/products/{id}`
- `DELETE /api/admin/products/{id}`

Admin order and operations APIs:

- `GET /api/admin/orders`
- `GET /api/admin/orders/{id}`
- `PATCH /api/admin/orders/{id}/status`
- `GET /api/admin/dashboard/metrics`
- `GET /api/admin/inventory/warnings`

All API responses use a consistent JSON envelope. Validation and security errors return English messages without exposing sensitive implementation details.

## Frontend Routes

Public storefront:

- `/`
- `/products`
- `/products/:id`
- `/cart`
- `/checkout`
- `/order-success/:id`

Admin:

- `/admin/login`
- `/admin/dashboard`
- `/admin/products`
- `/admin/products/new`
- `/admin/products/:id/edit`
- `/admin/categories`
- `/admin/orders`
- `/admin/orders/:id`
- `/admin/inventory`

## Frontend UX Notes

The Vue storefront includes a responsive homepage, searchable product catalog, category filters, price sorting, polished product detail pages, cart quantity controls, checkout validation, and order confirmation views. Shared UI components provide consistent page headers, product cards, loading states, empty states, error messages, quantity controls, metric cards, status badges, and toast feedback.

The admin workspace includes protected routing, session expiry handling, dashboard metrics, searchable product and order tables, inventory warning cards, category management feedback, and responsive navigation with keyboard skip links. The frontend API client centralizes bearer token handling and friendly error messages for validation, authentication, and unavailable backend states.

## Testing And Builds

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
```

## Development Notes

- Public storefront APIs are open.
- Admin APIs require a bearer token from the admin login endpoint.
- Checkout uses a transaction and pessimistic product locking to prevent negative stock.
- Controllers delegate business logic to services.
- DTOs define request and response boundaries.
- The frontend stores cart and admin session data in browser local storage.
- Keep all user-facing text and seed data in English.
- Commit changes in meaningful, stable units after running relevant checks.

## Troubleshooting

- If Maven commands fail, confirm Java 21 is installed and available on the system path.
- If MySQL connection fails, verify host, port, database name, username, password, and database privileges.
- If JWT login fails, confirm the seeded admin account exists and the backend has a sufficiently long `JWT_SECRET`.
- If browser requests are blocked by CORS, verify `CORS_ALLOWED_ORIGINS` includes the frontend origin.
- If npm is blocked in PowerShell, use `npm.cmd` commands.
- If frontend requests fail, confirm `VITE_API_BASE_URL` points to the backend API root.
