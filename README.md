# NovaCart Ecommerce

NovaCart Ecommerce is a full-stack online store system for small merchants. It includes a public storefront, a protected merchant administration workspace, MySQL persistence, JWT authentication, checkout with stock validation, order management, inventory warnings, and a RESTful JSON API.

The project is intentionally original in naming, layout, and content. All visible text, seed data, API messages, comments, documentation, and commit messages are written in English.

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
