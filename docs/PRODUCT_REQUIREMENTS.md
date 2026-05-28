# ReNova Marketplace Requirements

## Product Vision

ReNova is a peer-to-peer second-hand marketplace. Individuals can list pre-owned goods, discover local or shippable items, message sellers, negotiate offers, place orders, and review completed trades.

The product is not currently a multi-store website builder. Storefront-template work is out of scope for the current ReNova codebase unless a future migration reintroduces it with backend persistence.

## Core User Requirements

- Public home page that explains the marketplace value.
- Browse page with search, category, condition, price, location, and sort filters.
- Listing detail page with image gallery, seller profile, favorite, offer, message, and checkout actions.
- User sign-up, login, logout, and persisted session recovery.
- Seller listing creation and editing with simple fields an inexperienced user can understand.
- Favorites list for saved items.
- Offers inbox and sent-offers view.
- Buyer-seller conversations tied to listings.
- Checkout flow for creating marketplace orders.
- Buying and selling order views.
- Public and private profile views.
- Review creation after completed orders.
- English and Chinese UI text.

## Backend Requirements

- REST JSON API under `/api`.
- Public endpoints for categories, active listings, listing detail, public profiles, seller listings, and public reviews.
- Protected endpoints for listings, favorites, offers, conversations, orders, reviews, and profile updates.
- JWT authentication with BCrypt password hashes.
- DTO-based request and response contracts.
- Server-side validation with field-level error messages.
- Business-rule errors for invalid enum values and invalid state transitions.
- Transactional marketplace mutations.
- Public DTOs must not leak private account fields such as email.
- MySQL runtime support and H2 test profile support.

## Frontend Requirements

- Vue 3, Vue Router, Pinia, Vue I18n, Axios, and Vite.
- All API calls go through `frontend/src/api/endpoints.js`.
- Shared Axios behavior stays in `frontend/src/api/client.js`.
- Browser storage access stays in `frontend/src/utils/browserStorage.js`.
- UI workflows surface backend failures instead of silently swallowing them.
- Filter actions avoid duplicate network requests.
- Unit tests cover API endpoint contracts, storage resilience, error normalization, formatting, and i18n bundle shape.

## Demo And Production Boundaries

- Seeded accounts, categories, and listings are for local development and portfolio preview.
- Payment endpoints currently represent order-state transitions only. No real card processor is connected.
- GitHub Pages can serve the static frontend preview but cannot run the Spring Boot API or MySQL database.
- A production marketplace needs managed hosting, secrets, database backups, rate limits, observability, and a real payment provider before handling real money.

## Non-Goals For Current Codebase

- Website-builder templates and generated merchant stores.
- Real subdomain or custom-domain provisioning.
- Real payment capture, escrow custody, refunds, or fraud controls.
- Carrier label purchase or live shipping quotes.
- Production audit logging.
- Full end-to-end browser test suite in CI.

## Future Roadmap

- Persistent media uploads instead of external image URLs.
- Payment provider integration with webhooks and idempotency.
- Abuse reporting, moderation queue, and audit log.
- Inventory reservation timeout jobs.
- Production rate limiting and queue-backed write bursts.
- Accessibility and mobile UX audits on every major flow.
- Full E2E tests for signup, listing, offer, messaging, checkout, and review.
