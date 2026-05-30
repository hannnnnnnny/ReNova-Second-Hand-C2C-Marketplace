# ReNova — Second-Hand C2C Marketplace

A portfolio-grade peer-to-peer second-hand marketplace. Individuals list pre-loved goods, message each other, negotiate, place orders through a demo escrow state machine, ship, confirm receipt, and leave two-way reviews that roll up into user reputation.

This README is the deploy and run guide. It tells you what the project is, what's pinned, how to run it locally, what env vars are required, and how the security gates work. It does not claim anything it has not been verified to do.

The UI is bilingual: English and 简体中文, switchable from the header.

---

## Stack (pinned)

| Layer | What | Version |
|---|---|---|
| Backend | Java | 21 |
| | Spring Boot (parent) | 4.0.6 |
| | Spring Data JPA / Spring Security / Spring Webmvc | (from BOM) |
| | JJWT | 0.13.0 |
| | MySQL connector | (from BOM) |
| | H2 (runtime for `demo` profile + tests) | (from BOM) |
| Frontend | Node | 20 |
| | Vue | 3.5.34 |
| | vue-router | 4.6.4 |
| | Pinia | 3.0.4 |
| | vue-i18n | 11.4.4 |
| | axios | 1.16.1 |
| | Vite | 7.3.3 |
| | Vitest | 4.1.6 |

`frontend/package.json` uses exact versions (no caret ranges). Backend versions come from `spring-boot-starter-parent:4.0.6` plus an explicit JJWT pin.

---

## What works (verified by automated tests, see *Verification* below)

- **Signup + login** with BCrypt password hashing and JWT-based stateless auth.
- **Listings**: create, edit, delete, browse, search by keyword/category/condition/price/location, sort.
- **Listing detail** with view counter that ticks on every open and persists.
- **Buyer–seller messaging** per listing, with unread count.
- **Offer thread**: make / accept / reject / counter / withdraw. Accepted offers reserve the listing.
- **Order state machine**: `PENDING_PAYMENT → PAID → SHIPPED → COMPLETED` (also `CANCELLED`). Completing the order marks the listing `SOLD`.
- **Two-way reviews** after a completed order; ratings roll up to the user's profile.
- **Public profile** endpoint exposes display name, location, bio, average rating, member-since — never the user's email.
- **Favorites** per user.
- **i18n** with English and Simplified Chinese bundles (top-level keys must match across locales; enforced by a test).

## What is intentionally out of scope

- No real payment processor. The "pay" action is a state transition.
- No image upload pipeline; listings reference image URLs.
- No realtime push (WebSocket / SSE) for messages — unread count refreshes on route change.
- No dispute escalation UI (the `DISPUTED` order state exists but is not user-reachable).
- No admin dashboard UI surface (the role exists in the model).

---

## Run it locally

### Option A — Self-contained `demo` profile (zero external services)

This is the fastest path. The backend runs against an in-memory H2 database and **generates a fresh random JWT secret per JVM** (see Gate 3 below). MySQL is not required.

```bash
# from repo root
cd backend
./mvnw -DskipTests package
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

On Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd -DskipTests package
java -jar .\target\backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

Backend comes up at `http://localhost:8080`. The first launch seeds 9 categories, 5 demo users, and ~8 starter listings.

Then in another terminal:

```bash
cd frontend
npm ci
npm run dev
```

Frontend on `http://localhost:5173`. The Vite dev server proxies `/api` to `http://localhost:8080`.

### Option B — `docker compose` against MySQL

Copy the env example and fill in **real** values (the file is gitignored once renamed):

```bash
cp docker.env.example .env
# edit .env; every __SET_ME__ is required
docker compose up --build
```

`docker-compose.yml` uses `${VAR:?}` on every credential — compose refuses to start if any of them is missing or empty. There are no fallback values.

### Option C — Local MySQL + `mvn spring-boot:run`

Create the database and a user:

```sql
CREATE DATABASE renova CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'renova_user'@'localhost' IDENTIFIED BY 'a-real-password-please';
GRANT ALL PRIVILEGES ON renova.* TO 'renova_user'@'localhost';
FLUSH PRIVILEGES;
```

Then run with env vars set (or `cp backend/.env.example backend/.env` and source it):

```bash
DB_NAME=renova \
DB_USERNAME=renova_user \
DB_PASSWORD=a-real-password-please \
JWT_SECRET="$(openssl rand -base64 48)" \
./mvnw spring-boot:run
```

JPA `ddl-auto=update` creates the schema on first run.

---

## Environment variables

### Backend (`backend/.env.example`)

