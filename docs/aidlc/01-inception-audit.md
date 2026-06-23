# ReNova AIDLC Stage 1: Inception / Audit

Date: 2026-06-23

Branch: `codex/21st-motion-redesign`

Baseline: `013a7c3 Harden ReNova API boundaries and tests`

## Scope and decision boundary

This stage audits the existing Vue 3 + Spring Boot + MySQL marketplace, the three mandatory security gates, the real buyer/seller loop, deployment readiness, and interaction patterns worth adapting from 21st.dev. It does not implement features or visual changes. Construction must not start until the security design is approved and all three gates are fixed and proven.

## Executive result

All three mandatory security gates are currently **blocked**. The application has useful real foundations and its current test suite passes, but it is not yet a safe deployable marketplace.

| Gate | Result | Why |
| --- | --- | --- |
| 1 - No plaintext passwords | Blocked | User passwords are BCrypt-hashed, but demo/admin plaintext passwords are committed, rendered in the UI, and present in git history. |
| 2 - No frontend-only protection | Blocked | Most server ownership checks exist, but cross-user denial is not proven by tests; one order-review route does not enforce order participation; authorization failures return business-rule 400 responses instead of consistent 403/404 responses. |
| 3 - No secrets in git | Blocked | No real `.env` file or third-party API key was found, but database/JWT defaults and demo account credentials are committed and remain in history. |

## Gate 1 evidence: password handling

### What is already correct

- `SecurityConfig.passwordEncoder()` returns `BCryptPasswordEncoder` (`backend/src/main/java/com/novacart/store/config/SecurityConfig.java:69`).
- Registration hashes the password before persistence (`backend/src/main/java/com/novacart/store/service/AuthService.java:38`).
- Login uses `PasswordEncoder.matches` (`backend/src/main/java/com/novacart/store/service/AuthService.java:53`).
- The database entity stores `password_hash`, and auth response DTOs do not contain a password or password hash.
- Repository search found no application logging of passwords and no password field returned by API response DTOs.

### Blocking findings

- Default initialization commits five plaintext passwords (`backend/src/main/java/com/novacart/store/config/DataInitializer.java:66`).
- Login autofill and visible help text send a demo password to every client (`frontend/src/pages/LoginPage.vue:31`, `frontend/src/pages/LoginPage.vue:53`).
- The footer and documentation repeat the credentials (`frontend/src/components/AppFooter.vue:28`, `README.md:25`, `docs/API.md:62`).
- The current tests reuse the same committed demo password instead of creating isolated test users (`backend/src/test/java/com/novacart/store/controller/ReNovaApiControllerTests.java:30`).

Gate 1 must remove production/default demo accounts, scope optional seed data to an explicit demo profile, source any demo credential from the environment, remove credential autofill/display, and add response/hash regression tests.

## Gate 2 evidence: server authorization

### Existing server enforcement

- Spring Security permits only signup, login, public reads, and health/info; all other requests require authentication (`backend/src/main/java/com/novacart/store/config/SecurityConfig.java:58`).
- Listing update/delete checks the authenticated seller (`backend/src/main/java/com/novacart/store/service/ListingService.java:117`, `:153`).
- Order payment, shipment, receipt, cancellation, and read operations check buyer/seller participation (`backend/src/main/java/com/novacart/store/service/OrderService.java:108`, `:123`, `:140`, `:157`, `:179`).
- Conversation access checks buyer/seller participation (`backend/src/main/java/com/novacart/store/service/MessagingService.java:131`).
- Offer mutation checks the authenticated buyer or listing seller throughout `OfferService`.
- Current-user account reads/updates derive the user from the authenticated principal rather than a client-supplied user ID.

### Blocking findings

- There is no direct API test proving user A cannot update/delete user B's listing.
- There is no direct API test proving user A cannot read user B's order, conversation, or private account data.
- `ReviewService.listForOrder` loads an order without the participant check used by `OrderService.get` (`backend/src/main/java/com/novacart/store/service/ReviewService.java:87`). The route must either be deliberately public review data or participant-only order data; its current contract is ambiguous.
- Ownership failures use `BusinessRuleException`, which maps authorization failures to HTTP 400. The security model needs deliberate 403 or privacy-preserving 404 semantics.
- `ADMIN` exists as a role, but there is no admin API or admin UI to audit. It must not be treated as an implemented feature.

The Vue route guard is only navigation UX. It is not counted as security proof.

## Gate 3 evidence: secrets and history

### Repository scan result

- `.gitignore` excludes `.env` and `.env.*` while allowing examples.
- Git history contains only `backend/.env.example` and `frontend/.env.example`; no actual `.env`, PEM, private key, P12, or PFX file was found.
- High-confidence scans found no AWS, GitHub, OpenAI, Stripe, Slack, private-key, or JWT-shaped third-party token.

### Exposed committed credentials/defaults

