# ReNova Backend

The backend module contains the Spring Boot REST API for ReNova, a second-hand marketplace. It supports public listing discovery, JWT authentication, seller listings, favorites, offers, conversations, order state changes, reviews, MySQL persistence, H2 tests, and consistent JSON responses.

## Current Implementation

- Java 21 and Spring Boot 4.0.6.
- Maven wrapper for repeatable builds.
- Spring Web MVC REST controllers.
- Spring Data JPA and Hibernate entities for users, categories, listings, favorites, offers, conversations, messages, orders, and reviews.
- MySQL runtime persistence with H2 test profile support.
- JWT authentication for protected marketplace APIs.
- BCrypt password hashing for user accounts.
- Non-sensitive category seed data; optional sample sellers/listings require the explicit `demo` profile and an environment-supplied password.
- Server-side listing search, filtering, sorting, and pagination.
- Transactional listing, offer, order, and review business rules.
- Global exception handling with consistent English JSON error responses.
- MockMvc tests for public API reads, validation, auth, and listing creation.

## API Areas

Public endpoints:

- `GET /api/public/categories`
- `GET /api/public/listings`
- `GET /api/public/listings/{id}`
- `GET /api/public/users/{id}`
- `GET /api/public/users/{id}/listings`
- `GET /api/public/users/{id}/reviews`

Authentication:

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `GET /api/auth/me`

Protected marketplace endpoints:

- `POST /api/listings`
- `PUT /api/listings/{id}`
- `DELETE /api/listings/{id}`
- `GET /api/listings/mine`
- `POST /api/listings/{id}/favorite`
- `GET /api/listings/favorites`
- `POST /api/offers`
- `GET /api/offers/received`
- `GET /api/offers/sent`
- `GET /api/conversations`
- `POST /api/conversations`
- `POST /api/orders`
- `GET /api/orders/buying`
- `GET /api/orders/selling`
- `POST /api/reviews`
- `GET /api/users/me`
- `PUT /api/users/me`

## Local Requirements

- Java 21
- MySQL 8 or compatible database for runtime

## Environment Variables

See [`.env.example`](.env.example) for the full local configuration template.

Required production values should be supplied through environment variables or a secret manager. Do not commit real credentials.

## Commands

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

## Test Profile

Tests use `application-test.yml`, H2 in MySQL compatibility mode, and the `test` Spring profile. This keeps backend checks independent from a local MySQL server. The same profile can be used for local smoke runs with `.\mvnw.cmd -Dspring-boot.run.profiles=test spring-boot:run`.

## Security Notes

- Normal startup creates no user or admin accounts. Never expose the optional demo profile on a public deployment.
- Known accounts from earlier demo builds are automatically deactivated outside the `demo` profile.
- Replace `JWT_SECRET` with a long, random production secret.
- Keep database credentials, JWT secrets, and deployment settings out of version control.
- This backend does not process real card payments. Payment endpoints currently move marketplace order state only.