| Name | Required? | Notes |
|---|---|---|
| `DB_HOST`, `DB_PORT` | optional | default `localhost:3306` |
| `DB_NAME` | **required outside `demo`** | no default |
| `DB_USERNAME` | **required outside `demo`** | no default |
| `DB_PASSWORD` | **required outside `demo`** | no default |
| `JWT_SECRET` | **required outside `dev/test/demo`** | 32+ random bytes; `JwtSecretGuard` refuses to start otherwise (see Gate 3) |
| `JWT_EXPIRATION_MINUTES` | optional | default 120 |
| `CORS_ALLOWED_ORIGINS` | optional | comma-separated; default `http://localhost:5173,http://127.0.0.1:5173` |
| `SERVER_PORT` | optional | default 8080 |

Generate a real JWT secret:

```bash
openssl rand -base64 48
# or
python -c "import secrets; print(secrets.token_urlsafe(48))"
# or
node -e "console.log(require('crypto').randomBytes(48).toString('base64'))"
```

### Frontend (`frontend/.env.example`)

| Name | Required? | Notes |
|---|---|---|
| `VITE_API_BASE_URL` | optional | default `http://localhost:8080/api` |

### Docker (`docker.env.example`)

Every value is required. There are no fallback defaults. See `docker-compose.yml` for which one maps where.

---

## Demo accounts (seeded on first launch)

| Email | Password | Role |
|---|---|---|
| `ava@renova.local` | `DemoPassword1!` | USER |
| `liam@renova.local` | `DemoPassword1!` | USER |
| `nora@renova.local` | `DemoPassword1!` | USER |
| `sam@renova.local` | `DemoPassword1!` | USER |
| `admin@renova.local` | `DemoAdmin123!` | ADMIN |

You can also sign up a fresh account.

---

## Security model

### Gate 1 — Passwords

- Stored as BCrypt (`$2[aby]$…`, 60 chars). Never written in plaintext, never returned in any DTO, never logged. Login uses generic error messages so wrong-password and no-such-user are indistinguishable (no user enumeration). Signup rejects case-mangled duplicate emails so an attacker can't shadow-register over an existing account.
- Verified by `PasswordSecurityTests` (4 tests).

### Gate 2 — Server-side authorization

Every mutating or private route resolves the caller from the JWT subject (not from the request body) and checks ownership / participation server-side. The UI hiding a button is never the protection.