| Value class | Current locations | First known history commit |
| --- | --- | --- |
| Database username/password and JWT fallback | `backend/src/main/resources/application.yml:5`, `:7`, `:29` | `8a7c16d` |
| Docker DB/root passwords and JWT fallback | `docker-compose.yml:8`, `:9`, `:29`, `:30` | `221b2d9` |
| Demo user/admin passwords | `DataInitializer.java:66`, login UI, README/API docs | `65ad499` |

These values remain visible in old commits. If any deployed environment ever used them, rotate the database user password, database root password, JWT signing secret, and seeded demo/admin credentials. Rotating the JWT secret invalidates existing tokens. No third-party key was found that requires rotation.

Gate 3 must make runtime secrets mandatory environment inputs, keep only placeholders in `.env.example`, and remove plaintext account credentials from source and documentation.

## Core marketplace audit

| Flow | Current state | Gap |
| --- | --- | --- |
| Register/login | Real database persistence and JWT auth | Plaintext demo credentials; token persisted in `localStorage`; no rate limiting/password reset/email verification |
| Create/edit listing | Real JPA persistence and seller checks | Images are pasted URLs, not uploaded media |
| Browse/search/view | Real DB-backed public endpoints | Limited empty/loading transitions and no complete pagination UX evidence |
| Contact/offers | Real persisted conversations, messages, and offers | Cross-user denial tests are missing |
| Buy/order | Real persisted order state machine | Payment is a fake state transition; no provider/webhooks/refunds |
| Concurrent checkout | Not safe enough | No listing row lock, reservation transaction, or idempotency key; two buyers can race |
| Admin/moderation | Not implemented | Role exists without admin APIs, moderation, reports, or operational UI |
| Deployment | Dockerfiles/Compose and CI exist | Runtime uses `ddl-auto: update`, no migrations, insecure defaults, static Pages cannot host the API/database |

The claimed 100,000-user concurrency level is not proven. Application changes can add idempotency, row locking, bounded payloads, rate limits, and load-test scenarios, but the target also requires measured infrastructure capacity, database sizing, CDN/object storage, horizontal scaling, queues where justified, monitoring, and a paid hosting plan sized from load-test results.

## Additional security and maintainability findings

- Access tokens are stored in browser `localStorage` (`frontend/src/stores/auth.js:10`), increasing the impact of an XSS defect. The design stage must choose a secure cookie/CSRF model or document a narrower alternative.
- Login redirects trust `route.query.redirect` (`frontend/src/pages/LoginPage.vue:24`) and should accept only internal routes.
- Nginx currently sets no CSP or baseline browser security headers (`frontend/nginx.conf`).
- `spring.jpa.hibernate.ddl-auto=update` is not a controlled production migration strategy (`backend/src/main/resources/application.yml:10`).
- H2 is a runtime dependency rather than test-scoped (`backend/pom.xml`).
- Frontend dependency ranges use carets instead of exact versions (`frontend/package.json`).
- `npm audit --omit=dev` reports one high-severity advisory in `form-data@4.0.5`, reached through `axios@1.16.1`.
- The repository still contains legacy NovaCart screenshots and planning artifacts that do not represent the current product and should be classified before removal.

## 21st.dev interaction research

The following patterns are useful references, not code to copy. The examples are React/Tailwind; ReNova should implement equivalent Vue components and CSS behavior that fit its existing stack.

- Product Card: image/color state transition and stronger item focus. Candidate use: listing cards with image lift, status/seller reveal, and stable dimensions.
- Expandable Card: move from summary to focused detail. Candidate use: quick-view that preserves browse context without replacing the full listing route.
- Shine Hover: restrained directional highlight. Candidate use: one primary CTA per surface, with keyboard and reduced-motion equivalents.
- Animated Feature Carousel: segmented progress and overlapping imagery. Candidate use: a compact “how trading works” sequence using real marketplace states.
- Search/category gallery: prominent search, filter chips, and fast preview scanning. Candidate use: browse page information hierarchy and animated filter transitions.

Candidate references:

- https://21st.dev/community/components/YoucefBnm/product-card/default
- https://21st.dev/community/components/erikx/expandable-card/default
- https://21st.dev/community/components/shadcnspace/shine-hover/default
- https://21st.dev/community/components/thanh/animated-feature-carousel/default

Motion must communicate state, hierarchy, and continuity. It must honor `prefers-reduced-motion`, avoid layout shift, keep keyboard/focus behavior intact, and stay subtle during repeated marketplace tasks.

## Baseline verification run

Executed on 2026-06-23 before any implementation:

```text
backend:  Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
frontend: Test Files 5 passed; Tests 25 passed
build:    vite production build completed; 138 modules transformed
audit:    1 high severity vulnerability (form-data 4.0.5 via axios 1.16.1)
```

These tests prove the current baseline compiles and its existing checks pass. They do not prove the mandatory ownership, response secrecy, concurrency, upload, or full end-to-end requirements.

## Stage exit

Inception/Audit is complete. The next permitted step is **AIDLC Stage 2: Design**, covering the security model, endpoint behavior, data migrations, upload/storage boundary, checkout transaction/idempotency model, admin scope, Vue motion system, and reviewable construction increments. No feature construction should begin before that design is approved.