- A buyer cannot edit, delete, or read another user's listing or order via direct API call.
- A non-participant cannot read or post into someone else's conversation.
- A buyer cannot accept their own offer (only the seller can).
- Unauthenticated requests are 401 (including against permitAll routes — Spring's default anonymous token is rejected explicitly).
- `/api/public/users/{id}` returns a `PublicUser` DTO that does not include email.
- Verified by `AuthorizationAttackTests` (9 attack scenarios) that drive real HTTP through the Spring Security filter chain as a hostile second user.

### Gate 3 — No secrets in git

- No real `.env` was ever committed; only `.env.example` files.
- `JwtSecretGuard` runs at startup in every profile **except** `dev`, `test`, `demo`. It refuses to start if `JWT_SECRET` is unset, contains a known placeholder substring, is shorter than 32 bytes, or has fewer than 8 distinct characters.
- `application.yml` has no default for `JWT_SECRET`, `DB_USERNAME`, or `DB_PASSWORD`. Missing values cause Spring to fail at bean creation, not silently use a known string.
- `application-demo.yml` uses `JWT_SECRET:` (empty default). A `DemoJwtSecret` `EnvironmentPostProcessor` generates a fresh 384-bit random secret per JVM when the demo profile is active and no `JWT_SECRET` was supplied.
- `docker-compose.yml` uses `${VAR:?required}` syntax for every credential; missing values abort `docker compose up`.
- `.env` example files use `__SET_ME__` placeholders that the guard explicitly rejects.
- Verified by `JwtSecretGuardTests` (5 unit cases) plus 4 live boot attempts that exercise the contract end-to-end.

### Rotation notice

The placeholder strings previously committed (`change-this-development-secret-…`, `replace-with-a-long-random-secret-for-local-docker-only`, `renova-demo-secret-not-for-production-use-only`, and the DB password `novacart_password`) remain in git history. If any deployment ever used them verbatim, treat them as compromised and rotate at the source. Rewriting git history is out of scope here.

---

## Architecture

### Backend

```
backend/src/main/java/com/novacart/store/
  config/         SecurityConfig, JwtSecretGuard, DemoJwtSecret, DataInitializer
  controller/     Auth, Listing, Offer, Conversation, Order, Review, Category, User
  dto/            *Dtos.java (records, grouped by domain)
  entity/         User, Category, Listing, Favorite, Offer, Conversation,
                  Message, TradeOrder, Review, + enums
  exception/      GlobalExceptionHandler + business exceptions
  repository/     Spring Data JPA repositories
  security/       JwtAuthenticationFilter, JwtService, AppUserDetailsService,
                  CurrentUserService
  service/        Business logic per domain (each is one concrete @Service)
```

### Frontend

```
frontend/src/
  api/            axios client + endpoint groups
  components/     reusable atoms (AppHeader, AppFooter, ListingCard, Avatar, …)
  i18n/           en + zh message bundles + locale switcher
  layouts/        MainLayout (header + footer wrap)
  pages/          17 routed pages
  router/         vue-router config with auth guard
  stores/         Pinia (auth, toast)
  utils/          formatting helpers
  assets/         single design-token stylesheet
```

### Core flows

```mermaid
flowchart LR
  Browse[Browse / Search] --> Detail[Listing detail]
  Detail -- Make offer --> Offer[Offer thread]
  Offer -- Accept --> Reserve[Listing RESERVED]
  Detail -- Buy now --> Checkout
  Reserve --> Checkout
  Checkout --> Pending[Order PENDING_PAYMENT]
  Pending -- Pay (demo) --> Paid[PAID]
  Paid -- Seller ships --> Shipped[SHIPPED + tracking]
  Shipped -- Confirm receipt --> Done[COMPLETED + listing SOLD]
  Done --> Review[Two-way review]
```

---

## Verification

### Running the tests yourself

```bash
# backend (20 tests; ~25s cold)
cd backend
./mvnw test

# frontend (11 tests)
cd ../frontend
npm ci
npm run test:unit
```

### What the tests prove

| Suite | Count | Coverage |
|---|---|---|
| `PasswordSecurityTests` | 4 | Gate 1: BCrypt on disk, no plaintext in responses, generic auth errors, case-insensitive duplicate-email rejection |
| `AuthorizationAttackTests` | 9 | Gate 2: cross-user edit/delete refused, anonymous 401, forged tokens 401, buyer-accepts-own-offer refused, stranger-reads-conversation refused, public profile has no email |
| `JwtSecretGuardTests` | 5 | Gate 3: rejects missing / placeholder / too-short / low-entropy secrets, accepts a real one |
| `CoreLoopFlowTest` | 1 (10 sub-steps) | Phase A: signup → post → browse → detail → message → offer → order → pay/ship/confirm → SOLD + cold-re-fetch persistence |
| `NovacartBackendApplicationTests` | 1 | Spring context loads |
| Frontend `format.test.js` | 7 | Price/initials/avatar-bg helpers |
| Frontend `messages.test.js` | 4 | i18n bundle parity (EN and ZH have identical top-level keys) |

### Latest run (from this commit)

```
backend:  Tests run: 20, Failures: 0, Errors: 0, Skipped: 0   BUILD SUCCESS
frontend: Test Files 2 passed (2)   Tests 11 passed (11)
```

### Live boot proof for Gate 3 (4 attempts)

| Profile | `JWT_SECRET` | Result |
|---|---|---|
| `prod` | unset | `PlaceholderResolutionException` at bean creation — refused |
| `prod` | `change-this-development-secret-…` | `IllegalStateException: GATE 3 … known placeholder ('change-this')` — refused |
| `prod` | 20-byte value | `WeakKeyException: 160 bits which is not secure enough` — refused |
| `prod` | 48-byte random | `Started NovacartBackendApplication …`, `/actuator/health → 200` — accepted |

---

## API surface

All responses are wrapped in:

```json
{ "success": true, "message": "…", "data": …, "timestamp": "…" }
```

or, for failures:

```json
{ "success": false, "message": "…", "status": <http>, "path": "…", "errors": [], "timestamp": "…" }
```

Public (no token):

- `POST /api/auth/signup`, `POST /api/auth/login`
- `GET  /api/public/listings`, `GET /api/public/listings/{id}`
- `GET  /api/public/users/{id}`, `GET /api/public/users/{id}/listings`
- `GET  /api/public/categories`
- `GET  /actuator/health`, `/actuator/info`

Authenticated (`Authorization: Bearer …`):

- `GET  /api/auth/me`
- `POST /api/listings`, `PUT /api/listings/{id}`, `DELETE /api/listings/{id}`
- `GET  /api/listings/mine`, `GET /api/listings/favorites`, `POST /api/listings/{id}/favorite`
- `POST /api/offers`, `POST /api/offers/{id}/{accept|reject|counter|withdraw|accept-counter}`, `GET /api/offers/{received|sent}`
- `GET  /api/conversations`, `POST /api/conversations`, `GET /api/conversations/{id}`, `POST /api/conversations/{id}/messages`, `GET /api/conversations/unread-count`
- `POST /api/orders`, `GET /api/orders/{id}`, `POST /api/orders/{id}/{pay|ship|confirm-receipt|cancel}`, `GET /api/orders/{buying|selling}`
- `POST /api/reviews`
- `GET  /api/users/me`, `PUT /api/users/me`

---

## Project layout

```
renova-marketplace/
  backend/             Spring Boot 4 service
  frontend/            Vue 3 SPA
  docker-compose.yml   MySQL + backend + frontend, all credentials env-only
  docker.env.example   Template for docker compose .env
  backend/.env.example Template for backend env
  frontend/.env.example
```
